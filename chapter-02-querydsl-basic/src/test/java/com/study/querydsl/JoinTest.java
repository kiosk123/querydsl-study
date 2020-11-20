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

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.QMember;
import com.study.querydsl.domain.Team;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class JoinTest {
    
    @Autowired
    EntityManager em;
    
    JPAQueryFactory queryFactory;
    
    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);
        
        //given
        Team team1 = new Team("team1");
        Team team2 = new Team("team2");
        em.persist(team1);
        em.persist(team2);
        
        Member member1 = new Member("member1", 10, team1);
        Member member2 = new Member("member2", 20, team1);
        Member member3 = new Member("member3", 30, team1);
        Member member4 = new Member("member4", 40, team2);
        Member member5 = new Member("member5", 50, team2);
        Member member6 = new Member("member6", 60, null);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
        em.flush();
        em.clear();
    }
    
    @Test
    void innerJoin() {
        QMember member = QMember.member;
        
        List<Member> result = queryFactory.select(member)
                                         .from(member)
                                         .join(member.team)
                                         .where(member.team.name.eq("team1"))
                                         .fetch();
        
        assertEquals(3, result.size());
        result.forEach(m -> {
            if (!m.getTeam().getName().equals("team1")) {
                fail(m.getUserName() + " not in team1");
            }
        });
    }
    
    @Test
    void leftJoin() {
        QMember member = QMember.member;
        
        //team1에 속하지 않은 사람 조회
        List<Member> result = queryFactory.select(member)
                                         .from(member)
                                         .leftJoin(member.team)
                                         .where(member.team.name.ne("team1")
                                                .or(member.team.name.isNull()))
                                         .fetch();
        assertEquals(3, result.size());
    }
}
