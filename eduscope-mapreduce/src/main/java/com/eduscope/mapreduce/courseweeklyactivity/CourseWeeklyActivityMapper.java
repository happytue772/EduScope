package com.eduscope.mapreduce.courseweeklyactivity;

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
 * 강의 + 개설차수 + 상대주차 단위로 변환한다.
 */
public class CourseWeeklyActivityMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    private final StudentVleParser parser =
            new StudentVleParser();

    private final HeaderValidator headerValidator =
            new HeaderValidator();

    private final Text outputKey =
            new Text();

    private final Text outputValue =
            new Text();

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

            /*
             * 강의 시작일을 0일로 하여
             * 상대 주차 계산.
             *
             * 음수 date도 floorDiv를 통해
             * 올바른 음수 주차로 유지한다.
             */
            int relativeWeekNo =
                    Math.floorDiv(
                            record.getRelativeDay(),
                            7
                    );

            /*
             * Key:
             * module | presentation | week
             */
            outputKey.set(
                    record.getCodeModule()
                    + "|"
                    + record.getCodePresentation()
                    + "|"
                    + relativeWeekNo
            );

            /*
             * Reducer 전달값:
             * studentId | siteId | clickCount
             */
            outputValue.set(
                    record.getStudentId()
                    + "|"
                    + record.getSiteId()
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