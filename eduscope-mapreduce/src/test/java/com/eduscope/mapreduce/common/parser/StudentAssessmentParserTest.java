package com.eduscope.mapreduce.common.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.eduscope.mapreduce.common.model.StudentAssessmentRecord;
import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * 실제 studentAssessment.csv Parser 테스트.
 */
public class StudentAssessmentParserTest {

    @Test
    public void parseActualStudentAssessmentRow()
            throws Exception {

        Path csvPath = Paths.get(
                "..",
                "data",
                "raw",
                "studentAssessment.csv"
        );

        assertTrue(Files.exists(csvPath));

        HeaderValidator validator =
                new HeaderValidator();

        StudentAssessmentParser parser =
                new StudentAssessmentParser();

        try (BufferedReader reader =
                Files.newBufferedReader(
                        csvPath,
                        StandardCharsets.UTF_8)) {

            String header = reader.readLine();

            assertTrue(
                    "studentAssessment Header 오류",
                    validator.isStudentAssessmentHeader(header)
            );

            String dataLine = reader.readLine();

            assertNotNull(dataLine);

            StudentAssessmentRecord result =
                    parser.parse(dataLine);

            assertNotNull(result);
        }
    }
}