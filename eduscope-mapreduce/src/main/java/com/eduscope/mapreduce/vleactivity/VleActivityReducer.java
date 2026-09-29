package com.eduscope.mapreduce.vleactivity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * VLE 자료별 클릭 수,
 * 활동 학생 수, 활동 일수를 집계한다.
 */
public class VleActivityReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue =
            new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long totalClickCount = 0L;

        // 동일 학생 중복 제거
        Set<Long> activeStudents =
                new HashSet<Long>();

        // 동일 활동일 중복 제거
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

        /*
         * 출력:
         * total_click_count
         * active_student_count
         * active_day_count
         */
        outputValue.set(
                totalClickCount
                + "\t"
                + activeStudentCount
                + "\t"
                + activeDayCount
        );

        context.write(
                key,
                outputValue
        );
    }
}