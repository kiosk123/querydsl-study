# Querydsl 공부
* Querydsl에 대한 공부를 정리한 것으로 사용방법은 리포지토리 클래스와 테스트케이스를 참고  

## 구성정보
* JPA 2.2
* java 버전 11


## 스프링 부트 프로젝트 구성하기
* [Spring Initializr 사이트 활용](https://start.spring.io/)

## API 테스트
* [POSTMAN](https://www.postman.com/)
* [Katalon](https://www.katalon.com/)

## 현재 프로젝트의 의존관계 보기 gradlew 이용
```bash
./gradlew dependencies —configuration compileClasspath
```
## 참고사이트
 - [Spring 가이드 문서](https://spring.io/guides)
 - [Spring Boot 참고 문서](https://docs.spring.io/spring-boot/docs/)
 - [쿼리 파라미터 로그 남기기](https://github.com/gavlyukovskiy/spring-boot-data-source-decorator)
    - 그레이들에서 다음과 같이 설정
    - `implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.6'`
    - 운영에서는 사용하지 말 것
    
 - [테스트시 Unable to Find @SpringBootConfiguration 오류 해결 ](https://www.baeldung.com/spring-boot-unable-to-find-springbootconfiguration-with-datajpatest)
    - 테스트시 `@SpringBootApplication`이 설정된 클래스의 패키지와 같은 경로거나 하위 경로로 패키지 경로를 맞춰줘야 실행됨

## 스프링 부트 JPA 매핑 테이블및 컬럼 네이밍 규칙
 - 스프링 부트에서 엔티티명과 프로퍼티가 JPA 테이블과 테이블 컬럼과 매핑될때 기본적으로 카멜케이스 + 언더스코어 전략을 사용한다.
    - ex) 프로퍼티명이`userName`일 경우 -> `USER_NAME` 컬럼과 매핑
 - 카멜케이스 + 언더스코어 전략을 사용하지 않을 경우 `application.yml` 파일 옵션에 다음과 같이 설정한다. [참고](https://www.baeldung.com/hibernate-field-naming-spring-boot)

```yml
spring:
  jpa:
    hibernate:
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
        implicit-strategy: org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl
```
    

## 챕터별 설명
- 챕터 1 : 프로젝트 구성
- 챕터 2 : 기본 문법
- 챕터 3 : 중급 문법
- 챕터 4 : 실무 활용
- 챕터 5 : Spring Data Jpa와 Querydsl 혼합 사용

## 스프링 부트 2.6.x 이상 QueryDSL 버전 5.0 이상 설청
- 첨부된 **실전Querydsl v2022-01-12.pdf** 파일을 확인