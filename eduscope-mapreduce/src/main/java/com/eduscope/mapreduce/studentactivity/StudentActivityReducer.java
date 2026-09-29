package com.eduscope.mapreduce.studentactivity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * 학생 + 강의별 학습 활동 통계를 계산한다.
 */
public class StudentActivityReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue = new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long totalClickCount = 0L;

        Set<Integer> activeDays =
                new HashSet<Integer>();

        Set<Long> usedMaterials =
                new HashSet<Long>();

        Integer firstActivityDay = null;
        Integer lastActivityDay = null;

        for (Text value : values) {

            String[] parts =
                    value.toString().split("\\|");

            if (parts.length != 3) {
                continue;
            }

            int day =
                    Integer.parseInt(parts[0]);

            long siteId =
                    Long.parseLong(parts[1]);

            long clickCount =
                    Long.parseLong(parts[2]);

            totalClickCount += clickCount;

            // 서로 다른 활동일 계산
            activeDays.add(day);

            // 서로 다른 VLE 자료 계산
            usedMaterials.add(siteId);

            if (firstActivityDay == null
                    || day < firstActivityDay) {
                firstActivityDay = day;
            }

            if (lastActivityDay == null
                    || day > lastActivityDay) {
                lastActivityDay = day;
            }
        }

        int activeDayCount = activeDays.size();
        int usedMaterialCount = usedMaterials.size();

        double avgDailyClickCount = 0.0;

        if (activeDayCount > 0) {
            avgDailyClickCount =
                    (double) totalClickCount
                    / activeDayCount;
        }

        /*
         * 출력 순서:
         * total_click_count
         * active_day_count
         * used_material_count
         * avg_daily_click_count
         * first_activity_day
         * last_activity_day
         */
        outputValue.set(
                totalClickCount
                + "\t"
                + activeDayCount
                + "\t"
                + usedMaterialCount
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        avgDailyClickCount)
                + "\t"
                + firstActivityDay
                + "\t"
                + lastActivityDay
        );

        context.write(key, outputValue);
    }
}