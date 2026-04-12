package com.merge.final_project.user.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmailAndLoginType(String email, LoginType loginType);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneAndName(String phone,String name);

    Optional<User> findByEmailAndName(String email,String name);

    Optional<User> findByUserNo(Long userNo);

    boolean existsByNameHash(String nameHash);
    //4. 마이페이지 -1. 개인정보 불러오기 :


    //6. 기부내역 -1. 기부 증서 조회하기
}
