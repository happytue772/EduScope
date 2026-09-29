package com.eduscope.mapreduce.dataquality;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * EduScope DATA_QUALITY.
 *
 * Job 1:
 * HEADER_MISMATCH / PARSE_ERROR / MISSING_VALUE /
 * INVALID_RANGE / FK_MISMATCH
 *
 * Job 2:
 * DUPLICATE / PK_DUPLICATE
 *
 * 기존 3개 실행 인자 구조는 유지한다.
 */
public class DataQualityDriver {

    public static void main(String[] args)
            throws Exception {

        if (args.length != 3) {

            System.err.println(
                    "Usage: DataQualityDriver "
                    + "<rawBase> "
                    + "<rowQualityOutput> "
                    + "<duplicateOutput>"
            );

            System.exit(2);
        }

        Configuration configuration =
                new Configuration();

        /*
         * raw/ 아래 7개 하위 디렉터리를 모두 읽는다.
         */
        configuration.setBoolean(
                "mapreduce.input.fileinputformat.input.dir.recursive",
                true
        );

        /*
         * Row Mapper가 FK 검사용 기준 파일을
         * 동일 HDFS RAW 영역에서 읽을 수 있도록 전달한다.
         */
        configuration.set(
                DataQualitySupport.RAW_BASE_CONFIG,
                args[0]
        );

        // ==================================
        // Job 1 : Row Quality
        // ==================================

        Job rowJob =
                Job.getInstance(
                        configuration,
                        "EduScope Data Quality Row"
                );

        rowJob.setJarByClass(
                DataQualityDriver.class
        );

        rowJob.setMapperClass(
                DataQualityRowMapper.class
        );

        rowJob.setReducerClass(
                DataQualitySummaryReducer.class
        );

        rowJob.setMapOutputKeyClass(Text.class);
        rowJob.setMapOutputValueClass(Text.class);

        rowJob.setOutputKeyClass(Text.class);
        rowJob.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(
                rowJob,
                new Path(args[0])
        );

        FileOutputFormat.setOutputPath(
                rowJob,
                new Path(args[1])
        );

        if (!rowJob.waitForCompletion(true)) {
            System.exit(1);
        }

        // ==================================
        // Job 2 : Duplicate Quality
        // ==================================

        Job duplicateJob =
                Job.getInstance(
                        configuration,
                        "EduScope Data Quality Duplicate"
                );

        duplicateJob.setJarByClass(
                DataQualityDriver.class
        );

        duplicateJob.setMapperClass(
                DataQualityDuplicateMapper.class
        );

        duplicateJob.setReducerClass(
                DataQualityDuplicateReducer.class
        );

        duplicateJob.setMapOutputKeyClass(
                Text.class
        );

        duplicateJob.setMapOutputValueClass(
                LongWritable.class
        );

        duplicateJob.setOutputKeyClass(Text.class);
        duplicateJob.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(
                duplicateJob,
                new Path(args[0])
        );

        FileOutputFormat.setOutputPath(
                duplicateJob,
                new Path(args[2])
        );

        System.exit(
                duplicateJob.waitForCompletion(true)
                ? 0
                : 1
        );
    }
}
