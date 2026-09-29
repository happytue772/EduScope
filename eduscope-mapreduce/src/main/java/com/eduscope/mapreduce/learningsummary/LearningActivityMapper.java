package com.eduscope.mapreduce.learningsummary;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * STUDENT_ACTIVITY 결과를 최종 요약 Join으로 전달한다.
 */
public class LearningActivityMapper
        extends Mapper<LongWritable, Text, Text, Text> {

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

        /*
         * STUDENT_ACTIVITY:
         *
         * key
         * totalClick
         * activeDay
         * usedMaterial
         * avgDaily
         * firstDay
         * lastDay
         */
        String[] parts =
                value.toString().split("\\t", -1);

        if (parts.length < 7) {
            return;
        }

        outputKey.set(parts[0]);

        /*
         * A = Activity
         *
         * totalClick
         * activeDay
         * usedMaterial
         * firstDay
         * lastDay
         */
        outputValue.set(
                "A|"
                + parts[1]
                + "|"
                + parts[2]
                + "|"
                + parts[3]
                + "|"
                + parts[5]
                + "|"
                + parts[6]
        );

        context.write(
                outputKey,
                outputValue
        );
    }
}