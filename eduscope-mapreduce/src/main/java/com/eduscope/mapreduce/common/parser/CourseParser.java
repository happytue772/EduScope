package com.eduscope.mapreduce.common.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.eduscope.mapreduce.common.model.CourseRecord;

/**
 * courses.csv Parser.
 */
public class CourseParser
        implements RowParser<CourseRecord> {

    @Override
    public CourseRecord parse(String line)
            throws ParseException {

        if (line == null || line.trim().isEmpty()) {
            throw new ParseException("courses 행이 비어 있습니다.");
        }

        try (CSVParser parser =
                CSVFormat.DEFAULT.parse(
                        new StringReader(line))) {

            List<CSVRecord> records =
                    parser.getRecords();

            if (records.size() != 1) {
                throw new ParseException(
                        "courses Parsing 결과가 1건이 아닙니다."
                );
            }

            CSVRecord record = records.get(0);

            if (record.size() != 3) {
                throw new ParseException(
                        "courses 컬럼 개수 오류: "
                        + record.size()
                );
            }

            return new CourseRecord(
                    record.get(0).trim(),
                    record.get(1).trim(),
                    Integer.parseInt(
                            record.get(2).trim())
            );

        } catch (NumberFormatException e) {

            throw new ParseException(
                    "courses 숫자 Parsing 실패",
                    e
            );

        } catch (IOException e) {

            throw new ParseException(
                    "courses CSV Parsing 실패",
                    e
            );
        }
    }
}