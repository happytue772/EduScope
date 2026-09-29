package com.eduscope.mapreduce.courseactivity;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * COURSE_ACTIVITY MapReduce 실행 클래스.
 */
public class CourseActivityDriver {

    public static void main(String[] args)
            throws Exception {

        if (args.length != 2) {

            System.err.println(
                    "Usage: CourseActivityDriver "
                    + "<input> <output>"
            );

            System.exit(2);
        }

        Configuration configuration =
                new Configuration();

        Job job =
                Job.getInstance(
                        configuration,
                        "EduScope Course Activity"
                );

        job.setJarByClass(
                CourseActivityDriver.class
        );

        job.setMapperClass(
                CourseActivityMapper.class
        );

        job.setReducerClass(
                CourseActivityReducer.class
        );

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

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

        System.exit(
                success ? 0 : 1
        );
    }
}