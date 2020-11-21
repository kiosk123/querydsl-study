package com.study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.QMember;
import com.study.querydsl.domain.QTeam;
import com.study.querydsl.domain.Team;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SubQueryTest {
    
    @Autowired
    EntityManager em;
    
    @PersistenceUnit
    EntityManagerFactory emf;
    
    JPAQueryFactory queryFactory;
    
    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);
        
        //given
        Team team1 = new Team("team1");
        Team team2 = new Team("team2");
        Team team3 = new Team("team3");
        em.persist(team1);
        em.persist(team2);
        em.persist(team3);
        
        Member member1 = new Member("member1", 10, team1);
        Member member2 = new Member("member2", 20, team1);
        Member member3 = new Member("member3", 30, team1);
        Member member4 = new Member("member4", 40, team2);
        Member member5 = new Member("member5", 50, team2);
        Member member6 = new Member("member6", 60, null);
        Member member7 = new Member("member7", 70, null);
        
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
        em.persist(member7);
        em.flush();
        em.clear();
    }
    
    @Test
    void subQueryEq() {
        QMember member = QMember.member;
        QMember subMember = new QMember("subMember");
        
        //나이가 가장 많은 회원 조회 - 서브쿼리에서는 alias가 달라야함 일반 SQL사람과 동일
        Member findMember = queryFactory.selectFrom(member)
                                        .where(member.age.eq(
                                               JPAExpressions.select(subMember.age.max())
                                                              .from(subMember)
                                         ))
                                        .fetchOne();
        
        assertEquals(70, findMember.getAge());
        
    }
    
    @Test
    void subQueryIn() {
        QMember member = QMember.member;
        QMember subMember = new QMember("subMember");
        
        //나이가 10살 보다 많은  회원 조회 - 서브쿼리에서는 alias가 달라야함 일반 SQL사람과 동일
        List<Member> members = queryFactory.selectFrom(member)
                                        .where(member.age.in(
                                               JPAExpressions.select(subMember.age)
                                                             .from(subMember)
                                                             .where(subMember.age.gt(10))
                                         ))
                                        .fetch();
        
        assertEquals(6, members.size());
        assertThat(members).extracting("age")
                           .contains(20, 30, 40, 50, 60, 70);
        
    }
    
    @Test
    void subQuerySelectClause() {
        QMember member = QMember.member;
        QMember subMember = new QMember("subMember");
        
        /**
         * select 절에서 서브쿼리 실행
         * [member1, 40.0]
         * [member2, 40.0]
         * [member3, 40.0]
         * [member4, 40.0]
         * [member5, 40.0]
         * [member6, 40.0]
         * [member7, 40.0]
         */
        queryFactory.select(member.userName, JPAExpressions.select(subMember.age.avg())
                                                           .from(subMember))
                    .from(member)
                    .fetch()
                    .forEach(System.out::println);
        
        
        
    }
}
