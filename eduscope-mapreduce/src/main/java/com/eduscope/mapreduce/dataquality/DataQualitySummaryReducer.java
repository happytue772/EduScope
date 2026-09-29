package com.eduscope.mapreduce.dataquality;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * file_type + quality_type별 실제 발생 건수를 집계한다.
 */
public class DataQualitySummaryReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue =
            new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long count = 0L;
        String sample = "";

        for (Text value : values) {

            count++;

            // 최초 실제 사례 하나만 보관
            if (sample.isEmpty()) {
                sample = value.toString();
            }
        }

        /*
         * record_count | sample_message
         */
        outputValue.set(
                count
                + "\t"
                + sample
        );

        context.write(
                key,
                outputValue
        );
    }
}