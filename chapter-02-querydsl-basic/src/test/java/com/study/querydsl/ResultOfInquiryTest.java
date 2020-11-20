package com.study.querydsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.QMember;
import com.study.querydsl.domain.Team;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ResultOfInquiryTest {
    
    @Autowired
    EntityManager em;
    
    JPAQueryFactory queryFactory;
    
    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);
        
        //given
        Team team = new Team("team1");
        em.persist(team);
        
        Member member1 = new Member("member1", 20, team);
        Member member2 = new Member("member2", 30, team);
        em.persist(member1);
        em.persist(member2);
        em.flush();
        em.clear();
    }
    
    @Test
    void fetch() {
        QMember member = QMember.member;
        
        //when
        List<Member> members = queryFactory.selectFrom(member)
                                           .fetch();
        
        assertEquals(2, members.size());
    }
    
    @Test
    void fetchOne() {
        QMember member = QMember.member;
        
        //when
        Member findMember = queryFactory.selectFrom(member)
                                        .where(member.userName.eq("member1"))
                                        .fetchOne();
        
        //then
        assertEquals("member1", findMember.getUserName());
        assertEquals(20, findMember.getAge());
        
        assertThrows(NonUniqueResultException.class, () -> {
            queryFactory.selectFrom(member)
                        .fetchOne();
        });
    }
    
    @Test
    void fetchFirst() {
        QMember member = QMember.member;
        
        //when
        Member findMember = queryFactory.selectFrom(member)
                                        .where(member.userName.eq("member1"))
                                        .fetchFirst(); //limit 1 fetchone
        
        //then
        assertEquals("member1", findMember.getUserName());
        assertEquals(20, findMember.getAge());
    }
    
    @Test
    void fetchResults() {
        QMember member = QMember.member;
        
        //when
        QueryResults<Member> result = queryFactory.selectFrom(member)
                                                  .fetchResults(); 
        
        List<Member> members = result.getResults();
        assertEquals(2, members.size());
        assertEquals(2, result.getTotal());
        
        System.out.println("used limit for query : " + result.getLimit());
        System.out.println("used offset for query : " + result.getOffset());
    }
    
    @Test
    void fetchCount() {
        QMember member = QMember.member;
        
        Long count = queryFactory.selectFrom(member)
                                 .fetchCount(); 
        
        assertEquals(2, count);
    }
}
