package com.study.querydsl.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;

public interface MemberRepositoryCustom {
    List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condition);
    List<MemberTeamDTO> searchByWhereClause(MemberSearchCondition condition);
    
    //페이징
    Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
    
    //total count 최적화
    Page<MemberTeamDTO> searchPageOptimal(MemberSearchCondition condition, Pageable pageable);
    
    //sort
    Page<MemberTeamDTO> searchPageBySort(MemberSearchCondition condition, Pageable pageable);
}
