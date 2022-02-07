# 중급 문법
- 프로젝션
  - `일반타입`, `Tuple`, `DTO` 방식으로 조회 
    - `@QueryProjection` 활용 : `Projections.constructor`가 런타임에서 오류를 발견할 수 있는 반면에 `@QueryProjection`은 컴파일 타임에 오류를 발견할 수 있음
      - `DTO` 생성자에 `@QueryProjection`을 설정
      ```java
      @Data
      public class MemberDTO {
      
          private String userName;
          private int age;
      
          public MemberDTO() { } // querydsl 사용시 필수
      
          @QueryProjection
          public MemberDTO(String userName, int age) {
              this.userName = userName;
              this.age = age;
          }
      }
      ```
      - 그레이들 `compileQuerydsl` 태스크 실행 -> `Q이름 DTO` 생성
      - 생성된 `Q이름 DTO`를 **select절**에서 **new 키워드를 사용하여 생성** 후 처리
      - `@QueryProjection`을 사용할 경우 **단점**은 `@QueryProjection`을 사용한 `DTO`는 **Querydsl 라이브러리에 대한 의존성이 생김**
      - **차후에 Querydsl을 사용안한다면 코드 수정 및 운영하는데 문제 발생할 가능성은 있음**
      - **DTO를 순수하게 가져가냐 아니면 실용성 있게 가져가냐에 따라서 사용여부가 결정될 듯**
  - 프로젝션 예제
  ```java
  @ActiveProfiles("test")
  @SpringBootTest
  @Transactional
  class ProjectionTest {
  
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
      
      @DisplayName("컬럼 하나만 프로젝션")
      @Test
      void projectionTargetOne() {
          QMember member = QMember.member;
          
          List<String> result = queryFactory.select(member.userName)
                                            .from(member)
                                            .fetch();
          
          result.forEach(System.out::println);
          
      }
      
      @DisplayName("컬럼 하나여러개 프로젝션 Tuple 사용")
      @Test
      void projectionTargets() {
          QMember member = QMember.member;
          
          List<Tuple> result = queryFactory.select(member.userName, member.age)
                                           .from(member)
                                           .fetch();
          
          result.forEach(t -> {
              System.out.println("userName : " + t.get(member.userName) + ", age : " + t.get(member.age)); // Q타입 클래스의 프로퍼티를 이용하여 값을 가져오기
              System.out.println("userName : " + t.get(0, String.class) + ", age : " + t.get(1, Integer.class)); // 위치와 타입으로 값 가져오기
          });
      }
      
      @DisplayName("Projections를 이용하여 DTO로 프로젝션")
      @Test
      void projectionUsingDTO() {
          QMember member = QMember.member;
          QMember subMember = new QMember("subMember");
          
          
          /**
           * 인스턴스 생성후(디폴트 생성자 필수)
           * Projections.bean 사용시 setter 메서드로 접근하여 DTO에 값 설정
           * Q타입 프로퍼티 명과 DTO 필드명 일치해야함
           */
          List<MemberDTO> result = queryFactory.select(Projections.bean(MemberDTO.class, 
                                                                        member.userName, 
                                                                        member.age))
                                               .from(member)
                                               .fetch();
          
          result.forEach(o -> {
              System.out.println("userName : " + o.getUserName() +", age : " + o.getAge());
          });
          
          
          /**
           * Projections.fields 사용시 setter 메서드로 접근이 아닌 필드에 바로 접근하여 DTO에 값 세팅
           * Q타입 프로퍼티 명과 DTO 필드명 일치해야함
           */
          result = queryFactory.select(Projections.fields(MemberDTO.class, 
                                                          member.userName, 
                                                          member.age))
                               .from(member)
                               .fetch();
          
          result.forEach(o -> {
              System.out.println("userName : " + o.getUserName() +", age : " + o.getAge());
          });
          
          /**
           * Projections.constructor 사용시 생성자 파라미터 타입으로 프로젝션한 데이터 타입과 매칭하여 값을 설정
           * 
           */
          result = queryFactory.select(Projections.constructor(MemberDTO.class, 
                                                               member.userName, 
                                                               member.age))
                               .from(member)
                               .fetch();
                                          
          result.forEach(o -> {
              System.out.println("userName : " + o.getUserName() +", age : " + o.getAge());
          });
          
          /**
           * Projections.fields 사용시
           * DTO와 Q타입의 프로퍼티명이 일치하지 않을때 as메소드를 활용하여 값을 설정한 DTO 프로퍼티이름으로 별칭을 설정해야함
           * 프로퍼티 명이 일치 하지 않은 DTO의 프로퍼티는 null로 세팅됨
           */
          List<UserDTO> result2 = queryFactory.select(Projections.fields(UserDTO.class, 
                                                                         member.userName.as("name"), 
                                                                         member.age))
                                              .from(member)
                                              .fetch();
          
          result2.forEach(o -> {
              System.out.println("name : " + o.getName() +", age : " + o.getAge());
          });
  
          
          /**
           * ExpressionUtils.as를 이용하여 서브쿼리에 alias를 설정후 DTO로 값을 설정할 수도 있다.
           */
          result2 = queryFactory.select(Projections.constructor(UserDTO.class, 
                                                                member.userName.as("name"), 
                                                                ExpressionUtils.as(
                                                                JPAExpressions.select(subMember.age.max())
                                                                              .from(subMember),"age")
                                                                ))
                                 .from(member)
                                 .fetch();
          
          /**
           * name : member1, age : 70
             name : member2, age : 70
             name : member3, age : 70
             name : member4, age : 70
             name : member5, age : 70
             name : member6, age : 70
             name : member7, age : 70
           */
          result2.forEach(o -> {
              System.out.println("name : " + o.getName() +", age : " + o.getAge());
          });
  
      }
      
      @DisplayName("@QueryProjection를 이용한 DTO 프로젝션")
      @Test
      void queryProjection() {
          QMember member = QMember.member;
          
          /**
           * @QueryProjection을 DTO생성자에 설정한다.
           * 그레이들 compileQuerydsl 태스크를 실행한다.
           */
          List<MemberDTO> result = queryFactory.select(new QMemberDTO(member.userName, member.age))
                                               .from(member)
                                               .fetch();
          
          result.forEach(o -> {
              System.out.println("userName : " + o.getUserName() +", age : " + o.getAge());
          });
      }
  }
  ```
- 동적쿼리
  - `BooleanBuilder`
  - where 다중 파라미터
```java
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class DynamicQueryTest {

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
    
    @DisplayName("BooleanBuilder를 활용한 동적 쿼리")
    @Test
    void booleanBuilder() {
        String userNameParam = "member1";
        Integer ageParam = 10;
        
        List<Member> result = searchMember(userNameParam, ageParam);
        
        
        assertEquals(1, result.size());
        
        assertEquals("member1", result.get(0).getUserName());
        assertEquals(10, result.get(0).getAge());
    }
    
    private List<Member> searchMember(String userNameCond, Integer ageCond) {
        QMember member = QMember.member;
        BooleanBuilder builder = new BooleanBuilder();
        
        // 초기값 설정 - 파라미터 값이 반드시 NULL이 아니어야함
        // BooleanBuilder builder = new BooleanBuilder(member.userName.eq(userNameCond));
        
        if (userNameCond != null) {
            builder.and(member.userName.eq(userNameCond));
        }
        
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory.selectFrom(member)
                           .where(builder)
                           .fetch();
    }
    
    @DisplayName("where 다중 파라미터 동적 쿼리")
    @Test
    void whereMultiParams() {
        String userNameParam = "member1";
        Integer ageParam = 10;
        
        List<Member> result = searchMember2(userNameParam, ageParam);
        
        
        assertEquals(1, result.size());
        
        assertEquals("member1", result.get(0).getUserName());
        assertEquals(10, result.get(0).getAge());
    }
    
    /**
     * where절의 null값은 무시됨
     * where절에서 멀티 파라미터는 기본으로 and이다.
     */
    private List<Member> searchMember2(String userNameCond, Integer ageCond) {
        QMember member = QMember.member;
        return queryFactory.selectFrom(member)
                           //.where(userNameEq(userNameCond), ageEq(ageCond))
                           .where(allEq(userNameCond, ageCond))
                           .fetch();
    }
    

    /**
     * BooleanExpression을 반환할 것 - 메소드 체이닝을 위해서
     */
    private BooleanExpression userNameEq(String userNameCond) {
        QMember member = QMember.member;
        if (userNameCond == null) {
            return null;
        }
        return member.userName.eq(userNameCond);
    }
    
    private BooleanExpression ageEq(Integer ageCond) {
        QMember member = QMember.member;
        return ageCond != null ? member.age.eq(ageCond) : null;
    }
    
    private BooleanExpression allEq(String userNameCond, Integer ageCond) {
        return userNameEq(userNameCond).and(ageEq(ageCond));
    }
}
```
- BooleanBuilder를 이용하여 NULL 걱정 없이 체이닝 하기
```java
private BooleanBuilder ageEq(Integer ageCond) {
    return Objects.isNull(ageCond) ? new BooleanBuilder() : new BooleanBuilder(member.age.eq(ageCond));
}


private BooleanBuilder userNameEq(String userName) {
    return (!StringUtils.hasText(userName)) ? new BooleanBuilder() : new BooleanBuilder(member.userName.eq(userName));
}

private BooleanBuilder allEq(String userNameCond, Integer ageCond) {
    return userNameEq(userNameCond).and(ageEq(ageCond))
}
```
- 벌크연산 : 수정 삭제 벌크 연산
- SQL Function 호출
  - JPA와 같이 Dialect에 등록된 Function만 호출가능
  - **기본적으로 ANSI 표준함수들은 Querydsl에서 메소드로 정의**되어 있음
  - 특정 데이터베이스에서만 사용가능한 함수나 사용자 정의 함수를 호출할 때 사용