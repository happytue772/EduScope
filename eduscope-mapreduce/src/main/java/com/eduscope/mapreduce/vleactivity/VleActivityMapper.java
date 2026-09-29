package com.eduscope.mapreduce.vleactivity;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.eduscope.mapreduce.common.counter.EduScopeCounter;
import com.eduscope.mapreduce.common.model.StudentVleRecord;
import com.eduscope.mapreduce.common.parser.ParseException;
import com.eduscope.mapreduce.common.parser.StudentVleParser;
import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * studentVle.csv를
 * 강의 + 개설차수 + VLE 자료(id_site) 단위로 변환한다.
 */
public class VleActivityMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    private final StudentVleParser parser =
            new StudentVleParser();

    private final HeaderValidator headerValidator =
            new HeaderValidator();

    private final Text outputKey =
            new Text();

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
        if (key.get() == 0
                && headerValidator.isStudentVleHeader(line)) {
            return;
        }

        context.getCounter(
                EduScopeCounter.INPUT_RECORD
        ).increment(1);

        try {

            StudentVleRecord record =
                    parser.parse(line);

            /*
             * Key:
             * module | presentation | siteId
             */
            outputKey.set(
                    record.getCodeModule()
                    + "|"
                    + record.getCodePresentation()
                    + "|"
                    + record.getSiteId()
            );

            /*
             * Reducer 전달값:
             * studentId | relativeDay | sumClick
             */
            outputValue.set(
                    record.getStudentId()
                    + "|"
                    + record.getRelativeDay()
                    + "|"
                    + record.getSumClick()
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