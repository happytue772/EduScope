package com.eduscope.mapreduce.activityresult;

import java.io.IOException;
import java.util.Locale;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * course + presentation + final_result별 활동 통계를 계산한다.
 */
public class ActivityResultAggregateReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue = new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long studentCount = 0L;
        long totalClickCount = 0L;
        long activeDaySum = 0L;

        for (Text value : values) {

            String[] parts =
                    value.toString().split("\\|");

            if (parts.length != 3) {
                continue;
            }

            long click =
                    Long.parseLong(parts[1]);

            int activeDay =
                    Integer.parseInt(parts[2]);

            studentCount++;
            totalClickCount += click;
            activeDaySum += activeDay;
        }

        double avgClickCount =
                studentCount == 0
                ? 0.0
                : (double) totalClickCount
                    / studentCount;

        double avgActiveDayCount =
                studentCount == 0
                ? 0.0
                : (double) activeDaySum
                    / studentCount;

        outputValue.set(
                studentCount
                + "\t"
                + totalClickCount
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        avgClickCount)
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        avgActiveDayCount)
        );

        context.write(key, outputValue);
    }
}