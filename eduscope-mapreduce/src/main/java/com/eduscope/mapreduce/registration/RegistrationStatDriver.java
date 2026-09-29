package com.eduscope.mapreduce.registration;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * studentRegistration.csv + studentInfo.csv를
 * 하나의 Registration 통계 Job으로 처리한다.
 */
public class RegistrationStatDriver {

    public static void main(String[] args)
            throws Exception {

        if (args.length != 3) {

            System.err.println(
                    "Usage: RegistrationStatDriver "
                    + "<registrationInput> "
                    + "<studentInfoInput> "
                    + "<output>"
            );

            System.exit(2);
        }

        Configuration configuration =
                new Configuration();

        Job job = Job.getInstance(
                configuration,
                "EduScope Registration Stat"
        );

        job.setJarByClass(
                RegistrationStatDriver.class
        );

        // studentRegistration.csv
        MultipleInputs.addInputPath(
                job,
                new Path(args[0]),
                TextInputFormat.class,
                RegistrationStatRegistrationMapper.class
        );

        // studentInfo.csv
        MultipleInputs.addInputPath(
                job,
                new Path(args[1]),
                TextInputFormat.class,
                RegistrationStatStudentInfoMapper.class
        );

        job.setReducerClass(
                RegistrationStatReducer.class
        );

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(
                job,
                new Path(args[2])
        );

        boolean success =
                job.waitForCompletion(true);

        System.exit(success ? 0 : 1);
    }
}