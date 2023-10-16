package com.spring_batch.pass;

import com.spring_batch.pass.repository.statistics.AggregatedStatistics;
import com.spring_batch.pass.repository.statistics.StatisticsRepository;
import com.spring_batch.pass.util.LocalDateTimeUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component // 이 클래스를 스프링 컨테이너에 등록합니다.
@RequiredArgsConstructor // final 필드를 가진 클래스의 생성자를 자동으로 만들어줍니다.
public class JobExecutor {

    private final JobLauncher jobLauncher; // Job을 실행시키는 역할을 합니다.
    private final ApplicationContext context; // Job을 실행시키기 위해 필요한 Job과 Step을 가지고 있습니다.
    private final StatisticsRepository statisticsRepository;


    @PostConstruct // 의존성 주입이 이루어진 후에 실행됩니다.
    public void launchAllJobs() throws Exception{
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(7); // 예: 지난 7일
        LocalDateTime to = now;

        // 데이터베이스에서 가장 최근의 통계 데이터를 가져옵니다.
        List<AggregatedStatistics> recentStats = statisticsRepository.findByStatisticsAtBetweenAndGroupBy(from, to);
        if (!recentStats.isEmpty()) {
            from = recentStats.get(0).getStatisticsAt();
            to = recentStats.get(recentStats.size() - 1).getStatisticsAt();
        }

        executeJob("addPassesJob");
        executeJob("expirePassesJob");
        executeJob("usePassesJob");
        executeJob("sendNotificationBeforeClassJob");
    }

    @PostConstruct
    private void executeStatisticsJob() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(7); // 예: 지난 7일
        LocalDateTime to = now;

        // 데이터베이스에서 가장 최근의 통계 데이터를 가져옵니다.
        List<AggregatedStatistics> recentStats = statisticsRepository.findByStatisticsAtBetweenAndGroupBy(from, to);
        if (!recentStats.isEmpty()) {
            from = recentStats.get(0).getStatisticsAt();
            to = recentStats.get(recentStats.size() - 1).getStatisticsAt();
        }

        JobParameters parameters = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("from", from.format(LocalDateTimeUtils.YYYY_MM_DD_HH_MM))
                .addString("to", to.format(LocalDateTimeUtils.YYYY_MM_DD_HH_MM))
                .toJobParameters();


        Job job = (Job) context.getBean("makeStatisticsJob");
        jobLauncher.run(job, parameters);
    }
    private void executeJob(String jobBeanName) throws Exception {
                JobParameters parameters = new JobParametersBuilder() // Job을 실행시키기 위한 파라미터를 설정합니다.
                        .addString("JobID", String.valueOf(System.currentTimeMillis()))// Job의 ID를 설정합니다.
                        .toJobParameters(); // JobParameters를 생성합니다.
        Job job = (Job) context.getBean(jobBeanName); // Job을 가져옵니다.
        jobLauncher.run(job, parameters); // Job을 실행시킵니다.
    }
}

