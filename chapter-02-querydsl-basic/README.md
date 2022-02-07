# 예제 도메인 모델과 기본 문법

## 예제 도메인 모델
![.](./img/1.png)  
```java
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {
    
    @Column(name = "TEAM_ID")
    @Id @GeneratedValue
    @Getter
    private Long id;
    
    @Getter @Setter
    private String name;
    
    public Team(String name) {
        this.name = name; 
    }
    
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @Getter
    private List<Member> members = new ArrayList<>();
    
}


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "userName", "age"})
public class Member {
    
    @Column(name = "MEMBER_ID")
    @Id @GeneratedValue
    @Getter
    private Long id;
    
    @Getter @Setter
    private String userName;
    
    @Getter @Setter
    private Integer age;
    
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "TEAM_ID")
    @Getter
    Team team;
    
    
    public Member(String userName) {
        this.userName = userName;
    }
    
    public Member(String userName, Integer age) {
        this(userName);
        this.age = age;
    }
    
    public Member(String userName, Integer age, Team team) {
        this(userName, age);
        this.team = team;
    }
    
    public void changeTeam(Team team) {
        this.team = team;
        if (!team.getMembers().isEmpty() && !Objects.isNull(getId())) {
            team.getMembers().removeIf(m -> getId().equals(m.getId()));
        }
        team.getMembers().add(this);
    }
}
```
## JPLQueryFactory 특징
- 멤버 필드 레벨로 가도 thread-safe하기 때문에 문제 없다.

## 기본 문법
- Q타입 사용
  - **static Alias**를 사용하거나 **Alias로 사용할 값을 직접 생성자 파라미터**에 넘겨서 사용하는 것도 가능
  ```java
  @ActiveProfiles("test")
  @SpringBootTest
  @Transactional
  class AliasTest {
  
      @Autowired
      EntityManager em;
  
      @Test
      void queryDslTest01() {
          Hello hello = new Hello();
          em.persist(hello);
  
          JPAQueryFactory query = new JPAQueryFactory(em);
  
          // 생성자 파라미터로 alias가 들어감 JPQL에서 from Member m에서 m역할
          QHello qHello = new QHello("h");
  
          Hello result = query.selectFrom(qHello).fetchOne();
  
          assertEquals(result, hello);
          assertEquals(hello.getId(), hello.getId());
      }
  
      @Test
      void queryDslTest02() {
          Hello hello = new Hello();
          em.persist(hello);
  
          JPAQueryFactory query = new JPAQueryFactory(em);
  
          // hello 변수가 alias 역할
          QHello qHello = QHello.hello;
  
          Hello result = query.selectFrom(qHello).fetchOne();
  
          assertEquals(result, hello);
          assertEquals(hello.getId(), hello.getId());
      }
  }
  ```
- 검색조건 : 다음과 같이 다양하게 검색조건을 활용가능
  - `.eq(param)` : =
  - `.ne(param)` : !=
  - `.eq(param).not()` : !=
  - `.inNotNull()` : IS NOT NULL
  - `.in(param...)` : in ( a, b, c...)
  - `.notIn(param...)` : not in ( a, b, c...)
  - `.between(a, b)` : between a and b
  - `.goe(param)` : >=
  - `.gt(param)` : >
  - `.loe(param)` : <=
  - `.lt(param)` : <
  - `.like("%string")` : like '%string'
  - `.contains("string")` : like '%string%'	
  - `.startsWith("string")` : like "string%"
  ```java
  @ActiveProfiles("test")
  @SpringBootTest
  @Transactional
  class SearchConditionTest {
  
      @Autowired
      EntityManager em;
      
      JPAQueryFactory queryFactory;
      
      @BeforeEach
      void before() {
          queryFactory = new JPAQueryFactory(em);
          
          //given
          Team team = new Team("team1");
          em.persist(team);
          
          Member member1 = new Member("member1", 20, team);
          Member member2 = new Member("member2", 30, team);
          em.persist(member1);
          em.persist(member2);
          em.flush();
          em.clear();
      }
      
      @Test
      void searchInAndCondition() {
          QMember member = QMember.member;
          Member findMember1 = queryFactory.select(member)
                                          .from(member)
                                          .where(member.userName.eq("member1")
                                                 .and(member.age.eq(20)))
                                          .fetchOne();
          
          Member findMember2 = queryFactory.selectFrom(member) //위와 결과 동일
                                          .where(member.userName.eq("member1")
                                                 .and(member.age.eq(20)))
                                          .fetchOne();
        //and의 경우 체이닝 없이 다음과 같이 가변 파라미터 형식으로 넘기면 묵시적으로 and로 인식하여 and 쿼리로 변환
          Member findMember3 = queryFactory.selectFrom(member) 
                                           .where(member.userName.eq("member1"), member.age.eq(20))
                                           .fetchOne();
          
          assertThat("member1").isEqualTo(findMember1.getUserName());
          assertThat(20).isEqualTo(findMember1.getAge());
          assertEquals(findMember1, findMember2);
          assertEquals(findMember2, findMember3);
      }
      
  }
  ```
- 결과조회
  - `fetch()` : 리스트 조회, 데이터 없으면 빈 리스트 반환
  - `fetchOne()` : 단건 조회
    - 결과가 없으면 : `null`
    - 결과가 둘 이상이면 : `com.querydsl.cor.NonUniqueResultException`
  - `fetchFirst()` : `limit(1).fetchOne()`과 동일
  - `fetchResults()` : 페이징 포함한 객체를 반환 **total count 쿼리 추가 실행** - **페이징 쿼리가 복잡해 지면 사용하지 않을 것을 권장**
  - `fetchOunt()` : count 쿼리로 변경해서 count수 조회
  ```java
  @ActiveProfiles("test")
  @SpringBootTest
  @Transactional
  class ResultOfInquiryTest {
      
      @Autowired
      EntityManager em;
      
      JPAQueryFactory queryFactory;
      
      @BeforeEach
      void before() {
          queryFactory = new JPAQueryFactory(em);
          
          //given
          Team team = new Team("team1");
          em.persist(team);
          
          Member member1 = new Member("member1", 20, team);
          Member member2 = new Member("member2", 30, team);
          em.persist(member1);
          em.persist(member2);
          em.flush();
          em.clear();
      }
      
      @Test
      void fetch() {
          QMember member = QMember.member;
          
          //when
          List<Member> members = queryFactory.selectFrom(member)
                                             .fetch();
          
          assertEquals(2, members.size());
      }
      
      @Test
      void fetchOne() {
          QMember member = QMember.member;
          
          //when
          Member findMember = queryFactory.selectFrom(member)
                                          .where(member.userName.eq("member1"))
                                          .fetchOne();
          
          //then
          assertEquals("member1", findMember.getUserName());
          assertEquals(20, findMember.getAge());
          
          assertThrows(NonUniqueResultException.class, () -> {
              queryFactory.selectFrom(member)
                          .fetchOne();
          });
      }
      
      @Test
      void fetchFirst() {
          QMember member = QMember.member;
          
          //when
          Member findMember = queryFactory.selectFrom(member)
                                          .where(member.userName.eq("member1"))
                                          .fetchFirst(); //limit 1 fetchone
          
          //then
          assertEquals("member1", findMember.getUserName());
          assertEquals(20, findMember.getAge());
      }
      
      @Test
      void fetchResults() {
          QMember member = QMember.member;
          
          //when
          QueryResults<Member> result = queryFactory.selectFrom(member)
                                                    .fetchResults(); 
          
          List<Member> members = result.getResults();
          assertEquals(2, members.size());
          assertEquals(2, result.getTotal());
          
          System.out.println("used limit for query : " + result.getLimit());
          System.out.println("used offset for query : " + result.getOffset());
      }
      
      @Test
      void fetchCount() {
          QMember member = QMember.member;
          
          Long count = queryFactory.selectFrom(member)
                                   .fetchCount(); 
          
          assertEquals(2, count);
      }
  }
  ```
- 정렬 : `orderBy`
```java
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SortingTest {
    
    @Autowired
    EntityManager em;
    
    JPAQueryFactory queryFactory;
    
    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);
        
        //given
        Team team = new Team("team1");
        em.persist(team);
        
        Member member1 = new Member("member1", 20, team);
        Member member2 = new Member("member2", 30, team);
        Member member3 = new Member(null, 30, team);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.flush();
        em.clear();
    }
    
    @Test
    void sortingTest() {
        QMember member = QMember.member;
        List<Member> result = queryFactory.selectFrom(member)
                                          .orderBy(member.age.desc(), member.userName.asc().nullsLast())
                                          .fetch();
        
        assertEquals(3, result.size());
        assertEquals("member2", result.get(0).getUserName());
        assertEquals(null, result.get(1).getUserName());
        assertEquals("member1", result.get(2).getUserName());
    }
}
```
- 페이징 : `offset`, `limit`
```java
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PagingTest {
    
    @Autowired
    EntityManager em;
    
    JPAQueryFactory queryFactory;
    
    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);
        
        //given
        Team team = new Team("team1");
        em.persist(team);
        
        Member member1 = new Member("member1", 20, team);
        Member member2 = new Member("member2", 30, team);
        em.persist(member1);
        em.persist(member2);
        em.flush();
        em.clear();
    }
    
    @Test
    void paging() {
        QMember member = QMember.member;
        
        List<Member> members = queryFactory.selectFrom(member)
                                           .orderBy(member.userName.desc())
                                           .offset(0) //setFirstResult
                                           .limit(2)  //setMaxResult
                                           .fetch();
        assertEquals(2, members.size());
    }
}
```
- 집합 : `sum`, `count`, `avg`, `max`, `min`..., `groupBy`, `having 조건`
- 조인
```java
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class JoinTest {
    
    /**
     * innerJoin(조인대상, 별칭파라미터로 사용할 Q타임)
     * leftJoin(조인대상, 별칭파라미터로 사용할 Q타임)
     * join(조인대상, 별칭파라미터로 사용할 Q타임)
     * 
     * 기본적으로 alias를 이용하는 join은 on절에 식별값 비교가 들어가기 때문에
     * on 메소드를 활용하여 식별값 비교 조건을 추가할 필요가 없지만 (JPQL과 동일)
     * 쎄타조인 같은 경우는 on메소드를 이용하여 미리 조인할 대상을 필터링하는 것이 중요하다.
     */
    @Autowired
    EntityManager em;
    
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
        Member member7 = new Member("team3", 60, null);
        
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
    void innerJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        List<Member> result = queryFactory.select(member)
                                          .from(member)
                                          .innerJoin(member.team, team)
                                          .where(team.name.eq("team1"))
                                          .fetch();
        
        assertEquals(3, result.size());
        result.forEach(m -> {
            if (!m.getTeam().getName().equals("team1")) {
                fail(m.getUserName() + " not in team1");
            }
        });
    }
    
    @Test 
    void innerJoinFilteringUsingOn() {
        // on절을 사용해서 조인 대상을 필터링 한 후 조인할 수 있다.
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        List<Member> result = queryFactory.select(member)
                                         .from(member)
                                         .innerJoin(member.team, team)
                                         .on(team.name.eq("team1"))
                                         .fetch();        
        
        assertEquals(3, result.size());
        result.forEach(m -> {
            if (!m.getTeam().getName().equals("team1")) {
                fail(m.getUserName() + " not in team1");
            }
        });
    }
    
    @Test
    void leftJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        //team1에 속하지 않은 사람 조회
        List<Member> result = queryFactory.select(member)
                                         .from(member)
                                         .leftJoin(member.team, team)
                                         .where(team.name.ne("team1")
                                         .or(team.name.isNull()))
                                         .fetch();
        assertEquals(4, result.size());
        
        assertThat(result).extracting("userName")
                          .containsExactly("member4", "member5", "member6", "team3");
    }
    
    @Test
    void leftJoinFilteringUsingOn() {
        /**
         * 팀 이름이 team1인 팀만 조인, 회원은 모두 조회
         */
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        //팀이름이 팀 team1만 조인 회원은 모두 조회
        List<Tuple> result = queryFactory.select(member, team)
                                          .from(member)
                                          .leftJoin(member.team, team)
                                          .on(team.name.eq("team1"))
                                          .fetch();
        /**
         * Tuple로 Tuple.get(1, Team.class)로 Projection된 팀을 조회했을 때
         * member가 team1인 아닌 Tuple값인 team은 null이된다. 
         * 
         * leftJoinFilteringUsingOn tuple : [Member(id=4, userName=member1, age=10), Team(id=1, name=team1)]
         * leftJoinFilteringUsingOn tuple : [Member(id=5, userName=member2, age=20), Team(id=1, name=team1)]
         * leftJoinFilteringUsingOn tuple : [Member(id=6, userName=member3, age=30), Team(id=1, name=team1)]
         * leftJoinFilteringUsingOn tuple : [Member(id=7, userName=member4, age=40), null]
         * leftJoinFilteringUsingOn tuple : [Member(id=8, userName=member5, age=50), null]
         * leftJoinFilteringUsingOn tuple : [Member(id=9, userName=member6, age=60), null]
         * leftJoinFilteringUsingOn tuple : [Member(id=10, userName=team3, age=60), null]
         */

        result.forEach(t -> System.out.println("leftJoinFilteringUsingOn tuple : " + t));
    }
    
    // 라이트 조인
    @Test
    void rightJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        //아무런 멤버도 없는 팀조회
        List<Team> result = queryFactory.select(team)
                                        .from(member)
                                        .rightJoin(member.team, team)
                                        .where(team.members.size().eq(0))
                                        .fetch();
        assertEquals(1, result.size());
        
        assertThat(result).extracting("name")
                          .containsExactly("team3");
    }
    
    // 쎄타 조인
    @Test
    void thetaJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        Member findMember = queryFactory.select(member)
                                        .from(member, team)
                                        .where(member.userName.eq(team.name))
                                        .fetchOne();
        
        assertEquals("team3", findMember.getUserName());
    }
    
    // 쎄타 아우터 조인
    @Test
    void thetaOuterJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        /**
         * 이때 alias를 뺀다 alias를 넣으면
         * 조인대상에 식별값이 들어가서 식별값으로 매칭하지만
         * alias를 빼면 이름으로만 조인하여 조인대상이 필터링하기 때문에
         * 쎄타 외부 조인은 이렇게 해야한다.
         */
        List<Tuple> result = queryFactory.select(member, team)
                                         .from(member)
                                         .leftJoin(team) 
                                         .on(member.userName.eq(team.name))
                                         .fetch();
        
       assertEquals(7, result.size());
       
       /**
        * thetaOuterJoin tuple : [Member(id=54, userName=member1, age=10), null]
        * thetaOuterJoin tuple : [Member(id=55, userName=member2, age=20), null]
        * thetaOuterJoin tuple : [Member(id=56, userName=member3, age=30), null]
        * thetaOuterJoin tuple : [Member(id=57, userName=member4, age=40), null]
        * thetaOuterJoin tuple : [Member(id=58, userName=member5, age=50), null]
        * thetaOuterJoin tuple : [Member(id=59, userName=member6, age=60), null]
        * thetaOuterJoin tuple : [Member(id=60, userName=team3, age=60), Team(id=53, name=team3)]
        */
       result.forEach(t -> System.out.println("thetaOuterJoin tuple : " + t));
    }
}
```
- `fetch` 조인 : 조회대상의 연관관계 엔티티 필드까지 조회
```java
class FetchJoinTest {
    
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
        Member member7 = new Member("team3", 60, null);
        
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
    void fetchJoin() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        
        List<Member> result = queryFactory.select(member)
                                          .from(member)
                                          .innerJoin(member.team, team)
                                          .fetchJoin()
                                          .where(team.name.eq("team1"))
                                          .fetch();

        
        assertEquals(3, result.size());
        result.forEach(m -> {
            //연관 엔티티가 로딩 되었는지 확인
            if (!emf.getPersistenceUnitUtil().isLoaded(m.getTeam())) {
                fail(m + "'s team is not loaded");
            }
        });
    }
}
```
- 서브쿼리 : `JPAExpressions` 활용
  - `where`와 `select`(하이버네이트 기준)절 서브쿼리 지원한다.
  - **단 from 절 서브쿼리는 지원되지 않는다.(JPA스펙)**
    - `from` 절 서브쿼리는 `join`으로 **변경**하거나, 애플리케이션에서 쿼리를 2번 분리해서 실행하거나, `nativeSQL`을 사용한다.
```java
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SubQueryTest {
    
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
    void subQueryEq() {
        QMember member = QMember.member;
        QMember subMember = new QMember("subMember");
        
        //나이가 가장 많은 회원 조회 - 서브쿼리에서는 alias가 달라야함 일반 SQL사람과 동일
        Member findMember = queryFactory.selectFrom(member)
                                        .where(member.age.eq(
                                               JPAExpressions.select(subMember.age.max())
                                                              .from(subMember)
                                         ))
                                        .fetchOne();
        
        assertEquals(70, findMember.getAge());
        
    }
    
    @Test
    void subQueryIn() {
        QMember member = QMember.member;
        QMember subMember = new QMember("subMember");
        
        //나이가 10살 보다 많은  회원 조회 - 서브쿼리에서는 alias가 달라야함 일반 SQL사람과 동일
        List<Member> members = queryFactory.selectFrom(member)
                                        .where(member.age.in(
                                               JPAExpressions.select(subMember.age)
                                                             .from(subMember)
                                                             .where(subMember.age.gt(10))
                                         ))
                                        .fetch();
        
        assertEquals(6, members.size());
        assertThat(members).extracting("age")
                           .contains(20, 30, 40, 50, 60, 70);
        
    }
    
    @Test
    void subQuerySelectClause() {
        QMember member = QMember.member;
        QMember subMember = new QMember("subMember");
        
        /**
         * select 절에서 서브쿼리 실행
         * [member1, 40.0]
         * [member2, 40.0]
         * [member3, 40.0]
         * [member4, 40.0]
         * [member5, 40.0]
         * [member6, 40.0]
         * [member7, 40.0]
         */
        queryFactory.select(member.userName, JPAExpressions.select(subMember.age.avg())
                                                           .from(subMember))
                    .from(member)
                    .fetch()
                    .forEach(System.out::println);
        
        
        
    }
}
```
- case문
```java
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CaseClauseTest {

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
    void caseClauseSimpleTest() {
        QMember member = QMember.member;
        
        List<String> result = queryFactory.select(member.age.when(10).then("10살")
                                                            .when(20).then("20살")
                                                            .otherwise("기타"))
                                          .from(member)
                                          .fetch();
        
        result.forEach(System.out::println);
        
    }
    
    @Test
    void caseClauseAdvanceTest() {
        QMember member = QMember.member;
        
        queryFactory.select(new CaseBuilder().when(member.age.between(0, 20)).then("0~20살")
                                             .when(member.age.between(21, 30)).then("21~30살")
                                             .otherwise("기타"))
                     .from(member)
                     .fetch()
                     .forEach(System.out::println);
    }
}
```
- 상수와 문자더하기
```java
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ConstantAndStringConcatnTest {

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
    
    // 상수
    @Test
    void constant() {
        QMember member = QMember.member;
        
        List<Tuple> result = queryFactory.select(member.userName, Expressions.constant("A"))
                                         .from(member)
                                         .fetch();
        /*
         * [member1, A]
           [member2, A]
           [member3, A]
           [member4, A]
           [member5, A]
           [member6, A]
           [member7, A]
         */
        result.forEach(System.out::println);
        
    }
    
    // 문자더하기
    @Test
    void concat() {
        QMember member = QMember.member;
        
        List<String> result = queryFactory.select(member.userName.concat("_").concat(member.age.stringValue()))
                                          .from(member)
                                          .fetch();
        /*
         * member1_10
           member2_20
           member3_30
           member4_40
           member5_50
           member6_60
           member7_70
         */
        result.forEach(System.out::println);
    }
}
```