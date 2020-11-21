package com.study.querydsl;

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

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.QMember;
import com.study.querydsl.domain.Team;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class DynamicQueryTest {

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
    void booleanBuilder() {
        String userNameParam = "member1";
        Integer ageParam = 10;
        
        List<Member> result = searchMember(userNameParam, ageParam);
        
        
        assertEquals(1, result.size());
        
        assertEquals("member1", result.get(0).getUserName());
        assertEquals(10, result.get(0).getAge());
    }
    
    private List<Member> searchMember(String userNameCond, Integer ageCond) {
        QMember member = QMember.member;
        BooleanBuilder builder = new BooleanBuilder();
        
        // 초기값 설정 - 파라미터 값이 반드시 NULL이 아니어야함
        // BooleanBuilder builder = new BooleanBuilder(member.userName.eq(userNameCond));
        
        if (userNameCond != null) {
            builder.and(member.userName.eq(userNameCond));
        }
        
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory.selectFrom(member)
                           .where(builder)
                           .fetch();
    }
    
    @Test
    void whereMultiParams() {
        String userNameParam = "member1";
        Integer ageParam = 10;
        
        List<Member> result = searchMember2(userNameParam, ageParam);
        
        
        assertEquals(1, result.size());
        
        assertEquals("member1", result.get(0).getUserName());
        assertEquals(10, result.get(0).getAge());
    }
    
    /**
     * where절의 null값은 무시됨
     * where절에서 멀티 파라미터는 기본으로 and이다.
     */
    private List<Member> searchMember2(String userNameCond, Integer ageCond) {
        QMember member = QMember.member;
        return queryFactory.selectFrom(member)
                           //.where(userNameEq(userNameCond), ageEq(ageCond))
                           .where(allEq(userNameCond, ageCond))
                           .fetch();
    }
    

    /**
     * BooleanExpression을 반환할 것 - 메소드 체이닝을 위해서
     */
    private BooleanExpression userNameEq(String userNameCond) {
        QMember member = QMember.member;
        if (userNameCond == null) {
            return null;
        }
        return member.userName.eq(userNameCond);
    }
    
    private BooleanExpression ageEq(Integer ageCond) {
        QMember member = QMember.member;
        return ageCond != null ? member.age.eq(ageCond) : null;
    }
    
    private BooleanExpression allEq(String userNameCond, Integer ageCond) {
        return userNameEq(userNameCond).and(ageEq(ageCond));
    }

}
