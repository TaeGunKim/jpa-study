package org.study;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try{

            //insert
            /*Member member = new Member();
            member.setId(3L);
            member.setName("teacher");
            //위 객체 생성 단계가 비영속 상태
            //아래 persist 하는 순간이 영속 상태 (단, db에 저장되는 건 아니고 entityManager에 영속된 상태임)
            em.persist(member); // 1차 캐시에 저장됨

            // 1차 캐시에서 조회 (select 쿼리가 안나감)
            //Member findMember = em.find(Member.class, 1L);

            //1차캐시에 없음 -> db조회 -> 1차 캐시에 저장 -> 반환
            //Member findMember = em.find(Member.class, 1L);
            */

            //영속성 컨텍스트(Entity Manager)의 이점
            /*
            1. 1차 캐시 (한개의 database trasaction 에서만 있기 때문에, 성능 이점이 있지는 않다. 하지만 객체 지향 설계에 도움을 줌)
            2. 동일성(identity) 보장 (두번 조회시 a == b로 동일성을 갖는다. like Java Collection)
            3. 트랜잭션 지원하는쓰기 지연 (transactional write-behind) (쓰기 지연 SQL 저장소)
              - hibernate.jdbc.batch_size 옵션이 있어서 버퍼링을 할 수 있다. (실시간 쿼리보단, 배치성 쿼리에 성능에 도음을 줌)

            4. 변경 감지 (Dirty Checking), update에 써놨음
            5. 지연 로딩 (Lazy Loading)
            */


            //delete
            /*
            Member findMember = em.find(Member.class, 1L);
            //em.detach(findMember); //회원 엔티티를 영속성 컨텍스트에서 분리, 준영속 상태 , clear() -> 1차캐시 초기화, close() -> 종료, 의 방법이 있다.
            em.remove(findMember); // 객체를 삭제한 상태(삭제)
            */

            //update
            /*
            Member findMember = em.find(Member.class, 2L);
            findMember.setName("test");
            // update 할때 em.update나 em.persist를 해야 하는게 아닌가?
            // -> JPA는  변경 감지 (Dirty Checking)가 있기 때문에 객체를 변경하면 commit때 update 쿼리가 나간다.
            // -> tx.commit 되는 시점에 JPA 에서는 flush()가 일어남
            // -> 1차 캐시에는 PK, Entity(map), 스냅샷이 있는데 이걸 비교해서 UPDATE SQL을 생성하게 됨
            */


            //select (basic)
            /*Member findMember = em.find(Member.class, 1L);*/

            //select (JPQL, 쿼리를 직접 입력)
            List<Member> result = em.createQuery("select m from Member as m", Member.class)
                    .setFirstResult(5) // pagination
                    .setMaxResults(5) // pagination
                    .getResultList();
            for(Member member : result) {
                System.out.println(member.getName());
            }



            // 플러시(flush() , 영속성 컨텍스트의 변경내용읠 데이터 베이스에 반영
            // 변경감지 , 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
            // 쓰기 지연 SQL 저장소의 쿼리르 데이터베이스에 전송

            // 플러시 사용 방법
            // em.flush() - 직접 호출
            // 트랜잭션 커밋 - 플러시 자동 호출
            //  JPQL 쿼리 실행 - 플러시 자동 호출

            //플러시는
            //영속성 컨텍스트를 비우지 않음
            //영속성 컨텍스트의 변경내용을 데이터베이스에 동기화
            //트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화 하면 됨


            tx.commit(); //commit 되는 순간 영속 상태가 db로 넘어감

        }catch (Exception e){
            tx.rollback();
        }finally {
            em.close();
        }
        emf.close();

        //System.out.println("Hello world!");
    }
}