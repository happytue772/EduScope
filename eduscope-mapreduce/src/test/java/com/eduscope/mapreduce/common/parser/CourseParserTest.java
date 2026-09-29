package com.eduscope.mapreduce.common.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.eduscope.mapreduce.common.model.CourseRecord;
import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * 실제 courses.csv Parser 테스트.
 */
public class CourseParserTest {

    @Test
    public void parseActualCourseRow()
            throws Exception {

        Path csvPath = Paths.get(
                "..",
                "data",
                "raw",
                "courses.csv"
        );

        assertTrue(Files.exists(csvPath));

        HeaderValidator validator =
                new HeaderValidator();

        CourseParser parser =
                new CourseParser();

        try (BufferedReader reader =
                Files.newBufferedReader(
                        csvPath,
                        StandardCharsets.UTF_8)) {

            String header = reader.readLine();

            assertTrue(
                    "courses Header 오류",
                    validator.isCoursesHeader(header)
            );

            String dataLine = reader.readLine();

            assertNotNull(dataLine);

            CourseRecord result =
                    parser.parse(dataLine);

            assertNotNull(result);
        }
    }
}