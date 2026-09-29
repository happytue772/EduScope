package com.eduscope.mapreduce.learningsummary;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.eduscope.mapreduce.common.counter.EduScopeCounter;
import com.eduscope.mapreduce.common.model.StudentAssessmentRecord;
import com.eduscope.mapreduce.common.parser.ParseException;
import com.eduscope.mapreduce.common.parser.StudentAssessmentParser;

/**
 * studentAssessment.csv를 assessment_id 기준으로 전달한다.
 */
public class LearningAssessmentMapper
        extends Mapper<LongWritable, Text, LongWritable, Text> {

    private final StudentAssessmentParser parser =
            new StudentAssessmentParser();

    private final LongWritable outputKey =
            new LongWritable();

    private final Text outputValue =
            new Text();

    @Override
    protected void map(
            LongWritable key,
            Text value,
            Context context)
            throws IOException, InterruptedException {

        // Header 제외
        if (key.get() == 0) {
            return;
        }

        context.getCounter(
                EduScopeCounter.INPUT_RECORD
        ).increment(1);

        try {

            StudentAssessmentRecord record =
                    parser.parse(value.toString());

            outputKey.set(
                    record.getAssessmentId()
            );

            Double score = record.getScore();

            /*
             * S = StudentAssessment
             * studentId | score
             */
            outputValue.set(
                    "S|"
                    + record.getStudentId()
                    + "|"
                    + (score == null ? "" : score)
            );

            context.write(
                    outputKey,
                    outputValue
            );

            context.getCounter(
                    EduScopeCounter.VALID_RECORD
            ).increment(1);

        } catch (ParseException e) {

            context.getCounter(
                    EduScopeCounter.INVALID_RECORD
            ).increment(1);

            context.getCounter(
                    EduScopeCounter.PARSE_ERROR
            ).increment(1);
        }
    }
}