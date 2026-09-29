package com.eduscope.mapreduce.activityresult;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * ACTIVITY_RESULT 2단계 MapReduce 실행.
 *
 * args:
 * 0 = STUDENT_ACTIVITY 결과
 * 1 = studentInfo.csv
 * 2 = 임시 Join 출력
 * 3 = 최종 출력
 */
public class ActivityResultDriver {

    public static void main(String[] args)
            throws Exception {

        if (args.length != 4) {

            System.err.println(
                    "Usage: ActivityResultDriver "
                    + "<studentActivityInput> "
                    + "<studentInfoInput> "
                    + "<tempOutput> "
                    + "<finalOutput>"
            );

            System.exit(2);
        }

        Configuration configuration =
                new Configuration();

        // ===============================
        // Job 1 : 학생별 Join
        // ===============================

        Job joinJob = Job.getInstance(
                configuration,
                "EduScope Activity Result Join"
        );

        joinJob.setJarByClass(
                ActivityResultDriver.class
        );

        MultipleInputs.addInputPath(
                joinJob,
                new Path(args[0]),
                TextInputFormat.class,
                ActivityResultActivityMapper.class
        );

        MultipleInputs.addInputPath(
                joinJob,
                new Path(args[1]),
                TextInputFormat.class,
                ActivityResultStudentInfoMapper.class
        );

        joinJob.setReducerClass(
                ActivityResultJoinReducer.class
        );

        joinJob.setMapOutputKeyClass(Text.class);
        joinJob.setMapOutputValueClass(Text.class);

        joinJob.setOutputKeyClass(Text.class);
        joinJob.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(
                joinJob,
                new Path(args[2])
        );

        if (!joinJob.waitForCompletion(true)) {
            System.exit(1);
        }

        // ===============================
        // Job 2 : 결과 그룹별 집계
        // ===============================

        Job aggregateJob = Job.getInstance(
                configuration,
                "EduScope Activity Result Aggregate"
        );

        aggregateJob.setJarByClass(
                ActivityResultDriver.class
        );

        aggregateJob.setMapperClass(
                ActivityResultAggregateMapper.class
        );

        aggregateJob.setReducerClass(
                ActivityResultAggregateReducer.class
        );

        aggregateJob.setMapOutputKeyClass(Text.class);
        aggregateJob.setMapOutputValueClass(Text.class);

        aggregateJob.setOutputKeyClass(Text.class);
        aggregateJob.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(
                aggregateJob,
                new Path(args[2])
        );

        FileOutputFormat.setOutputPath(
                aggregateJob,
                new Path(args[3])
        );

        System.exit(
                aggregateJob.waitForCompletion(true)
                ? 0
                : 1
        );
    }
}