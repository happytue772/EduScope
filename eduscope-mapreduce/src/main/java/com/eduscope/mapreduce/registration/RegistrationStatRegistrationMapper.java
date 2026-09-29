package com.eduscope.mapreduce.registration;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.eduscope.mapreduce.common.counter.EduScopeCounter;
import com.eduscope.mapreduce.common.model.StudentRegistrationRecord;
import com.eduscope.mapreduce.common.parser.ParseException;
import com.eduscope.mapreduce.common.parser.StudentRegistrationParser;

/**
 * studentRegistration.csv를 처리한다.
 * 강의 + 개설차수별 등록/등록해제 정보를 Reducer로 전달한다.
 */
public class RegistrationStatRegistrationMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    private final StudentRegistrationParser parser =
            new StudentRegistrationParser();

    private final Text outputKey = new Text();
    private final Text outputValue = new Text();

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

            StudentRegistrationRecord record =
                    parser.parse(value.toString());

            outputKey.set(
                    record.getCodeModule()
                    + "|"
                    + record.getCodePresentation()
            );

            /*
             * R = Registration 데이터
             *
             * R | registrationDay | unregistrationDay
             */
            outputValue.set(
                    "R|"
                    + (record.getRegistrationDay() == null
                        ? ""
                        : record.getRegistrationDay())
                    + "|"
                    + (record.getUnregistrationDay() == null
                        ? ""
                        : record.getUnregistrationDay())
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