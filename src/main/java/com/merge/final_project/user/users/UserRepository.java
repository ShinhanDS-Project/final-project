package com.merge.final_project.user.users;

import org.springframework.data.domain.Page; // [가빈] 추가
import org.springframework.data.domain.Pageable; // [가빈] 추가
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    Optional<User> findByPhoneAndName(String phone,String name);

    Optional<User> findByEmailAndName(String email,String name);

    Optional<User> findByEmailAndNameAndLoginType(String email, String name, LoginType loginType);

    Optional<User> findByUserNo(Long userNo);

    boolean existsByNameHash(String nameHash);

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

    // [가빈] 전체 공지용 - 활성 회원 PK 목록
    @Query("SELECT u.userNo FROM User u WHERE u.status = 'ACTIVE'")
    List<Long> findAllActiveUserNos();

    //4. 마이페이지 -1. 개인정보 불러오기 :


    //6. 기부내역 -1. 기부 증서 조회하기
}
