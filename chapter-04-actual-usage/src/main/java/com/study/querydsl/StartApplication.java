package com.study.querydsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StartApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartApplication.class, args);
	}
	
	
//	@Bean
//	JPAQueryFactory jpaQueryFactory(EntityManager em) {
//	    return new JPAQueryFactory(em);
//	}
}
