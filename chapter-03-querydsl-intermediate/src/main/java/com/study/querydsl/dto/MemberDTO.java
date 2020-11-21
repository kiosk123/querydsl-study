package com.study.querydsl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberDTO {
    
    public MemberDTO() {} //querydsl 사용시 필수
    
    private String userName;
    private int age;
}
