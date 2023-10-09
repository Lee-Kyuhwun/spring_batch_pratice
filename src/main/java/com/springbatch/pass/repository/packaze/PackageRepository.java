package com.springbatch.pass.repository.packaze;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, Integer> {


    List<PackageEntity> findByCreatedAtAfter(LocalDateTime dateTime, Pageable packageSeq);


    @Transactional // Transactional 어노테이션을 추가해야 update, delete 쿼리가 정상적으로 실행됨
    @Modifying // update, delete 쿼리를 실행하기 위해 필요
    @Query("UPDATE PackageEntity p SET p.count = :count, p.period = :period WHERE p.packageSeq = :packageSeq")
    int updateCountAndPeriod(@Param("packageSeq") Integer packageSeq, @Param("count") Integer count, @Param("period") Integer period);

}
