package com.study.querydsl.domain;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {
    
    @Column(name = "TEAM_ID")
    @Id @GeneratedValue
    @Getter
    private Long id;
    
    @Getter @Setter
    private String name;
    
    public Team(String name) {
        this.name = name; 
    }
    
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @Getter
    private List<Member> members = new ArrayList<>();
    
}
