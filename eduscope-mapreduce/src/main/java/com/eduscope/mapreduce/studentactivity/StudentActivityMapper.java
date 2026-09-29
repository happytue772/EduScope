package com.eduscope.mapreduce.studentactivity;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.eduscope.mapreduce.common.counter.EduScopeCounter;
import com.eduscope.mapreduce.common.model.StudentVleRecord;
import com.eduscope.mapreduce.common.parser.ParseException;
import com.eduscope.mapreduce.common.parser.StudentVleParser;
import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * studentVle.csv 한 행을 읽어
 * 강의 + 개설차수 + 학생 단위 Key로 변환한다.
 */
public class StudentActivityMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    private final StudentVleParser parser =
            new StudentVleParser();

    private final HeaderValidator headerValidator =
            new HeaderValidator();

    private final Text outputKey = new Text();
    private final Text outputValue = new Text();

    @Override
    protected void map(
            LongWritable key,
            Text value,
            Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        // CSV Header는 분석 대상에서 제외
        if (key.get() == 0
                && headerValidator.isStudentVleHeader(line)) {
            return;
        }

        context.getCounter(
                EduScopeCounter.INPUT_RECORD
        ).increment(1);

        try {

            StudentVleRecord record =
                    parser.parse(line);

            /*
             * Key
             * codeModule | codePresentation | studentId
             */
            outputKey.set(
                    record.getCodeModule()
                    + "|"
                    + record.getCodePresentation()
                    + "|"
                    + record.getStudentId()
            );

            /*
             * Reducer 전달값
             * relativeDay | siteId | sumClick
             */
            outputValue.set(
                    record.getRelativeDay()
                    + "|"
                    + record.getSiteId()
                    + "|"
                    + record.getSumClick()
            );

            context.write(outputKey, outputValue);

            context.getCounter(
                    EduScopeCounter.VALID_RECORD
            ).increment(1);

        } catch (ParseException e) {

            context.getCounter(
                    EduScopeCounter.INVALID_RECORD
            ).increment(1);

            context.getCounter(
                    EduScopeCounter.PARSE_ERROR
            ).increment(1);
        }
    }
}