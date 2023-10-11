/*
package com.spring_batch.pass.job.pass;


import com.spring_batch.pass.repository.booking.BookingEntity;
import com.spring_batch.pass.repository.booking.BookingRepository;
import com.spring_batch.pass.repository.booking.BookingStatus;
import com.spring_batch.pass.repository.pass.PassEntity;
import com.spring_batch.pass.repository.pass.PassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Future;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UsePassesJobConfig {
    private final int CHUNK_SIZE = 10;
    private final PassRepository passRepository;
    private final BookingRepository bookingRepository;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job usePassesJob(JobRepository jobRepository, @Qualifier("usePassesStep") Step usePassesStep) throws Exception {
        log.info("Creating usePassesJob bean");
        return new JobBuilder("usePassesJob", jobRepository)
                .start(usePassesStep)
                .build();
    }


    @JobScope
    @Bean
    public Step usePassesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        log.info("Creating usePassesStep bean");
        return new StepBuilder("usePassesStep", jobRepository)
                .<BookingEntity, Future<BookingEntity>>chunk(CHUNK_SIZE, transactionManager)
                .reader(usePassesItemReader())
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<BookingEntity> usePassesItemReader() {
        log.info("Creating usePassesItemReader bean");
        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("usePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select b from BookingEntity b join fetch b.passEntity where b.status = :status and b.usedPass = false and b.endedAt < :endedAt")
                .parameterValues(Map.of("status", BookingStatus.COMPLETED, "endedAt", LocalDateTime.now()))
                .build();
    }

    @Bean
    public AsyncItemProcessor<BookingEntity, BookingEntity> asyncItemProcessor() throws Exception {
        log.info("Creating asyncItemProcessor bean");
        AsyncItemProcessor<BookingEntity, BookingEntity> processor = new AsyncItemProcessor<>();
        processor.setDelegate(usePassesItemProcessor());
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        processor.afterPropertiesSet();
        return processor;
    }

    @Bean
    public ItemProcessor<BookingEntity, BookingEntity> usePassesItemProcessor() {
        log.info("Creating usePassesItemProcessor bean");
        return bookingEntity -> {
            log.debug("Processing bookingEntity: {}", bookingEntity);
            PassEntity passEntity = bookingEntity.getPassEntity();
            passEntity.setRemainingCount(passEntity.getRemainingCount() - 1);
            bookingEntity.setPassEntity(passEntity);
            bookingEntity.setUsedPass(true);
            return bookingEntity;
        };
    }

    @Bean
    public AsyncItemWriter<BookingEntity> asyncItemWriter() {
        log.info("Creating asyncItemWriter bean");
        AsyncItemWriter<BookingEntity> writer = new AsyncItemWriter<>();
        writer.setDelegate(usePassesItemWriter());
        return writer;
    }

    @Bean
    public ItemWriter<BookingEntity> usePassesItemWriter() {
        log.info("Creating usePassesItemWriter bean");
        return bookingEntities -> {
            for(BookingEntity bookingEntity : bookingEntities) {
                log.debug("Writing bookingEntity: {}", bookingEntity);
                int updateCount = passRepository.updateRemainingCount(bookingEntity.getPassEntity().getPassSeq(), bookingEntity.getPassEntity().getRemainingCount());
                if(updateCount > 0) {
                    bookingRepository.updateUsedPass(bookingEntity.getBookingSeq(), true);
                }
            }
        };
    }
}*/
