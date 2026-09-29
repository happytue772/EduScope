package com.eduscope.mapreduce.common.counter;

/**
 * Hadoop Job 처리건수와 품질 문제를 기록한다.
 */
public enum EduScopeCounter {

    INPUT_RECORD,

    VALID_RECORD,

    INVALID_RECORD,

    PARSE_ERROR,

    MISSING_VALUE,

    DUPLICATE,

    PK_DUPLICATE,

    FK_MISMATCH,

    INVALID_RANGE
}