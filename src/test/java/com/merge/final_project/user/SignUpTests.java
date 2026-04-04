package com.merge.final_project.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class SignUpTests {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void PasswordEncorederTest(){
        String password="111";
        String enpw=passwordEncoder.encode(password);
        System.out.println("enpw="+enpw);
        boolean matchResult=passwordEncoder.matches(password,enpw);
        System.out.println("m="+matchResult);
    }
}
