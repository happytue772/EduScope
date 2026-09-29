package com.eduscope.mapreduce.activityresult;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.eduscope.mapreduce.common.model.StudentInfoRecord;
import com.eduscope.mapreduce.common.parser.ParseException;
import com.eduscope.mapreduce.common.parser.StudentInfoParser;

/**
 * studentInfo.csv에서 학생의 final_result를 읽는다.
 */
public class ActivityResultStudentInfoMapper
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

        if (key.get() == 0) {
            return;
        }

        try {

            StudentInfoRecord record =
                    parser.parse(value.toString());

            outputKey.set(
                    record.getCodeModule()
                    + "|"
                    + record.getCodePresentation()
                    + "|"
                    + record.getStudentId()
            );

            outputValue.set(
                    "I|" + record.getFinalResult()
            );

            context.write(outputKey, outputValue);

        } catch (ParseException e) {
            // DATA_QUALITY 단계에서 실제 오류 집계
        }
    }
}