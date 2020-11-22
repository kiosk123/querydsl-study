package com.study.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {
    
    private String userName;
    private String teamName;
    private Integer ageGoe; //나이가 크거나 같거나
    private Integer ageLoe; //나이가 작거나 같거나
    
    
}
