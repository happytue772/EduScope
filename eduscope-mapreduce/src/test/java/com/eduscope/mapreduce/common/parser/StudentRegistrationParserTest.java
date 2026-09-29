package com.eduscope.mapreduce.common.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.eduscope.mapreduce.common.model.StudentRegistrationRecord;
import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * 실제 studentRegistration.csv Parser 테스트.
 */
public class StudentRegistrationParserTest {

    @Test
    public void parseActualStudentRegistrationRow()
            throws Exception {

        Path csvPath = Paths.get(
                "..",
                "data",
                "raw",
                "studentRegistration.csv"
        );

        assertTrue(Files.exists(csvPath));

        HeaderValidator validator =
                new HeaderValidator();

        StudentRegistrationParser parser =
                new StudentRegistrationParser();

        try (BufferedReader reader =
                Files.newBufferedReader(
                        csvPath,
                        StandardCharsets.UTF_8)) {

            String header = reader.readLine();

            assertTrue(
                    "studentRegistration Header 오류",
                    validator.isStudentRegistrationHeader(header)
            );

            String dataLine = reader.readLine();

            assertNotNull(dataLine);

            StudentRegistrationRecord result =
                    parser.parse(dataLine);

            assertNotNull(result);
        }
    }
}