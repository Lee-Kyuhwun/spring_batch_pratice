package com.spring_batch.pass;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobExecutor {

    private final JobLauncher jobLauncher;
    private final ApplicationContext context;

    @PostConstruct
    public void launchExpirePassesJob() throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        Job job = (Job) context.getBean("expirePassesJob");
        jobLauncher.run(job, parameters);
    }
}

