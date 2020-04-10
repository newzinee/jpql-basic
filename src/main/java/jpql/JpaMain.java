package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setAge(10);
            member.changeTeam(team);
            member.setType(MemberType.ADMIN);
            em.persist(member);

            em.flush();
            em.clear();

            // 기본
//            basic(em);


            // new로 조회
//            newConstructor(em);

            // 페이징
//            paging(em);

            // 조인
//            join(em);

            // 타입
//            type(em);

            // case
//            caseBasic(em);

            // basic function
            String query0 = "select 'a' || 'b' from Member m";
            String query1 = "select concat('a','b') from Member m";
            String query2 = "select substring(m.username, 2, 3) from Member m";
            String query = "select locate('de','abcdefg') from Member m";   // type Integer, 결과4
            List<String> result = em.createQuery(query, String.class)
                    .getResultList();
            for (String s : result) {
                System.out.println("s = " + s);
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();

    }

    private static void caseBasic(EntityManager em) {
        String query = "select " +
                " case when m.age <= 10 then '학생요금'" +
                "      when m.age >= 10 then '경로요금'" +
                "      else '일반요금'" +
                " end" +
                " from Member m";
        String query2 = "select coalesce(m.username, '이름 없는 회원') from Member m";
        String query3 = "select nullif(m.username, 'member1') from Member m";   // null
        List<String> result = em.createQuery(query3, String.class)
                .getResultList();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    private static void type(EntityManager em) {
        String query = "select m.username, 'Hello', TRUE from Member m " +
                "where m.type = jpql.MemberType.ADMIN";
        List<Object[]> result = em.createQuery(query)
                .getResultList();

        for (Object[] objects : result) {
            System.out.println("objects = " + objects[0]);
            System.out.println("objects = " + objects[1]);
            System.out.println("objects = " + objects[2]);
        }
    }

    private static void join(EntityManager em) {
        String query = "select m from Member m inner join m.team t";
        String query2 = "select m from Member m left join m.team t on t.name = 'teamA'";
        List<Member> result = em.createQuery(query2, Member.class)
                .getResultList();
        System.out.println("result = "+result.size());
    }

    private static void basic(EntityManager em) {
        TypedQuery<Member> query1 = em.createQuery("select m from Member m", Member.class);
        Query query2 = em.createQuery("select m.username, m.age from Member m");

        Member query3 = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        System.out.println("query3 = " + query3.getUsername());
    }

    private static void newConstructor(EntityManager em) {
        List<Member> result = em.createQuery("select m from Member m", Member.class)
                .getResultList();
        Member findMember = result.get(0);
        findMember.setAge(20);  // update query 나감


        List<MemberDTO> result2 = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
                .getResultList();
        MemberDTO memberDTO = result2.get(0);
        System.out.println("memberDTO = " + memberDTO.getUsername());
        System.out.println("memberDTO = " + memberDTO.getAge());
    }

    private static void paging(EntityManager em) {
        List<Member> result3 = em.createQuery("select m from Member m order by m.age desc", Member.class)
                .setFirstResult(1)
                .setMaxResults(10)
                .getResultList();
        System.out.println("result.size(): "+result3.size());
        for (Member member : result3) {
            System.out.println("member = " + member);
        }
    }
}
