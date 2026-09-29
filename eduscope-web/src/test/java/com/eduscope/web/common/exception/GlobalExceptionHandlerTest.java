package com.eduscope.web.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/** DB 연결 없이 공통 예외 처리의 상태 코드와 응답 구조를 검증한다. */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void illegalArgumentReturns400() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/analysis-jobs");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgumentException(
            new IllegalArgumentException("요청값 오류"), request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.timestamp());
        assertEquals(400, body.status());
        assertEquals("INVALID_ARGUMENT", body.code());
        assertEquals("요청값 오류", body.message());
        assertEquals("/api/analysis-jobs", body.path());
        assertEquals(0, body.fieldErrors().size());
    }

    @Test
    void illegalStateReturns409() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/analysis-jobs");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalStateException(
            new IllegalStateException("상태 전환 불가"), request
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(409, body.status());
        assertEquals("INVALID_STATE", body.code());
        assertEquals("상태 전환 불가", body.message());
        assertEquals("/api/analysis-jobs", body.path());
    }

    @Test
    void validationReturns400AndFirstFieldError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/analysis-jobs");
        // 실제 저장 데이터가 아닌 요청 검증 실패 상황만 구성한다.
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "analysisType", "필수값입니다"));
        binding.addError(new FieldError("request", "analysisType", "형식을 확인하세요"));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(binding);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("VALIDATION_FAILED", body.code());
        assertEquals("/api/analysis-jobs", body.path());
        assertEquals(1, body.fieldErrors().size());
        assertEquals("필수값입니다", body.fieldErrors().get("analysisType"));
    }
}
