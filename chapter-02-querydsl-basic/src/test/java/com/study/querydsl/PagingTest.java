package com.study.querydsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.QMember;
import com.study.querydsl.domain.Team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PagingTest {
    
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
    void paging() {
        QMember member = QMember.member;
        
        List<Member> members = queryFactory.selectFrom(member)
                                           .orderBy(member.userName.desc())
                                           .offset(0) //setFirstResult
                                           .limit(2)  //setMaxResult
                                           .fetch();
        assertEquals(2, members.size());
    }
}
