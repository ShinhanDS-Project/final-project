package com.merge.final_project.admin;

import com.merge.final_project.admin.admins.Admin;
import com.merge.final_project.admin.admins.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AdminRepositoryTests {

    @Autowired
    private AdminRepository adminRepository;

    //id 조회 테스트
    @Test
    public void testLogin () {
        Optional<Admin> admin = adminRepository.findByAdminId("admin_gabeen");

        assertTrue(admin.isPresent());
        assertEquals("admin_gabeen", admin.get().getAdminId());
    }
}
