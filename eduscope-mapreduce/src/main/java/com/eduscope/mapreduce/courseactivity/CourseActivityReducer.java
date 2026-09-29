package com.eduscope.mapreduce.courseactivity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * 강의 + 개설차수별 전체 활동 통계를 계산한다.
 */
public class CourseActivityReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue = new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long totalClickCount = 0L;

        Set<Long> activeStudents =
                new HashSet<Long>();

        Set<Integer> activeDays =
                new HashSet<Integer>();

        for (Text value : values) {

            String[] parts =
                    value.toString().split("\\|");

            if (parts.length != 3) {
                continue;
            }

            long studentId =
                    Long.parseLong(parts[0]);

            int relativeDay =
                    Integer.parseInt(parts[1]);

            long clickCount =
                    Long.parseLong(parts[2]);

            activeStudents.add(studentId);
            activeDays.add(relativeDay);

            totalClickCount += clickCount;
        }

        int activeStudentCount =
                activeStudents.size();

        int activeDayCount =
                activeDays.size();

        double avgClickPerStudent = 0.0;

        if (activeStudentCount > 0) {
            avgClickPerStudent =
                    (double) totalClickCount
                    / activeStudentCount;
        }

        /*
         * 출력:
         * active_student_count
         * total_click_count
         * avg_click_per_student
         * active_day_count
         */
        outputValue.set(
                activeStudentCount
                + "\t"
                + totalClickCount
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        avgClickPerStudent)
                + "\t"
                + activeDayCount
        );

        context.write(
                key,
                outputValue
        );
    }
}