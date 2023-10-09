package com.springbatch.pass.repository;


import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass // 상속받은 entity에서 아래 필들들을 컬럼으로 사용할 수 있게 해준다.
@EntityListeners(AuditingEntityListener.class) // jpa auditing이란 엔티티 생성, 수정 시간을 자동으로 반영하는 기능
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // 마지막 수정일시를 생성합니다.
    private LocalDateTime modifiedAt;




}
