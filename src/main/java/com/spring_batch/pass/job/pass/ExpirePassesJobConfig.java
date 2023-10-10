package com.spring_batch.pass.job.pass;


import com.spring_batch.pass.repository.pass.PassEntity;
import com.spring_batch.pass.repository.pass.PassStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
public class ExpirePassesJobConfig {

    private final int CHUNK_SIZE = 5;
    //@EnableBatchProcessing로 인해 Bean으로 제공된 JobBuilderFactory, StepBuilderFactory를 주입받는다.
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory entityManagerFactory; //JPA를 사용하기 위해 EntityManagerFactory를 주입받는다.

    public ExpirePassesJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job expirePassesJob() {
        return this.jobBuilderFactory.get("expirePassesJob") //Job 이름을 expirePassesJob로 지정한다.
                .start(expirePassesStep()) //Job 실행시 최초로 실행될 Step을 지정한다.
                .build(); //Job을 빌드한다.
    }


    /*
    * step은 get으로 이름을 정의하고 청크의 input,output을 설정하고
    * 청크사이즈를 설정한다.
    * 그 이후에는 reader, processor, writer를 정의하면 된다.
    * */
    @Bean
    public Step expirePassesStep() {
        return stepBuilderFactory.get("expirePassesStep")
                .<PassEntity, PassEntity>chunk(CHUNK_SIZE)//첫번째 PassEntity는 Reader에서 반환할 타입, 두번째 PassEntity는 Writer에 파라미터로 넘어올 타입
                .reader(expirePassesItemReader())
                .processor(expirePassesItemProcessor())
                .writer(expirePassesItemWriter())
                .build();
    }


    /*
    * JpaCursorItemReader: JpaPagingItemReader와 유사하지만, 페이징 처리를 하지 않고 Cursor를 사용하여 데이터를 읽어온다.
    * 페이징 기법보다 높은 성능을 보장하지만, Cursor를 사용하기 때문에 트랜잭션 범위가 Reader에서 Writer까지 전달되어야 한다.
    * 데이터 변경에 무관한 조회가 가능하다.
    * */

    @Bean
    @StepScope // Step의 실행시점에 Bean이 생성되도록 한다.
    public JpaCursorItemReader<PassEntity> expirePassesItemReader() {
        return new JpaCursorItemReaderBuilder<PassEntity>()
                .name("expirePassesItemReader")
                .entityManagerFactory(entityManagerFactory) //JpaCursorItemReader는 EntityManagerFactory를 주입받아야 한다. 왜 주입받냐면 Reader가 실행될 때마다 EntityManager를 생성하기 때문이다.
                .queryString("SELECT p FROM PassEntity p WHERE p.status = :status AND p.endedAt < :endedAt") //상태가 진행중일때 만료일이 현재일보다 이전인 데이터를 조회한다.
                .parameterValues(Map.of("status", PassStatus.PROGRESSED, "endedAt", LocalDateTime.now()))
                // 커서를 사용한 이유는 데이터에서 status인것들만 읽어와서  처리를 하게 되는데 그렇게 되면
                // 페이징을 사용할 경우에는 누락이 될 수 있다. 그래서 무결성조회가 가능한 커서를 사용한다.
                .build();
    }



    @Bean
    public ItemProcessor<PassEntity,PassEntity> expirePassesItemProcessor() {

        return passEntity -> {
            passEntity.setStatus(PassStatus.EXPIRED);
            return passEntity;
        };

    }

    @Bean
    public JpaItemWriter<PassEntity> expirePassesItemWriter() {

        return new JpaItemWriterBuilder<PassEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }


}
