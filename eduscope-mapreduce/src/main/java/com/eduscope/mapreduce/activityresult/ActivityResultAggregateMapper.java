package com.eduscope.mapreduce.activityresult;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Join 결과를 그룹 통계용 형태로 변환한다.
 */
public class ActivityResultAggregateMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    private final Text outputKey = new Text();
    private final Text outputValue = new Text();

    @Override
    protected void map(
            LongWritable key,
            Text value,
            Context context)
            throws IOException, InterruptedException {

        String[] parts =
                value.toString().split("\\t");

        if (parts.length != 2) {
            return;
        }

        outputKey.set(parts[0]);
        outputValue.set(parts[1]);

        context.write(outputKey, outputValue);
    }
}