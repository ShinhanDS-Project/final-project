package com.merge.final_project.admin;

import com.merge.final_project.admin.admins.Admin;
import com.merge.final_project.admin.admins.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AdminRepositoryTests {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void 아이디_조회 () {
        Optional<Admin> admin = adminRepository.findByAdminId("admin_gabeen");

        assertTrue(admin.isPresent());
        assertEquals("admin_gabeen", admin.get().getAdminId());
    }

    @Test
    void 비밀번호_암호화_확인() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("1234"));
    }
}
