package com.eduscope.mapreduce.common.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.eduscope.mapreduce.common.model.AssessmentRecord;
import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * 실제 OULAD assessments.csv를 이용한 Parser 테스트.
 */
public class AssessmentParserTest {

    @Test
    public void parseActualAssessmentRow()
            throws Exception {

        // 실제 OULAD 원본 파일 경로
        Path csvPath = Paths.get(
                "..",
                "data",
                "raw",
                "assessments.csv"
        );

        // 파일 존재 여부 확인
        assertTrue(
                "assessments.csv 파일이 존재해야 합니다.",
                Files.exists(csvPath)
        );

        HeaderValidator validator =
                new HeaderValidator();

        AssessmentParser parser =
                new AssessmentParser();

        try (BufferedReader reader =
                Files.newBufferedReader(
                        csvPath,
                        StandardCharsets.UTF_8)) {

            // 실제 Header 검증
            String header = reader.readLine();

            assertTrue(
                    "assessments Header 오류",
                    validator.isAssessmentsHeader(header)
            );

            // 실제 첫 번째 데이터 행
            String dataLine = reader.readLine();

            assertNotNull(
                    "assessments.csv 데이터가 없습니다.",
                    dataLine
            );

            AssessmentRecord result =
                    parser.parse(dataLine);

            assertNotNull(result);
        }
    }
}