package com.eduscope.mapreduce.assessment;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.eduscope.mapreduce.common.counter.EduScopeCounter;
import com.eduscope.mapreduce.common.model.StudentAssessmentRecord;
import com.eduscope.mapreduce.common.parser.ParseException;
import com.eduscope.mapreduce.common.parser.StudentAssessmentParser;

/**
 * studentAssessment.csv를 assessment 단위로 변환한다.
 */
public class AssessmentStatMapper
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

        String line = value.toString();

        // CSV Header 제외
        if (line.startsWith("id_assessment,")) {
            return;
        }

        context.getCounter(
                EduScopeCounter.INPUT_RECORD
        ).increment(1);

        try {
            StudentAssessmentRecord record =
                    parser.parse(line);

            outputKey.set(record.getAssessmentId());

            /*
             * 기존 StudentAssessmentRecord의 getter를 사용한다.
             * 제출일 | banked 여부 | 점수
             */
            Integer submittedDay = record.getSubmittedDay();
            Integer banked = record.getBanked();
            Double score = record.getScore();

            outputValue.set(
                    (submittedDay == null ? "" : submittedDay)
                    + "|"
                    + (banked == null ? "" : banked)
                    + "|"
                    + (score == null ? "" : score)
            );

            context.write(outputKey, outputValue);

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