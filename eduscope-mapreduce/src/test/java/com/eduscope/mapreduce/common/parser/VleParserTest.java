package com.eduscope.mapreduce.common.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.eduscope.mapreduce.common.model.VleRecord;
import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * 실제 vle.csv Parser 테스트.
 */
public class VleParserTest {

    @Test
    public void parseActualVleRow() throws Exception {

        Path csvPath = Paths.get(
                "..", "data", "raw", "vle.csv"
        );

        assertTrue(Files.exists(csvPath));

        HeaderValidator validator =
                new HeaderValidator();

        VleParser parser =
                new VleParser();

        try (BufferedReader reader =
                Files.newBufferedReader(
                        csvPath,
                        StandardCharsets.UTF_8)) {

            String header = reader.readLine();

            assertTrue(
                    "vle Header 오류",
                    validator.isVleHeader(header)
            );

            String dataLine = reader.readLine();

            assertNotNull(dataLine);

            VleRecord result =
                    parser.parse(dataLine);

            assertNotNull(result);
        }
    }
}