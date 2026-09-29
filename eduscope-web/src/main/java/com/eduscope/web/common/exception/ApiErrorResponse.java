package com.eduscope.web.common.exception;

import java.time.LocalDateTime;
import java.util.Map;


/**
 * EduScope REST API 공통 오류 응답 DTO.
 *
 * 모든 Controller / Service 예외를
 * 동일한 JSON 구조로 반환하기 위해 사용한다.
 */
public record ApiErrorResponse(

        LocalDateTime timestamp,

        int status,

        String error,

        String code,

        String message,

        String path,

        Map<String, String> fieldErrors

) {
}