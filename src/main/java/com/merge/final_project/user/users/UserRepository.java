package com.merge.final_project.user.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);
    //4. 마이페이지 -1. 개인정보 불러오기 :
    //5. 마이페이지 -1. 계정별 기부 결과 불러오기:
    //6. 기부내역 -1. 기부 증서 조회하기
    //7. 마이페이지 -2. 찜 등록
    //8. 마이페이지 -3. 찜 해제
    //9. 마이페이지 4- 찜 조회
}
