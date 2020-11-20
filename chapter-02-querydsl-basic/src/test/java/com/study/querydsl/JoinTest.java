package com.study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.study.querydsl.domain.QTeam;
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
        Member member7 = new Member("team3", 60, null);
        
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
    void innerJoin() {
        QMember member = QMember.member;
        
        List<Member> result = queryFactory.select(member)
                                         .from(member)
                                         .innerJoin(member.team)
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
        assertEquals(4, result.size());
        
        assertThat(result).extracting("userName")
                          .containsExactly("member4", "member5", "member6", "team3");
    }
    
    @Test
    void rightJoin() {
        QMember member = QMember.member;
        
        //아무런 멤버도 없는 팀조회
        List<Team> result = queryFactory.select(member.team)
                                        .from(member)
                                        .rightJoin(member.team)
                                        .where(member.team.isNull())
                                        .fetch();
        
        assertEquals(1, result.size());
        
        assertThat(result).extracting("name")
                          .containsExactly("team3");
    }
    
    @Test
    void thetaJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        Member findMember = queryFactory.select(member)
                                        .from(member, team)
                                        .where(member.userName.eq(team.name))
                                        .fetchOne();
        
        assertEquals("team3", findMember.getUserName());
    }
}
