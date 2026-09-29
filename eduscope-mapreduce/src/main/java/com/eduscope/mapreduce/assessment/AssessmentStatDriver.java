package com.eduscope.mapreduce.assessment;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * ASSESSMENT 통계 MapReduce 실행 클래스.
 */
public class AssessmentStatDriver {

    public static void main(String[] args)
            throws Exception {

        if (args.length != 2) {

            System.err.println(
                    "Usage: AssessmentStatDriver "
                    + "<input> <output>"
            );

            System.exit(2);
        }

        Configuration configuration =
                new Configuration();

        Job job =
                Job.getInstance(
                        configuration,
                        "EduScope Assessment Stat"
                );

        job.setJarByClass(
                AssessmentStatDriver.class
        );

        job.setMapperClass(
                AssessmentStatMapper.class
        );

        job.setReducerClass(
                AssessmentStatReducer.class
        );

        job.setMapOutputKeyClass(
                LongWritable.class
        );

        job.setMapOutputValueClass(
                Text.class
        );

        job.setOutputKeyClass(
                LongWritable.class
        );

        job.setOutputValueClass(
                Text.class
        );

        FileInputFormat.addInputPath(
                job,
                new Path(args[0])
        );

        FileOutputFormat.setOutputPath(
                job,
                new Path(args[1])
        );

        boolean success =
                job.waitForCompletion(true);

        System.exit(success ? 0 : 1);
    }
}