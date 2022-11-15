package com.whoIsLeader.GooDoggy.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.whoIsLeader.GooDoggy.util.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "user")
@NoArgsConstructor
@DynamicInsert
public class UserEntity extends BaseEntity {

    //userIdx, name, email, id, password, profileImgUrl, status
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userIdx;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 30)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String id;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String profileImgUrl;

    @Column(columnDefinition = "varchar(10) default 'active'")
    private String status;

    @Builder
    public UserEntity(String name, String email, String id, String password, String profileImgUrl, String status){
        this.name = name;
        this.email = email;
        this.id = id;
        this.password = password;
        this.profileImgUrl = profileImgUrl;
        this.status = status;
    }
}