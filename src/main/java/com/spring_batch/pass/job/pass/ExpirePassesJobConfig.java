package com.spring_batch.pass.job.pass;


import com.spring_batch.pass.repository.pass.PassEntity;
import com.spring_batch.pass.repository.pass.PassStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExpirePassesJobConfig {

    private final int CHUNK_SIZE = 5;

    private final EntityManagerFactory entityManagerFactory;


    @Bean
    public Job expirePassesJob(@Qualifier("expirePassesStep") Step expirePassesStep, JobRepository jobRepository) {
        log.info("Creating expirePassesJob bean");
        System.out.println("expirePassesJob complete");
        return new JobBuilder("expirePassesJob", jobRepository)
                .start(expirePassesStep)
                .build();
    }
    @JobScope//JobScope는 Job이 실행될 때마다 Bean이 생성됩니다.
    @Bean
    public Step expirePassesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("Creating expirePassesStep bean");
        System.out.println("expirePassesStep complete");
        return new StepBuilder("expirePassesStep", jobRepository)
                .<PassEntity, PassEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(expirePassesItemReader())
                .processor(expirePassesItemProcessor())
                .writer(expirePassesItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<PassEntity> expirePassesItemReader() {
        log.info("Creating expirePassesItemReader bean");
        System.out.println("expirePassesItemReader complete");
        return new JpaCursorItemReaderBuilder<PassEntity>()
                .name("expirePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT p FROM PassEntity p WHERE p.status = :status AND p.endedAt < :endedAt")
                .parameterValues(Map.of("status", PassStatus.PROGRESSED, "endedAt", LocalDateTime.now()))
                .build();
    }

    @Bean
    public ItemProcessor<PassEntity, PassEntity> expirePassesItemProcessor() {
        log.info("Creating expirePassesItemProcessor bean");
        System.out.println("expirePassesItemProcessor complete");
        return passEntity -> {
            log.debug("Processing passEntity for expiration: {}", passEntity);
            passEntity.setStatus(PassStatus.EXPIRED);
            return passEntity;
        };
    }

    @Bean
    public JpaItemWriter<PassEntity> expirePassesItemWriter() {
        log.info("Creating expirePassesItemWriter bean");
        System.out.println("expirePassesItemWriter complete");
        return new JpaItemWriterBuilder<PassEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
