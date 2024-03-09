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
@ManyToMany
@OneToMany
@ManyToOne


## 3/10 (일)
## 6. 고급 매핑

## 3/11 (월)
## 7. 프록시와 연관관계 관리
## 8. 값 타입

## 3/12 (화)
## 9. 객체지향 쿼리 언어 1 (기본 문법)
## 10. 객체지향 쿼리 언어 2 (중급 문법)



## JPA 공부 완료되면, pama 앱의 backend로 3/13~17일 까지 완료 해보자...
## xcv

## 이후 계획
## 3/18~24 vue study..
