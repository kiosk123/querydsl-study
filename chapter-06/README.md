# 스프링 부트 2.6 이상, Querydsl 5.0 지원 방법

최신 스프링 부트 2.6부터는 Querydsl 5.0을 사용한다.  
스프링 부트 2.6 이상 사용시 다음과 같은 부분을 확인해야 한다.  
1. `build.gradle` 설정 변경  
2. `PageableExecutionUtils` Deprecated(향후 미지원) 패키지 변경  
3. Querydsl `fetchResults()`, `fetchCount()` Deprecated(향후 미지원)  

```gradle
// querydsl-jpa , querydsl-apt 를 추가하고 버전을 명시해야 한다.
buildscript { 
    ext { queryDslVersion = "5.0.0"
    } 
}

plugins { 
    id 'org.springframework.boot' version '2.6.2'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE' 
    id "com.ewerk.gradle.plugins.querydsl" version "1.0.10" //querydsl 추가 
    id 'java'
}

group = 'com.example' 
version = '0.0.1-SNAPSHOT' 
sourceCompatibility = '11'

configurations { 
    compileOnly { 
        extendsFrom annotationProcessor 
    } 
}

repositories { 
    mavenCentral() 
}

dependencies { 
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa' 
    implementation 'org.springframework.boot:spring-boot-starter-web'
    //querydsl 추가 
    implementation "com.querydsl:querydsl-jpa:${queryDslVersion}" 
    annotationProcessor "com.querydsl:querydsl-apt:${queryDslVersion}"

    compileOnly 'org.projectlombok:lombok' runtimeOnly 'com.h2database:h2'
    annotationProcessor 'org.projectlombok:lombok'
    //테스트에서 lombok 사용 
    testCompileOnly 'org.projectlombok:lombok' 
    testAnnotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

test { 
    useJUnitPlatform() 
}

//querydsl 추가 시작 
def querydslDir = "$buildDir/generated/querydsl"
querydsl { 
    jpa = true querydslSourcesDir = querydslDir 
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
//querydsl 추가 끝
```

# PageableExecutionUtils Deprecated(향후 미지원) 패키지 변경
`PageableExecutionUtils` 클래스 사용 패키지 변경  
기능이 Deprecated 된 것은 아니고, 사용 패키지 위치가 변경됨. 기존 위치를 신규 위치로 변경해주시면 문제 없이 사용할 수 있음  
**기존**: `org.springframework.data.repository.support.PageableExecutionUtils`  
**신규**: `org.springframework.data.support.PageableExecutionUtils`  

# Querydsl `fetchResults()` , `fetchCount()` Deprecated(향후 미지원)
Querydsl의 `fetchCount()` , `fetchResult()` 는 개발자가 작성한 select 쿼리를 기반으로 count용쿼리를 내부에서 만들어서 실행.  
  
그런데 이 기능은  select 구문을 단순히 count 처리하는 용도로 바꾸는 정도.  
  
따라서 단순한 쿼리에서는 잘 동작하지만, 복잡한 쿼리에서는 제대로 동작하지 않는다.  
  
Querydsl은 향후 `fetchCount()` , `fetchResult()` 를 지원하지 않기로 결정.  
참고로 Querydsl의 변화가 빠르지는 않기 때문에 당장 해당 기능을 제거하지는 않을 예정.  
  
따라서 **count 쿼리가 필요하면 다음과 같이 별도로 작성**.  
```java
import org.springframework.data.support.PageableExecutionUtils; //패키지 변경

public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
    List<MemberTeamDto> content = queryFactory
            .select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age, team.id.as("teamId"),
                    team.name.as("teamName")))
            .from(member).leftJoin(member.team, team)
            .where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()))
            .offset(pageable.getOffset()).limit(pageable.getPageSize())
            .fetch();

    JPAQuery<Long> countQuery = queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamName()), 
                        ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()));
            
    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
}
```
