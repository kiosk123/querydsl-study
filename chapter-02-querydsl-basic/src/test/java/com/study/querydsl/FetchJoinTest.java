package com.study.querydsl;

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

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.QMember;
import com.study.querydsl.domain.QTeam;
import com.study.querydsl.domain.Team;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class FetchJoinTest {
    
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
    void fetchJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        List<Member> result = queryFactory.select(member)
                                          .from(member)
                                          .innerJoin(member.team, team)
                                          .fetchJoin()
                                          .where(team.name.eq("team1"))
                                          .fetch();

        
        assertEquals(3, result.size());
        result.forEach(m -> {
            //연관 엔티티가 로딩 되었는지 확인
            if (!emf.getPersistenceUnitUtil().isLoaded(m.getTeam())) {
                fail(m + "'s team is not loaded");
            }
        });
    }
}
