/*
package com.spring_batch.pass.job.statistics;

import com.spring_batch.pass.repository.booking.BookingEntity;
import com.spring_batch.pass.repository.statistics.StatisticsEntity;
import com.spring_batch.pass.repository.statistics.StatisticsRepository;
import com.spring_batch.pass.util.LocalDateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MakeStatisticsJobConfig {
    private final int CHUNK_SIZE = 10;

    private final EntityManagerFactory entityManagerFactory;
    private final StatisticsRepository statisticsRepository;
    private final MakeDailyStatisticsTasklet makeDailyStatisticsTasklet;
    private final MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet;
    private final JobRepository jobRepository;

    // `PlatformTransactionManager`를 주입받기 위한 코드 추가
    private final PlatformTransactionManager transactionManager;


    @Bean
    public Job makeStatisticsJob() {
        log.info("Initializing makeStatisticsJob...");

        Flow addStatisticsFlow = new FlowBuilder<Flow>("addStatisticsFlow")
                .start(addStatisticsStep())
                .build();

        Flow makeDailyStatisticsFlow = new FlowBuilder<Flow>("makeDailyStatisticsFlow")
                .start(makeDailyStatisticsStep())
                .build();

        Flow makeWeeklyStatisticsFlow = new FlowBuilder<Flow>("makeWeeklyStatisticsFlow")
                .start(makeWeeklyStatisticsStep())
                .build();

        Flow parallelMakeStatisticsFlow = new FlowBuilder<Flow>("parallelMakeStatisticsFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(makeDailyStatisticsFlow, makeWeeklyStatisticsFlow)
                .build();

        return new JobBuilder("makeStatisticsJob",jobRepository)
                .start(addStatisticsFlow)
                .next(parallelMakeStatisticsFlow)
                .build()
                .build();
    }

    @Bean
    public Step addStatisticsStep() {
        log.info("Initializing addStatisticsStep...");
        return new StepBuilder("addStatisticsStep",jobRepository)
                .<BookingEntity, BookingEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(addStatisticsItemReader(null, null))
                .writer(addStatisticsItemWriter())
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<BookingEntity> addStatisticsItemReader(@Value("#{jobParameters[from]}") String fromString, @Value("#{jobParameters[to]}") String toString) {
        log.info("Setting up addStatisticsItemReader...");
        final LocalDateTime from = LocalDateTimeUtils.parse(fromString);
        final LocalDateTime to = LocalDateTimeUtils.parse(toString);

        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("usePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select b from BookingEntity b where b.endedAt between :from and :to")
                .parameterValues(Map.of("from", from, "to", to))
                .build();
    }

    @Bean
    public ItemWriter<BookingEntity> addStatisticsItemWriter() {
        log.info("Setting up addStatisticsItemWriter...");
        return bookingEntities -> {
            Map<LocalDateTime, StatisticsEntity> statisticsEntityMap = new LinkedHashMap<>();

            for (BookingEntity bookingEntity : bookingEntities) {
                final LocalDateTime statisticsAt = bookingEntity.getStatisticsAt();
                StatisticsEntity statisticsEntity = statisticsEntityMap.get(statisticsAt);

                if (statisticsEntity == null) {
                    statisticsEntityMap.put(statisticsAt, StatisticsEntity.create(bookingEntity));
                } else {
                    statisticsEntity.add(bookingEntity);
                }

            }
            final List<StatisticsEntity> statisticsEntities = new ArrayList<>(statisticsEntityMap.values());
            statisticsRepository.saveAll(statisticsEntities);
            log.info("### addStatisticsStep 종료");
        };
    }

    @Bean
    public Step makeDailyStatisticsStep() {
        log.info("Initializing makeDailyStatisticsStep...");
        return new StepBuilder("makeDailyStatisticsStep",jobRepository)
                .tasklet(makeDailyStatisticsTasklet, transactionManager)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Step makeWeeklyStatisticsStep() {
        log.info("Initializing makeWeeklyStatisticsStep...");
        return new StepBuilder("makeWeeklyStatisticsStep",jobRepository)
                .tasklet(makeWeeklyStatisticsTasklet, transactionManager)
                .transactionManager(transactionManager)
                .build();
    }
}*/
