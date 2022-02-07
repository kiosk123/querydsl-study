package com.study.querydsl.repository;

import static com.study.querydsl.domain.QMember.member;
import static com.study.querydsl.domain.QTeam.team;

import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.dto.QMemberTeamDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em); // 스프링 빈으로 등록해서 처리해도됨
    }

    @Override
    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDTO> results = 
                queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()), 
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDTO> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 데이터 가져오는 쿼리와 Total count 쿼리를 분리 - 데이터가 많을 때 최적화를 위해 사용할 것을 추천
     */
    @Override
    public Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> content = 
                queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()), 
                       ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = 
                queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()), 
                       ageLoe(condition.getAgeLoe()))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * total count 최적화
     */
    @Override
    public Page<MemberTeamDTO> searchPageOptimal(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> content = 
                queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()), 
                       ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        /**
         * 람다를 이용해서 count 쿼리가 조건에 따라 호출되어 쿼리 최적화
         * 
         * 스프링 데이터 라이브러리가 제공
         * count 쿼리가 생략 가능한 경우 생략해서 처리
         * - 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
         * - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)
         */
        JPAQuery<Member> countQuery = queryFactory
                    .select(member)
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(userNameEq(condition.getUserName()), 
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()), 
                           ageLoe(condition.getAgeLoe()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * sorting (정렬)
     */
    @Override
    public Page<MemberTeamDTO> searchPageBySort(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<MemberTeamDTO> query = 
                queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .where(userNameEq(condition.getUserName()), 
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()), 
                        ageLoe(condition.getAgeLoe()));
        
        pageable.getSort().forEach(sort -> {
            Sort.Order o = sort;
            PathBuilder pathBuilder = new PathBuilder(member.getType(), member.getMetadata());
            query.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
        });
        
        List<MemberTeamDTO> content = query.fetch();
        
        return PageableExecutionUtils.getPage(content, pageable, () -> {
            return queryFactory
                    .select(member)
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(userNameEq(condition.getUserName()), 
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()), 
                           ageLoe(condition.getAgeLoe()))
                    .fetchCount();
        });
    }

    @Override
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
        return queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    @Override
    public List<MemberTeamDTO> searchByWhereClause(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
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

//    BooleanBuilder를 이용하면 null 걱정 없이 체이닝이 가능하다
//    private BooleanBuilder ageLoe(Integer ageLoe) {
//        return Objects.isNull(ageLoe) ? new BooleanBuilder() : new BooleanBuilder(member.age.loe(ageLoe));
//    }
//
//    private BooleanBuilder ageGoe(Integer ageGoe) {
//        return Objects.isNull(ageGoe) ? new BooleanBuilder() : new BooleanBuilder(member.age.goe(ageGoe));
//    }
//
//    private BooleanBuilder teamNameEq(String teamName) {
//        return (!StringUtils.hasText(teamName)) ? new BooleanBuilder() : new BooleanBuilder(team.name.eq(teamName));
//    }
//
//    private BooleanBuilder userNameEq(String userName) {
//        return (!StringUtils.hasText(userName)) ? new BooleanBuilder() : new BooleanBuilder(member.userName.eq(userName));
//    }

}
