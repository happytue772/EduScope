package com.eduscope.mapreduce.dataquality;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * 동일 Row / 자연키가 2번 이상 발생했는지 판단한다.
 */
public class DataQualityDuplicateReducer
        extends Reducer<Text, LongWritable, Text, Text> {

    private final Text outputKey =
            new Text();

    private final Text outputValue =
            new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<LongWritable> values,
            Context context)
            throws IOException, InterruptedException {

        long count = 0L;

        for (LongWritable value : values) {
            count += value.get();
        }

        if (count <= 1) {
            return;
        }

        String[] parts =
                key.toString().split("\\|", 3);

        if (parts.length != 3) {
            return;
        }

        String type = parts[0];
        String fileType = parts[1];
        String sampleKey = parts[2];

        String qualityType =
                "E".equals(type)
                ? "DUPLICATE"
                : "PK_DUPLICATE";

        /*
         * 예:
         * 동일 Row가 3번 존재
         * → 원본 1건 + 중복 2건
         * → duplicateCount = 2
         */
        long duplicateCount =
                count - 1;

        outputKey.set(
                fileType
                + "|"
                + qualityType
        );

        outputValue.set(
                duplicateCount
                + "\t"
                + sampleKey
        );

        context.write(
                outputKey,
                outputValue
        );
    }
}