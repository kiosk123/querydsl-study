package com.study.querydsl.repository;

import static com.study.querydsl.domain.QMember.member;
import static com.study.querydsl.domain.QTeam.team;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.study.querydsl.domain.Member;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.dto.QMemberTeamDTO;

/**
 * 엔티티 매니저 알아서 주입해줌
 * 엔티티 매니저 호출시 getEntityManager() 호출
 */
@Repository
public class MemberQuerydslSupportRepository extends QuerydslRepositorySupport {

    public MemberQuerydslSupportRepository() {
        super(Member.class);
    }
    
    public List<MemberTeamDTO> search(MemberSearchCondition condition) {
        return from(member)
                .leftJoin(member.team, team)
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()), 
                       ageLoe(condition.getAgeLoe()))
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .fetch();
    }
    
    public Page<MemberTeamDTO> searchPaging(MemberSearchCondition condition, Pageable pageable) {
        JPQLQuery<MemberTeamDTO> query =
            from(member)
            .leftJoin(member.team, team)
            .from(member)
            .leftJoin(member.team, team)
            .where(userNameEq(condition.getUserName()), 
                   teamNameEq(condition.getTeamName()),
                   ageGoe(condition.getAgeGoe()), 
                   ageLoe(condition.getAgeLoe()))
            .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name));
        
        query = getQuerydsl().applyPagination(pageable, query);
        QueryResults<MemberTeamDTO> results = query.fetchResults();
        
        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
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
