package com.spring_batch.pass.repository.pass;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PassModelMapperTest {



    @Test
    public void test_toPassEntity(){ // PassModelMapper를 이용해서 PassEntity를 생성하는 테스트
        // Given
        final LocalDateTime now = LocalDateTime.now();
        final String userId = "A1000000";

        BulkPassEntity bulkPassEntity = new BulkPassEntity();
        bulkPassEntity.setBulkPassSeq(1);
        bulkPassEntity.setUserGroupId("GROUP");
        bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
        bulkPassEntity.setCount(10);
        bulkPassEntity.setStartedAt(now.minusDays(60));
        bulkPassEntity.setEndedAt(now);

        // When
        final PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId); // PassModelMapper를 이용해서 PassEntity를 생성한다.

        // Then
        assertEquals(1,passEntity.getPassSeq());// 패스 시퀀스가 1인지 확인한다.
        assertEquals(PassStatus.READY, passEntity.getStatus()); // 패스 상태가 READY인지 확인한다.
        assertEquals(10, passEntity.getRemainingCount()); // 패스의 잔여 횟수가 10인지 확인한다.
        assertEquals(now.minusDays(60), passEntity.getStartedAt()); // 패스의 시작일이 60일 전인지 확인한다.
        assertEquals(now, passEntity.getEndedAt()); // 패스의 종료일이 현재 시간인지 확인한다.\
        assertEquals(userId, passEntity.getUserId()); // 패스의 유저아이디가 A1000000인지 확인한다.
    }
}