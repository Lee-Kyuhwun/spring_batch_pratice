package com.spring_batch.config;


import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration //설정파일임을 명시한다.
@EnableJpaAuditing //JPA Auditing을 활성화한다.
// Auditing이란 생성자, 수정자, 생성일, 수정일 같은 정보를 자동으로 관리해주는 기능이다.
@EnableAutoConfiguration //Spring Boot에게 설정을 위임한다.
@EnableBatchProcessing // 배치 기능 활성화
@EntityScan("com.springbatch.pass.repository") // 엔티티 클래스가 있는 패키지를 지정한다.
@EnableJpaRepositories("com.spring_batch.pass.repository") // JpaRepository 인터페이스가 있는 패키지를 지정한다.
@EnableTransactionManagement // 트랜잭션 관리를 활성화한다.
public class TestBatchConfig {
}