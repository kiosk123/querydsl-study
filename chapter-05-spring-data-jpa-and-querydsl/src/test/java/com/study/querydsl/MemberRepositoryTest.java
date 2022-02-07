package com.study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.study.querydsl.domain.Member;
import com.study.querydsl.domain.Team;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDTO;
import com.study.querydsl.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;
    
    @Autowired
    MemberRepository memberRepository;
    
    @BeforeEach
    void before() {
        
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
        Member member6 = new Member("member6", 60, team2);
        Member member7 = new Member("member7", 70, team2);
        
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
    void searchInAndConditionUsingBuilder() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        List<MemberTeamDTO> collect = memberRepository.searchByBuilder(condition);
        collect.forEach(System.out::println);
        
        assertEquals(3, collect.size());
        assertThat(collect).extracting("userName")
                           .containsExactly("member4", "member5", "member6");
        
    }    
    
    @Test
    void searchInAndConditionUsingWhereClause() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        List<MemberTeamDTO> collect = memberRepository.searchByWhereClause(condition);
        collect.forEach(System.out::println);
        
        assertEquals(3, collect.size());
        assertThat(collect).extracting("userName")
                           .containsExactly("member4", "member5", "member6");
    }
    
    @Test
    void searchPageSimple() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        Page<MemberTeamDTO> result = memberRepository.searchPageSimple(condition, PageRequest.of(0, 3));
        
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("userName")
                                       .containsExactly("member4", "member5", "member6");
    }
    
    
    @Test
    void searchComplex() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        Page<MemberTeamDTO> result = memberRepository.searchPageComplex(condition, PageRequest.of(0, 3));
        
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("userName")
                                       .containsExactly("member4", "member5", "member6");
        
        assertEquals(3L, result.getTotalElements());
    }
    
    @Test
    void searchOptimal() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        Page<MemberTeamDTO> result = memberRepository.searchPageOptimal(condition, PageRequest.of(0, 3));
        
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("userName")
                                       .containsExactly("member4", "member5", "member6");
        
        assertEquals(3L, result.getTotalElements());
    }
    
    @Test
    void searchPageBySort() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(50);
        condition.setAgeLoe(70);
        condition.setTeamName("team2");
        
        Page<MemberTeamDTO> result = memberRepository.searchPageBySort(condition, PageRequest.of(0, 3, Sort.by(Direction.DESC, "userName")));
        
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("userName")
                                       .containsExactly("member7", "member6", "member5");
        
        assertEquals(3L, result.getTotalElements());
    }
}
