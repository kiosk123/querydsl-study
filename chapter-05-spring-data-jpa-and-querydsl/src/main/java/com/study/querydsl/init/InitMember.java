package com.study.querydsl.init;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.Team;

import lombok.RequiredArgsConstructor;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {
   
    private final InitMemberService initMemberService;
    
    @PostConstruct
    public void init() {
        initMemberService.init();
    }
    
//    @Component
//    static class InitMemberService { //static 안붙이면 순환참조 관계로 인해 오류 발생
//        @PersistenceContext
//        private EntityManager em;
//        
//        @Transactional
//        public void init() {
//            Team teamA = new Team("teamA");
//            Team teamB = new Team("teamB");
//            em.persist(teamA);
//            em.persist(teamB);
//            
//            for (int i = 0; i < 100; i++) {
//                Team team = (i % 2 == 0) ? teamA : teamB;
//                em.persist(new Member("member" + i, i, team));
//            }
//        }
//    }
}


@Component
class InitMemberService { 
    @PersistenceContext
    private EntityManager em;
    
    @Transactional
    public void init() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        
        for (int i = 0; i < 100; i++) {
            Team team = (i % 2 == 0) ? teamA : teamB;
            em.persist(new Member("member" + i, i, team));
        }
    }
}
