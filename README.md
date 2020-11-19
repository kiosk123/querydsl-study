# Spring Data JPA 공부
* Spring Data JPA에 대한 공부를 정리한 것으로 사용방법은 리포지토리 클래스와 테스트케이스를 참고  

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
    - @테스트시 @SpringBootApplication이 설정된 클래스의 패키와 같은 경로거나 하위 경로로 패키지 경로를 맞춰줘야 실행됨

## 스프링 부트 JPA 매핑 테이블및 컬럼 네이밍 규칙
 - 스프링 부트에서 엔티티명과 프로퍼티가 JPA 테이블과 테이블 컬럼과 매핑될때 기본적으로 카멜케이스 + 언더스코어 전략을 사용한다.
    - ex) 프포퍼티명이 userName일 경우 -> USER_NAME 컬럼과 매핑
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
    - Querydsl은 오픈소스 프로젝트이므로 다운받은 프로젝트에서 별도의 구성이 필요한다. 
       - 아래의 querydsl build.gradle 설정을 참고하라
    - 프로젝트 구성 후 엔티트를 작성하고 나면 Querydsl이 인식할 수 있는 Q클래스를 만들어야한다.
    - 그레이들 태스크에서 other -> complieQuerydsl을 실행한다.
       - gradlew를 직접 사용하려면 프로젝트 폴터에서 다음과 같이 입력한다
            - ./gradlew clean # Q타입클래스를 버롯해서 컴파일되거나 빌드된 파일 전부다 삭제
           - ./gradlew comipleQuerydsl # Q타입클래스 생성 compileJava로도 가능함
    - **참고로 querydsl용으로 생성된 Q클래스는 계속 변화될 가능성이 높기 때문에 git 커밋 하지 말것을 권장** 
       - **.gitignore에 build 폴더를 예외대상으로하고 Q타입 클래스는 build폴더에서 생성해서 사용하는 것을 권장**
       - **다른 디렉터리에 빌드할꺼면 그 폴더자체를 .gitignore 대상으로 해야한다 **
    - 이클립스에서는 Q클래스가 생성되는 디렉터리를 프로젝트의 빌드 패스에 추가해야한다
       - 프로젝트에서 오른쪽 마우스 버튼 클릭
       - Build Path -> Configure Build Path -> Source 탭 이동 후 다음 그림과 같이 설정
    
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

