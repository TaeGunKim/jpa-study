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
            em.persist(member);*/


            //delete
            /*Member findMember = em.find(Member.class, 1L);
            em.remove(findMember);*/

            //update
            /*Member findMember = em.find(Member.class, 2L);
            findMember.setName("test");*/


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

            tx.commit();
        }catch (Exception e){
            tx.rollback();
        }finally {
            em.close();
        }
        emf.close();

        //System.out.println("Hello world!");
    }
}