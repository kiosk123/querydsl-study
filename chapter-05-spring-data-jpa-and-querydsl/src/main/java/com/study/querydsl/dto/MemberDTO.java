package com.study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Data;

@Data
public class MemberDTO {

    private String userName;
    private int age;

    public MemberDTO() { } // querydsl 사용시 필수

    @QueryProjection
    public MemberDTO(String userName, int age) {
        this.userName = userName;
        this.age = age;
    }
}
