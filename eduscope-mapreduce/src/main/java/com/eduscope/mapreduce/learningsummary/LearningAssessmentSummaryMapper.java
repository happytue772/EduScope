package com.eduscope.mapreduce.learningsummary;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Assessment Join 결과를 학생별 집계로 전달한다.
 */
public class LearningAssessmentSummaryMapper
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

        if (parts.length != 2) {
            return;
        }

        outputKey.set(parts[0]);
        outputValue.set(parts[1]);

        context.write(
                outputKey,
                outputValue
        );
    }
}