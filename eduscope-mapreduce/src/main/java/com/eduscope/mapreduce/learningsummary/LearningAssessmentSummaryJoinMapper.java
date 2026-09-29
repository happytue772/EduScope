package com.eduscope.mapreduce.learningsummary;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * 학생별 Assessment 요약 결과를 최종 Join으로 전달한다.
 */
public class LearningAssessmentSummaryJoinMapper
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

        String[] parts =
                value.toString().split("\\t", -1);

        if (parts.length != 4) {
            return;
        }

        outputKey.set(parts[0]);

        /*
         * S = Assessment Summary
         */
        outputValue.set(
                "S|"
                + parts[1]
                + "|"
                + parts[2]
                + "|"
                + parts[3]
        );

        context.write(
                outputKey,
                outputValue
        );
    }
}