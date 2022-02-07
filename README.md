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
    - Spring Data Jpa 리포지토리와 Querydsl 리포지토리 병합
    - Querydsl과 Spring Data Jpa 페이징 연동
        - count 쿼리를 생략 가능한 경우 생략해서 처리할 수도 있음
           - org.springframework.data.support.PageableExecutionUtils를 활용
             - 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을때
             - 마지막 페이지 일때 (offset + 컨텐츠 사이즈를 더해서 전체사이즈를 구한다)
    - 정렬 : SpringDataJpa의 정렬을 Querydsl의 정렬(OrderSpecifier)로 변경
        - 단 단순한 엔티티 하나일때는 가능하지만 조인시에는 안되는 방법
        - **조건이 복잡해 지면 별도의 파라미터를 받아서 직접 받아서 처리하는 것을 권장**
    - 번외
       - QuerydslPredicateExecutor
          - Spring Data Jpa 인터페이스를 상속한 리포지토리 인터페이스에서 QuerydslPredicateExecutor를 상속받는다.
          - Spring Data Jpa 인터페이스에서 제공하는 기본 메서드 파라미터에 Querydsl 조건식 사용가능
          - **묵지석 조인은 가능하지만 left join이 불가능하다**
          - **클라이언트가 Querydsl에 의존해야한다. 서비스 클래스가 Querydsl이라는 구현 기술에 의존해야한다**
          - **실무의 복잡한 환경에서 한계가 있음**
       - [Querydsl Web](https://docs.spring.io/spring-data/jpa/docs/2.2.3.RELEASE/reference/html/#core.web.type-safe)
          - **단순 조건만 가능**
          - **컨트롤러가 Querydsl에 의존**
       - QuerydslRepositorySupport
          - 스프링 데이터가 제공하는 페이징을 편리하게 변환 - **(단!Sort는 오류 발생)**
          - 페이징과 카운트 쿼리 분리 가능
          - 스프링 데이터 Sort 지원
          - select() , selectFrom() 으로 시작 가능
          - EntityManager , QueryFactory 제공
          - **Querydsl3.x버전 대상으로 만들었으며 스프링 데이터 Sort기능이 정상 동작하지 않음**
       - Querydsl지원 클래스를 직접만들기
