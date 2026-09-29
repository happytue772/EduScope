package com.eduscope.web.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * EduScope Swagger / OpenAPI 기본 문서 정보.
 *
 * Controller의 실제 API 경로와 DTO 구조는
 * SpringDoc이 자동으로 읽어 문서화한다.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI eduScopeOpenAPI() {

        return new OpenAPI()
            .info(
                new Info()
                    .title("EduScope API")
                    .version("1.0.0")
                    .description(
                        "OULAD 기반 교육 데이터 분석 플랫폼의 REST API 문서"
                    )
                    .contact(
                        new Contact()
                            .name("EduScope")
                    )
            );
    }
}
