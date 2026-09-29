package com.eduscope.web.analysis.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.eduscope.web.analysis.entity.AnalysisJob;

/**
 * Analysis Type을 실제 MapReduce Driver와
 * 실행 인자로 변환한다.
 */
@Component
public class AnalysisExecutionRegistry {

    private static final String HDFS_BASE =
        "/user/user/eduscope";


    /*
     * Shell Injection 방지를 위해
     * EduScope HDFS 경로에서 사용할 문자만 허용한다.
     */
    private static final Pattern SAFE_HDFS_PATH =
        Pattern.compile(
            "^/user/user/eduscope/[A-Za-z0-9._/-]+$"
        );


    public AnalysisExecutionPlan resolve(
            AnalysisJob job) {

        List<String> inputs =
            splitPaths(
                job.getHdfsInputPath()
            );


        List<String> outputs =
            splitPaths(
                job.getHdfsOutputPath()
            );


        String tempBase =
            HDFS_BASE
            + "/tmp/job-"
            + job.getJobId();


        switch (
            job.getAnalysisType()
        ) {

        case "STUDENT_ACTIVITY":

            return singleInputPlan(
                job,
                "com.eduscope.mapreduce.studentactivity.StudentActivityDriver",
                inputs,
                outputs
            );


        case "COURSE_ACTIVITY":

            return singleInputPlan(
                job,
                "com.eduscope.mapreduce.courseactivity.CourseActivityDriver",
                inputs,
                outputs
            );


        case "COURSE_WEEKLY_ACTIVITY":

            return singleInputPlan(
                job,
                "com.eduscope.mapreduce.courseweeklyactivity.CourseWeeklyActivityDriver",
                inputs,
                outputs
            );


        case "VLE_ACTIVITY":

            return singleInputPlan(
                job,
                "com.eduscope.mapreduce.vleactivity.VleActivityDriver",
                inputs,
                outputs
            );


        case "ASSESSMENT":

            return singleInputPlan(
                job,
                "com.eduscope.mapreduce.assessment.AssessmentStatDriver",
                inputs,
                outputs
            );


        case "COURSE_RESULT":

            return singleInputPlan(
                job,
                "com.eduscope.mapreduce.courseresult.CourseResultDriver",
                inputs,
                outputs
            );


        case "REGISTRATION":

            requireCount(
                inputs,
                2,
                "REGISTRATION Input"
            );

            requireCount(
                outputs,
                1,
                "REGISTRATION Output"
            );


            return new AnalysisExecutionPlan(
                job.getJobId(),
                job.getAnalysisType(),
                "com.eduscope.mapreduce.registration.RegistrationStatDriver",

                List.of(
                    inputs.get(0),
                    inputs.get(1),
                    outputs.get(0)
                ),

                inputs,
                outputs,
                Collections.emptyList()
            );


        case "ACTIVITY_RESULT":

            requireCount(
                inputs,
                2,
                "ACTIVITY_RESULT Input"
            );

            requireCount(
                outputs,
                1,
                "ACTIVITY_RESULT Output"
            );


            String activityTemp =
                tempBase
                + "/activity-result-join";


            return new AnalysisExecutionPlan(
                job.getJobId(),
                job.getAnalysisType(),
                "com.eduscope.mapreduce.activityresult.ActivityResultDriver",

                List.of(
                    inputs.get(0),
                    inputs.get(1),
                    activityTemp,
                    outputs.get(0)
                ),

                inputs,
                outputs,

                List.of(
                    activityTemp
                )
            );


        case "STUDENT_LEARNING_SUMMARY":

            requireCount(
                inputs,
                3,
                "STUDENT_LEARNING_SUMMARY Input"
            );

            requireCount(
                outputs,
                1,
                "STUDENT_LEARNING_SUMMARY Output"
            );


            String learningJoinTemp =
                tempBase
                + "/learning-assessment-join";


            String learningSummaryTemp =
                tempBase
                + "/learning-assessment-summary";


            return new AnalysisExecutionPlan(
                job.getJobId(),
                job.getAnalysisType(),
                "com.eduscope.mapreduce.learningsummary.StudentLearningSummaryDriver",

                List.of(
                    inputs.get(0),
                    inputs.get(1),
                    inputs.get(2),
                    learningJoinTemp,
                    learningSummaryTemp,
                    outputs.get(0)
                ),

                inputs,
                outputs,

                List.of(
                    learningJoinTemp,
                    learningSummaryTemp
                )
            );


        case "DATA_QUALITY":

            requireCount(
                inputs,
                1,
                "DATA_QUALITY Input"
            );

            requireCount(
                outputs,
                2,
                "DATA_QUALITY Output"
            );


            return new AnalysisExecutionPlan(
                job.getJobId(),
                job.getAnalysisType(),
                "com.eduscope.mapreduce.dataquality.DataQualityDriver",

                List.of(
                    inputs.get(0),
                    outputs.get(0),
                    outputs.get(1)
                ),

                inputs,
                outputs,
                Collections.emptyList()
            );


        default:

            throw new IllegalArgumentException(
                "실행할 수 없는 Analysis Type입니다: "
                + job.getAnalysisType()
            );
        }
    }


    /**
     * Input 1개 + Output 1개 Driver 공통 처리.
     */
    private AnalysisExecutionPlan singleInputPlan(
            AnalysisJob job,
            String driverClass,
            List<String> inputs,
            List<String> outputs) {

        requireCount(
            inputs,
            1,
            job.getAnalysisType()
            + " Input"
        );


        requireCount(
            outputs,
            1,
            job.getAnalysisType()
            + " Output"
        );


        return new AnalysisExecutionPlan(
            job.getJobId(),
            job.getAnalysisType(),
            driverClass,

            List.of(
                inputs.get(0),
                outputs.get(0)
            ),

            inputs,
            outputs,
            Collections.emptyList()
        );
    }


    /**
     * DB에서는 복수 HDFS 경로를
     * 세미콜론(;)으로 구분한다.
     */
    private List<String> splitPaths(
            String rawPaths) {

        if (
            rawPaths == null
            ||
            rawPaths.isBlank()
        ) {

            throw new IllegalArgumentException(
                "HDFS 경로가 비어 있습니다."
            );
        }


        String[] values =
            rawPaths.split(";");


        List<String> result =
            new ArrayList<>();


        for (
            String value
            : values
        ) {

            String path =
                value.trim();


            validatePath(
                path
            );


            result.add(
                path
            );
        }


        return result;
    }


    /**
     * 실제 EduScope HDFS 경로만 허용.
     */
    private void validatePath(
            String path) {

        if (
            !SAFE_HDFS_PATH
                .matcher(path)
                .matches()
        ) {

            throw new IllegalArgumentException(
                "허용되지 않는 HDFS 경로입니다: "
                + path
            );
        }
    }


    private void requireCount(
            List<String> paths,
            int expected,
            String name) {

        if (
            paths.size()
            != expected
        ) {

            throw new IllegalArgumentException(
                name
                + " 경로는 "
                + expected
                + "개가 필요합니다. 현재="
                + paths.size()
            );
        }
    }
}