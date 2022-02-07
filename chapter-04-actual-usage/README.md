# 실무 활용 
## 순수 Querydsl로 리포지 토리 구성
- 동적 쿼리와 성능 최적화 조회
  - **동적 쿼리 작성시에는 조건이 아무것도 없으면 데이터 전체를 끌어오는 현상이 발생할 수 있기 때문에 기본 조건이라도(최소한 limit)넣어서 최적화를 해주는 것이 좋다**
```java
@Repository
public class MemberRepository {
    
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    
    public MemberRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em); //스프링 빈으로 등록해서 처리해도됨
    }
    
    public Long save(Member member) {
        em.persist(member);
        return member.getId();
    }
    
    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }
    
    public List<Member> findByUserName(String userName) {
        return queryFactory.selectFrom(member)
                           .where(member.userName.lower().eq(userName.toLowerCase()))
                           .fetch();
    }
    
    public List<Member> findAll() {
        return queryFactory.selectFrom(member)
                           .fetch();
    }
    
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
        return queryFactory.select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
                           .from(member)
                           .leftJoin(member.team, team)
                           .where(builder)
                           .fetch();
    }
    
    public List<MemberTeamDTO> searchByWhereClause(MemberSearchCondition condition) {
        return queryFactory.select(new QMemberTeamDTO(member.id, member.userName, member.age, team.id, team.name))
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
## 조회 API 컨트롤러
- MemberController
```java
@RestController
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberRepository memberRepository;
    
    @GetMapping("/v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition condition) {
        return memberRepository.searchByWhereClause(condition);
    }
}
```