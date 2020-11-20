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
class GroupByFunctionTest {
    
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
        Member member2 = new Member("member1", 30, team);
        Member member3 = new Member("member2", 30, team);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.flush();
        em.clear();
    }
    
    @Test
    void functions() {
        QMember member = QMember.member;
        List<Tuple> result = queryFactory.select(member.count(), 
                                                 member.age.sum(), 
                                                 member.age.avg(), 
                                                 member.age.max(), 
                                                 member.age.min())
                                         .from(member)
                                         .fetch();
        
        Tuple tuple = result.get(0);
        Long count = tuple.get(0, Long.class);
        Integer sum = tuple.get(1, Integer.class);
        Double avg = tuple.get(2, Double.class);
        Integer max = tuple.get(3, Integer.class);
        Integer min = tuple.get(4, Integer.class);
        
        assertEquals(3, count);
        assertEquals(80, sum);
        assertEquals((30.0 + 30.0 + 20.0)/3.0, avg);
        assertEquals(30, max);
        assertEquals(20, min);
    }
    
    @Test 
    void groupByFunctions() {
        QMember member = QMember.member;
        List<Tuple> result = queryFactory.select(member.count(), 
                                                 member.age.sum(), 
                                                 member.age.avg(), 
                                                 member.age.max(), 
                                                 member.age.min())
                                         .from(member)
                                         .groupBy(member.userName)
                                         .having(member.age.sum().goe(50))
                                         .fetch();

        
        Tuple tuple = result.get(0);
        Long count = tuple.get(0, Long.class);
        Integer sum = tuple.get(1, Integer.class);
        Double avg = tuple.get(2, Double.class);
        Integer max = tuple.get(3, Integer.class);
        Integer min = tuple.get(4, Integer.class);
        
        assertEquals(1, result.size());
        assertEquals(2, count);
        assertEquals(50, sum);
        assertEquals((20.0 + 30.0)/2.0, avg);
        assertEquals(30, max);
        assertEquals(20, min);
    }
}
