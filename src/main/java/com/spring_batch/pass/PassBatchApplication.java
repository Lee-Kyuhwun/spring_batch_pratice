package com.spring_batch.pass;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
@EnableBatchProcessing
public class PassBatchApplication {

/*
	private final EntityManagerFactory entityManagerFactory;




	@Bean
	public Job passJob(JobRepository jobRepository, @Qualifier("passStep") Step passStep) {
		log.info("PassBatchApplication : Creating passJob Bean");
		System.out.println("passjob complete");
		return new JobBuilder("passJob", jobRepository)
				.start(passStep)
				.build();
	}

	@Bean
	@JobScope // JobScope는 Job이 실행될 때마다 Bean이 생성됩니다.
	public Step passStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		log.info("Creating passStep Bean");
		System.out.println("passStep complete");
		return new StepBuilder("passStep", jobRepository)
//				.allowStartIfComplete(true)
				.tasklet((contribution, chunkContext) -> {
					log.info("Execute PassStep");
					return RepeatStatus.FINISHED;
				}, transactionManager)
				.build();
	}
*/

	public static void main(String[] args) {
		log.info("Starting PassBatchApplication");
		SpringApplication.run(PassBatchApplication.class, args);
	}
}
