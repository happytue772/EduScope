package com.eduscope.mapreduce.learningsummary;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Student Activity + Assessment Summary를 통합한다.
 */
public class StudentLearningSummaryReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue =
            new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long totalClick = 0L;
        int activeDay = 0;
        int usedMaterial = 0;

        String firstActivityDay = "";
        String lastActivityDay = "";

        long submittedAssessmentCount = 0L;
        double avgAssessmentScore = 0.0;
        long failedAssessmentCount = 0L;

        boolean activityFound = false;
        boolean assessmentFound = false;

        for (Text value : values) {

            String[] parts =
                    value.toString().split("\\|", -1);

            if (parts.length < 2) {
                continue;
            }

            if ("A".equals(parts[0])
                    && parts.length >= 6) {

                totalClick =
                        Long.parseLong(parts[1]);

                activeDay =
                        Integer.parseInt(parts[2]);

                usedMaterial =
                        Integer.parseInt(parts[3]);

                firstActivityDay = parts[4];
                lastActivityDay = parts[5];

                activityFound = true;

            } else if ("S".equals(parts[0])
                    && parts.length >= 4) {

                submittedAssessmentCount =
                        Long.parseLong(parts[1]);

                avgAssessmentScore =
                        Double.parseDouble(parts[2]);

                failedAssessmentCount =
                        Long.parseLong(parts[3]);

                assessmentFound = true;
            }
        }

        /*
         * 둘 중 하나라도 실제 데이터가 존재하면 결과 생성.
         * 없는 영역은 0 또는 빈 값으로 유지한다.
         */
        if (!activityFound && !assessmentFound) {
            return;
        }

        /*
         * 최종 출력 순서:
         *
         * total_click_count
         * active_day_count
         * used_material_count
         * submitted_assessment_count
         * avg_assessment_score
         * failed_assessment_count
         * first_activity_day
         * last_activity_day
         */
        outputValue.set(
                totalClick
                + "\t"
                + activeDay
                + "\t"
                + usedMaterial
                + "\t"
                + submittedAssessmentCount
                + "\t"
                + String.format(
                        java.util.Locale.US,
                        "%.2f",
                        avgAssessmentScore)
                + "\t"
                + failedAssessmentCount
                + "\t"
                + firstActivityDay
                + "\t"
                + lastActivityDay
        );

        context.write(
                key,
                outputValue
        );
    }
}