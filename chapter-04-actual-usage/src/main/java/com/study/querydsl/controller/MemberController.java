package com.study.querydsl.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberRepository memberRepository;
    
    @GetMapping("/v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition condition) {
        return memberRepository.searchByWhereClause(condition);
    }
}
