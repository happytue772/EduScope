package com.eduscope.mapreduce.dataquality;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * 정확히 동일한 행과 자연키 중복을 검출하기 위한 Mapper.
 */
public class DataQualityDuplicateMapper
        extends Mapper<LongWritable, Text, Text, LongWritable> {

    private String fileType;

    private static final LongWritable ONE =
            new LongWritable(1);

    private final Text outputKey =
            new Text();

    @Override
    protected void setup(Context context) {

        FileSplit split =
                (FileSplit) context.getInputSplit();

        fileType =
                DataQualitySupport.detectFileType(
                        split.getPath().toString()
                );
    }

    @Override
    protected void map(
            LongWritable key,
            Text value,
            Context context)
            throws IOException, InterruptedException {

        if (key.get() == 0) {
            return;
        }

        String line =
                value.toString();

        try {

            List<String> columns =
                    DataQualitySupport.parseColumns(line);

            if (columns.size()
                    != DataQualitySupport
                        .expectedColumnCount(fileType)) {

                return;
            }

            /*
             * E = Exact Row
             */
            String rowHash =
                    DataQualitySupport.sha256(line);

            outputKey.set(
                    "E|"
                    + fileType
                    + "|"
                    + rowHash
            );

            context.write(
                    outputKey,
                    ONE
            );

            /*
             * P = Natural Primary Key
             */
            String pk =
                    DataQualitySupport
                        .buildPrimaryKey(
                            fileType,
                            columns
                        );

            if (pk != null) {

                outputKey.set(
                        "P|"
                        + fileType
                        + "|"
                        + pk
                );

                context.write(
                        outputKey,
                        ONE
                );
            }

        } catch (Exception e) {
            // Parser 품질 오류는 Row Job에서 이미 계산한다.
        }
    }
}