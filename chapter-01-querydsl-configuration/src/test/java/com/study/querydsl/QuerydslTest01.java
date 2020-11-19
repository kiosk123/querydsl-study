package com.study.querydsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Hello;
import com.study.querydsl.domain.QHello;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class QuerydslTest01 {

    @Autowired
    EntityManager em;
    
	@Test
	void contextLoads() {
	    Hello hello = new Hello();
	    em.persist(hello);
	    
	    JPAQueryFactory query = new JPAQueryFactory(em);
	    
	    //생성자 파라미터로 alias가 들어감 JPQL에서 from Member m에서 m역할
	    QHello qHello = new QHello("h"); 
	    
	    Hello result = query.selectFrom(qHello)
	                        .fetchOne();
	    
	    assertEquals(result, hello);
	    assertEquals(hello.getId(), hello.getId());
	}
}
