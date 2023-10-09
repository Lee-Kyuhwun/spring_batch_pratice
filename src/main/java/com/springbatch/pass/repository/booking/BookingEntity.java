package com.springbatch.pass.repository.booking;


import com.springbatch.pass.repository.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "booking")
public class BookingEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookingSeq;

    private Integer passSeq;

    private String userId;

    @Enumerated(EnumType.STRING) // Enum 타입을 DB에 String으로 저장
    private BookingStatus status;

    private boolean usedPass;

    private boolean attended;






}
