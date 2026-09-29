package com.eduscope.mapreduce.learningsummary;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.eduscope.mapreduce.common.counter.EduScopeCounter;
import com.eduscope.mapreduce.common.model.AssessmentRecord;
import com.eduscope.mapreduce.common.parser.AssessmentParser;
import com.eduscope.mapreduce.common.parser.ParseException;

/**
 * assessments.csv에서 assessment와
 * 강의/개설차수 관계를 가져온다.
 */
public class LearningAssessmentMetaMapper
        extends Mapper<LongWritable, Text, LongWritable, Text> {

    private final AssessmentParser parser =
            new AssessmentParser();

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

        if (key.get() == 0) {
            return;
        }

        context.getCounter(
                EduScopeCounter.INPUT_RECORD
        ).increment(1);

        try {

            AssessmentRecord record =
                    parser.parse(value.toString());

            outputKey.set(
                    record.getAssessmentId()
            );

            /*
             * M = Assessment Metadata
             */
            outputValue.set(
                    "M|"
                    + record.getCodeModule()
                    + "|"
                    + record.getCodePresentation()
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