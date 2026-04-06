package com.merge.final_project.user.signUp;

import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSignUpRepository extends JpaRepository<User, Long> {
    boolean existsByEmailAndLoginType(String email, LoginType loginType);

    boolean existsByPhone(String phone);

    Optional<User> findByEmailAndLoginType(String email, LoginType loginType);

    boolean findByPhoneAndLoginType(String phone, LoginType loginType);

    boolean existsByNameHash(String hash);
}
