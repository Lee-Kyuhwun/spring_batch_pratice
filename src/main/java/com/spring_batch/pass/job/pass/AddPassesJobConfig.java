package com.spring_batch.pass.job.pass;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
public class AddPassesJobConfig {

    private final AddPassesTasklet addPassesTasklet;
    private final JobRepository jobRepository;
    private final JpaTransactionManager transactionManager;

    @Autowired
    public AddPassesJobConfig(AddPassesTasklet addPassesTasklet, JpaTransactionManager transactionManager, JobRepository jobRepository) {
        this.addPassesTasklet = addPassesTasklet;
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
    }




    @Bean
    public Job addPassesJob() {
        return new JobBuilder("addPassesJob", jobRepository)
                .start(addPassesStep())
                .build();
    }

    @Bean
    public Step addPassesStep() {
        return new StepBuilder("addPassesStep", jobRepository)
                .tasklet(addPassesTasklet, transactionManager)
                .build();
    }

}
