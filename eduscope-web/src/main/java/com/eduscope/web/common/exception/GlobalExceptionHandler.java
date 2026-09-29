package com.eduscope.web.common.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.validation.FieldError;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;


/**
 * EduScope REST API 공통 예외 처리.
 *
 * 역할:
 *
 * 1. 잘못된 요청 -> 400
 * 2. 현재 상태와 충돌 -> 409
 * 3. DB 제약조건 충돌 -> 409
 * 4. 업로드 용량 초과 -> 413
 * 5. 지원하지 않는 Method -> 405
 * 6. 지원하지 않는 Content-Type -> 415
 * 7. 예상하지 못한 서버 오류 -> 500
 *
 * 주의:
 *
 * Spring Security의 인증/인가 오류(401/403)는
 * AuthenticationEntryPoint /
 * AccessDeniedHandler에서 처리한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
        LoggerFactory.getLogger(
            GlobalExceptionHandler.class
        );


    /**
     * @Valid 검증 실패.
     *
     * 예:
     * @NotBlank
     * @NotNull
     * @Size
     */
    @ExceptionHandler(
        MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleValidationException(

                MethodArgumentNotValidException exception,

                HttpServletRequest request
            ) {

        Map<String, String> fieldErrors =
            new LinkedHashMap<>();


        /*
         * 같은 필드 오류가 여러 개 발생하더라도
         * 첫 번째 메시지만 사용자에게 전달한다.
         */
        for (
            FieldError fieldError
            :
            exception
                .getBindingResult()
                .getFieldErrors()
        ) {

            fieldErrors.putIfAbsent(

                fieldError.getField(),

                fieldError.getDefaultMessage()
            );
        }


        return createResponse(

            HttpStatus.BAD_REQUEST,

            "VALIDATION_FAILED",

            "입력값을 확인해주세요.",

            request,

            fieldErrors
        );
    }


    /**
     * 요청 Parameter 검증 실패.
     *
     * Controller Method Parameter 등에
     * Bean Validation이 적용된 경우 처리한다.
     */
    @ExceptionHandler(
        ConstraintViolationException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleConstraintViolationException(

                ConstraintViolationException exception,

                HttpServletRequest request
            ) {

        Map<String, String> fieldErrors =
            new LinkedHashMap<>();


        exception
            .getConstraintViolations()
            .forEach(

                violation -> {

                    String field =
                        violation
                            .getPropertyPath()
                            .toString();


                    fieldErrors.putIfAbsent(

                        field,

                        violation.getMessage()
                    );
                }
            );


        return createResponse(

            HttpStatus.BAD_REQUEST,

            "VALIDATION_FAILED",

            "요청값을 확인해주세요.",

            request,

            fieldErrors
        );
    }


    /**
     * Service에서 명시적으로 발생시키는
     * 잘못된 입력값 예외.
     *
     * EduScope 기존 Service에서 사용하는
     * IllegalArgumentException을 처리한다.
     */
    @ExceptionHandler(
        IllegalArgumentException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleIllegalArgumentException(

                IllegalArgumentException exception,

                HttpServletRequest request
            ) {

        return createResponse(

            HttpStatus.BAD_REQUEST,

            "INVALID_ARGUMENT",

            exception.getMessage(),

            request,

            Map.of()
        );
    }


    /**
     * 현재 상태에서 수행할 수 없는 요청.
     *
     * 예:
     * 이미 처리된 Job
     * 허용되지 않는 상태 전환
     */
    @ExceptionHandler(
        IllegalStateException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleIllegalStateException(

                IllegalStateException exception,

                HttpServletRequest request
            ) {

        return createResponse(

            HttpStatus.CONFLICT,

            "INVALID_STATE",

            exception.getMessage(),

            request,

            Map.of()
        );
    }


    /**
     * JSON Body가 깨져 있거나
     * 요청 DTO 형식과 맞지 않는 경우.
     */
    @ExceptionHandler(
        HttpMessageNotReadableException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleHttpMessageNotReadableException(

                HttpMessageNotReadableException exception,

                HttpServletRequest request
            ) {

        return createResponse(

            HttpStatus.BAD_REQUEST,

            "INVALID_REQUEST_BODY",

            "요청 본문 형식을 확인해주세요.",

            request,

            Map.of()
        );
    }


    /**
     * 필수 Query Parameter 누락.
     */
    @ExceptionHandler(
        MissingServletRequestParameterException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleMissingParameterException(

                MissingServletRequestParameterException exception,

                HttpServletRequest request
            ) {

        String message =
            "필수 요청값이 없습니다: "
            + exception.getParameterName();


        return createResponse(

            HttpStatus.BAD_REQUEST,

            "MISSING_PARAMETER",

            message,

            request,

            Map.of()
        );
    }


    /**
     * Parameter 자료형 변환 실패.
     *
     * 예:
     * datasetId=abc
     */
    @ExceptionHandler(
        MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleTypeMismatchException(

                MethodArgumentTypeMismatchException exception,

                HttpServletRequest request
            ) {

        String message =
            "요청값 형식을 확인해주세요: "
            + exception.getName();


        return createResponse(

            HttpStatus.BAD_REQUEST,

            "INVALID_PARAMETER_TYPE",

            message,

            request,

            Map.of()
        );
    }


    /**
     * Oracle PK / FK / UNIQUE 등
     * DB 제약조건 충돌.
     *
     * 내부 DB 구조나 Constraint 이름은
     * 사용자 응답에 그대로 노출하지 않는다.
     */
    @ExceptionHandler(
        DataIntegrityViolationException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleDataIntegrityViolationException(

                DataIntegrityViolationException exception,

                HttpServletRequest request
            ) {

        log.warn(
            "DB 제약조건 충돌: path={}",
            request.getRequestURI()
        );


        return createResponse(

            HttpStatus.CONFLICT,

            "DATA_CONFLICT",

            "데이터 상태가 요청과 충돌합니다.",

            request,

            Map.of()
        );
    }


    /**
     * Dataset 등의 파일 업로드에서
     * 설정된 최대 용량을 초과한 경우.
     */
    @ExceptionHandler(
        MaxUploadSizeExceededException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleMaxUploadSizeExceededException(

                MaxUploadSizeExceededException exception,

                HttpServletRequest request
            ) {

        return createResponse(

            HttpStatus.PAYLOAD_TOO_LARGE,

            "UPLOAD_TOO_LARGE",

            "업로드 가능한 파일 크기를 초과했습니다.",

            request,

            Map.of()
        );
    }


    /**
     * 지원하지 않는 HTTP Method.
     *
     * 예:
     * GET 전용 API에 POST 요청
     */
    @ExceptionHandler(
        HttpRequestMethodNotSupportedException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleMethodNotSupportedException(

                HttpRequestMethodNotSupportedException exception,

                HttpServletRequest request
            ) {

        return createResponse(

            HttpStatus.METHOD_NOT_ALLOWED,

            "METHOD_NOT_ALLOWED",

            "지원하지 않는 요청 방식입니다.",

            request,

            Map.of()
        );
    }


    /**
     * 지원하지 않는 Content-Type.
     */
    @ExceptionHandler(
        HttpMediaTypeNotSupportedException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleMediaTypeNotSupportedException(

                HttpMediaTypeNotSupportedException exception,

                HttpServletRequest request
            ) {

        return createResponse(

            HttpStatus.UNSUPPORTED_MEDIA_TYPE,

            "UNSUPPORTED_MEDIA_TYPE",

            "지원하지 않는 요청 Content-Type입니다.",

            request,

            Map.of()
        );
    }


    /**
     * 위에서 예상하지 못한 모든 서버 오류.
     *
     * 내부 Exception Message나 Stack Trace를
     * 사용자에게 그대로 노출하지 않는다.
     *
     * 실제 상세 원인은 Server Log에만 남긴다.
     */
    @ExceptionHandler(
        Exception.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleUnexpectedException(

                Exception exception,

                HttpServletRequest request
            ) {

        log.error(

            "예상하지 못한 서버 오류: method={}, path={}",

            request.getMethod(),

            request.getRequestURI(),

            exception
        );


        return createResponse(

            HttpStatus.INTERNAL_SERVER_ERROR,

            "INTERNAL_SERVER_ERROR",

            "서버 내부 오류가 발생했습니다.",

            request,

            Map.of()
        );
    }


    /**
     * 공통 오류 응답 생성.
     */
    private ResponseEntity<ApiErrorResponse>
            createResponse(

                HttpStatus status,

                String code,

                String message,

                HttpServletRequest request,

                Map<String, String> fieldErrors
            ) {

        ApiErrorResponse response =
            new ApiErrorResponse(

                LocalDateTime.now(),

                status.value(),

                status.getReasonPhrase(),

                code,

                message,

                request.getRequestURI(),

                fieldErrors
            );


        return ResponseEntity
            .status(status)
            .body(response);
    }
}