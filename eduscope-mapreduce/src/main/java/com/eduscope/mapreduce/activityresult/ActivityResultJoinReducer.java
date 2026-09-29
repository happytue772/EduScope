package com.eduscope.mapreduce.activityresult;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * 학생별 Activity 통계와 final_result를 Join한다.
 */
public class ActivityResultJoinReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputKey = new Text();
    private final Text outputValue = new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        Long totalClick = null;
        Integer activeDay = null;
        String finalResult = null;

        for (Text value : values) {

            String[] parts =
                    value.toString().split("\\|", -1);

            if (parts.length < 2) {
                continue;
            }

            if ("A".equals(parts[0])) {

                if (parts.length >= 3) {
                    totalClick =
                            Long.parseLong(parts[1]);

                    activeDay =
                            Integer.parseInt(parts[2]);
                }

            } else if ("I".equals(parts[0])) {

                finalResult = parts[1];
            }
        }

        // Activity와 studentInfo 둘 다 있는 경우만 Join
        if (totalClick == null
                || activeDay == null
                || finalResult == null) {
            return;
        }

        String[] keyParts =
                key.toString().split("\\|");

        if (keyParts.length != 3) {
            return;
        }

        /*
         * 새로운 그룹 Key:
         * module | presentation | finalResult
         */
        outputKey.set(
                keyParts[0]
                + "|"
                + keyParts[1]
                + "|"
                + finalResult
        );

        /*
         * studentId | totalClick | activeDay
         */
        outputValue.set(
                keyParts[2]
                + "|"
                + totalClick
                + "|"
                + activeDay
        );

        context.write(outputKey, outputValue);
    }
}