package com.study.querydsl.repository;

import static com.study.querydsl.domain.QMember.member;
import static com.study.querydsl.domain.QTeam.team;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.dto.QMemberTeamDTO;

@Repository
public class MemberRepository {
    
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    
    public MemberRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em); //스프링 빈으로 등록해서 처리해도됨
    }
    
    public Long save(Member member) {
        em.persist(member);
        return member.getId();
    }
    
    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }
    
    public List<Member> findByUserName(String userName) {
        return queryFactory.selectFrom(member)
                           .where(member.userName.lower().eq(userName.toLowerCase()))
                           .fetch();
    }
    
    public List<Member> findAll() {
        return queryFactory.selectFrom(member)
                           .fetch();
    }
    
    public List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(condition.getUserName())) {
            builder.and(member.userName.eq(condition.getUserName()));
        }
        
        if (StringUtils.hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        
        if (!Objects.isNull(condition.getAgeGoe())) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        
        if (!Objects.isNull(condition.getAgeLoe())) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }
        return queryFactory.select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                           .from(member)
                           .leftJoin(member.team, team)
                           .where(builder)
                           .fetch();
    }
    
    public List<MemberTeamDTO> searchByWhereClause(MemberSearchCondition condition) {
        return queryFactory.select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()),
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()),
                       ageLoe(condition.getAgeLoe()))
                .fetch();
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
