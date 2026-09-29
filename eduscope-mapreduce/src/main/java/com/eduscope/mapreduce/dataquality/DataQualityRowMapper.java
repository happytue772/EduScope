package com.eduscope.mapreduce.dataquality;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * 모든 OULAD CSV의 행 단위 품질 검사.
 *
 * 검사 범위:
 * Header / 필수값 / 타입 / 범위 / FK 참조 무결성.
 */
public class DataQualityRowMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    private String fileType;

    private DataQualitySupport.ReferenceData
            referenceData;

    private final Text outputKey = new Text();
    private final Text outputValue = new Text();

    @Override
    protected void setup(Context context)
            throws IOException, InterruptedException {

        FileSplit split =
                (FileSplit) context.getInputSplit();

        fileType =
                DataQualitySupport.detectFileType(
                        split.getPath().toString()
                );

        try {
            referenceData =
                    DataQualitySupport
                        .loadReferenceData(
                            context.getConfiguration()
                        );

        } catch (Exception e) {
            throw new IOException(
                    "DATA_QUALITY FK 기준정보 로딩 실패"
                    + " / fileType=" + fileType,
                    e
            );
        }
    }

    @Override
    protected void map(
            LongWritable key,
            Text value,
            Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        /*
         * 각 원본 CSV의 첫 행은 Header다.
         * 기존처럼 단순 제외하지 않고 실제 Header를 검증한다.
         */
        if (key.get() == 0) {

            if (!DataQualitySupport
                    .isExpectedHeader(
                            fileType,
                            line)) {

                emit(
                    context,
                    "HEADER_MISMATCH",
                    DataQualitySupport.sample(line)
                );
            }

            return;
        }

        try {

            List<String> columns =
                    DataQualitySupport.parseColumns(line);

            int expected =
                    DataQualitySupport
                        .expectedColumnCount(fileType);

            // 컬럼 수가 원본 스키마와 다름
            if (columns.size() != expected) {

                emit(
                    context,
                    "PARSE_ERROR",
                    "column-count=" + columns.size()
                    + ", expected=" + expected
                    + ", row="
                    + DataQualitySupport.sample(line)
                );

                return;
            }

            boolean missing =
                    DataQualitySupport
                        .hasMissingRequiredValue(
                                fileType,
                                columns);

            if (missing) {

                emit(
                    context,
                    "MISSING_VALUE",
                    DataQualitySupport.sample(line)
                );
            }

            /*
             * 숫자형 타입 오류와 값 범위 오류를 분리한다.
             */
            boolean invalidType =
                    DataQualitySupport
                        .hasInvalidType(
                                fileType,
                                columns);

            if (invalidType) {

                emit(
                    context,
                    "PARSE_ERROR",
                    "type-error, row="
                    + DataQualitySupport.sample(line)
                );

                return;
            }

            if (DataQualitySupport
                    .hasInvalidRange(
                            fileType,
                            columns)) {

                emit(
                    context,
                    "INVALID_RANGE",
                    DataQualitySupport.sample(line)
                );
            }

            /*
             * 필수 FK 컬럼이 빠진 행은 이미 MISSING_VALUE로 잡았으므로
             * 추가적인 FK 오탐을 만들지 않는다.
             */
            if (!missing) {

                String fkError =
                        DataQualitySupport
                            .findForeignKeyError(
                                fileType,
                                columns,
                                referenceData
                            );

                if (fkError != null) {

                    emit(
                        context,
                        "FK_MISMATCH",
                        fkError
                        + ", row="
                        + DataQualitySupport.sample(line)
                    );
                }
            }

        } catch (Exception e) {

            emit(
                context,
                "PARSE_ERROR",
                DataQualitySupport.sample(line)
            );
        }
    }

    private void emit(
            Context context,
            String qualityType,
            String sample)
            throws IOException, InterruptedException {

        outputKey.set(
                fileType
                + "|"
                + qualityType
        );

        outputValue.set(sample);

        context.write(
                outputKey,
                outputValue
        );
    }
}
