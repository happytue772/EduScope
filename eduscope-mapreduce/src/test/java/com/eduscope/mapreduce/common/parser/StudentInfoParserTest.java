package com.eduscope.mapreduce.common.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.eduscope.mapreduce.common.model.StudentInfoRecord;
import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * 실제 studentInfo.csv Parser 테스트.
 */
public class StudentInfoParserTest {

    @Test
    public void parseActualStudentInfoRow()
            throws Exception {

        Path csvPath = Paths.get(
                "..", "data", "raw", "studentInfo.csv"
        );

        assertTrue(Files.exists(csvPath));

        HeaderValidator validator =
                new HeaderValidator();

        StudentInfoParser parser =
                new StudentInfoParser();

        try (BufferedReader reader =
                Files.newBufferedReader(
                        csvPath,
                        StandardCharsets.UTF_8)) {

            String header = reader.readLine();

            assertTrue(
                    "studentInfo Header 오류",
                    validator.isStudentInfoHeader(header)
            );

            String dataLine = reader.readLine();

            assertNotNull(dataLine);

            StudentInfoRecord result =
                    parser.parse(dataLine);

            assertNotNull(result);
        }
    }
}