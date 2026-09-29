package com.eduscope.mapreduce.learningsummary;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * STUDENT_LEARNING_SUMMARY 3단계 MapReduce.
 *
 * args:
 * 0 studentAssessment
 * 1 assessments
 * 2 STUDENT_ACTIVITY 결과
 * 3 assessment join 임시경로
 * 4 assessment summary 임시경로
 * 5 최종 출력경로
 */
public class StudentLearningSummaryDriver {

    public static void main(String[] args)
            throws Exception {

        if (args.length != 6) {

            System.err.println(
                    "Usage: StudentLearningSummaryDriver "
                    + "<studentAssessment> "
                    + "<assessments> "
                    + "<studentActivity> "
                    + "<assessmentJoinTemp> "
                    + "<assessmentSummaryTemp> "
                    + "<finalOutput>"
            );

            System.exit(2);
        }

        Configuration configuration =
                new Configuration();

        // ======================================
        // Job 1 : Assessment Metadata Join
        // ======================================

        Job joinJob =
                Job.getInstance(
                        configuration,
                        "EduScope Learning Assessment Join"
                );

        joinJob.setJarByClass(
                StudentLearningSummaryDriver.class
        );

        MultipleInputs.addInputPath(
                joinJob,
                new Path(args[0]),
                TextInputFormat.class,
                LearningAssessmentMapper.class
        );

        MultipleInputs.addInputPath(
                joinJob,
                new Path(args[1]),
                TextInputFormat.class,
                LearningAssessmentMetaMapper.class
        );

        joinJob.setReducerClass(
                LearningAssessmentJoinReducer.class
        );

        joinJob.setMapOutputKeyClass(
                LongWritable.class
        );

        joinJob.setMapOutputValueClass(
                Text.class
        );

        joinJob.setOutputKeyClass(Text.class);
        joinJob.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(
                joinJob,
                new Path(args[3])
        );

        if (!joinJob.waitForCompletion(true)) {
            System.exit(1);
        }

        // ======================================
        // Job 2 : 학생별 Assessment 집계
        // ======================================

        Job summaryJob =
                Job.getInstance(
                        configuration,
                        "EduScope Learning Assessment Summary"
                );

        summaryJob.setJarByClass(
                StudentLearningSummaryDriver.class
        );

        summaryJob.setMapperClass(
                LearningAssessmentSummaryMapper.class
        );

        summaryJob.setReducerClass(
                LearningAssessmentSummaryReducer.class
        );

        summaryJob.setMapOutputKeyClass(Text.class);
        summaryJob.setMapOutputValueClass(Text.class);

        summaryJob.setOutputKeyClass(Text.class);
        summaryJob.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(
                summaryJob,
                new Path(args[3])
        );

        FileOutputFormat.setOutputPath(
                summaryJob,
                new Path(args[4])
        );

        if (!summaryJob.waitForCompletion(true)) {
            System.exit(1);
        }

        // ======================================
        // Job 3 : Activity + Assessment Summary
        // ======================================

        Job finalJob =
                Job.getInstance(
                        configuration,
                        "EduScope Student Learning Summary"
                );

        finalJob.setJarByClass(
                StudentLearningSummaryDriver.class
        );

        MultipleInputs.addInputPath(
                finalJob,
                new Path(args[2]),
                TextInputFormat.class,
                LearningActivityMapper.class
        );

        MultipleInputs.addInputPath(
                finalJob,
                new Path(args[4]),
                TextInputFormat.class,
                LearningAssessmentSummaryJoinMapper.class
        );

        finalJob.setReducerClass(
                StudentLearningSummaryReducer.class
        );

        finalJob.setMapOutputKeyClass(Text.class);
        finalJob.setMapOutputValueClass(Text.class);

        finalJob.setOutputKeyClass(Text.class);
        finalJob.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(
                finalJob,
                new Path(args[5])
        );

        System.exit(
                finalJob.waitForCompletion(true)
                ? 0
                : 1
        );
    }
}