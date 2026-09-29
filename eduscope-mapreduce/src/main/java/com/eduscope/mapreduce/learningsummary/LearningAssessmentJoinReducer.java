package com.eduscope.mapreduce.learningsummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * assessment_id 기준으로
 * studentAssessment + assessments를 Join한다.
 */
public class LearningAssessmentJoinReducer
        extends Reducer<LongWritable, Text, Text, Text> {

    private final Text outputKey =
            new Text();

    private final Text outputValue =
            new Text();

    @Override
    protected void reduce(
            LongWritable key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        String codeModule = null;
        String codePresentation = null;

        /*
         * Metadata가 뒤에 도착할 수도 있으므로
         * 학생 데이터를 잠시 저장한다.
         */
        List<String> studentRows =
                new ArrayList<String>();

        for (Text value : values) {

            String[] parts =
                    value.toString().split("\\|", -1);

            if (parts.length < 2) {
                continue;
            }

            if ("M".equals(parts[0])
                    && parts.length >= 3) {

                codeModule = parts[1];
                codePresentation = parts[2];

            } else if ("S".equals(parts[0])
                    && parts.length >= 3) {

                studentRows.add(
                        parts[1] + "|" + parts[2]
                );
            }
        }

        // Assessment Metadata가 없는 데이터는 Join 불가
        if (codeModule == null
                || codePresentation == null) {
            return;
        }

        for (String studentRow : studentRows) {

            String[] parts =
                    studentRow.split("\\|", -1);

            if (parts.length != 2) {
                continue;
            }

            /*
             * Key:
             * module | presentation | student
             */
            outputKey.set(
                    codeModule
                    + "|"
                    + codePresentation
                    + "|"
                    + parts[0]
            );

            // Value = score
            outputValue.set(parts[1]);

            context.write(
                    outputKey,
                    outputValue
            );
        }
    }
}