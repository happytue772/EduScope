package com.eduscope.mapreduce.courseresult;

import java.io.IOException;
import java.util.Locale;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.eduscope.mapreduce.common.counter.EduScopeCounter;

/**
 * 강의 + 개설차수별 최종 결과 통계를 계산한다.
 */
public class CourseResultReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue =
            new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long studentCount = 0L;

        long passCount = 0L;
        long failCount = 0L;
        long withdrawnCount = 0L;
        long distinctionCount = 0L;

        for (Text value : values) {

            String finalResult =
                    value.toString();

            /*
             * OULAD 원본 final_result만 인정한다.
             */
            if ("Pass".equals(finalResult)) {

                passCount++;
                studentCount++;

            } else if ("Fail".equals(finalResult)) {

                failCount++;
                studentCount++;

            } else if ("Withdrawn".equals(finalResult)) {

                withdrawnCount++;
                studentCount++;

            } else if ("Distinction".equals(finalResult)) {

                distinctionCount++;
                studentCount++;

            } else {

                // 예상 범위를 벗어난 실제 데이터가 있으면 기록
                context.getCounter(
                        EduScopeCounter.INVALID_RANGE
                ).increment(1);
            }
        }

        double passRate =
                studentCount == 0
                ? 0.0
                : (double) passCount
                    / studentCount;

        double failRate =
                studentCount == 0
                ? 0.0
                : (double) failCount
                    / studentCount;

        double withdrawnRate =
                studentCount == 0
                ? 0.0
                : (double) withdrawnCount
                    / studentCount;

        double distinctionRate =
                studentCount == 0
                ? 0.0
                : (double) distinctionCount
                    / studentCount;

        /*
         * 출력 순서:
         *
         * student_count
         * pass_count
         * fail_count
         * withdrawn_count
         * distinction_count
         * pass_rate
         * fail_rate
         * withdrawn_rate
         * distinction_rate
         */
        outputValue.set(
                studentCount
                + "\t"
                + passCount
                + "\t"
                + failCount
                + "\t"
                + withdrawnCount
                + "\t"
                + distinctionCount
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        passRate)
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        failRate)
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        withdrawnRate)
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        distinctionRate)
        );

        context.write(
                key,
                outputValue
        );
    }
}