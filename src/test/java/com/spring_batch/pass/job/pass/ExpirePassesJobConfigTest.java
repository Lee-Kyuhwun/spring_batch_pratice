package com.spring_batch.pass.job.pass;


import com.spring_batch.config.TestBatchConfig;
import com.spring_batch.pass.repository.pass.PassEntity;
import com.spring_batch.pass.repository.pass.PassStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test") //테스트시 사용할 application.yml의 설정파일을 지정한다.
@ContextConfiguration(classes = {ExpirePassesJobConfig.class , TestBatchConfig.class}) //테스트시 사용할 설정파일을 지정한다.
public class ExpirePassesJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils; // end to end 테스트를 지원하는 JobLauncherTestUtils를 주입받는다.
    // end to end 테스트란, Job을 실행시키고, Job의 실행결과를 확인하는 테스트이다.



    @Test
    public void test_expiredPassStep() throws Exception{
        //given
        addPassEntites(10);

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(); //Job을 실행시킨다.
        JobInstance jobInstance = jobExecution.getJobInstance(); // JobInstance를 조회한다.

        //then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus()); //Job의 실행결과가 COMPLETED인지 확인한다.
        assertEquals("expirePassesJob", jobInstance.getJobName()); //Job의 이름이 expirePassesJob인지 확인한다.
    }



    private void addPassEntites(int size){
        final LocalDateTime now = LocalDateTime.now();
        final Random random = new Random();


        List<PassEntity> passEntities = new ArrayList<>();
        for(int i=0; i<size; i++){
            PassEntity passEntity = new PassEntity();
            passEntity.setPackageSeq(1);
            passEntity.setUserId("A" + 1000000 +i);
            passEntity.setStatus(PassStatus.PROGRESSED);
            passEntity.setRemainingCount(random.nextInt(11));
            passEntity.setStartedAt(now.minusDays(60));
            passEntity.setEndedAt(now.minusDays(1));
            passEntities.add(passEntity);
        }

    }


}