package com.eduscope.mapreduce.common.parser;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.eduscope.mapreduce.common.model.StudentVleRecord;

/**
 * OULAD studentVle.csv 전용 Parser.
 */
public class StudentVleParser
        implements RowParser<StudentVleRecord> {

    @Override
    public StudentVleRecord parse(String line)
            throws ParseException {

        try {

            CSVParser parser =
                    CSVParser.parse(
                            line,
                            CSVFormat.DEFAULT
                    );

            List<CSVRecord> records =
                    parser.getRecords();

            if (records.size() != 1) {
                throw new ParseException(
                        "CSV 한 행이 정상적으로 Parsing되지 않았습니다."
                );
            }

            CSVRecord record = records.get(0);

            if (record.size() != 6) {
                throw new ParseException(
                        "studentVle 컬럼 개수가 6개가 아닙니다: "
                        + record.size()
                );
            }

            return new StudentVleRecord(
                    record.get(0).trim(),
                    record.get(1).trim(),
                    Long.parseLong(record.get(2).trim()),
                    Long.parseLong(record.get(3).trim()),
                    Integer.parseInt(record.get(4).trim()),
                    Long.parseLong(record.get(5).trim())
            );

        } catch (NumberFormatException e) {

            throw new ParseException(
                    "studentVle 숫자 컬럼 Parsing 실패",
                    e
            );

        } catch (IOException e) {

            throw new ParseException(
                    "studentVle CSV Parsing 실패",
                    e
            );
        }
    }
}