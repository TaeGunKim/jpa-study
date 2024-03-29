### JPA-STUDY 계획

## 3/7 (목)
## 1. 기본 애플리캐이션 개발

## 3/8 (금)
## 2. 영속성 관리 (내부동작방식)


## 3/9 (토)
## 3. 앤티티 매핑
- hibernate.hbm2ddl.auto 속성으로 db 스키마 자동 생성 가능함 (로컬개발에 유용, 운영사용 하면 안됨)
- create : 기존 테이블 삭제 후 다시 생성(drop + create)
- create-drop : create 와 같으나 종료 시점에 테이블 drop
- update : 변경분만 반영 (운영DB사용X)
- validate 엔티티와 테이블이 정상 매핑 되었느지만 확인
- none : 사용하지 않음 

- 매핑 어노테이션 정리 ()
- @Column : 컬럼매핑 , @Temporal : 날짜 타입 매핑, @Enumerated : enum 타입 매핑 (Ordinal은 enum의 순서가 integer로 들어감, 사용하면 안됨..)
- @Lob : BOLB, CLOB 매핑 , @Transient : 특정필드를 컬럼에 매핑안함

기본키 매핑 방법
- 직접할당 : @Id 만 사용
- 자동생성 : @GeneratedValue   (IDENTITY : 데이터 베이스에 위임, SEQUENCE : 시퀀스 오브젝트 사용, TABLE : 키 생성용 테이블 사용 @TableGenerator 필요, AUTO : DB에 따라 자동 지정)
- 권장하는 식별자 전략 (기본키 제약 조건 : null 아님, 유일, 변하면 안됨, 권장 : Long형 + 대체키 + 키 생성전략 사용)
- (예외) GeneratedValue   (IDENTITY) 인 경우에만 1차 캐시에 PK값이 없기 떄문에 commit 시점이 아닌, persist 시점에 SQL을 날림..
- 그래서 보통 commit 시점에 한번에 쿼리 날라가는 (성능) JPA 기능에 IDENTITY 전략에선 사용이 안됨

## 4. 연관관계 매핑 기초
- 방향(Direction) : 단방향, 양방향
- 다중성(Muliplicity) : 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M) 이해
- 연관관계의 주인 (Owner) : 객체 양방향 연관관계는 관리 필요

- 객체를 테이블에 맞추어 데이터 중심으로 모델링 하면, 협력관계를 만들수 없다.
- 외래키로 연결해야함, 안하면 insert하고 select를 계속 해와야 원하는 데이터를 얻을 수 있음
- 1:다 (팀:회원) @ManyToOne @JoinColumn(name="team_id")

> 객체지향스럽지 못한 코드 (팀을 찾기 위해선..?)
```java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeamId(team.getId());
em.persist(member);

Member findMember = em.find(Member.class, member.getId());
Long findTeamId = findMember.getTeamId();
Team findTeam = em.find(Team.class, findTeamId);

tx.commit();
```

> 객체 지향 적인 쿼리
```java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeam(team); // 알아서 foreign key값을 넣어줌
em.persist(member);

//1차캐쉬에서가져오기 싫다면? 
//em.flush();
//em.clear();

Member findMember = em.find(Member.class, member.getId());
Team findTeam = findMember.getTeam(); //team정보를 바로 가져올 수 있음

tx.commit();
```

* 양방향 연관관계의 연관관계의 주인
- team에 속하는 memberList를 구하기 위한 어노테이션 (반대 방향으로 객체 그래프 탐색)
@OneToMany(mappedBy = "team")
private List<Member> memlist = new ArrayList();
- 객체의 양방향 관계는 사실 양방향 관계까 아니라 서로 다른 단방향 관계 2개다 ( 테이블은  외래키(forignkey) 하나로 두 테이블의 연관관계를 관리, 양쪽으로 조인할 수 있다. )
- 양방향 매핑 규칙 :: 둘 중 하나로 외래 키를 관리해야 한다. & 외래키가 있는 곳을 주인으로 한다. 

* 양방향 매핑시 가장 많이 하는 실수
- (연관관계의 주인에 값을 입력하지 않음)
```java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

//연관관계의 주인
Member member = new Member();
member.setUsername("member1");
//member.setTeam(team); <- 연관관계 주인에 꼭 넣어줘야 함 (없으면 null) 

//역방향(주인이 아닌 방향)만 연관관계 설정 (결론 : 순수한 객체 관계를 고려하면 항상 양쪽에 넣어줘야 함)
team.getMember().add(member); //보통은 setter에 이 부분을 넣어주면 됨 (연관관계 편의 메서드)

em.persist(member);
tx.commit();
```
- 양방향 매핑시에 무한 루프 조심하자 ( toString(), lombok, JSON 생성 라이브러리)
- 웬만해서는 entity를 controller에서 반환하지 말자..
- 단방향 매핑만으로도 이미 연관관계 매핑은 완료
- 양방향 매핑은 반대 방향으로조회(객체 그래프 탐색) 기능이 추가 된 것 뿐
- JPQL에서 역방향으로 탐색할 일이 많음
- 단방향 매핑을 잘 하고 양방향은 필요할 떄 추가해도 됨 (테이블에 영향 없음)
- 연관관계의 주인은 외래 키의 위치를 기준으로 정해야 함

## 5. 다양한 연관관계 매핑
@OneToOne
@ManyToMany (실전에서는 중간 테이블이 단순하지 않다, 중간 테이블을 이용해서 1:N N:1을 섞어서 쓰는걸 추천)
@OneToMany
@ManyToOne  (강의상 결론은 다대일 관계로 설정해야, 이해도 쉽고 설계가 어렵지 않다고 함)


## 3/10 (일)
## 6. 고급 매핑
상속관계 매핑
- 관계형 데이터베이스는 상속관게 X
- 슈퍼타입 서브타입 관계라는 모델링 기법이 객체 상속과 유사
- 상속관계 배핑 : 객체의 상속과 구조와 DB의 슈퍼타입 서브타입 관계를 매핑
- @Inheritance
- (1) 조인전략 (strategy = InheritanceType.JOINED), @DiscrimiatorColumn (타입같은거, entity 명이 들어가게 됨, default는 DTYPE으로 들어감), @DiscrimiatorValue (자식테이블에서 entity명이 아닌 다른 값을 넣기 위한 어노테이션), 가장 정규화된 구조
- (2) 단일 테이블 전략(strategy = InheritanceType.SINGLE_TABLE) : default, @DiscrimiatorColumn있어야 함
- (3) 각각 테이블전략 테이븡ㄹ 퍼 클랫ㅡ
@MapepdSuperclass (속성만 상속?)
- 공통 매핑 정보가 필요할 떄 사용 (id, name 같은..)
- 상속관게 매핑 X, 엔티티X, 테이블과 매핑 X
- 부모 클래스를 상속 받는 **자식 클래스에 매핑 정보만 제공**
- 조회, 검색 불가 (em.find(BaseEntity) 불가)
- 직접 생성해서 사용할 일이 없으므로 **추상(abstract) 클래스 권장**

## 7. 프록시와 연관관계 관리
(1) 프록시
- em.find() vs em.getReference() 
- em.find() 데이터베이스를 통해서 실제 엔티티 객체 조회
- em.getReference() 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회 (성능최적화로 필요할 때 가져오는 거지..)
- 프록시 객체를 초기화 할 떄, 프록시 객체가 실제 엔티티로 바뀌는 것은 아님, 초기화되면 프록시 객체를 통해서 실제 엔티티에 접근 간으
- 프록시 객체는 원본 엔티리를 상속받음, 따라서 타입체크시 주의해야 함(Entity.getClass 의 == 비교 실패, 대신 instance of 사용)

(2) 즉시 로딩과 지연 로딩
- @ManyToOne(Fetch = FetchType.Eager) , 즉시로딩 (무조건 join 해옴, proxy로 가져오지 않음)
- @ManyToOne(Fetch = FetchType.LAZY) , 이렇게 Fetch 타입을 주면 엔티티 하위의 엔티티 객체를 바로 가져오지 않고, 사용할 때 가져오기떄문에 프록시로 가져오게 됨

(3) 지연 로딩 활용
- 실무에서는 가급적 지연 로딩만 사용
- 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생
- 즉시로딩은 JPQL에서 N+1 문제를 일으킨다.
- @ManyToOne, @OneToOne에서는 기본이 즉시로딩, LAZY로 설정해서 사용!
- @OneToMany, @ManyToMany는 기본이 지연 로딩

(4) 영속성 전이 : CASCADE
- persist를 할떄, 연관관계 있는 entity를 연쇄적으로 persist 해줌
- @OneToMany(mappdBy ="", cascade=CascadeType.PERSIST)  , ALL : 모두적용, PERSIST : 영속, REMOVE : 삭제, MERGE : 병합, REFRESH, DETACH
- 참조하는 곳이 하나일 때 사용해야 함
(5) 고아 객체
- 고아 객체 제거 : 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제
- orphanRemoval = true
- 참조하는 곳이 하나일 때 사용해야 함
- 특정 엔티티가 개인 소유할 떄 사용
- @OneToOne, @OneToMany만 가능
```java
Parent parent1 = em.find(Parent.class, id);
parent1.getChildren().remove(0);
//자식 엔티티를 컬렉션에서 제거
```
- DELETE FROM CHILD WHERE ID = ?
- 개념적으로 부모를 제거하면 자식은 고아가 된다, 따라서 고아 객체 제거 기능을 활성화 하면, 부모를 제거할 떄도 자식도 함께 제거가 됨
- 이것은 CascadeType.REMOVE 처럼 동작한다.
- 
(6) 영속성 전이 + 고아 객체, 생명주기
- CasecadeType.ALL + orphanRemovel=true
- 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거
- 두 옵션을 활성화 하면 부모 엔티티를 통해서 자식의 생명주기를 관리할 수 있음
- 도메인 주도 설계(DDD)의 Aggregate Root 개념을 구현할 떄 유용


## 3/11 (월)
## 8. 값 타입
임베디드 타입 사용법
@Embeddable 값 타입을 정의하는 곳에 표시
@Embedded 값 타입을 사용하는 곳에 표시
기본 생성자 필수 

재사용 가능, 높은 응집도, 의미있는 메소드 만들 수 있음, 값 타입을 소유한 엔티티에 생명주기를 의존함
캡슐화 처럼 쓸 수 있음
```java
@Embeded
private Period workPeroid;

@Embeddable
public class Period {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
```
@AttributeOverrides

- 임베디드 같은 값 타입을 여러 엔티티에서 공유하면 위험함
- 불변객체 : 객체 타입을 수정할 수 없게 만들면 부작용을 원천 차단
- 값 타입은 불변객체(immutable object)로 설계해야 함
- Integer, String은 자바가 제공하는 대표적인 불변 객체

값 타입의 비교 (@override로 equals와 hascode를 생성해야함, 엔티티에
동일성(identity) 비교 : 인스턴스 참조 값을 비교, == 사용
동등성(equivalence) 비교 : 인스턴스 값을 비교, equals 사용

값 타입 컬렉션... 어려운데?


## 3/12 (화)
## 9. 객체지향 쿼리 언어 1 (기본 문법)
JPA는 다양한 쿼리방법을 지원
JPQL, JPA Criteria, QueryDSL, 네이티브 SQL, JDBC API 직접 사용, MyBatis, SpringJdbcTemplate와 함꼐 사용..

(1)JPQL
가장 단순한 조회 방법
EntityManager.find()
객체 그래프 탐색(a.getB().getC())
나이가 18살 이상인 회원을 모두 검색하고 싶다면? 
JPQL은 엔티티를 검색하고 SQL은 테이블을 검색함
```java
//검색 (여기서 member는 entity다)
String jpql = "select m From Member m where m.name like '%hello%'";

List<Member> result = em.createQuery(jpql, Member.class).getResultList();
```
테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
SQL를 추상화해서 특정 데이터 베이스 SQL에 의존하지 않는다.
JPQL을 한마디로 정의하면 객체지향 SQL

(2) Criteria

```java
import org.hibernate.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> query = cb.createQuery(Member.class);

Root<Member> m = query.from(Member.class);

CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get("username"), "kim"));
List<Member> resultList = em.createQuery(cq).getResultList();
```

음... 더 어려운것 같은데?
문자가 아닌 자바코드로 JPQL을 작성할수 있고 JQPL 빌더 역할을 함, JPA공식기능
단, 너무 복잡하고 실용성잉 벗다.
Criteria 대신에 QueryDSL 사용 권장

(3) QueryDSL
```java
//JPQL
//select m from Member m where m.age > 18
JPAFactoryQuery query = new JPAQueryFactory(em);
QMember m = QMember.member;

List<Member> list = 
            query.selectFrom(m)
                  .where(m.age.gt(18))
                  .orderBy(m.name.desc())
                  .fetch();
```
실무사용 권장😂😂😂
문자가 아닌 자바코드로 JPQL 작성
JPQL 빌더 역할
컴파일 시점에 문법 오류를 찾을 수 있음
동적쿼리 작성 편리함
단순하고 쉬움

(4)네이티브 SQL
JPA가 제공하는 SQL을 직접 사용하는 기능
```java
String sql = "SELECT ID,AGE,NAME FROM MEMBER WHERE NAME = 'kim'";
List<Member> resultList = em.createNativeQuery(sql,Member.class).getResultList();
```

(5)JDBC 직접 사용, SpringJdbcTEemplate 등
JPA를 사용하면서 JDBC 커넥션을 직접 사용하거나, 스프링 JdbcTemplate, 마이바티스를 함꼐 사용 가능
단 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요
(JPA를 우회해서 SQL을 실행하기 직전에 영속성 컨텍스트 수동 플러시)

JPQL (Java Persistence Query Language)
- JPQL은 객체 지향 쿼리 언어로, 엔티티 객체를 대상으로 쿼리 한다.
- JQPL은 SQL을 추상화 해서 특정 데이터베이스 SQL에 의존하지 않는다.
- JPQL은 결국 SQL로 변환된다.
- 엔티티와 속성은 대소문자 구분
- JQPL 키워드는 대소문자 구분X(SELECT, FROM, where)
- 엔티티 이름을 사용한다 (테이블 이름이 아님)
- 별칭(m)은 필수 (as는 생략가능, select m from Member as m where m.age > 18)

집합과 정렬
select
 COUNT(m),
 SUM(m.age),
 AVG(m.age),
 MAX(m.age),
 MIN(m.age)
from Member m

GROUP BY, HAVING, ORDER BY..

TypeQuery : 반환 타입이 명확할 떄 사용
```
TypeQuery<Member> query = em.createQuery("SELECT m FROM Member m", Member.class);
```
Query : 반환 타입이 명확하지 않을 떄 사용
```
Query query = em.createQuery("SELECT m.username, m.age from Member m");
```
결과조회 API
query.getResultList() : 결과가 하나 이살일 때, 리스트 반환 (값 없으면 빈 리스트)
query.getSingleResult() : 결롸가 정확히 하나, 단일 객체 반환 
- 결과가 없으면 javax.persistence.NoResultException
- 둘 이상이면 javax.persistence.NonUniqueResultException 

파라미터 바인딩 (이름기준, 숫자로 위치기준도 있는데 버그가 많아 사용하지 말것)
```java
Member result = m.createQuery("SELECT m FROM Member m where m.username = :username", Member.class)
        .setParameter("usrename","member1")
        .getSingleResult();
```

프로젝션
Select 절에 조회할 대상을 지정하는것
프로젝션 대상 : 엔티티, 임베디드타입, 스칼라타입(숫자,문자등 기본 데이터 타입)
- SELECT m FROM Member m -> 엔티티 프로젝션
- SELECT m.team FROM Member m -> 엔티티 프로젝션
- SELECT m.address FROM Member m -> 임베디드 프로젝션
- SELECT m.username,m.age FROM Member m -> 스칼라타입 프로젝션
DISTINCT로 중복 제거

프로젝션 - 여러 값 조회
- SELECT m.username, m.age FROM Member m
- (1) Query 타입으로 조회
- (2) Object[] 타입으로 조회
- (3) new 명령어로 조회

페이징
- JPA는 페이징을 다음 두 API로 추상화
- setFirstResult(int startPosition) : 조회 시작 위치(0부터 시작)
- setMaxResults(int maxResult) : 조회할 데이터 수
```java
em.createQuery("select m from Member m order by m.age desc", Member.class)
.setFirstResult(1)
.setMaxResults(10)
.getResultList();
```

조인 (entity 객체 중심으로)
내부조인 SELECT m FROM Member m [INNER] JOIN m.team t
외부조인 SELECT m FROM Member m LEFT [OUTER] JOIN m team t
세타조인 select count(m) from Member m, Team t where m.username = t.name

서브쿼리
나이가 평균보다 많은 회원
```jpqlcommunity
select m from Member m where m.age > (select avg(m2.age) from MEmber m2)
```
- JPA는 WHERE HAVING 절에서만 서브쿼리 사용 가능, SELECT 절도 가능(하이버네이트에서 지원)
- 단 FROM 절의 서브쿼리는 현재 JPQL에서 불가능 (조인으로 풀 수 있으면 풀어서 해결)
  
## 10. 객체지향 쿼리 언어 2 (중급 문법)



## JPA 공부 완료되면, pama 앱의 backend로 3/13~17일 까지 완료 해보자...
활용 강의가 또 있네.. ㅎ;
## xcv

## 이후 계획
## 3/18~24 vue study..
