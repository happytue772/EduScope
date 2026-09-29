package com.eduscope.loader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EduScope Hadoop 분석 결과를
 * Oracle DB에 적재하는 Loader 애플리케이션.
 */
@SpringBootApplication
public class EduScopeLoaderApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                EduScopeLoaderApplication.class,
                args
        );
    }
}