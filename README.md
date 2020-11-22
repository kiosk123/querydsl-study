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

## 참고사이트
 - [Spring 가이드 문서](https://spring.io/guides)
 - [Spring Boot 참고 문서](https://docs.spring.io/spring-boot/docs/)
 - [쿼리 파라미터 로그 남기기](https://github.com/gavlyukovskiy/spring-boot-data-source-decorator)
    - 그레이들에서 다음과 같이 설정
    - implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.6' 
    - 운영에서는 사용하지 말 것
    
 - [테스트시 Unable to Find @SpringBootConfiguration 오류 해결 ](https://www.baeldung.com/spring-boot-unable-to-find-springbootconfiguration-with-datajpatest)
    - 테스트시 @SpringBootApplication이 설정된 클래스의 패키와 같은 경로거나 하위 경로로 패키지 경로를 맞춰줘야 실행됨

## 스프링 부트 JPA 매핑 테이블및 컬럼 네이밍 규칙
 - 스프링 부트에서 엔티티명과 프로퍼티가 JPA 테이블과 테이블 컬럼과 매핑될때 기본적으로 카멜케이스 + 언더스코어 전략을 사용한다.
    - ex) 프로퍼티명이 userName일 경우 -> USER_NAME 컬럼과 매핑
 - 카멜케이스 + 언더스코어 전략을 사용하지 않을 경우 application.yml 파일 옵션에 다음과 같이 설정한다. [참고](https://www.baeldung.com/hibernate-field-naming-spring-boot)

```
spring:
  jpa:
    hibernate:
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
        implicit-strategy: org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl
```
    

## 챕터별 설명
 - 챕터 1 : 프로젝트 구성
    - [Spring Initializr](https://start.spring.io/)에서 스프링 부트 기본 프로젝트를 구성한다.
    - **Querydsl은 오픈소스 프로젝트이므로 다운받은 프로젝트에서 별도의 구성이 필요한다.**
       - **아래의 querydsl build.gradle 설정을 참고하라**
    - 프로젝트 구성 후 엔티트를 작성하고 나면 Querydsl이 인식할 수 있는 Q클래스를 만들어야한다.
    - 그레이들 태스크에서 other -> complieQuerydsl을 실행한다.
       - gradlew를 직접 사용하려면 프로젝트 폴터에서 다음과 같이 입력한다
            - ./gradlew clean # Q타입클래스를 버롯해서 컴파일되거나 빌드된 파일 전부다 삭제
           - ./gradlew comipleQuerydsl # Q타입클래스 생성 compileJava로도 가능함
    - **참고로 querydsl용으로 생성된 Q클래스는 계속 변화될 가능성이 높기 때문에 git 커밋 하지 말것을 권장** 
       - **.gitignore에 build 폴더를 예외대상으로하고 Q타입 클래스는 build폴더에서 생성해서 사용하는 것을 권장**
       - **다른 디렉터리에 빌드할꺼면 그 폴더자체를 .gitignore 대상으로 해야한다**
    - 이클립스에서는 Q클래스가 생성되는 디렉터리를 프로젝트의 빌드 패스에 추가해야한다
       - 프로젝트에서 오른쪽 마우스 버튼 클릭
       - Build Path -> Configure Build Path -> Source 탭 이동 후 다음 그림 순으로 설정
![스텝1](https://github.com/kiosk123/querydsl-study/blob/master/%EC%86%8C%EC%8A%A4%ED%83%AD.png)
![스텝2](https://github.com/kiosk123/querydsl-study/blob/master/%EC%86%8C%EC%8A%A4%ED%8F%B4%EB%8D%94%EC%84%A0%ED%83%9D.png)
    
```
/*Querydsl build.gradle 설정*/
plugins {
    //생략..
	//querydsl gradle 플러그인
	id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
}
//생략...
dependencies {
    //생략..
	//querydsl 추가
	implementation 'com.querydsl:querydsl-jpa'
}

//생략..
//querydsl 추가 시작
def querydslDir = "$buildDir/generated/querydsl"

querydsl {
	jpa = true
	querydslSourcesDir = querydslDir
}
sourceSets {
	main.java.srcDir querydslDir
}

configurations {
	querydsl.extendsFrom compileClasspath
}

compileQuerydsl {
	options.annotationProcessorPath = configurations.querydsl
}
```

 - 챕터 2 : 기본 문법
    - Q타입 사용
        - static Alias를 사용하거나 Alias를 직접 생성자 파라미터에 넘겨서 사용하는 것도 가능
    - 검색조건 : 다음과 같이 다양하게 검색조건을 활용가능
        - <target property>.eq(param) : =
        - <target property>.ne(param) : !=
        - <target property>.eq(param).not() : !=
        - <target property>.inNotNull() : IS NOT NULL
        - <target property>.in(param...) : in ( a, b, c...)
        - <target property>.notIn(param...) : not in ( a, b, c...)
        - <target property>.between(a, b) : between a and b
        - <target property>.goe(param) : >=
        - <target property>.gt(param) : >
        - <target property>.loe(param) : <=
        - <target property>.lt(param) : <
        - <target property>.like("%string") : like '%string'
        - <target property>.contains("string") : like '%string%'	
        - <target property>.startsWith("string") : like "string%"
    - 결과조회
        - fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
        - fetchOne() : 단건 조회
            - 결과가 없으면 : null
            - 결과가 둘 이상이면 : com.querydsl.cor.NonUniqueResultException
        - fetchFirst() : limit(1).fetchOne()
        - fetchResults() : 페이징 포함한 객체를 반환 total count 쿼리 추가 실행 - 페이징 쿼리가 복잡해 지면 사용하지 않을 것을 권장
        - fetchOunt() : count 쿼리로 변경해서 count수 조회
    - 정렬 : orderBy
    - 페이징 : offset, limit
    - 집합 : sum, count, avg, max, min..., groupBy, having 조건
    - 조인
    - fetch 조인 : 조회대상의 연관관계 엔티티 필드까지 조회
    - 서브쿼리 : JPAExpressions 활용
        - where와 select(하이버네이트 기준)절 서브쿼리 지원한다.
        - **단 from 절 서브쿼리는 지원되지 않는다.(JPA스펙)**
            - from 절 서브쿼리는 join으로 변경하거나, 애플리케이션에서 쿼리를 2번 분리해서 실행하거나, nativeSQL을 사용한다.
    - case문
    - 상수와 문자더하기
 - 챕터 3 : 중급 문법
    - 프로젝션
        - 일반타입, Tuple, DTO 변환
        - @QueryProjection 활용 : Projections.constructor가 런타임에서 오류를 발견할 수 있는 반면에 @QueryProjection은 컴파일 타임에 오류를 발견할 수 있음
            - DTO 생성자에 @QueryProject을 설정
            - 그레이들 compileQuerydsl 태스크 실행 -> Q이름 DTO 생성
            - 생성된 Q이름 DTO를 select절에서 new 키워드를 사용하여 생성 후 처리
        - @QueryProjection을 사용할 경우 단점은 @queryProjections을 사용한 DTO는 Querydsl 라이브러리에 대한 의존성이 생김
            - 차후에 Querydsl을 사용안한다면 코드 수정 및 운영하는데 문제 발생할 가능성은 있음
            - DTO를 순수하게 가져가냐 아니면 실용성 있게 가져가냐에 따라서 사용여부가 결정될 듯
    - 동적쿼리
        - BooleanBuilder
        - where 다중 파라미터
    - 벌크연산 : 수정 삭제 벌크 연산
    - SQL Function 호출
        - JPA와 같이 Dialect에 등록된 Function만 호출가능
        - 기본적으로 ANSI 표준함수들은 Querydsl에서 메소드로 정의되어 있음
        - 특정 데이터베이스에서만 사용가능한 함수나 사용자 정의 함수를 호출할 때 사용
 - 챕터 4 : 실무 활용
    - 순수 Querydsl로 리포지 토리 구성
    - 동적 쿼리와 성능 최적화 조회
        - **동적 쿼리 작성시에는 조건이 아무것도 없으면 데이터 전체를 끌어오는 현상이 발생할 수 있기 때문에 기본 조건이라도(최소한 limit)넣어서 최적화를 해주는 것이 좋다**
    - 조회 API 컨트롤러
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