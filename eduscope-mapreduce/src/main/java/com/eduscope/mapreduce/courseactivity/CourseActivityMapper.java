package com.eduscope.mapreduce.courseactivity;

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
 * studentVle.csv를
 * 강의 + 개설차수 단위로 Mapper 처리한다.
 */
public class CourseActivityMapper
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

        // CSV Header 제외
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

            // 강의 + 개설차수
            outputKey.set(
                    record.getCodeModule()
                    + "|"
                    + record.getCodePresentation()
            );

            /*
             * Reducer에서 필요한 값
             * 학생ID | 상대일수 | 클릭수
             */
            outputValue.set(
                    record.getStudentId()
                    + "|"
                    + record.getRelativeDay()
                    + "|"
                    + record.getSumClick()
            );

            context.write(
                    outputKey,
                    outputValue
            );

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