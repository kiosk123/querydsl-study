package com.study.querydsl.repository;

import java.util.List;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;

public interface MemberQuerydslRepository {
    List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condition);
    List<MemberTeamDTO> searchByWhereClause(MemberSearchCondition condition);
}
