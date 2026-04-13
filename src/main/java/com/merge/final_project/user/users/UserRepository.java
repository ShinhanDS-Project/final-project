package com.merge.final_project.user.users;

import org.springframework.data.domain.Page; // [가빈] 추가
import org.springframework.data.domain.Pageable; // [가빈] 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;   //[가빈] 추가
import org.springframework.data.repository.query.Param; //[가빈] 추가

import java.time.LocalDateTime; //[가빈] 추가
import java.util.List;  //[가빈] 추가
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmailAndLoginType(String email, LoginType loginType);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneAndName(String email,String name);

    //[가빈] 관리자가 오늘 날짜 기준으로 가입한 사용자 수를 조회할 수 있는 쿼리문 하나 추가했습니다
    @Query(value = """
            SELECT TO_CHAR(created_at, 'YYYY-MM-DD') AS date, COUNT(*) AS count
            FROM users
            WHERE created_at >= :since
            GROUP BY TO_CHAR(created_at, 'YYYY-MM-DD')
            ORDER BY date ASC
            """, nativeQuery = true)

    List<Object[]> findDailyUserRegistrations(@Param("since") LocalDateTime since);

    // [가빈] 관리자 회원 목록 — 상태 필터 + 키워드 검색 (이름, 이메일)
    @Query("SELECT u FROM User u WHERE (:status IS NULL OR u.status = :status) AND (:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> findUsersWithFilter(@Param("status") UserStatus status, @Param("keyword") String keyword, Pageable pageable);

    //4. 마이페이지 -1. 개인정보 불러오기 :
    //5. 마이페이지 -1. 계정별 기부 결과 불러오기:
    //6. 기부내역 -1. 기부 증서 조회하기
    //7. 마이페이지 -2. 찜 등록
    //8. 마이페이지 -3. 찜 해제
    //9. 마이페이지 4- 찜 조회
}
