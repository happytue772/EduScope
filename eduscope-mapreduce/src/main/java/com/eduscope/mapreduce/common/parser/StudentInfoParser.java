package com.eduscope.mapreduce.common.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.eduscope.mapreduce.common.model.StudentInfoRecord;

/**
 * OULAD studentInfo.csv 한 행을
 * StudentInfoRecord 객체로 변환한다.
 */
public class StudentInfoParser
        implements RowParser<StudentInfoRecord> {

    private static final int EXPECTED_COLUMN_COUNT = 12;

    @Override
    public StudentInfoRecord parse(String line)
            throws ParseException {

        if (line == null || line.trim().isEmpty()) {
            throw new ParseException(
                    "studentInfo 행이 비어 있습니다."
            );
        }

        try (CSVParser parser =
                CSVFormat.DEFAULT.parse(
                        new StringReader(line))) {

            List<CSVRecord> records =
                    parser.getRecords();

            if (records.size() != 1) {
                throw new ParseException(
                        "studentInfo Parsing 결과가 1건이 아닙니다."
                );
            }

            CSVRecord record = records.get(0);

            if (record.size() != EXPECTED_COLUMN_COUNT) {
                throw new ParseException(
                        "studentInfo 컬럼 개수 오류. 실제: "
                        + record.size()
                        + ", 예상: "
                        + EXPECTED_COLUMN_COUNT
                );
            }

            return new StudentInfoRecord(
                    record.get(0).trim(),
                    record.get(1).trim(),

                    Long.parseLong(
                            record.get(2).trim()),

                    parseNullableString(
                            record.get(3)),

                    parseNullableString(
                            record.get(4)),

                    parseNullableString(
                            record.get(5)),

                    parseNullableString(
                            record.get(6)),

                    parseNullableString(
                            record.get(7)),

                    Integer.parseInt(
                            record.get(8).trim()),

                    Integer.parseInt(
                            record.get(9).trim()),

                    parseNullableString(
                            record.get(10)),

                    record.get(11).trim()
            );

        } catch (NumberFormatException e) {

            throw new ParseException(
                    "studentInfo 숫자 Parsing 실패",
                    e
            );

        } catch (IOException e) {

            throw new ParseException(
                    "studentInfo CSV Parsing 실패",
                    e
            );
        }
    }

    /**
     * 빈 문자열은 임의 값으로 변경하지 않고 null 유지.
     */
    private String parseNullableString(
            String value) {

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }
}