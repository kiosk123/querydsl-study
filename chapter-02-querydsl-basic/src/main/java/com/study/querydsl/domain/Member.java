package com.study.querydsl.domain;

import java.util.Objects;

import javax.jdo.annotations.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    
    @Column(name = "MEMBER_ID")
    @Id @GeneratedValue
    @Getter
    private Long id;
    
    @Getter @Setter
    private String userName;
    
    @Getter @Setter
    private Integer age;
    
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "TEAM_ID")
    Team team;
    
    
    public Member(String userName) {
        this.userName = userName;
    }
    
    public Member(String userName, Integer age) {
        this(userName);
        this.age = age;
    }
    
    public Member(String userName, Integer age, Team team) {
        this(userName, age);
        this.team = team;
    }
    
    public void changeTeam(Team team) {
        this.team = team;
        if (!team.getMembers().isEmpty() && !Objects.isNull(getId())) {
            team.getMembers().removeIf(m -> getId().equals(m.getId()));
        }
        team.getMembers().add(this);
    }
}
