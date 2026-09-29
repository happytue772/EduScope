package com.eduscope.mapreduce.vleactivity;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * VLE_ACTIVITY MapReduce 실행 클래스.
 */
public class VleActivityDriver {

    public static void main(String[] args)
            throws Exception {

        if (args.length != 2) {

            System.err.println(
                    "Usage: VleActivityDriver "
                    + "<input> <output>"
            );

            System.exit(2);
        }

        Configuration configuration =
                new Configuration();

        Job job =
                Job.getInstance(
                        configuration,
                        "EduScope VLE Activity"
                );

        job.setJarByClass(
                VleActivityDriver.class
        );

        job.setMapperClass(
                VleActivityMapper.class
        );

        job.setReducerClass(
                VleActivityReducer.class
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