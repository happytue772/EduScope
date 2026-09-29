package com.eduscope.mapreduce.common.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.eduscope.mapreduce.common.model.StudentVleRecord;
import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * 실제 OULAD studentVle.csv를 이용한 Parser 테스트.
 */
public class StudentVleParserTest {

    @Test
    public void parseActualStudentVleRow()
            throws Exception {

        // 실제 EduScope 원본 CSV 경로
        Path csvPath = Paths.get(
                "..",
                "data",
                "raw",
                "studentVle.csv"
        );

        // 실제 파일 존재 여부 확인
        assertTrue(
                "studentVle.csv 파일이 존재해야 합니다.",
                Files.exists(csvPath)
        );

        HeaderValidator headerValidator =
                new HeaderValidator();

        StudentVleParser parser =
                new StudentVleParser();

        try (BufferedReader reader =
                Files.newBufferedReader(
                        csvPath,
                        StandardCharsets.UTF_8)) {

            // 실제 첫 번째 줄 = Header
            String header = reader.readLine();
          

            // 실제 두 번째 줄 = 첫 데이터
            String firstDataLine = reader.readLine();

            assertNotNull(
                    "studentVle 데이터 행이 없습니다.",
                    firstDataLine
            );

            StudentVleRecord result =
                    parser.parse(firstDataLine);

            assertNotNull(result);
        }
    }
}