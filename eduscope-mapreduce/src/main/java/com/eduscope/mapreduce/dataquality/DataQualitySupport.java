package com.eduscope.mapreduce.dataquality;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

import com.eduscope.mapreduce.common.validator.HeaderValidator;

/**
 * EduScope DATA_QUALITY 공통 유틸.
 *
 * CSV 종류 판별, Header, 컬럼 Parsing, 필수값,
 * 타입/범위 검증, 자연키, FK 기준정보 로딩을 관리한다.
 */
public final class DataQualitySupport {

    public static final String RAW_BASE_CONFIG =
            "eduscope.dataquality.raw.base";

    private static final HeaderValidator HEADER_VALIDATOR =
            new HeaderValidator();

    private DataQualitySupport() {
    }

    public static String detectFileType(String path) {

        if (path.contains("/student-assessment/")) {
            return "STUDENT_ASSESSMENT";
        }
        if (path.contains("/student-registration/")) {
            return "STUDENT_REGISTRATION";
        }
        if (path.contains("/student-info/")) {
            return "STUDENT_INFO";
        }
        if (path.contains("/student-vle/")) {
            return "STUDENT_VLE";
        }
        if (path.contains("/assessments/")) {
            return "ASSESSMENTS";
        }
        if (path.contains("/courses/")) {
            return "COURSES";
        }
        if (path.contains("/vle/")) {
            return "VLE";
        }

        return "UNKNOWN";
    }

    /**
     * 실제 OULAD Header와 정확히 일치하는지 검사한다.
     */
    public static boolean isExpectedHeader(
            String fileType,
            String line) {

        switch (fileType) {

        case "COURSES":
            return HEADER_VALIDATOR
                    .isCoursesHeader(line);

        case "ASSESSMENTS":
            return HEADER_VALIDATOR
                    .isAssessmentsHeader(line);

        case "VLE":
            return HEADER_VALIDATOR
                    .isVleHeader(line);

        case "STUDENT_INFO":
            return HEADER_VALIDATOR
                    .isStudentInfoHeader(line);

        case "STUDENT_REGISTRATION":
            return HEADER_VALIDATOR
                    .isStudentRegistrationHeader(line);

        case "STUDENT_ASSESSMENT":
            return HEADER_VALIDATOR
                    .isStudentAssessmentHeader(line);

        case "STUDENT_VLE":
            return HEADER_VALIDATOR
                    .isStudentVleHeader(line);

        default:
            return false;
        }
    }

    /**
     * Commons CSV로 한 행을 안전하게 컬럼 List로 변환.
     */
    public static List<String> parseColumns(String line)
            throws Exception {

        try (CSVParser parser =
                CSVFormat.DEFAULT.parse(
                        new StringReader(line))) {

            List<CSVRecord> records =
                    parser.getRecords();

            if (records.size() != 1) {
                throw new IllegalArgumentException(
                        "CSV record count != 1");
            }

            CSVRecord record = records.get(0);

            List<String> columns =
                    new ArrayList<String>();

            for (String value : record) {
                columns.add(value);
            }

            return columns;
        }
    }

    public static int expectedColumnCount(
            String fileType) {

        switch (fileType) {

        case "COURSES":
            return 3;

        case "ASSESSMENTS":
            return 6;

        case "VLE":
            return 6;

        case "STUDENT_INFO":
            return 12;

        case "STUDENT_REGISTRATION":
            return 5;

        case "STUDENT_ASSESSMENT":
            return 5;

        case "STUDENT_VLE":
            return 6;

        default:
            return -1;
        }
    }

    /**
     * nullable 컬럼까지 무조건 오류로 판단하지 않는다.
     * 실제 관계/식별에 필요한 필수 컬럼만 검사한다.
     */
    public static boolean hasMissingRequiredValue(
            String fileType,
            List<String> c) {

        switch (fileType) {

        case "COURSES":
            return empty(c, 0)
                    || empty(c, 1)
                    || empty(c, 2);

        case "ASSESSMENTS":
            // date는 NULL 가능
            return empty(c, 0)
                    || empty(c, 1)
                    || empty(c, 2)
                    || empty(c, 3)
                    || empty(c, 5);

        case "VLE":
            // week_from, week_to는 NULL 가능
            return empty(c, 0)
                    || empty(c, 1)
                    || empty(c, 2)
                    || empty(c, 3);

        case "STUDENT_INFO":
            // imd_band는 NULL 가능
            return empty(c, 0)
                    || empty(c, 1)
                    || empty(c, 2)
                    || empty(c, 3)
                    || empty(c, 4)
                    || empty(c, 5)
                    || empty(c, 7)
                    || empty(c, 8)
                    || empty(c, 9)
                    || empty(c, 10)
                    || empty(c, 11);

        case "STUDENT_REGISTRATION":
            // registration/unregistration 날짜는 nullable 처리
            return empty(c, 0)
                    || empty(c, 1)
                    || empty(c, 2);

        case "STUDENT_ASSESSMENT":
            // score는 원본 NULL 가능성을 보존
            return empty(c, 0)
                    || empty(c, 1)
                    || empty(c, 2)
                    || empty(c, 3);

        case "STUDENT_VLE":
            return empty(c, 0)
                    || empty(c, 1)
                    || empty(c, 2)
                    || empty(c, 3)
                    || empty(c, 4)
                    || empty(c, 5);

        default:
            return false;
        }
    }

    /**
     * 숫자형 컬럼이 실제 숫자로 Parsing 가능한지 검사한다.
     *
     * 빈 값 자체는 MISSING_VALUE에서 별도 처리하므로
     * nullable 빈 문자열은 타입 오류로 잡지 않는다.
     */
    public static boolean hasInvalidType(
            String fileType,
            List<String> c) {

        try {

            switch (fileType) {

            case "COURSES":
                parseInteger(c, 2);
                break;

            case "ASSESSMENTS":
                parseLong(c, 2);
                parseNullableInteger(c, 4);
                parseDouble(c, 5);
                break;

            case "VLE":
                parseLong(c, 0);
                parseNullableInteger(c, 4);
                parseNullableInteger(c, 5);
                break;

            case "STUDENT_INFO":
                parseLong(c, 2);
                parseInteger(c, 8);
                parseInteger(c, 9);
                break;

            case "STUDENT_REGISTRATION":
                parseLong(c, 2);
                parseNullableInteger(c, 3);
                parseNullableInteger(c, 4);
                break;

            case "STUDENT_ASSESSMENT":
                parseLong(c, 0);
                parseLong(c, 1);
                parseInteger(c, 2);
                parseInteger(c, 3);
                parseNullableDouble(c, 4);
                break;

            case "STUDENT_VLE":
                parseLong(c, 2);
                parseLong(c, 3);
                parseInteger(c, 4);
                parseLong(c, 5);
                break;

            default:
                break;
            }

            return false;

        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * 실제 데이터 도메인 범위 검증.
     * 타입 오류는 hasInvalidType에서 먼저 처리한다.
     */
    public static boolean hasInvalidRange(
            String fileType,
            List<String> c) {

        try {

            switch (fileType) {

            case "COURSES":
                return Integer.parseInt(
                        c.get(2).trim()) < 0;

            case "ASSESSMENTS":

                double weight =
                        Double.parseDouble(
                                c.get(5).trim());

                return weight < 0.0
                        || weight > 100.0;

            case "VLE":

                if (!empty(c, 4)
                        && !empty(c, 5)) {

                    int from =
                            Integer.parseInt(
                                    c.get(4).trim());

                    int to =
                            Integer.parseInt(
                                    c.get(5).trim());

                    return from > to;
                }

                return false;

            case "STUDENT_INFO":

                int previousAttempts =
                        Integer.parseInt(
                                c.get(8).trim());

                int credits =
                        Integer.parseInt(
                                c.get(9).trim());

                String result =
                        c.get(11).trim();

                boolean validResult =
                        "Pass".equals(result)
                        || "Fail".equals(result)
                        || "Withdrawn".equals(result)
                        || "Distinction".equals(result);

                return previousAttempts < 0
                        || credits < 0
                        || !validResult;

            case "STUDENT_ASSESSMENT":

                int banked =
                        Integer.parseInt(
                                c.get(3).trim());

                if (banked != 0 && banked != 1) {
                    return true;
                }

                if (!empty(c, 4)) {

                    double score =
                            Double.parseDouble(
                                    c.get(4).trim());

                    return score < 0.0
                            || score > 100.0;
                }

                return false;

            case "STUDENT_VLE":

                long click =
                        Long.parseLong(
                                c.get(5).trim());

                return click < 0;

            /*
             * registration date에는 음수가 정상적으로 가능하고,
             * assessments date에도 상대일 개념이 있으므로
             * 임의 범위를 강제하지 않는다.
             */
            default:
                return false;
            }

        } catch (NumberFormatException e) {
            // 타입 오류는 PARSE_ERROR로 별도 집계한다.
            return false;
        }
    }

    /**
     * 각 원본 CSV의 자연키.
     */
    public static String buildPrimaryKey(
            String fileType,
            List<String> c) {

        switch (fileType) {

        case "COURSES":
            return c.get(0)
                    + "|" + c.get(1);

        case "ASSESSMENTS":
            return c.get(2);

        case "VLE":
            return c.get(1)
                    + "|" + c.get(2)
                    + "|" + c.get(0);

        case "STUDENT_INFO":
            return c.get(0)
                    + "|" + c.get(1)
                    + "|" + c.get(2);

        case "STUDENT_REGISTRATION":
            return c.get(0)
                    + "|" + c.get(1)
                    + "|" + c.get(2);

        case "STUDENT_ASSESSMENT":
            return c.get(0)
                    + "|" + c.get(1);

        case "STUDENT_VLE":
            /*
             * 학생 + 강의 + VLE + 활동일 단위
             * sum_click은 측정값이므로 PK에서 제외.
             */
            return c.get(0)
                    + "|" + c.get(1)
                    + "|" + c.get(2)
                    + "|" + c.get(3)
                    + "|" + c.get(4);

        default:
            return null;
        }
    }

    /**
     * FK 검사용 소규모 기준정보를 HDFS RAW에서 읽는다.
     *
     * courses / assessments / vle / studentInfo만 메모리에 올리며,
     * 대용량 studentVle 원본은 기준정보로 로딩하지 않는다.
     */
    public static ReferenceData loadReferenceData(
            Configuration configuration)
            throws Exception {

        String rawBase =
                configuration.get(
                        RAW_BASE_CONFIG,
                        ""
                ).trim();

        if (rawBase.isEmpty()) {
            throw new IllegalStateException(
                    "DATA_QUALITY raw base 설정이 없습니다."
            );
        }

        ReferenceData result =
                new ReferenceData();

        loadCourses(
                configuration,
                new Path(rawBase, "courses"),
                result
        );

        loadAssessments(
                configuration,
                new Path(rawBase, "assessments"),
                result
        );

        loadVle(
                configuration,
                new Path(rawBase, "vle"),
                result
        );

        loadStudentInfo(
                configuration,
                new Path(rawBase, "student-info"),
                result
        );

        return result;
    }

    /**
     * 현재 행이 부모 CSV를 실제로 참조하는지 검사한다.
     *
     * 반환값이 null이면 현재 확인 가능한 FK 기준에서는 문제가 없다.
     */
    public static String findForeignKeyError(
            String fileType,
            List<String> c,
            ReferenceData referenceData) {

        if (referenceData == null) {
            return "REFERENCE_DATA_NOT_LOADED";
        }

        switch (fileType) {

        case "COURSES":
            return null;

        case "ASSESSMENTS": {

            String courseKey =
                    courseKey(c.get(0), c.get(1));

            return referenceData.courseKeys
                    .contains(courseKey)
                    ? null
                    : "COURSE_NOT_FOUND:" + courseKey;
        }

        case "VLE": {

            String courseKey =
                    courseKey(c.get(1), c.get(2));

            return referenceData.courseKeys
                    .contains(courseKey)
                    ? null
                    : "COURSE_NOT_FOUND:" + courseKey;
        }

        case "STUDENT_INFO": {

            String courseKey =
                    courseKey(c.get(0), c.get(1));

            return referenceData.courseKeys
                    .contains(courseKey)
                    ? null
                    : "COURSE_NOT_FOUND:" + courseKey;
        }

        case "STUDENT_REGISTRATION": {

            String studentCourseKey =
                    studentCourseKey(
                            c.get(0),
                            c.get(1),
                            c.get(2)
                    );

            return referenceData.studentCourseKeys
                    .contains(studentCourseKey)
                    ? null
                    : "STUDENT_COURSE_NOT_FOUND:"
                    + studentCourseKey;
        }

        case "STUDENT_ASSESSMENT": {

            String assessmentId =
                    c.get(0).trim();

            String studentId =
                    c.get(1).trim();

            String assessmentCourse =
                    referenceData
                        .assessmentCourseById
                        .get(assessmentId);

            if (assessmentCourse == null) {
                return "ASSESSMENT_NOT_FOUND:"
                        + assessmentId;
            }

            String studentCourseKey =
                    assessmentCourse
                    + "|"
                    + studentId;

            return referenceData.studentCourseKeys
                    .contains(studentCourseKey)
                    ? null
                    : "STUDENT_COURSE_NOT_FOUND:"
                    + studentCourseKey;
        }

        case "STUDENT_VLE": {

            String studentCourseKey =
                    studentCourseKey(
                            c.get(0),
                            c.get(1),
                            c.get(2)
                    );

            if (!referenceData.studentCourseKeys
                    .contains(studentCourseKey)) {

                return "STUDENT_COURSE_NOT_FOUND:"
                        + studentCourseKey;
            }

            String vleKey =
                    vleKey(
                            c.get(0),
                            c.get(1),
                            c.get(3)
                    );

            return referenceData.vleKeys
                    .contains(vleKey)
                    ? null
                    : "VLE_NOT_FOUND:" + vleKey;
        }

        default:
            return null;
        }
    }

    public static String sha256(String text)
            throws Exception {

        MessageDigest md =
                MessageDigest.getInstance("SHA-256");

        byte[] digest =
                md.digest(
                    text.getBytes(StandardCharsets.UTF_8)
                );

        StringBuilder result =
                new StringBuilder();

        for (byte b : digest) {
            result.append(
                    String.format("%02x", b)
            );
        }

        return result.toString();
    }

    public static String sample(String line) {

        if (line == null) {
            return "";
        }

        // sample_message가 지나치게 커지지 않도록 제한
        return line.length() <= 200
                ? line
                : line.substring(0, 200);
    }

    private static void loadCourses(
            Configuration configuration,
            Path directory,
            ReferenceData result)
            throws Exception {

        readDataRows(
                configuration,
                directory,
                "COURSES",
                new RowConsumer() {

                    @Override
                    public void accept(List<String> c) {

                        result.courseKeys.add(
                                courseKey(
                                        c.get(0),
                                        c.get(1)
                                )
                        );
                    }
                }
        );
    }

    private static void loadAssessments(
            Configuration configuration,
            Path directory,
            ReferenceData result)
            throws Exception {

        readDataRows(
                configuration,
                directory,
                "ASSESSMENTS",
                new RowConsumer() {

                    @Override
                    public void accept(List<String> c) {

                        result.assessmentCourseById.put(
                                c.get(2).trim(),
                                courseKey(
                                        c.get(0),
                                        c.get(1)
                                )
                        );
                    }
                }
        );
    }

    private static void loadVle(
            Configuration configuration,
            Path directory,
            ReferenceData result)
            throws Exception {

        readDataRows(
                configuration,
                directory,
                "VLE",
                new RowConsumer() {

                    @Override
                    public void accept(List<String> c) {

                        result.vleKeys.add(
                                vleKey(
                                        c.get(1),
                                        c.get(2),
                                        c.get(0)
                                )
                        );
                    }
                }
        );
    }

    private static void loadStudentInfo(
            Configuration configuration,
            Path directory,
            ReferenceData result)
            throws Exception {

        readDataRows(
                configuration,
                directory,
                "STUDENT_INFO",
                new RowConsumer() {

                    @Override
                    public void accept(List<String> c) {

                        result.studentCourseKeys.add(
                                studentCourseKey(
                                        c.get(0),
                                        c.get(1),
                                        c.get(2)
                                )
                        );
                    }
                }
        );
    }

    /**
     * HDFS 디렉터리의 실제 데이터 행만 읽는다.
     * Header는 첫 행에서 제외하고, 형식이 깨진 행은 기준정보에 넣지 않는다.
     */
    private static void readDataRows(
            Configuration configuration,
            Path directory,
            String fileType,
            RowConsumer consumer)
            throws Exception {

        FileSystem fileSystem =
                directory.getFileSystem(configuration);

        if (!fileSystem.exists(directory)) {
            throw new IllegalStateException(
                    "FK 기준 HDFS 경로가 없습니다: "
                    + directory
            );
        }

        RemoteIterator<LocatedFileStatus> files =
                fileSystem.listFiles(
                        directory,
                        true
                );

        while (files.hasNext()) {

            LocatedFileStatus status =
                    files.next();

            String name =
                    status.getPath()
                        .getName();

            if (name.startsWith("_")
                    || name.startsWith(".")) {
                continue;
            }

            try (FSDataInputStream input =
                    fileSystem.open(
                            status.getPath());
                 BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(
                            input,
                            StandardCharsets.UTF_8
                        )
                    )) {

                String line;
                boolean firstLine = true;

                while ((line = reader.readLine()) != null) {

                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }

                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    try {

                        List<String> columns =
                                parseColumns(line);

                        if (columns.size()
                                != expectedColumnCount(
                                        fileType)) {
                            continue;
                        }

                        if (hasMissingRequiredValue(
                                fileType,
                                columns)) {
                            continue;
                        }

                        if (hasInvalidType(
                                fileType,
                                columns)) {
                            continue;
                        }

                        consumer.accept(columns);

                    } catch (Exception e) {
                        // 해당 행의 품질 오류는 Row Job에서 별도 집계한다.
                    }
                }
            }
        }
    }

    private static String courseKey(
            String codeModule,
            String codePresentation) {

        return codeModule.trim()
                + "|"
                + codePresentation.trim();
    }

    private static String studentCourseKey(
            String codeModule,
            String codePresentation,
            String studentId) {

        return courseKey(
                codeModule,
                codePresentation
        )
        + "|"
        + studentId.trim();
    }

    private static String vleKey(
            String codeModule,
            String codePresentation,
            String siteId) {

        return courseKey(
                codeModule,
                codePresentation
        )
        + "|"
        + siteId.trim();
    }

    private static void parseInteger(
            List<String> c,
            int index) {

        Integer.parseInt(
                c.get(index).trim());
    }

    private static void parseLong(
            List<String> c,
            int index) {

        Long.parseLong(
                c.get(index).trim());
    }

    private static void parseDouble(
            List<String> c,
            int index) {

        Double.parseDouble(
                c.get(index).trim());
    }

    private static void parseNullableInteger(
            List<String> c,
            int index) {

        if (!empty(c, index)) {
            parseInteger(c, index);
        }
    }

    private static void parseNullableDouble(
            List<String> c,
            int index) {

        if (!empty(c, index)) {
            parseDouble(c, index);
        }
    }

    private static boolean empty(
            List<String> c,
            int index) {

        return index >= c.size()
                || c.get(index) == null
                || c.get(index).trim().isEmpty();
    }

    private interface RowConsumer {
        void accept(List<String> columns);
    }

    /**
     * Mapper가 재사용하는 FK 기준정보.
     */
    public static final class ReferenceData {

        private final Set<String> courseKeys =
                new HashSet<String>();

        private final Map<String, String>
                assessmentCourseById =
                    new HashMap<String, String>();

        private final Set<String> vleKeys =
                new HashSet<String>();

        private final Set<String> studentCourseKeys =
                new HashSet<String>();

        private ReferenceData() {
        }
    }
}
