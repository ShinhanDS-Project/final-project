package com.merge.final_project.user;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.signUp.UserSignUpService;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
@Transactional // 테스트가 끝나면 DB를 자동으로 롤백해줍니다.
@SpringBootTest
public class SignUpTests {
    @MockitoBean
    private JavaMailSender javaMailSender;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserSignUpService userSignUpService;

    @Autowired
    UserSignUpRepository userSignUpRepository;
    //비밀번호 틀렸는지 확인하는 test
    @Test
    public void PasswordEncorederTest(){
        String password="111";
        String enpw=passwordEncoder.encode(password);
        System.out.println("enpw="+enpw);
        boolean matchResult=passwordEncoder.matches(password,enpw);
        System.out.println("m="+matchResult);
    }

//    @Test
//    void localSignUpTest() throws IllegalAccessException {
//        UserSignUpRequestDTO requestDTO= new UserSignUpRequestDTO(
//                "tester@test.com",
//                "rawPassword123!",
//                "테스터",
//                "010-9999-8888",
//                LocalDate.of(1990, 1, 1),
//                LoginType.LOCAL,
//                "/path/to/profile.png"
//        );
//
//        userSignUpService.register(requestDTO);
//
//
//
//    }
private UserSignUpRequestDTO createLocalDto(String email, String phone) {
    return UserSignUpRequestDTO.builder()
            .email(email)
            .password("1234")
            .name("테스터")
            .phone(phone)
            .birth(LocalDate.of(1990, 1, 1))
            .profilePath("/path/to/profile.png")
            .loginType(LoginType.LOCAL)
            .build();
}
    private UserSignUpRequestDTO createGoogleDto(String email, String phone) {
        return UserSignUpRequestDTO.builder()
                .email(email)
                .password(null)
                .name("구글테스터")
                .phone(phone)
                .birth(LocalDate.of(1995, 5, 5))
                .profilePath("/path/to/google-profile.png")
                .loginType(LoginType.GOOGLE)
                .build();
    }
    @Test
    @DisplayName("로컬 회원가입 성공")
    void localSignUpSuccessTest() {
        UserSignUpRequestDTO dto = createLocalDto("local1@test.com", "010-1111-1111");

        userSignUpService.register(dto);

        User savedUser = userSignUpRepository.findByEmailAndLoginType("local1@test.com", LoginType.LOCAL)
                .orElseThrow(() -> new AssertionError("회원가입된 유저가 없어."));

        assertEquals("local1@test.com", savedUser.getEmail());
        assertEquals("테스터", savedUser.getName());
        assertEquals("010-1111-1111", savedUser.getPhone());
        assertEquals(LoginType.LOCAL, savedUser.getLoginType());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertEquals(0, savedUser.getLoginCount());

        assertNotNull(savedUser.getPasswordHash());
        assertNotEquals("1234", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("1234", savedUser.getPasswordHash()));

        assertNotNull(savedUser.getNameHash());
        assertFalse(savedUser.getNameHash().isBlank());

        //assertNotNull(savedUser.getWallet());
    }

    @Test
    @DisplayName("전화번호 중복이면 회원가입 실패")
    void duplicatePhoneFailTest() {
        UserSignUpRequestDTO dto1 = createLocalDto("phone1@test.com", "010-2222-2222");
        UserSignUpRequestDTO dto2 = createLocalDto("phone2@test.com", "010-2222-2222");

        userSignUpService.register(dto1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto2)
        );

        assertEquals("이미 존재하는 회원 정보입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("같은 이메일 + 같은 loginType이면 회원가입 실패")
    void duplicateEmailSameLoginTypeFailTest() {
        UserSignUpRequestDTO dto1 = createLocalDto("same@test.com", "010-3333-3333");
        UserSignUpRequestDTO dto2 = createLocalDto("same@test.com", "010-4444-4444");

        userSignUpService.register(dto1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto2)
        );

        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("loginType이 null이면 회원가입 실패")
    void nullLoginTypeFailTest() {
        UserSignUpRequestDTO dto = UserSignUpRequestDTO.builder()
                .email("nulltype@test.com")
                .password("1234")
                .name("테스터")
                .phone("010-5555-5555")
                .birth(LocalDate.of(1990, 1, 1))
                .profilePath("/path/to/profile.png")
                .loginType(null)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto)
        );

        assertEquals("잘못된 접근입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("구글 회원가입 성공")
    void googleSignUpSuccessTest() {
        UserSignUpRequestDTO dto = createGoogleDto("google1@test.com", "010-6666-6666");

        userSignUpService.register(dto);

        User savedUser = userSignUpRepository.findByEmailAndLoginType("google1@test.com", LoginType.GOOGLE)
                .orElseThrow(() -> new AssertionError("잘못된 접근입니다."));

        assertEquals("google1@test.com", savedUser.getEmail());
        assertEquals(LoginType.GOOGLE, savedUser.getLoginType());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertNull(savedUser.getPasswordHash());

        assertNotNull(savedUser.getNameHash());
       // assertNotNull(savedUser.getWallet());
    }

    @Test
    @DisplayName("같은 이메일이어도 loginType이 다르면 가입 가능 - 현재 정책 기준")
    void sameEmailDifferentLoginTypeSuccessTest() {
        UserSignUpRequestDTO localDto = createLocalDto("multi@test.com", "010-7777-7777");
        UserSignUpRequestDTO googleDto = createGoogleDto("multi@test.com", "010-8888-8888");

        userSignUpService.register(localDto);
        userSignUpService.register(googleDto);

        User localUser = userSignUpRepository.findByEmailAndLoginType("multi@test.com", LoginType.LOCAL)
                .orElseThrow(() -> new AssertionError("LOCAL 유저가 없어."));
        User googleUser = userSignUpRepository.findByEmailAndLoginType("multi@test.com", LoginType.GOOGLE)
                .orElseThrow(() -> new AssertionError("GOOGLE 유저가 없어."));

        assertEquals(LoginType.LOCAL, localUser.getLoginType());
        assertEquals(LoginType.GOOGLE, googleUser.getLoginType());
    }

    @Test
    @DisplayName("nameHash는 생성되어야 한다")
    void nameHashGeneratedTest() {
        UserSignUpRequestDTO dto = createLocalDto("hash@test.com", "010-9999-1111");

        userSignUpService.register(dto);

        User savedUser = userSignUpRepository.findByEmailAndLoginType("hash@test.com", LoginType.LOCAL)
                .orElseThrow(() -> new AssertionError("회원가입된 유저가 없어."));

        assertNotNull(savedUser.getNameHash());
        assertFalse(savedUser.getNameHash().isBlank());
    }

    @Test
    @DisplayName("LOCAL 회원가입에서 비밀번호가 없으면 실패")
    void localPasswordNullFailTest() {
        UserSignUpRequestDTO dto = UserSignUpRequestDTO.builder()
                .email("nopw@test.com")
                .password(null)
                .name("테스터")
                .phone("010-1212-3434")
                .birth(LocalDate.of(1990, 1, 1))
                .profilePath("/path/to/profile.png")
                .loginType(LoginType.LOCAL)
                .build();

        assertThrows(IllegalArgumentException.class, () -> userSignUpService.register(dto));
    }
//    @Test
//    @DisplayName("회원가입 시 wallet이 생성되고 연결되어야 한다")
//    void walletCreatedAndConnectedTest() {
//        UserSignUpRequestDTO dto = createLocalDto("wallet@test.com", "010-9999-2222");
//
//        userSignUpService.register(dto);
//
//        User savedUser = userSignUpRepository.findByEmailAndLoginType("wallet@test.com", LoginType.LOCAL)
//                .orElseThrow(() -> new AssertionError("회원가입된 유저가 없어."));
//
//        assertNotNull(savedUser.getWallet());
//        assertNotNull(savedUser.getWallet().getWalletNo());
//    }

}
