package com.study.querydsl.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;

public interface MemberQuerydslRepository {
    List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condition);
    List<MemberTeamDTO> searchByWhereClause(MemberSearchCondition condition);
    
    //페이징
    Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
