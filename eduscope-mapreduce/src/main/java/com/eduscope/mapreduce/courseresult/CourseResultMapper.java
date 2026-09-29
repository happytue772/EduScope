package com.eduscope.mapreduce.courseresult;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.eduscope.mapreduce.common.counter.EduScopeCounter;
import com.eduscope.mapreduce.common.model.StudentInfoRecord;
import com.eduscope.mapreduce.common.parser.ParseException;
import com.eduscope.mapreduce.common.parser.StudentInfoParser;

/**
 * studentInfo.csv의 final_result를
 * 강의 + 개설차수 기준으로 전달한다.
 */
public class CourseResultMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    private final StudentInfoParser parser =
            new StudentInfoParser();

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

        // CSV Header 제외
        if (key.get() == 0) {
            return;
        }

        context.getCounter(
                EduScopeCounter.INPUT_RECORD
        ).increment(1);

        try {

            StudentInfoRecord record =
                    parser.parse(value.toString());

            /*
             * Key:
             * code_module | code_presentation
             */
            outputKey.set(
                    record.getCodeModule()
                    + "|"
                    + record.getCodePresentation()
            );

            /*
             * Value:
             * Pass / Fail / Withdrawn / Distinction
             */
            outputValue.set(
                    record.getFinalResult()
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