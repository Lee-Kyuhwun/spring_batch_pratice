package com.spring_batch.pass.repository.user;

import com.spring_batch.pass.repository.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "user_group_mapping")
@IdClass(UserGroupMappingId.class)
public class UserGroupMappingEntity extends BaseEntity {
    @Id
    private String userGroupId;
    @Id
    private String userId;

    private String userGroupName;
    private String description;

}
