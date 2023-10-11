package com.spring_batch.pass.job.pass;


import com.spring_batch.pass.repository.booking.BookingEntity;
import com.spring_batch.pass.repository.booking.BookingRepository;
import com.spring_batch.pass.repository.booking.BookingStatus;
import com.spring_batch.pass.repository.pass.PassEntity;
import com.spring_batch.pass.repository.pass.PassRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Future;

@Configuration
public class UsePassesJobConfig {
    private final int CHUNK_SIZE = 10;

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final JpaTransactionManager transactionManager;
    private final PassRepository passRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public UsePassesJobConfig(EntityManagerFactory entityManagerFactory1, JobRepository jobRepository,
                              JpaTransactionManager entityManagerFactory,
                              PassRepository passRepository,
                              BookingRepository bookingRepository) {
        this.entityManagerFactory = entityManagerFactory1;
        this.jobRepository = jobRepository;
        this.transactionManager = entityManagerFactory;
        this.passRepository = passRepository;
        this.bookingRepository = bookingRepository;
    }


    @Bean
    public Job usePassesJob() throws Exception {
        return new JobBuilder("usePassesJob", jobRepository)
                .start(usePassesStep())
                .build();
    }

    @Bean
    public Step usePassesStep() throws Exception {
        return new StepBuilder("usePassesStep", jobRepository)
                .<BookingEntity, Future<BookingEntity>>chunk(CHUNK_SIZE, transactionManager)
                .reader(usePassesItemReader())
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
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
    public AsyncItemProcessor<BookingEntity, BookingEntity> asyncItemProcessor() throws Exception {
        AsyncItemProcessor<BookingEntity, BookingEntity> processor = new AsyncItemProcessor<>();
        processor.setDelegate(usePassesItemProcessor());
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        processor.afterPropertiesSet();
        return processor;
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
    public AsyncItemWriter<BookingEntity> asyncItemWriter() {
        AsyncItemWriter<BookingEntity> writer = new AsyncItemWriter<>();
        writer.setDelegate(usePassesItemWriter()); // 이는 실제로 데이터를 데이터베이스 또는 다른 저장소에 저장하는 `ItemWriter`입니다.
        return writer;
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
