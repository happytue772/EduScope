package com.eduscope.mapreduce.activityresult;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * 기존 STUDENT_ACTIVITY MapReduce 결과를 읽는다.
 */
public class ActivityResultActivityMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    private final Text outputKey = new Text();
    private final Text outputValue = new Text();

    @Override
    protected void map(
            LongWritable key,
            Text value,
            Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        /*
         * STUDENT_ACTIVITY 출력:
         *
         * module|presentation|studentId
         * totalClick
         * activeDay
         * usedMaterial
         * avgDailyClick
         * firstDay
         * lastDay
         */
        String[] parts = line.split("\\t");

        if (parts.length < 7) {
            return;
        }

        // 학생 + 강의 Key 그대로 사용
        outputKey.set(parts[0]);

        /*
         * A = Activity
         * totalClick | activeDay
         */
        outputValue.set(
                "A|"
                + parts[1]
                + "|"
                + parts[2]
        );

        context.write(outputKey, outputValue);
    }
}