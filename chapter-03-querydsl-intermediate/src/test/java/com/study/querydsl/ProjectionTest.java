package com.study.querydsl;

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

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.QMember;
import com.study.querydsl.domain.Team;
import com.study.querydsl.dto.MemberDTO;
import com.study.querydsl.dto.QMemberDTO;
import com.study.querydsl.dto.UserDTO;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ProjectionTest {

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
    void projectionTargetOne() {
        QMember member = QMember.member;
        
        List<String> result = queryFactory.select(member.userName)
                                          .from(member)
                                          .fetch();
        
        result.forEach(System.out::println);
        
    }
    
    @Test
    void projectionTargets() {
        QMember member = QMember.member;
        
        List<Tuple> result = queryFactory.select(member.userName, member.age)
                                         .from(member)
                                         .fetch();
        
        result.forEach(t -> {
            System.out.println("userName : " + t.get(member.userName) + ", age : " + t.get(member.age));
            System.out.println("userName : " + t.get(0, String.class) + ", age : " + t.get(1, Integer.class));
        });
    }
    
    @Test
    void projectionUsingDTO() {
        QMember member = QMember.member;
        QMember subMember = new QMember("subMember");
        
        
        /**
         * 인스턴스 생성후(디폴트 생성자 필수)
         * Projections.bean 사용시 settter getter로 접근하여 DTO에 값 설정
         * Q타입 프로퍼티 명과 DTO 필드명 일치해야함
         */
        List<MemberDTO> result = queryFactory.select(Projections.bean(MemberDTO.class, 
                                                                      member.userName, 
                                                                      member.age))
                                             .from(member)
                                             .fetch();
        
        result.forEach(o -> {
            System.out.println("userName : " + o.getUserName() +", age : " + o.getAge());
        });
        
        
        /**
         * Projections.fields 사용시 settter getter 접근이 아닌 필드에 바로 접근하여 DTO에 값 세팅
         * Q타입 프로퍼티 명과 DTO 필드명 일치해야함
         */
        result = queryFactory.select(Projections.fields(MemberDTO.class, 
                                                        member.userName, 
                                                        member.age))
                             .from(member)
                             .fetch();
        
        result.forEach(o -> {
            System.out.println("userName : " + o.getUserName() +", age : " + o.getAge());
        });
        
        /**
         * Projections.constructor 사용시 생성자 파라미터 타입으로 프로젝션한 데이터 타입과 매칭하여 값을 설정
         * 
         */
        result = queryFactory.select(Projections.constructor(MemberDTO.class, 
                                                             member.userName, 
                                                             member.age))
                             .from(member)
                             .fetch();
                                        
        result.forEach(o -> {
            System.out.println("userName : " + o.getUserName() +", age : " + o.getAge());
        });
        
        /**
         * DTO와 Q타입의 프로퍼티명이 일치하지 않을때 as메소드를 활용하여 값을 설정한 DTO 프로퍼티이름으로 별칭을 설정해야함
         * 프로퍼티 명이 일치 하지 않은 DTO의 프로퍼티는 null로 세팅됨
         */
        List<UserDTO> result2 = queryFactory.select(Projections.constructor(UserDTO.class, 
                                                                            member.userName.as("name"), 
                                                                            member.age))
                                            .from(member)
                                            .fetch();
        
        result2.forEach(o -> {
            System.out.println("name : " + o.getName() +", age : " + o.getAge());
        });

        
        /**
         * 서브쿼레에 alias를 설정후 DTO로 값을 설정할 수도 있다.
         */
        result2 = queryFactory.select(Projections.constructor(UserDTO.class, 
                                                              member.userName.as("name"), 
                                                              ExpressionUtils.as(
                                                              JPAExpressions.select(subMember.age.max())
                                                                            .from(subMember),"age")
                                                              ))
                               .from(member)
                               .fetch();
        
        /**
         * name : member1, age : 70
           name : member2, age : 70
           name : member3, age : 70
           name : member4, age : 70
           name : member5, age : 70
           name : member6, age : 70
           name : member7, age : 70
         */
        result2.forEach(o -> {
            System.out.println("name : " + o.getName() +", age : " + o.getAge());
        });

    }
    
    @Test
    void queryProjection() {
        QMember member = QMember.member;
        
        /**
         * @QueryProjection을 DTO생성자에 설정한다.
         * 그레이들 compileQuerydsl 태스크를 실행한다.
         */
        List<MemberDTO> result = queryFactory.select(new QMemberDTO(member.userName, member.age))
                                             .from(member)
                                             .fetch();
        
        result.forEach(o -> {
            System.out.println("userName : " + o.getUserName() +", age : " + o.getAge());
        });
    }
}
