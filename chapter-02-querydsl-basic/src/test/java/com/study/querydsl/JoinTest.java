package com.study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
    
    /**
     * innerJoin(조인대상, 별칭파라미터로 사용할 Q타임)
     * leftJoin(조인대상, 별칭파라미터로 사용할 Q타임)
     * join(조인대상, 별칭파라미터로 사용할 Q타임)
     * 
     * 기본적으로 alias를 이용하는 join은 on절에 식별값 비교가 들어가기 때문에
     * on 메소드를 활용하여 식별값 비교 조건을 추가할 필요가 없지만 (JPQL과 동일)
     * 쎄타조인 같은 경우는 on메소드를 이용하여 미리 조인할 대상을 필터링하는 것이 중요하다.
     */
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
        QTeam team = QTeam.team;
        
        List<Member> result = queryFactory.select(member)
                                          .from(member)
                                          .innerJoin(member.team, team)
                                          .where(team.name.eq("team1"))
                                          .fetch();
        
        assertEquals(3, result.size());
        result.forEach(m -> {
            if (!m.getTeam().getName().equals("team1")) {
                fail(m.getUserName() + " not in team1");
            }
        });
    }
    
    @Test 
    void innerJoinFilteringUsingOn() {
        // on절을 사용해서 조인 대상을 필터링 한 후 조인할 수 있다.
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        List<Member> result = queryFactory.select(member)
                                         .from(member)
                                         .innerJoin(member.team, team)
                                         .on(team.name.eq("team1"))
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
        QTeam team = QTeam.team;
        
        //team1에 속하지 않은 사람 조회
        List<Member> result = queryFactory.select(member)
                                         .from(member)
                                         .leftJoin(member.team, team)
                                         .where(team.name.ne("team1")
                                         .or(team.name.isNull()))
                                         .fetch();
        assertEquals(4, result.size());
        
        assertThat(result).extracting("userName")
                          .containsExactly("member4", "member5", "member6", "team3");
    }
    
    @Test
    void leftJoinFilteringUsingOn() {
        /**
         * 팀 이름이 team1인 팀만 조인, 회원은 모두 조회
         */
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        //팀이름이 팀 team1만 조인 회원은 모두 조회
        List<Tuple> result = queryFactory.select(member, team)
                                          .from(member)
                                          .leftJoin(member.team, team)
                                          .on(team.name.eq("team1"))
                                          .fetch();
        /**
         * Tuple로 Tuple.get(1, Team.class)로 Projection된 팀을 조회했을 때
         * member가 team1인 아닌 Tuple값인 team은 null이된다. 
         * 
         * leftJoinFilteringUsingOn tuple : [Member(id=4, userName=member1, age=10), Team(id=1, name=team1)]
         * leftJoinFilteringUsingOn tuple : [Member(id=5, userName=member2, age=20), Team(id=1, name=team1)]
         * leftJoinFilteringUsingOn tuple : [Member(id=6, userName=member3, age=30), Team(id=1, name=team1)]
         * leftJoinFilteringUsingOn tuple : [Member(id=7, userName=member4, age=40), null]
         * leftJoinFilteringUsingOn tuple : [Member(id=8, userName=member5, age=50), null]
         * leftJoinFilteringUsingOn tuple : [Member(id=9, userName=member6, age=60), null]
         * leftJoinFilteringUsingOn tuple : [Member(id=10, userName=team3, age=60), null]
         */

        result.forEach(t -> System.out.println("leftJoinFilteringUsingOn tuple : " + t));
    }
    
    @Test
    void rightJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        //아무런 멤버도 없는 팀조회
        List<Team> result = queryFactory.select(team)
                                        .from(member)
                                        .rightJoin(member.team, team)
                                        .where(team.members.size().eq(0))
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
    
    
    @Test
    void thetaOuterJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        /**
         * 이때 alias를 뺀다 alias를 넣으면
         * 조인대상에 식별값이 들어가서 식별값으로 매칭하지만
         * alias를 빼면 이름으로만 조인하여 조인대상이 필터링하기 때문에
         * 쎄타 외부 조인은 이렇게 해야한다.
         */
        List<Tuple> result = queryFactory.select(member, team)
                                         .from(member)
                                         .leftJoin(team) 
                                         .on(member.userName.eq(team.name))
                                         .fetch();
        
       assertEquals(7, result.size());
       
       /**
        * thetaOuterJoin tuple : [Member(id=54, userName=member1, age=10), null]
        * thetaOuterJoin tuple : [Member(id=55, userName=member2, age=20), null]
        * thetaOuterJoin tuple : [Member(id=56, userName=member3, age=30), null]
        * thetaOuterJoin tuple : [Member(id=57, userName=member4, age=40), null]
        * thetaOuterJoin tuple : [Member(id=58, userName=member5, age=50), null]
        * thetaOuterJoin tuple : [Member(id=59, userName=member6, age=60), null]
        * thetaOuterJoin tuple : [Member(id=60, userName=team3, age=60), Team(id=53, name=team3)]
        */
       result.forEach(t -> System.out.println("thetaOuterJoin tuple : " + t));
    }
}
