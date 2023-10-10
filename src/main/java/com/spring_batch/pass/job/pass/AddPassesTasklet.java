package com.spring_batch.pass.job.pass;


import com.spring_batch.pass.repository.pass.*;
import com.spring_batch.pass.repository.user.UserGroupMappingEntity;
import com.spring_batch.pass.repository.user.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;


@Slf4j
@Component//Tasklet을 스프링 빈으로 등록한다.
public class AddPassesTasklet implements Tasklet {
    private final PassRepository passRepository;
    private final BulkPassRepository bulkPassRepository;
    private final UserGroupMappingRepository userGroupMappingRepository;

    public AddPassesTasklet(PassRepository passRepository, BulkPassRepository bulkPassRepository, UserGroupMappingRepository userGroupMappingRepository) {
        this.passRepository = passRepository;
        this.bulkPassRepository = bulkPassRepository;
        this.userGroupMappingRepository = userGroupMappingRepository;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // 이용권 시작 일시 1일 전user group 내 각 사용자에게 이용권을 추가해줍니다.
        final LocalDateTime startedAt = LocalDateTime.now().minusDays(1); // 하루전에 이용권을 넣어줘서 이용자들이 확인할 수 있기위해
        final List<BulkPassEntity> bulkPassEntities = bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY, startedAt); // 대량 이용권 정보를 조회한다.
        // BulkPass가 아직 처리가 안된 Ready일때 그리고 하루전보다 미래일때 조회한다.

        int count = 0;
        // 대량 이용권 정보를 돌면서 group에 속한 userId를 조회하고 해동 userId로 이용권을 추가한다.
        for (BulkPassEntity bulkPassEntity : bulkPassEntities) {
            final List<String> userGroupId = userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())
                    .stream().map(UserGroupMappingEntity::getUserId).toList(); // userGroupId로 userId를 조회한다.
            //stream이란 자바8에서 추가된 기능으로 컬렉션의 요소를 하나씩 참조해서 람다식으로 처리할 수 있도록 해주는 기능이다.
            // ::은 메소드 레퍼런스로 메소드를 참조하는 것이다. 원래 표현식은 (x) -> x.toString() 이지만 x -> x.toString() 이렇게 표현할 수 있다.


            // 이 건수를 로깅하기 위해서 더해준다.
            count = count + addPasses(bulkPassEntity, userGroupId);


            bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
            bulkPassRepository.save(bulkPassEntity);
        }


        log.info("AddPassesTasklet - execute: 이용권 {}건 추가 완료 , startedAt: {}", count, startedAt);
        return RepeatStatus.FINISHED;
    }


    //bulkPass의 정보로 pass데이터를 생성한다.
    private int addPasses(BulkPassEntity bulkPassEntity, List<String> userIds) {
        //bulkPass의 정보로 pass데이터를 생성한다.
        List<PassEntity> passEntities = new ArrayList<>();

        for (String userId : userIds) {
            PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId); // PassModelMapper를 사용해서 PassEntity를 생성한다.
            passEntities.add(passEntity);
        }
        return passRepository.saveAll(passEntities).size();
    }


}
