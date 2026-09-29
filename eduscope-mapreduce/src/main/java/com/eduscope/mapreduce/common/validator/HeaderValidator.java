
package com.eduscope.mapreduce.common.validator;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * OULAD 7개 CSV Header 공통 검증.
 */
public class HeaderValidator {

    public boolean isCoursesHeader(String line) {
        return matchesHeader(
                line,
                "code_module",
                "code_presentation",
                "module_presentation_length"
        );
    }

    public boolean isAssessmentsHeader(String line) {
        return matchesHeader(
                line,
                "code_module",
                "code_presentation",
                "id_assessment",
                "assessment_type",
                "date",
                "weight"
        );
    }

    public boolean isVleHeader(String line) {
        return matchesHeader(
                line,
                "id_site",
                "code_module",
                "code_presentation",
                "activity_type",
                "week_from",
                "week_to"
        );
    }

    public boolean isStudentInfoHeader(String line) {
        return matchesHeader(
                line,
                "code_module",
                "code_presentation",
                "id_student",
                "gender",
                "region",
                "highest_education",
                "imd_band",
                "age_band",
                "num_of_prev_attempts",
                "studied_credits",
                "disability",
                "final_result"
        );
    }

    public boolean isStudentRegistrationHeader(String line) {
        return matchesHeader(
                line,
                "code_module",
                "code_presentation",
                "id_student",
                "date_registration",
                "date_unregistration"
        );
    }

    public boolean isStudentAssessmentHeader(String line) {
        return matchesHeader(
                line,
                "id_assessment",
                "id_student",
                "date_submitted",
                "is_banked",
                "score"
        );
    }

    public boolean isStudentVleHeader(String line) {
        return matchesHeader(
                line,
                "code_module",
                "code_presentation",
                "id_student",
                "id_site",
                "date",
                "sum_click"
        );
    }

    /**
     * 실제 CSV 형식으로 Header를 Parsing한 뒤 컬럼별 비교.
     */
    private boolean matchesHeader(
            String line,
            String... expectedColumns) {

        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        String normalized = removeBom(line);

        try (CSVParser parser =
                CSVFormat.DEFAULT.parse(
                        new StringReader(normalized))) {

            List<CSVRecord> records =
                    parser.getRecords();

            if (records.size() != 1) {
                return false;
            }

            CSVRecord record = records.get(0);

            if (record.size() != expectedColumns.length) {
                return false;
            }

            for (int i = 0; i < expectedColumns.length; i++) {

                if (!expectedColumns[i].equals(
                        record.get(i).trim())) {

                    return false;
                }
            }

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    // UTF-8 BOM 대응
    private String removeBom(String value) {

        if (!value.isEmpty()
                && value.charAt(0) == '\uFEFF') {

            return value.substring(1);
        }

        return value;
    }
}