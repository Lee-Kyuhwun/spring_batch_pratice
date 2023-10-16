package com.spring_batch.pass.job.notification;

import com.spring_batch.pass.repository.booking.BookingEntity;
import com.spring_batch.pass.repository.booking.BookingStatus;
import com.spring_batch.pass.repository.notification.NotificationEntity;
import com.spring_batch.pass.repository.notification.NotificationEvent;
import com.spring_batch.pass.repository.notification.NotificationModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SendNotificationBeforeClassJobConfig  {
    private final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final SendNotificationItemWriter sendNotificationItemWriter;
    private final PlatformTransactionManager transactionManager;



    @Bean
    public Job sendNotificationBeforeClassJob() {
        log.info("Initializing sendNotificationBeforeClassJob...");
        return new JobBuilder("sendNotificationBeforeClassJob", jobRepository)
                .start(addNotificationStep())
                .next(sendNotificationStep())
                .build();
    }

    @Bean
    public Step addNotificationStep() {
        log.info("Initializing addNotificationStep...");
        return new StepBuilder("addNotificationStep", jobRepository)
                .<BookingEntity, NotificationEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(addNotificationItemReader())
                .processor(addNotificationItemProcessor())
                .writer(addNotificationItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<BookingEntity> addNotificationItemReader() {
        log.info("Setting up addNotificationItemReader...");
        return new JpaPagingItemReaderBuilder<BookingEntity>()
                .name("addNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("select b from BookingEntity b join fetch b.userEntity where b.status = :status and b.startedAt <= :startedAt order by b.bookingSeq")
                .parameterValues(Map.of("status", BookingStatus.READY, "startedAt", LocalDateTime.now().plusMinutes(10)))
                .build();
    }

    @Bean
    public ItemProcessor<BookingEntity, NotificationEntity> addNotificationItemProcessor() {
        log.info("Setting up addNotificationItemProcessor...");
        return bookingEntity -> {
            log.info("Processing bookingEntity with");  // Assuming there's a getId() method in BookingEntity
            return NotificationModelMapper.INSTANCE.toNotificationEntity(bookingEntity, NotificationEvent.BEFORE_CLASS);
        };
    }

    @Bean
    public JpaItemWriter<NotificationEntity> addNotificationItemWriter() {
        log.info("Setting up addNotificationItemWriter...");
        return new JpaItemWriterBuilder<NotificationEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step sendNotificationStep() {
        log.info("Initializing sendNotificationStep...");
        return new StepBuilder("sendNotificationStep", jobRepository)
                .<NotificationEntity, NotificationEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(sendNotificationItemReader())
                .writer(sendNotificationItemWriter)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<NotificationEntity> sendNotificationItemReader() {
        log.info("Setting up sendNotificationItemReader...");
        JpaCursorItemReader<NotificationEntity> itemReader = new JpaCursorItemReaderBuilder<NotificationEntity>()
                .name("sendNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select n from NotificationEntity n where n.event = :event and n.sent = :sent")
                .parameterValues(Map.of("event", NotificationEvent.BEFORE_CLASS, "sent", false))
                .build();

        return new SynchronizedItemStreamReaderBuilder<NotificationEntity>()
                .delegate(itemReader)
                .build();
    }
}
