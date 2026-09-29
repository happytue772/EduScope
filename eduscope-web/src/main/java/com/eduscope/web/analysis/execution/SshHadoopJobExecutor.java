package com.eduscope.web.analysis.execution;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Windows OpenSSH Client를 이용하여
 * VMware Ubuntu의 Hadoop Job을 실제 실행한다.
 *
 * 별도 SSH Library를 추가하지 않고
 * 현재 검증된 Windows ssh.exe를 사용한다.
 */
@Component
public class SshHadoopJobExecutor
        implements AnalysisJobExecutor {

    private static final Pattern OUTPUT_COUNT_PATTERN =
        Pattern.compile(
            "EDUSCOPE_OUTPUT_COUNT=(\\d+)"
        );


    private final String host;

    private final int port;

    private final String username;

    private final String privateKey;

    private final String environmentScript;

    private final String remoteJar;

    private final long timeoutMinutes;


    public SshHadoopJobExecutor(

            @Value(
                "${eduscope.hadoop.ssh.host}"
            )
            String host,

            @Value(
                "${eduscope.hadoop.ssh.port:22}"
            )
            int port,

            @Value(
                "${eduscope.hadoop.ssh.user}"
            )
            String username,

            @Value(
                "${eduscope.hadoop.ssh.private-key}"
            )
            String privateKey,

            @Value(
                "${eduscope.hadoop.remote.env-script}"
            )
            String environmentScript,

            @Value(
                "${eduscope.hadoop.remote.jar}"
            )
            String remoteJar,

            @Value(
                "${eduscope.hadoop.execution-timeout-minutes:120}"
            )
            long timeoutMinutes) {

        this.host =
            host;

        this.port =
            port;

        this.username =
            username;

        this.privateKey =
            privateKey;

        this.environmentScript =
            environmentScript;

        this.remoteJar =
            remoteJar;

        this.timeoutMinutes =
            timeoutMinutes;
    }


    @Override
    public AnalysisExecutionResult execute(
            AnalysisExecutionPlan plan) {

        long startedAt =
            System.currentTimeMillis();


        ExecutorService streamExecutor =
            null;


        try {

            validatePrivateKey();


            String remoteCommand =
                buildRemoteCommand(
                    plan
                );


            List<String> command =
                new ArrayList<>();


            command.add("ssh");

            command.add("-i");

            command.add(privateKey);

            command.add("-p");

            command.add(
                String.valueOf(port)
            );

            command.add("-o");

            command.add(
                "BatchMode=yes"
            );

            command.add("-o");

            command.add(
                "ConnectTimeout=10"
            );

            command.add("-o");

            command.add(
                "ServerAliveInterval=15"
            );

            command.add(
                username
                + "@"
                + host
            );

            command.add(
                remoteCommand
            );


            Process process =
                new ProcessBuilder(
                    command
                )
                .redirectErrorStream(true)
                .start();


            streamExecutor =
                Executors
                    .newSingleThreadExecutor();


            Future<String> outputFuture =
                streamExecutor.submit(
                    () ->
                        readOutput(
                            process.getInputStream()
                        )
                );


            boolean finished =
                process.waitFor(
                    timeoutMinutes,
                    TimeUnit.MINUTES
                );


            if (!finished) {

                process.destroyForcibly();


                long elapsed =
                    System.currentTimeMillis()
                    - startedAt;


                return AnalysisExecutionResult.failure(
                    -1,
                    elapsed,
                    "SSH_TIMEOUT",
                    "Hadoop 실행 제한시간을 초과했습니다."
                );
            }


            int exitCode =
                process.exitValue();


            String output =
                outputFuture.get(
                    10,
                    TimeUnit.SECONDS
                );


            long elapsed =
                System.currentTimeMillis()
                - startedAt;


            if (
                exitCode != 0
            ) {

                return AnalysisExecutionResult.failure(
                    exitCode,
                    elapsed,
                    detectErrorStep(
                        exitCode,
                        output
                    ),
                    output
                );
            }


            Long outputCount =
                parseOutputCount(
                    output
                );


            if (
                outputCount == null
            ) {

                return AnalysisExecutionResult.failure(
                    exitCode,
                    elapsed,
                    "RESULT_PARSE",
                    "MapReduce는 종료되었지만 "
                    + "Output Count를 확인하지 못했습니다.\n"
                    + output
                );
            }


            return AnalysisExecutionResult.success(
                exitCode,
                outputCount,
                elapsed,
                output
            );


        } catch (Exception ex) {

            long elapsed =
                System.currentTimeMillis()
                - startedAt;


            return AnalysisExecutionResult.failure(
                -1,
                elapsed,
                "SSH_EXECUTION",
                ex.getClass().getSimpleName()
                + ": "
                + ex.getMessage()
            );

        } finally {

            if (
                streamExecutor != null
            ) {

                streamExecutor.shutdownNow();
            }
        }
    }


    /**
     * 실제 원격 실행 Shell 구성.
     */
    private String buildRemoteCommand(
            AnalysisExecutionPlan plan) {

        StringBuilder command =
            new StringBuilder();


        /*
         * SSH 세션에서도 반드시
         * EduScope 전용 Hadoop 환경을 적용한다.
         */
        command
            .append("source ")
            .append(environmentScript)
            .append(" >/dev/null; ");


        /*
         * Input 존재 확인.
         */
        for (
            String input
            : plan.getInputPaths()
        ) {

            command
                .append(
                    "if ! hdfs dfs -test -e "
                )
                .append(input)
                .append(
                    "; then "
                )
                .append(
                    "echo EDUSCOPE_ERROR=INPUT_NOT_FOUND:"
                )
                .append(input)
                .append(
                    "; exit 41; fi; "
                );
        }


        /*
         * 기존 Output은 덮어쓰지 않는다.
         */
        for (
            String output
            : plan.getOutputPaths()
        ) {

            appendOutputNotExistsCheck(
                command,
                output
            );
        }


        /*
         * 다단계 분석의 Job 전용 임시 경로도
         * 기존 경로를 덮어쓰지 않는다.
         */
        for (
            String temp
            : plan.getTemporaryPaths()
        ) {

            appendOutputNotExistsCheck(
                command,
                temp
            );
        }


        /*
         * 실제 hadoop jar 실행.
         */
        command
            .append("hadoop jar ")
            .append(remoteJar)
            .append(" ")
            .append(
                plan.getDriverClass()
            );


        for (
            String argument
            : plan.getArguments()
        ) {

            command
                .append(" ")
                .append(argument);
        }


        command.append("; ");


        /*
         * Driver의 실제 Exit Code 검사.
         */
        command
            .append(
                "rc=$?; "
            )
            .append(
                "if [ $rc -ne 0 ]; then "
            )
            .append(
                "echo EDUSCOPE_ERROR=HADOOP_EXIT:$rc; "
            )
            .append(
                "exit $rc; "
            )
            .append(
                "fi; "
            );


        /*
         * Exit Code 0만으로 SUCCESS 처리하지 않고
         * 실제 _SUCCESS까지 확인한다.
         */
        for (
            String output
            : plan.getOutputPaths()
        ) {

            command
                .append(
                    "if ! hdfs dfs -test -e "
                )
                .append(output)
                .append(
                    "/_SUCCESS; then "
                )
                .append(
                    "echo EDUSCOPE_ERROR=SUCCESS_FILE_MISSING:"
                )
                .append(output)
                .append(
                    "; exit 43; fi; "
                );
        }


        /*
         * 실제 최종 결과 Row 수 계산.
         */
        command.append(
            "total=0; "
        );


        for (
            String output
            : plan.getOutputPaths()
        ) {

            command
                .append(
                    "count=$(hdfs dfs -cat "
                )
                .append(output)
                .append(
                    "/part-* 2>/dev/null | wc -l); "
                )
                .append(
                    "total=$((total+count)); "
                );
        }


        command.append(
            "echo EDUSCOPE_OUTPUT_COUNT=$total"
        );


        return "bash -lc '"
            + command
            + "'";
    }


    /**
     * Output 및 임시 경로가 기존에 존재하는지 검사한다.
     */
    private void appendOutputNotExistsCheck(
            StringBuilder command,
            String path) {

        command
            .append(
                "if hdfs dfs -test -e "
            )
            .append(path)
            .append(
                "; then "
            )
            .append(
                "echo EDUSCOPE_ERROR=OUTPUT_EXISTS:"
            )
            .append(path)
            .append(
                "; exit 42; fi; "
            );
    }


    /**
     * Hadoop 로그 전체를 메모리에 쌓지 않고
     * 마지막 200줄만 보관한다.
     */
    private String readOutput(
            InputStream inputStream)
            throws Exception {

        Deque<String> tail =
            new ArrayDeque<>();


        try (
            BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                    )
                )
        ) {

            String line;


            while (
                (line = reader.readLine())
                != null
            ) {

                if (
                    tail.size()
                    >= 200
                ) {

                    tail.removeFirst();
                }


                tail.addLast(
                    line
                );
            }
        }


        return String.join(
            System.lineSeparator(),
            tail
        );
    }


    /**
     * 원격 Shell이 반환한 실제 Output 건수를 추출한다.
     */
    private Long parseOutputCount(
            String output) {

        Matcher matcher =
            OUTPUT_COUNT_PATTERN
                .matcher(output);


        Long result =
            null;


        while (
            matcher.find()
        ) {

            result =
                Long.valueOf(
                    matcher.group(1)
                );
        }


        return result;
    }


    /**
     * 실제 실패 지점을 분류한다.
     */
    private String detectErrorStep(
            int exitCode,
            String output) {

        if (
            output.contains(
                "INPUT_NOT_FOUND"
            )
        ) {

            return "HDFS_INPUT_CHECK";
        }


        if (
            output.contains(
                "OUTPUT_EXISTS"
            )
        ) {

            return "HDFS_OUTPUT_CHECK";
        }


        if (
            output.contains(
                "SUCCESS_FILE_MISSING"
            )
        ) {

            return "HDFS_SUCCESS_CHECK";
        }


        if (
            exitCode == 255
        ) {

            return "SSH_CONNECTION";
        }


        return "MAPREDUCE";
    }


    /**
     * SSH Private Key가 실제로 존재하는지 검사한다.
     */
    private void validatePrivateKey() {

        if (
            !Files.isRegularFile(
                Paths.get(
                    privateKey
                )
            )
        ) {

            throw new IllegalStateException(
                "SSH Private Key를 찾을 수 없습니다: "
                + privateKey
            );
        }
    }
}