package com.eduscope.web.studentanalysis.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduscope.web.studentanalysis.dto.StudentAnalysisResponse;
import com.eduscope.web.studentanalysis.dto.StudentSearchResponse;
import com.eduscope.web.studentanalysis.dto.StudentSnapshotPageResponse;
import com.eduscope.web.studentanalysis.repository.StudentSnapshotRepository;

/**
 * Snapshot Export 전용 학생 Bulk Service.
 *
 * 기존 학생 분석 API는 그대로 유지하고,
 * Snapshot 생성 시에만 최대 500명 단위로 묶어서 처리한다.
 */
@Service
@Transactional(readOnly = true)
public class StudentSnapshotService {

    private static final int DEFAULT_PAGE_SIZE = 500;
    private static final int MAX_PAGE_SIZE = 500;

    private final StudentSnapshotRepository repository;

    public StudentSnapshotService(
            StudentSnapshotRepository repository) {

        this.repository = repository;
    }

    public StudentSnapshotPageResponse getPage(
            Integer offset,
            Integer limit) {

        int safeOffset =
            offset == null
                ? 0
                : Math.max(0, offset);

        int safeLimit =
            limit == null
                ? DEFAULT_PAGE_SIZE
                : Math.min(
                    Math.max(1, limit),
                    MAX_PAGE_SIZE
                );

        List<Map<String, Object>> baseRows =
            repository.findPage(
                safeOffset,
                safeLimit
            );

        if (baseRows.isEmpty()) {
            return new StudentSnapshotPageResponse(
                safeOffset,
                safeLimit,
                0,
                safeOffset,
                false,
                List.of()
            );
        }

        List<Long> studentCourseIds =
            baseRows.stream()
                .map(
                    row -> longValue(
                        row.get(
                            "STUDENT_COURSE_ID"
                        )
                    )
                )
                .toList();

        Map<Long, List<StudentAnalysisResponse.AssessmentHistory>>
            assessmentsByStudent =
                new HashMap<>();

        for (
            Map<String, Object> row
                : repository.findAssessments(
                    studentCourseIds
                )
        ) {
            Long studentCourseId =
                longValue(
                    row.get(
                        "STUDENT_COURSE_ID"
                    )
                );

            assessmentsByStudent
                .computeIfAbsent(
                    studentCourseId,
                    ignored -> new ArrayList<>()
                )
                .add(
                    new StudentAnalysisResponse.AssessmentHistory(
                        longValue(
                            row.get(
                                "ASSESSMENT_ID"
                            )
                        ),
                        longValue(
                            row.get(
                                "SOURCE_ASSESSMENT_ID"
                            )
                        ),
                        stringValue(
                            row.get(
                                "ASSESSMENT_TYPE"
                            )
                        ),
                        integerValue(
                            row.get(
                                "ASSESSMENT_DUE_DAY"
                            )
                        ),
                        decimalValue(
                            row.get(
                                "ASSESSMENT_WEIGHT"
                            )
                        ),
                        integerValue(
                            row.get(
                                "SUBMITTED_DAY"
                            )
                        ),
                        stringValue(
                            row.get(
                                "IS_BANKED"
                            )
                        ),
                        decimalValue(
                            row.get(
                                "SCORE"
                            )
                        )
                    )
                );
        }

        List<StudentSnapshotPageResponse.Item> items =
            new ArrayList<>(
                baseRows.size()
            );

        for (Map<String, Object> row : baseRows) {

            Long studentCourseId =
                longValue(
                    row.get(
                        "STUDENT_COURSE_ID"
                    )
                );

            StudentSearchResponse search =
                new StudentSearchResponse(
                    studentCourseId,
                    longValue(
                        row.get(
                            "SOURCE_STUDENT_ID"
                        )
                    ),
                    longValue(
                        row.get(
                            "COURSE_PRESENTATION_ID"
                        )
                    ),
                    stringValue(
                        row.get(
                            "CODE_MODULE"
                        )
                    ),
                    stringValue(
                        row.get(
                            "CODE_PRESENTATION"
                        )
                    ),
                    stringValue(
                        row.get(
                            "FINAL_RESULT"
                        )
                    )
                );

            StudentAnalysisResponse.StudentCourseInfo student =
                new StudentAnalysisResponse.StudentCourseInfo(
                    studentCourseId,
                    longValue(
                        row.get(
                            "STUDENT_ID"
                        )
                    ),
                    longValue(
                        row.get(
                            "SOURCE_STUDENT_ID"
                        )
                    ),
                    longValue(
                        row.get(
                            "COURSE_PRESENTATION_ID"
                        )
                    ),
                    longValue(
                        row.get(
                            "DATASET_ID"
                        )
                    ),
                    stringValue(
                        row.get(
                            "CODE_MODULE"
                        )
                    ),
                    stringValue(
                        row.get(
                            "CODE_PRESENTATION"
                        )
                    ),
                    stringValue(
                        row.get(
                            "GENDER"
                        )
                    ),
                    stringValue(
                        row.get(
                            "REGION"
                        )
                    ),
                    stringValue(
                        row.get(
                            "HIGHEST_EDUCATION"
                        )
                    ),
                    stringValue(
                        row.get(
                            "IMD_BAND"
                        )
                    ),
                    stringValue(
                        row.get(
                            "AGE_BAND"
                        )
                    ),
                    integerValue(
                        row.get(
                            "NUM_OF_PREV_ATTEMPTS"
                        )
                    ),
                    integerValue(
                        row.get(
                            "STUDIED_CREDITS"
                        )
                    ),
                    stringValue(
                        row.get(
                            "DISABILITY"
                        )
                    ),
                    stringValue(
                        row.get(
                            "FINAL_RESULT"
                        )
                    )
                );

            Integer registrationDay =
                integerValue(
                    row.get(
                        "REGISTRATION_DAY"
                    )
                );

            Integer unregistrationDay =
                integerValue(
                    row.get(
                        "UNREGISTRATION_DAY"
                    )
                );

            StudentAnalysisResponse.RegistrationInfo registration =
                registrationDay == null
                && unregistrationDay == null
                    ? null
                    : new StudentAnalysisResponse.RegistrationInfo(
                        registrationDay,
                        unregistrationDay
                    );

            Long activityJobId =
                longValue(
                    row.get(
                        "ACTIVITY_JOB_ID"
                    )
                );

            StudentAnalysisResponse.ActivitySummary activity =
                row.get(
                    "ACT_TOTAL_CLICK_COUNT"
                ) == null
                    ? null
                    : new StudentAnalysisResponse.ActivitySummary(
                        longValue(
                            row.get(
                                "ACT_TOTAL_CLICK_COUNT"
                            )
                        ),
                        longValue(
                            row.get(
                                "ACT_ACTIVE_DAY_COUNT"
                            )
                        ),
                        longValue(
                            row.get(
                                "ACT_USED_MATERIAL_COUNT"
                            )
                        ),
                        decimalValue(
                            row.get(
                                "AVG_DAILY_CLICK_COUNT"
                            )
                        ),
                        integerValue(
                            row.get(
                                "ACT_FIRST_ACTIVITY_DAY"
                            )
                        ),
                        integerValue(
                            row.get(
                                "ACT_LAST_ACTIVITY_DAY"
                            )
                        )
                    );

            Long learningJobId =
                longValue(
                    row.get(
                        "LEARNING_JOB_ID"
                    )
                );

            StudentAnalysisResponse.LearningSummary learningSummary =
                row.get(
                    "LEARN_TOTAL_CLICK_COUNT"
                ) == null
                    ? null
                    : new StudentAnalysisResponse.LearningSummary(
                        longValue(
                            row.get(
                                "LEARN_TOTAL_CLICK_COUNT"
                            )
                        ),
                        longValue(
                            row.get(
                                "LEARN_ACTIVE_DAY_COUNT"
                            )
                        ),
                        longValue(
                            row.get(
                                "LEARN_USED_MATERIAL_COUNT"
                            )
                        ),
                        longValue(
                            row.get(
                                "SUBMITTED_ASSESSMENT_COUNT"
                            )
                        ),
                        decimalValue(
                            row.get(
                                "AVG_ASSESSMENT_SCORE"
                            )
                        ),
                        longValue(
                            row.get(
                                "FAILED_ASSESSMENT_COUNT"
                            )
                        ),
                        integerValue(
                            row.get(
                                "LEARN_FIRST_ACTIVITY_DAY"
                            )
                        ),
                        integerValue(
                            row.get(
                                "LEARN_LAST_ACTIVITY_DAY"
                            )
                        )
                    );

            StudentAnalysisResponse analysis =
                new StudentAnalysisResponse(
                    student,
                    registration,
                    activityJobId,
                    learningJobId,
                    activity,
                    learningSummary,
                    assessmentsByStudent.getOrDefault(
                        studentCourseId,
                        List.of()
                    )
                );

            items.add(
                new StudentSnapshotPageResponse.Item(
                    search,
                    analysis
                )
            );
        }

        int returnedCount = items.size();
        int nextOffset =
            safeOffset + returnedCount;

        return new StudentSnapshotPageResponse(
            safeOffset,
            safeLimit,
            returnedCount,
            nextOffset,
            returnedCount == safeLimit,
            items
        );
    }

    private static String stringValue(
            Object value) {

        return value == null
            ? null
            : String.valueOf(value);
    }

    private static Long longValue(
            Object value) {

        return value == null
            ? null
            : ((Number) value)
                .longValue();
    }

    private static Integer integerValue(
            Object value) {

        return value == null
            ? null
            : ((Number) value)
                .intValue();
    }

    private static BigDecimal decimalValue(
            Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal decimal) {
            return decimal;
        }

        return new BigDecimal(
            value.toString()
        );
    }
}
