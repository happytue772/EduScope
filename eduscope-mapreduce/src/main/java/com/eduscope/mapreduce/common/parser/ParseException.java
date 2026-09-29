package com.eduscope.mapreduce.common.parser;

/**
 * OULAD CSV Parsing 과정의 오류를 구분하기 위한 예외.
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = 1L;

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}