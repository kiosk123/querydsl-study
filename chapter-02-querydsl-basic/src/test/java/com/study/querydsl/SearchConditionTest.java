package com.study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
class SearchConditionTest {

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
    }
    
    @Test
    void searchInAndCondition() {
        QMember member = QMember.member;
        Member findMember1 = queryFactory.select(member)
                                        .from(member)
                                        .where(member.userName.eq("member1")
                                               .and(member.age.eq(20)))
                                        .fetchOne();
        
        Member findMember2 = queryFactory.selectFrom(member) //위와 결과 동일
                                        .where(member.userName.eq("member1")
                                               .and(member.age.eq(20)))
                                        .fetchOne();
      //and의 경우 체이닝 없이 다음과 같이 가변 파라미터 형식으로 넘기면 묵시적으로 and로 인식하여 and 쿼리로 변환
        Member findMember3 = queryFactory.selectFrom(member) 
                                         .where(member.userName.eq("member1"), member.age.eq(20))
                                         .fetchOne();
        
        assertThat("member1").isEqualTo(findMember1.getUserName());
        assertThat(20).isEqualTo(findMember1.getAge());
        assertEquals(findMember1, findMember2);
        assertEquals(findMember2, findMember3);
    }
    
}
