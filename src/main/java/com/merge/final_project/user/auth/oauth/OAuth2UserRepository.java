package com.merge.final_project.user.auth.oauth;

import com.merge.final_project.user.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2UserRepository extends JpaRepository<User, Integer> {

}
