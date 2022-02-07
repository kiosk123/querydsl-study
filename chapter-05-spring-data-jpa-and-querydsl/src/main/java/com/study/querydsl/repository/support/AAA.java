package com.study.querydsl.repository.support;

import java.util.List;

import com.querydsl.jpa.impl.JPAQuery;
import com.study.querydsl.dto.MemberSearchCondition;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils; //패키지 변경

public class AAA {
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age, team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member).leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset()).limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                    .select(member.count())
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamName()), 
                           ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()));
                
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
