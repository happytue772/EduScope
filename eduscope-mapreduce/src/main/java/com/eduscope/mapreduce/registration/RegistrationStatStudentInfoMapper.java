package com.eduscope.mapreduce.registration;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.eduscope.mapreduce.common.counter.EduScopeCounter;
import com.eduscope.mapreduce.common.model.StudentInfoRecord;
import com.eduscope.mapreduce.common.parser.ParseException;
import com.eduscope.mapreduce.common.parser.StudentInfoParser;

/**
 * studentInfo.csv에서 강의별 final_result 정보를 전달한다.
 */
public class RegistrationStatStudentInfoMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    private final StudentInfoParser parser =
            new StudentInfoParser();

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

            StudentInfoRecord record =
                    parser.parse(value.toString());

            outputKey.set(
                    record.getCodeModule()
                    + "|"
                    + record.getCodePresentation()
            );

            /*
             * I = StudentInfo 데이터
             *
             * I | finalResult
             */
            outputValue.set(
                    "I|" + record.getFinalResult()
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