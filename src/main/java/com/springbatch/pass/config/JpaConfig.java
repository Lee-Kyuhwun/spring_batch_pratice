package com.springbatch.pass.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // jpa auditing이란 엔티티 생성, 수정 시간을 자동으로 반영하는 기능
@Configuration
public class JpaConfig {

    


}