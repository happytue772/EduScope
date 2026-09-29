package com.eduscope.mapreduce.learningsummary;

import java.io.IOException;
import java.util.Locale;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * 학생 + 강의별 Assessment 요약.
 */
public class LearningAssessmentSummaryReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue =
            new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long submittedCount = 0L;

        long scoreCount = 0L;
        double scoreSum = 0.0;

        long failedCount = 0L;

        for (Text value : values) {

            submittedCount++;

            String scoreText =
                    value.toString().trim();

            if (scoreText.isEmpty()) {
                continue;
            }

            double score =
                    Double.parseDouble(scoreText);

            scoreSum += score;
            scoreCount++;

            // 기존 EduScope 평가 실패 기준
            if (score < 40.0) {
                failedCount++;
            }
        }

        double avgScore =
                scoreCount == 0
                ? 0.0
                : scoreSum / scoreCount;

        /*
         * submitted_assessment_count
         * avg_assessment_score
         * failed_assessment_count
         */
        outputValue.set(
                submittedCount
                + "\t"
                + String.format(
                        Locale.US,
                        "%.2f",
                        avgScore)
                + "\t"
                + failedCount
        );

        context.write(
                key,
                outputValue
        );
    }
}