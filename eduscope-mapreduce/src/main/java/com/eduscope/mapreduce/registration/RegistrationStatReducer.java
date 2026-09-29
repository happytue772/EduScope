package com.eduscope.mapreduce.registration;

import java.io.IOException;
import java.util.Locale;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * 강의 + 개설차수별 등록/등록해제/Withdrawn 통계를 계산한다.
 */
public class RegistrationStatReducer
        extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue = new Text();

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        long registrationCount = 0L;
        long unregistrationCount = 0L;
        long withdrawnResultCount = 0L;

        long registrationDaySum = 0L;
        long registrationDayCount = 0L;

        long unregistrationDaySum = 0L;
        long unregistrationDayCount = 0L;

        for (Text value : values) {

            String[] parts =
                    value.toString().split("\\|", -1);

            if (parts.length < 2) {
                continue;
            }

            /*
             * studentRegistration.csv
             */
            if ("R".equals(parts[0])) {

                registrationCount++;

                // 등록일
                if (parts.length > 1
                        && !parts[1].isEmpty()) {

                    int registrationDay =
                            Integer.parseInt(parts[1]);

                    registrationDaySum += registrationDay;
                    registrationDayCount++;
                }

                // 등록해제일
                if (parts.length > 2
                        && !parts[2].isEmpty()) {

                    int unregistrationDay =
                            Integer.parseInt(parts[2]);

                    unregistrationCount++;

                    unregistrationDaySum +=
                            unregistrationDay;

                    unregistrationDayCount++;
                }

            /*
             * studentInfo.csv
             */
            } else if ("I".equals(parts[0])) {

                if ("Withdrawn".equals(parts[1])) {
                    withdrawnResultCount++;
                }
            }
        }

        double unregistrationRate =
                registrationCount == 0
                ? 0.0
                : (double) unregistrationCount
                    / registrationCount;

        double avgRegistrationDay =
                registrationDayCount == 0
                ? 0.0
                : (double) registrationDaySum
                    / registrationDayCount;

        double avgUnregistrationDay =
                unregistrationDayCount == 0
                ? 0.0
                : (double) unregistrationDaySum
                    / unregistrationDayCount;

        /*
         * 출력:
         * registration_count
         * unregistration_count
         * withdrawn_result_count
         * unregistration_rate
         * avg_registration_day
         * avg_unregistration_day
         */
        outputValue.set(
                registrationCount
                + "\t"
                + unregistrationCount
                + "\t"
                + withdrawnResultCount
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        unregistrationRate)
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        avgRegistrationDay)
                + "\t"
                + String.format(
                        Locale.US,
                        "%.4f",
                        avgUnregistrationDay)
        );

        context.write(key, outputValue);
    }
}