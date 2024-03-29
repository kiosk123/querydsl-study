package com.study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.QMember;
import com.study.querydsl.domain.Team;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BulkQueryTest {

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
    void bulkQuery() {
        QMember member = QMember.member;
        
        long count = queryFactory.update(member)
                                 .set(member.userName, "비회원")
                                 .where(member.age.gt(30))
                                 .execute();
        em.flush();
        em.clear(); //벌크 연산 후 항상 컨텍스트 초기화
        
        String userName = queryFactory.select(member.userName)
                                      .distinct()
                                      .from(member)
                                      .where(member.age.gt(30))
                                      .fetchOne();
        
        //30살 넘어서 비회원 처리 당한 회원수(처리된 로우의 수)
        assertEquals(4, count);
        assertEquals("비회원", userName);
    }
    
    
    @Test
    void bulkAdd() {
        QMember member = QMember.member;
        
        long count = queryFactory.update(member)
                                 .set(member.age, member.age.add(1))
                                 .execute();

        em.flush();
        em.clear(); //벌크 연산 후 항상 컨텍스트 초기화
        
        List<Integer> ages = queryFactory.select(member.age)
                                         .from(member)
                                         .fetch();
        
        assertEquals(7, count);
        assertThat(ages).contains(11, 21, 31, 41, 51, 61, 71);
    }
    
    
    @Test
    void bulkMinus() {
        QMember member = QMember.member;
        
        long count = queryFactory.update(member)
                                 .set(member.age, member.age.add(-1))
                                 .execute();
        em.flush();
        em.clear(); //벌크 연산 후 항상 컨텍스트 초기화

        List<Integer> ages = queryFactory.select(member.age)
                                         .from(member)
                                         .fetch();
        
        assertEquals(7, count);
        assertThat(ages).contains(9, 19, 29, 39, 49, 59, 69);
    }
    
    @Test
    void bulkMultiply() {
        QMember member = QMember.member;
        
        long count = queryFactory.update(member)
                                 .set(member.age, member.age.multiply(2))
                                 .execute();
        em.flush();
        em.clear(); //벌크 연산 후 항상 컨텍스트 초기화
        List<Integer> ages = queryFactory.select(member.age)
                                         .from(member)
                                         .fetch();
        
        assertEquals(7, count);
        assertThat(ages).contains(20, 40, 60, 80, 100, 120, 140);
    }
    
    @Test
    void bulkDelete() {
        QMember member = QMember.member;
        
        long count = queryFactory.delete(member)
                                 .where(member.age.gt(30))
                                 .execute();
        
        assertEquals(4, count);
    }
}
