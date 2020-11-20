package com.study.querydsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.QMember;
import com.study.querydsl.domain.Team;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SortingTest {
    
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
        Member member3 = new Member(null, 30, team);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.flush();
        em.clear();
    }
    
    @Test
    void sortingTest() {
        QMember member = QMember.member;
        List<Member> result = queryFactory.selectFrom(member)
                                          .orderBy(member.age.desc(), member.userName.asc().nullsLast())
                                          .fetch();
        
        assertEquals(3, result.size());
        assertEquals("member2", result.get(0).getUserName());
        assertEquals(null, result.get(1).getUserName());
        assertEquals("member1", result.get(2).getUserName());
    }
}
