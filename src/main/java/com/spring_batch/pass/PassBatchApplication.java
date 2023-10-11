package com.spring_batch.pass;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
@SpringBootApplication
@Slf4j
public class PassBatchApplication {

	private final EntityManagerFactory entityManagerFactory;

	@Autowired
	public PassBatchApplication(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
		log.info("Initialized EntityManagerFactory");
	}

	@Bean
	public JpaTransactionManager transactionManager() {
		log.info("PassBatchApplication : Creating JpaTransactionManager Bean");
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean
	public Job passJob(JobRepository jobRepository, @Qualifier("passStep") Step passStep) {
		log.info("PassBatchApplication : Creating passJob Bean");
		return new JobBuilder("passJob", jobRepository)
				.start(passStep)
				.build();
	}

	@Bean
	public Step passStep(JobRepository jobRepository) {
		log.info("Creating passStep Bean");
		return new StepBuilder("passStep", jobRepository)
				.allowStartIfComplete(true)
				.tasklet((contribution, chunkContext) -> {
					log.info("Execute PassStep");
					return RepeatStatus.FINISHED;
				}, transactionManager())
				.build();
	}

	public static void main(String[] args) {
		log.info("Starting PassBatchApplication");
		SpringApplication.run(PassBatchApplication.class, args);
	}
}
