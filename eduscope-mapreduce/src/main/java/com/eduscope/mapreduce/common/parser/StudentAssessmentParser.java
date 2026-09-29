package com.eduscope.mapreduce.common.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.eduscope.mapreduce.common.model.StudentAssessmentRecord;

/**
 * OULAD studentAssessment.csv 한 행을
 * StudentAssessmentRecord 객체로 변환한다.
 */
public class StudentAssessmentParser
        implements RowParser<StudentAssessmentRecord> {

    private static final int EXPECTED_COLUMN_COUNT = 5;

    @Override
    public StudentAssessmentRecord parse(String line)
            throws ParseException {

        if (line == null || line.trim().isEmpty()) {
            throw new ParseException(
                    "studentAssessment 행이 비어 있습니다."
            );
        }

        try (CSVParser parser =
                CSVFormat.DEFAULT.parse(
                        new StringReader(line))) {

            List<CSVRecord> records =
                    parser.getRecords();

            if (records.size() != 1) {
                throw new ParseException(
                        "studentAssessment Parsing 결과가 "
                        + "1건이 아닙니다."
                );
            }

            CSVRecord record = records.get(0);

            if (record.size() != EXPECTED_COLUMN_COUNT) {
                throw new ParseException(
                        "studentAssessment 컬럼 개수 오류. 실제: "
                        + record.size()
                        + ", 예상: "
                        + EXPECTED_COLUMN_COUNT
                );
            }

            return new StudentAssessmentRecord(
                    Long.parseLong(
                            record.get(0).trim()),

                    Long.parseLong(
                            record.get(1).trim()),

                    Integer.parseInt(
                            record.get(2).trim()),

                    Integer.parseInt(
                            record.get(3).trim()),

                    parseNullableDouble(
                            record.get(4))
            );

        } catch (NumberFormatException e) {

            throw new ParseException(
                    "studentAssessment 숫자 Parsing 실패",
                    e
            );

        } catch (IOException e) {

            throw new ParseException(
                    "studentAssessment CSV Parsing 실패",
                    e
            );
        }
    }

    /**
     * score가 비어 있으면 0점으로 만들지 않고 null 유지.
     */
    private Double parseNullableDouble(
            String value) {

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return Double.valueOf(trimmed);
    }
}