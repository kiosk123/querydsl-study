# Spring Data Jpa와 Querydsl 혼합 사용

- Spring Data Jpa 리포지토리와 Querydsl 리포지토리 병합
  - Querydsl과 Spring Data Jpa 페이징 연동
    - **count 쿼리를 생략 가능한 경우 생략**해서 처리할 수도 있음
      - `org.springframework.data.support.PageableExecutionUtils`를 활용
        - 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을때
        - 마지막 페이지 일때 (**offset + 컨텐츠 사이즈를 더해서 전체사이즈를 구한다**)
 - 정렬 : SpringDataJpa의 정렬을 Querydsl의 정렬(`OrderSpecifier`)로 변경
   - 단 **단순한 엔티티 하나일때는 가능하지만 조인시에는 안되는 방법**
   - **조건이 복잡해 지면 별도의 파라미터를 받아서 직접 받아서 처리하는 것을 권장**

## 예제

`MemberRepository` Spring Data JPA 리포지토리
```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    List<Member> findByUserName(String userName);
}
```
**사용자 정의 리포지토리 사용법**  
1. 사용자 정의 인터페이스 작성  
2. 사용자 정의 인터페이스 구현  
3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속  
![.](./img/1.png)  

`MemberRepositoryCustom`
```java
public interface MemberRepositoryCustom {
    List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condition);
    List<MemberTeamDTO> searchByWhereClause(MemberSearchCondition condition);
    
    //페이징
    Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
    
    //total count 최적화
    Page<MemberTeamDTO> searchPageOptimal(MemberSearchCondition condition, Pageable pageable);
    
    //sort
    Page<MemberTeamDTO> searchPageBySort(MemberSearchCondition condition, Pageable pageable);
}
```
`MemberRepositoryCustom`를 구현한 `MemberRepositoryImpl`
```java
//...
import static com.study.querydsl.domain.QMember.member;
import static com.study.querydsl.domain.QTeam.team;

//...
@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em); // 스프링 빈으로 등록해서 처리해도됨
    }

    @Override
    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDTO> results = 
                queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()), 
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDTO> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 데이터 가져오는 쿼리와 Total count 쿼리를 분리 - 데이터가 많을 때 최적화를 위해 사용할 것을 추천
     */
    @Override
    public Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> content = 
                queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()), 
                       ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = 
                queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()), 
                       ageLoe(condition.getAgeLoe()))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * total count 최적화
     */
    @Override
    public Page<MemberTeamDTO> searchPageOptimal(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> content = 
                queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()), 
                       ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        /**
         * PageableExecutionUtils.getPage()로 최적화
         * 
         * 스프링 데이터 라이브러리가 제공
         * count 쿼리가 생략 가능한 경우 생략해서 처리
         * - 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
         * - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)
         */
        JPAQuery<Member> countQuery = queryFactory
                    .select(member)
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(userNameEq(condition.getUserName()), 
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()), 
                           ageLoe(condition.getAgeLoe()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * sorting (정렬)
     */
    @Override
    public Page<MemberTeamDTO> searchPageBySort(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<MemberTeamDTO> query = 
                queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .where(userNameEq(condition.getUserName()), 
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()), 
                        ageLoe(condition.getAgeLoe()));
        
        pageable.getSort().forEach(sort -> {
            Sort.Order o = sort;
            PathBuilder pathBuilder = new PathBuilder(member.getType(), member.getMetadata());
            query.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
        });
        
        List<MemberTeamDTO> content = query.fetch();
        
        return PageableExecutionUtils.getPage(content, pageable, () -> {
            return queryFactory
                    .select(member)
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(userNameEq(condition.getUserName()), 
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()), 
                           ageLoe(condition.getAgeLoe()))
                    .fetchCount();
        });
    }

    @Override
    public List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(condition.getUserName())) {
            builder.and(member.userName.eq(condition.getUserName()));
        }

        if (StringUtils.hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if (!Objects.isNull(condition.getAgeGoe())) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if (!Objects.isNull(condition.getAgeLoe())) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }
        return queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    @Override
    public List<MemberTeamDTO> searchByWhereClause(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()), 
                       ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return Objects.isNull(ageLoe) ? null : member.age.loe(ageLoe);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return Objects.isNull(ageGoe) ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression teamNameEq(String teamName) {
        return (!StringUtils.hasText(teamName)) ? null : team.name.eq(teamName);
    }

    private BooleanExpression userNameEq(String userName) {
        return (!StringUtils.hasText(userName)) ? null : member.userName.eq(userName);
    }
}

```
테스트 코드
```java
class MemberRepositoryTest {

    @Autowired
    EntityManager em;
    
    @Autowired
    MemberRepository memberRepository;
    
    @BeforeEach
    void before() {
        
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
        Member member6 = new Member("member6", 60, team2);
        Member member7 = new Member("member7", 70, team2);
        
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
    void searchInAndConditionUsingBuilder() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        List<MemberTeamDTO> collect = memberRepository.searchByBuilder(condition);
        collect.forEach(System.out::println);
        
        assertEquals(3, collect.size());
        assertThat(collect).extracting("userName")
                           .containsExactly("member4", "member5", "member6");
        
    }    
    
    @Test
    void searchInAndConditionUsingWhereClause() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        List<MemberTeamDTO> collect = memberRepository.searchByWhereClause(condition);
        collect.forEach(System.out::println);
        
        assertEquals(3, collect.size());
        assertThat(collect).extracting("userName")
                           .containsExactly("member4", "member5", "member6");
    }
    
    @Test
    void searchPageSimple() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        Page<MemberTeamDTO> result = memberRepository.searchPageSimple(condition, PageRequest.of(0, 3));
        
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("userName")
                                       .containsExactly("member4", "member5", "member6");
    }
    
    
    @Test
    void searchComplex() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        Page<MemberTeamDTO> result = memberRepository.searchPageComplex(condition, PageRequest.of(0, 3));
        
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("userName")
                                       .containsExactly("member4", "member5", "member6");
        
        assertEquals(3L, result.getTotalElements());
    }
    
    @Test
    void searchOptimal() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(60);
        condition.setTeamName("team2");
        
        Page<MemberTeamDTO> result = memberRepository.searchPageOptimal(condition, PageRequest.of(0, 3));
        
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("userName")
                                       .containsExactly("member4", "member5", "member6");
        
        assertEquals(3L, result.getTotalElements());
    }
    
    @Test
    void searchPageBySort() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(50);
        condition.setAgeLoe(70);
        condition.setTeamName("team2");
        
        Page<MemberTeamDTO> result = memberRepository.searchPageBySort(condition, PageRequest.of(0, 3, Sort.by(Direction.DESC, "userName")));
        
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("userName")
                                       .containsExactly("member7", "member6", "member5");
        
        assertEquals(3L, result.getTotalElements());
    }
}
```
`MemberController`
```java
@RestController
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberRepositoryImpl memberQuerydslRepository;
    
    @GetMapping("/v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition condition) {
        return memberQuerydslRepository.searchByWhereClause(condition);
    }
    
    @GetMapping("/v2/members")
    public Page<MemberTeamDTO> searchMemberV2(MemberSearchCondition condition, 
                                              @PageableDefault(page = 0, size = 20)Pageable pageable) {
        return memberQuerydslRepository.searchPageOptimal(condition, pageable);
    }
}
```


## 번외
- QuerydslPredicateExecutor
  - Spring Data Jpa 인터페이스를 상속한 리포지토리 인터페이스에서 QuerydslPredicateExecutor를 상속받는다.
  - Spring Data Jpa 인터페이스에서 제공하는 기본 메서드 파라미터에 Querydsl 조건식 사용가능
  - **묵시적 조인은 가능하지만 left join이 불가능하다**
  - **클라이언트가 Querydsl에 의존해야한다. 서비스 클래스가 Querydsl이라는 구현 기술에 의존해야한다**
  - **실무의 복잡한 환경에서 한계가 있음**
  - 참고 : `QuerydslPredicateExecutor` 는 `Pagable`, `Sort`를 모두 지원하고 정상 동작한다.
  ```java
  /*리포지토리에 적용*/
  interface MemberRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> { 

  }
  ```
  ```java
  Iterable result = memberRepository.findAll( member.age.between(10, 40) 
                                    .and(member.username.eq("member1")));
  ```
- [Querydsl Web](https://docs.spring.io/spring-data/jpa/docs/2.2.3.RELEASE/reference/html/#core.web.type-safe)
  - **단순 조건만 가능**
  - **컨트롤러가 Querydsl에 의존**
- **QuerydslRepositorySupport**
  - 장점
    - `getQuerydsl().applyPagination()` 스프링 데이터가 제공하는 페이징을 `Querydsl`로 편리하게 변환 가능 **(단! Sort는 오류발생)**  
    - `from()` 으로 시작 가능(최근에는 `QueryFactory`를 사용해서 `select()` 로 시작하는 것이 더 명시적) `EntityManager` 제공
  - 한계
    - Querydsl 3.x 버전을 대상으로 만듬
    - Querydsl 4.x에 나온 JPAQueryFactory로 시작할 수 없음
      - select로 시작할 수 없음 (from으로 시작해야함)
    - QueryFactory 를 제공하지 않음
    - **스프링 데이터 Sort 기능이 정상 동작하지 않음**
    ```java
    import static com.study.querydsl.domain.QMember.member;
    import static com.study.querydsl.domain.QTeam.team;
    
    //...
    
    /**
     * 엔티티 매니저 알아서 주입해줌
     * 엔티티 매니저 호출시 getEntityManager() 호출
     */
    @Repository
    public class MemberQuerydslRepositorySupport extends QuerydslRepositorySupport {
    
        public MemberQuerydslRepositorySupport() {
            super(Member.class);
        }
        
        public List<MemberTeamDTO> search(MemberSearchCondition condition) {
            return from(member)
                    .leftJoin(member.team, team)
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(userNameEq(condition.getUserName()), 
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()), 
                           ageLoe(condition.getAgeLoe()))
                    .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                    .fetch();
        }
        
        public Page<MemberTeamDTO> searchPaging(MemberSearchCondition condition, Pageable pageable) {
            JPQLQuery<MemberTeamDTO> query =
                from(member)
                .leftJoin(member.team, team)
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()), 
                       teamNameEq(condition.getTeamName()),
                       ageGoe(condition.getAgeGoe()), 
                       ageLoe(condition.getAgeLoe()))
                .select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name));
           
            /* 페이징 처리 부분 */
            query = getQuerydsl().applyPagination(pageable, query);
            QueryResults<MemberTeamDTO> results = query.fetchResults();
            
            return new PageImpl<>(results.getResults(), pageable, results.getTotal());
        }
        
    
        private BooleanExpression ageLoe(Integer ageLoe) {
            return Objects.isNull(ageLoe) ? null : member.age.loe(ageLoe);
        }
    
        private BooleanExpression ageGoe(Integer ageGoe) {
            return Objects.isNull(ageGoe) ? null : member.age.goe(ageGoe);
        }
    
        private BooleanExpression teamNameEq(String teamName) {
            return (!StringUtils.hasText(teamName)) ? null : team.name.eq(teamName);
        }
    
        private BooleanExpression userNameEq(String userName) {
            return (!StringUtils.hasText(userName)) ? null : member.userName.eq(userName);
        }
    
    }
    ```
- Querydsl지원 클래스를 직접만들기
```java
/**
 * Querydsl 4.x 버전에 맞춘 Querydsl 지원 라이브러리
 *
 * @author Younghan Kim
 * @see org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
 */
@Repository
public abstract class Querydsl4RepositorySupport {
    private final Class domainClass;
    private Querydsl querydsl;
    private EntityManager entityManager;
    private JPAQueryFactory queryFactory;

    public Querydsl4RepositorySupport(Class<?> domainClass) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        this.domainClass = domainClass;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        //동적 Sort(정렬) 관련 코드
        JpaEntityInformation entityInformation = 
                JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);
        SimpleEntityPathResolver resolver = SimpleEntityPathResolver.INSTANCE;
        EntityPath path = resolver.createPath(entityInformation.getJavaType());
        this.entityManager = entityManager;
        this.querydsl = new Querydsl(entityManager, new PathBuilder<>(path.getType(), path.getMetadata()));
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @PostConstruct
    public void validate() {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        Assert.notNull(querydsl, "Querydsl must not be null!");
        Assert.notNull(queryFactory, "QueryFactory must not be null!");
    }

    protected JPAQueryFactory getQueryFactory() {
        return queryFactory;
    }

    protected Querydsl getQuerydsl() {
        return querydsl;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    protected <T> JPAQuery<T> select(Expression<T> expr) {
        return getQueryFactory().select(expr);
    }

    protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
        return getQueryFactory().selectFrom(from);
    }

    protected <T> Page<T> applyPagination(Pageable pageable, Function<JPAQueryFactory, JPAQuery> contentQuery) {
        JPAQuery jpaQuery = contentQuery.apply(getQueryFactory());
        List<T> content = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();
        return PageableExecutionUtils.getPage(content, pageable, jpaQuery::fetchCount);
    }

    protected <T> Page<T> applyPagination(Pageable pageable, Function<JPAQueryFactory, JPAQuery> contentQuery,
            Function<JPAQueryFactory, JPAQuery> countQuery) {
        JPAQuery jpaContentQuery = contentQuery.apply(getQueryFactory());
        List<T> content = getQuerydsl().applyPagination(pageable, jpaContentQuery).fetch();
        JPAQuery countResult = countQuery.apply(getQueryFactory());
        return PageableExecutionUtils.getPage(content, pageable, countResult::fetchCount);
    }
}
```
- `Querydsl4RepositorySupport` 상속
```java
@Repository
public class MemberRepositoryUsingCustomSupport extends Querydsl4RepositorySupport{

    public MemberRepositoryUsingCustomSupport() {
        super(Member.class);
    }
    
    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }
    
    public List<Member> basicSelectfrom() {
        return selectFrom(member)
                .fetch();
    }
    
    /**
     * 기존의 querydsl을 사용해서 데이터를 가져오는 방식
     */
    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<Member> query = selectFrom(member)
                                .where(userNameEq(condition.getUserName()),
                                       teamNameEq(condition.getTeamName()),
                                       ageGoe(condition.getAgeGoe()),
                                       ageLoe(condition.getAgeLoe()));
        List<Member> content = getQuerydsl().applyPagination(pageable, query).fetch();
        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }
    
    /**
     * 커스텀 Support를 사용해서 데이터를 가져오는 방식
     */
    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, query -> 
                    query
                    .selectFrom(member)
                    .where(userNameEq(condition.getUserName()),
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()),
                           ageLoe(condition.getAgeLoe()))
                );
    }
    
    /**
     * 커스텀 Support를 사용해서 데이터를 가져오는 방식 counter 쿼리 분리
     */
    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, query -> 
                    query
                    .selectFrom(member)
                    .where(userNameEq(condition.getUserName()),
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()),
                           ageLoe(condition.getAgeLoe())),
                    
                    countQuery -> 
                    countQuery
                    .select(member)
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(userNameEq(condition.getUserName()), 
                           teamNameEq(condition.getTeamName()),
                           ageGoe(condition.getAgeGoe()), 
                           ageLoe(condition.getAgeLoe())));
    }
    
    private BooleanExpression ageLoe(Integer ageLoe) {
        return Objects.isNull(ageLoe) ? null : member.age.loe(ageLoe);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return Objects.isNull(ageGoe) ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression teamNameEq(String teamName) {
        return (!StringUtils.hasText(teamName)) ? null : team.name.eq(teamName);
    }

    private BooleanExpression userNameEq(String userName) {
        return (!StringUtils.hasText(userName)) ? null : member.userName.eq(userName);
    }
}
``` 