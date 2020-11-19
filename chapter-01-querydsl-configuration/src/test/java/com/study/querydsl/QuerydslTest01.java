package com.study.querydsl;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.domain.Hello;

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
	    
	    em.flush();
	    em.clear();
	    
	    JPAQueryFactory query = new JPAQueryFactory(em);
	}
}
