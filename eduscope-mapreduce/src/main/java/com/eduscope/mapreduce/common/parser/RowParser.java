package com.eduscope.mapreduce.common.parser;

/**
 * CSV 한 행을 Java 객체로 변환하는 공통 규칙.
 */
public interface RowParser<T> {

    T parse(String line)
            throws ParseException;
}