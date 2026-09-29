package com.eduscope.web.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

/** Swagger 문서의 프로젝트 메타정보를 DB 연결 없이 검증한다. */
class OpenApiConfigTest {

    @Test
    void eduScopeApiMetadataShouldBeConfigured() {

        OpenAPI openAPI =
            new OpenApiConfig()
                .eduScopeOpenAPI();

        assertNotNull(openAPI.getInfo());
        assertEquals("EduScope API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }
}
