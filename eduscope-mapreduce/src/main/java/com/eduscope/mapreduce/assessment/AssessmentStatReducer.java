package com.eduscope.mapreduce.assessment;

import java.io.IOException;
import java.util.Locale;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * assessment별 제출/점수 통계를 집계한다.
 */
public class AssessmentStatReducer
        extends Reducer<LongWritable, Text, LongWritable, Text> {

    private final Text outputValue =
            new Text();

    @Override
    protected void reduce(
            LongWritable key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long submissionCount = 0;

        long scoreCount = 0;
        double scoreSum = 0;

        long failCount = 0;
        long bankedCount = 0;

        long submittedDayCount = 0;
        long submittedDaySum = 0;

        for (Text value : values) {

            String[] parts =
                    value.toString().split("\\|", -1);

            if (parts.length != 3) {
                continue;
            }

            submissionCount++;

            // 제출일
            if (!parts[0].isEmpty()) {
                int submittedDay =
                        Integer.parseInt(parts[0]);

                submittedDaySum += submittedDay;
                submittedDayCount++;
            }

            // Banked 여부
            if (!parts[1].isEmpty()) {
                int banked =
                        Integer.parseInt(parts[1]);

                if (banked == 1) {
                    bankedCount++;
                }
            }

            // 점수
            if (!parts[2].isEmpty()) {
                double score =
                        Double.parseDouble(parts[2]);
                scoreSum += score;
                scoreCount++;

                // OULAD/EduScope 평가 실패 기준
                if (score < 40) {
                    failCount++;
                }
            }
        }

        double avgScore =
                scoreCount == 0
                ? 0.0
                : (double) scoreSum / scoreCount;

        double failRate =
                scoreCount == 0
                ? 0.0
                : (double) failCount / scoreCount;

        double avgSubmissionDay =
                submittedDayCount == 0
                ? 0.0
                : (double) submittedDaySum
                        / submittedDayCount;

        /*
         * 출력:
         * submission_count
         * avg_score
         * fail_count
         * fail_rate
         * banked_count
         * avg_submission_day
         */
        outputValue.set(
                submissionCount
                + "\t"
                + String.format(
                        Locale.US, "%.2f", avgScore)
                + "\t"
                + failCount
                + "\t"
                + String.format(
                        Locale.US, "%.4f", failRate)
                + "\t"
                + bankedCount
                + "\t"
                + String.format(
                        Locale.US, "%.4f", avgSubmissionDay)
        );

        context.write(key, outputValue);
    }
}