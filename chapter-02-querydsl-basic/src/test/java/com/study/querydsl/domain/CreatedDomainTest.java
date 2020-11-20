package com.study.querydsl.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CreatedDomainTest {

    @Autowired
    EntityManager em;
    
    @Test
    void test() {
        //given
        Team team = new Team("team1");
        em.persist(team);
        
        Member member1 = new Member("member1", 20, team);
        Member member2 = new Member("member2", 30, team);
        em.persist(member1);
        em.persist(member2);
        em.flush();
        em.clear();
        
        //when
        Team findTeam = em.find(Team.class, team.getId());
        Member findMember1 = em.find(Member.class, member1.getId());
        Member findMember2 = em.find(Member.class, member2.getId());
        
        //then
        assertEquals(team.getId(), findTeam.getId());
        assertEquals(2, findTeam.getMembers().size());
        
        assertEquals(member1.getId(), findMember1.getId());
        assertEquals(member2.getId(), findMember2.getId());
    }
}
