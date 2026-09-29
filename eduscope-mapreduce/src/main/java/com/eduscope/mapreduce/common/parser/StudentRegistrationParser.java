package com.eduscope.mapreduce.common.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.eduscope.mapreduce.common.model.StudentRegistrationRecord;

/**
 * OULAD studentRegistration.csv 한 행을
 * StudentRegistrationRecord 객체로 변환한다.
 */
public class StudentRegistrationParser
        implements RowParser<StudentRegistrationRecord> {

    private static final int EXPECTED_COLUMN_COUNT = 5;

    @Override
    public StudentRegistrationRecord parse(String line)
            throws ParseException {

        if (line == null || line.trim().isEmpty()) {
            throw new ParseException(
                    "studentRegistration 행이 비어 있습니다."
            );
        }

        try (CSVParser parser =
                CSVFormat.DEFAULT.parse(
                        new StringReader(line))) {

            List<CSVRecord> records =
                    parser.getRecords();

            if (records.size() != 1) {
                throw new ParseException(
                        "studentRegistration Parsing 결과가 "
                        + "1건이 아닙니다."
                );
            }

            CSVRecord record = records.get(0);

            if (record.size() != EXPECTED_COLUMN_COUNT) {
                throw new ParseException(
                        "studentRegistration 컬럼 개수 오류. 실제: "
                        + record.size()
                        + ", 예상: "
                        + EXPECTED_COLUMN_COUNT
                );
            }

            return new StudentRegistrationRecord(
                    record.get(0).trim(),
                    record.get(1).trim(),

                    Long.parseLong(
                            record.get(2).trim()),

                    parseNullableInteger(
                            record.get(3)),

                    parseNullableInteger(
                            record.get(4))
            );

        } catch (NumberFormatException e) {

            throw new ParseException(
                    "studentRegistration 숫자 Parsing 실패",
                    e
            );

        } catch (IOException e) {

            throw new ParseException(
                    "studentRegistration CSV Parsing 실패",
                    e
            );
        }
    }

    /**
     * 등록일/철회일이 비어 있으면 null 유지.
     */
    private Integer parseNullableInteger(
            String value) {

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return Integer.valueOf(trimmed);
    }
}