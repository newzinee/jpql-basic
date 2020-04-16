package jpql;

import javax.persistence.*;
import java.util.Collection;
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

             Team teamB = new Team();
            teamB.setName("teamB");
            em.persist(teamB);



            Member member1 = new Member();
            member1.setUsername("member1");
            member1.setAge(10);
            member1.changeTeam(team);
            member1.setType(MemberType.ADMIN);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("member2");
            member2.changeTeam(team);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("member3");
            member3.changeTeam(teamB);
            em.persist(member3);

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
//            basicFunction(em);

            // 경로 표현식
//            경로표현식(em);

            // fetch join
//            String query = "select m from Member m";    // LAZY - sout 시 team 쿼리 실행
            // 회원(SQL)
            // 회원1, 팀A(SQL)
            // 회원2, 팀A(1차캐시)
            // 회원3, 팀B(SQL)
            // SQL 총 3번 나감
            // 문제: 회원 100명 -> N(100명) + 1(회원)

//            String query = "select m from Member m join fetch m.team";
            // 회원 + 팀 (SQL)
            // 여기서 TEAM은 proxy가 아닌 실제 데이터

//            String query = "select t from Team t join fetch t.members";
            // teamA는 하난데 member가 2명이라, teamA row가 2줄 나와.

//            String query = "select distinct t from Team t join fetch t.members";
            // sql의 distinct + entity 중복 제거

            String query = "select t from Team t";
            // team SQL 1번 + team에 Member 조회(team 갯수) 2번: N+1 문제
            // 해결 1-> class Team의 members에 @BatchSize(size=100) 추가 (in 쿼리)
            // 해결 2-> hibernate.default_batch_fetch_size 설정 (in 쿼리)

            List<Team> result = em.createQuery(query, Team.class)
                    .setFirstResult(0)
                    .setMaxResults(2)
                    .getResultList();

            System.out.println("result = " + result.size());

            for (Team t : result) {
                System.out.println("team = " + t.getName() + " | members = " + t.getMembers().size());
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

    private static void 경로표현식(EntityManager em) {
        String query0 = "select m.team From Member m";    // 단일값 연관 경로 - 묵시적 내부 조인 발생
        String query1 = "select t.members From Team t";    // 컬렉션 값 연관 경로 - 묵시적 내부 조인 발생
        String query = "select m.username from Team t join t.members m";   // 명시적 조인

        List<String> result = em.createQuery(query, String.class)
                .getResultList();

        System.out.println("result = " + result);
    }

    private static void basicFunction(EntityManager em) {
        String query0 = "select 'a' || 'b' from Member m";
        String query1 = "select concat('a','b') from Member m";
        String query2 = "select substring(m.username, 2, 3) from Member m";
        String query3 = "select locate('de','abcdefg') from Member m";   // type Integer, 결과4
        String query4 = "select size(t.members) from Team t";    // type Integer
        String query5 = "select index(t.members) from Team t";    // type Integer, 안쓰는게 좋아
        String query6 = "select function('group_concat', m.username) from Member m";    // 사용자정의 함수
        String query = "select group_concat(m.username) from Member m";    // 사용자정의 함수 - hibernate

        List<String> result = em.createQuery(query, String.class)
                .getResultList();
        for (String s : result) {
            System.out.println("s = " + s);
        }
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
