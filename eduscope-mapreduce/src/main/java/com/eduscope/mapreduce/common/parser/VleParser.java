package com.eduscope.mapreduce.common.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.eduscope.mapreduce.common.model.VleRecord;

/**
 * OULAD vle.csv 한 행을 VleRecord로 변환한다.
 */
public class VleParser implements RowParser<VleRecord> {

    private static final int EXPECTED_COLUMN_COUNT = 6;

    @Override
    public VleRecord parse(String line)
            throws ParseException {

        if (line == null || line.trim().isEmpty()) {
            throw new ParseException(
                    "vle 행이 비어 있습니다."
            );
        }

        try (CSVParser parser =
                CSVFormat.DEFAULT.parse(
                        new StringReader(line))) {

            List<CSVRecord> records =
                    parser.getRecords();

            if (records.size() != 1) {
                throw new ParseException(
                        "vle Parsing 결과가 1건이 아닙니다."
                );
            }

            CSVRecord record = records.get(0);

            if (record.size() != EXPECTED_COLUMN_COUNT) {
                throw new ParseException(
                        "vle 컬럼 개수 오류. 실제: "
                        + record.size()
                        + ", 예상: "
                        + EXPECTED_COLUMN_COUNT
                );
            }

            return new VleRecord(
                    Long.parseLong(
                            record.get(0).trim()),
                    record.get(1).trim(),
                    record.get(2).trim(),
                    record.get(3).trim(),
                    parseNullableInteger(
                            record.get(4)),
                    parseNullableInteger(
                            record.get(5))
            );

        } catch (NumberFormatException e) {

            throw new ParseException(
                    "vle 숫자 컬럼 Parsing 실패",
                    e
            );

        } catch (IOException e) {

            throw new ParseException(
                    "vle CSV Parsing 실패",
                    e
            );
        }
    }

    /**
     * 빈 값을 임의의 0으로 바꾸지 않고 null로 유지한다.
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