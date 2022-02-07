package com.study.querydsl.repository;

import static com.study.querydsl.domain.QMember.member;
import static com.study.querydsl.domain.QTeam.team;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.study.querydsl.domain.Member;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.repository.support.Querydsl4RepositorySupport;

@Repository
public class MemberRepositoryUsingCustomSupport extends Querydsl4RepositorySupport{

    public MemberRepositoryUsingCustomSupport() {
        super(Member.class);
    }
    
    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }
    
    public List<Member> basicSelectfrom() {
        return selectFrom(member)
                .fetch();
    }
    
    /**
     * 기존의 querydsl을 사용해서 데이터를 가져오는 방식
     */
    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<Member> query = selectFrom(member)
                                .where(userNameEq(condition.getUserName()),
                                       teamNameEq(condition.getTeamName()),
                                       ageGoe(condition.getAgeGoe()),
                                       ageLoe(condition.getAgeLoe()));
        List<Member> content = getQuerydsl().applyPagination(pageable, query).fetch();
        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }
    
    /**
     * 커스텀 Support를 사용해서 데이터를 가져오는 방식
     */
    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, query -> 
                    query
                    .selectFrom(member)
                    .where(userNameEq(condition.getUserName()),
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()),
                           ageLoe(condition.getAgeLoe()))
                );
    }
    
    /**
     * 커스텀 Support를 사용해서 데이터를 가져오는 방식 counter 쿼리 분리
     */
    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, query -> 
                    query
                    .selectFrom(member)
                    .where(userNameEq(condition.getUserName()),
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()),
                           ageLoe(condition.getAgeLoe())),
                    
                    countQuery -> 
                    countQuery
                    .select(member)
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(userNameEq(condition.getUserName()), 
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()), 
                           ageLoe(condition.getAgeLoe())));
    }
    
    private BooleanExpression ageLoe(Integer ageLoe) {
        return Objects.isNull(ageLoe) ? null : member.age.loe(ageLoe);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return Objects.isNull(ageGoe) ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression teamNameEq(String teamName) {
        return (!StringUtils.hasText(teamName)) ? null : team.name.eq(teamName);
    }

    private BooleanExpression userNameEq(String userName) {
        return (!StringUtils.hasText(userName)) ? null : member.userName.eq(userName);
    }
}
