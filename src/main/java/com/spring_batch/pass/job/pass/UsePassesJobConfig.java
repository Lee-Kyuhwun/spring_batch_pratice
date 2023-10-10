package com.spring_batch.pass.job.pass;


import com.spring_batch.pass.repository.booking.BookingEntity;
import com.spring_batch.pass.repository.booking.BookingRepository;
import com.spring_batch.pass.repository.booking.BookingStatus;
import com.spring_batch.pass.repository.pass.PassEntity;
import com.spring_batch.pass.repository.pass.PassRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Future;

@Configuration
public class UsePassesJobConfig {
    private final int CHUNK_SIZE = 10;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final PassRepository passRepository;
    private final BookingRepository bookingRepository;

    public UsePassesJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory, PassRepository passRepository, BookingRepository bookingRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.passRepository = passRepository;
        this.bookingRepository = bookingRepository;
    }


    @Bean
    public Job usePassesJob(){
        return this.jobBuilderFactory.get("usePassesJob")
                .start(usePassesStep())
                .build();
    }

    @Bean
    public Step usePassesStep() {
        return this.stepBuilderFactory.get("usePassesStep")
                .<BookingEntity, Future<BookingEntity>>chunk(CHUNK_SIZE)
                .reader(usePassesItemReader())
                .processor(usePassesAsyncItemProcessor())
                .writer(usePassesAsyncItemWriter())
                .build();

    }


    @Bean
    public JpaCursorItemReader<BookingEntity> usePassesItemReader() {
        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("usePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select b from BookingEntity b join fetch b.passEntity where b.status = :status and b.usedPass = false and b.endedAt < :endedAt")
                .parameterValues(Map.of("status", BookingStatus.COMPLETED, "endedAt", LocalDateTime.now()))
                .build();
    }

    @Bean
    public AsyncItemProcessor<BookingEntity , BookingEntity> usePassesAsyncItemProcessor() {
        AsyncItemProcessor<BookingEntity, BookingEntity> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(usePassesItemProcessor()); // 1. ItemProcessor 지정
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor()) ;// 2. TaskExecutor 지정
        return asyncItemProcessor;
    }


    @Bean
    public ItemProcessor<BookingEntity, BookingEntity> usePassesItemProcessor() {
        return bookingEntity -> {
            PassEntity passEntity = bookingEntity.getPassEntity(); // 1. passEntity 조회
            passEntity.setRemainingCount(passEntity.getRemainingCount() - 1); // 2. 남은 횟수 -1
            bookingEntity.setPassEntity(passEntity); // 3. bookingEntity에 passEntity를 다시 set
            bookingEntity.setUsedPass(true); // 4. usedPass true로 변경
            return bookingEntity;
        };
    }

    @Bean
    public AsyncItemWriter<BookingEntity>usePassesAsyncItemWriter(){
        AsyncItemWriter<BookingEntity> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(usePassesItemWriter()); // 1. ItemWriter 지정
        return asyncItemWriter;
    }

    @Bean
    public ItemWriter<BookingEntity> usePassesItemWriter(){
        return bookingEntities -> {
            for(BookingEntity bookingEntity : bookingEntities){
                int updateCount = passRepository.updateRemainingCount(bookingEntity.getPassEntity().getPassSeq(), bookingEntity.getPassEntity().getRemainingCount());
                if(updateCount >0){ // update되는 카운트가 있다면
                    bookingRepository.updateUsedPass(bookingEntity.getBookingSeq(), true); // bookingEntity의 usedPass를 true로 변경

                }
            }
        };
    }

}
