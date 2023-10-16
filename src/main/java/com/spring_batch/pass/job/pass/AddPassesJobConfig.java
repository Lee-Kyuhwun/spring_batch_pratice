package com.spring_batch.pass.job.pass;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.security.cert.PolicyQualifierInfo;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AddPassesJobConfig {


    private final   AddPassesTasklet addPassesTasklet;


    @Bean
    public Job addPassesJob(JobRepository jobRepository, @Qualifier("addPassesStep") Step addPassesStep) {
        log.info("Creating addPassesJob bean");
        return new JobBuilder("addPassesJob", jobRepository)
                .start(addPassesStep)
                .build();
    }

    @Bean
    @JobScope
    public Step addPassesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("Creating addPassesStep bean");
        return new StepBuilder("addPassesStep", jobRepository)
                .tasklet(addPassesTasklet, transactionManager)
                .build();
    }

}
