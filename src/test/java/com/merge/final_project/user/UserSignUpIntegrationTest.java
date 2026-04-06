package com.merge.final_project.user;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.signUp.UserSignUpService;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import com.merge.final_project.user.verify.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional // 테스트 후 DB를 자동으로 롤백해줍니다.
public class UserSignUpIntegrationTest {

    @Autowired
    private UserSignUpService userSignUpService;

    @Autowired
    private UserSignUpRepository userSignUpRepository;

    @MockitoBean
    private VerificationService verificationService; // 이메일 인증 서비스 모킹

    @BeforeEach
    void setUp() {
        // 기본적으로 모든 이메일 인증은 성공한 것으로 설정
        when(verificationService.isVerifiedEmail(anyString())).thenReturn(true);
    }
    @Test
    @DisplayName("구글 회원가입 성공 - 필수 정보 저장 확인")
    void googleSignUpSuccess() {
        // Given: 구글 가입 정보 준비
        UserSignUpRequestDTO dto = UserSignUpRequestDTO.builder()
                .email("chaewon@gmail.com")
                .name("이채원")
                .phone("010-1234-5678")
                .birth(LocalDate.of(2000, 2, 14))
                .loginType(LoginType.GOOGLE)
                .build();

        // When: 서비스 호출
        userSignUpService.register(dto);

        // Then: DB에서 유저를 찾아 검증
        User savedUser = userSignUpRepository.findByEmailAndLoginType("chaewon@gmail.com", LoginType.GOOGLE)
                .orElseThrow(() -> new AssertionError("유저가 DB에 저장되지 않았습니다."));

        assertAll(
                () -> assertEquals("이채원", savedUser.getName()),
                () -> assertEquals(UserStatus.ACTIVE, savedUser.getStatus()),
                () -> assertNull(savedUser.getPasswordHash()), // 구글 가입은 비번이 없어야 함
                () -> assertNotNull(savedUser.getNameHash())   // 해시값이 생성되었는지 확인
        );
    }

    @Test
    @DisplayName("실패: 이미 가입된 전화번호로 가입 시도")
    void duplicatePhoneFail() {
        // Given: 한 명을 먼저 가입시킴
        UserSignUpRequestDTO dto1 = UserSignUpRequestDTO.builder()
                .email("user1@gmail.com")
                .phone("010-9999-8888")
                .name("테스터1")
                .birth(LocalDate.of(1995, 1, 1))
                .loginType(LoginType.LOCAL)
                .password("pw1234!")
                .build();
        userSignUpService.register(dto1);

        // When & Then: 동일한 폰 번호로 다른 이메일 가입 시도
        UserSignUpRequestDTO dto2 = UserSignUpRequestDTO.builder()
                .email("user2@gmail.com")
                .phone("010-9999-8888") // 중복된 번호
                .name("테스터2")
                .birth(LocalDate.of(1995, 5, 5))
                .loginType(LoginType.GOOGLE)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userSignUpService.register(dto2);
        });

        assertEquals("이미 가입된 전화번호입니다.", exception.getMessage());
    }


    @MockitoBean
    private JavaMailSender javaMailSender;


    @Autowired
    private PasswordEncoder passwordEncoder;


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

        assertAll(
                () -> assertEquals("local1@test.com", savedUser.getEmail()),
                () -> assertEquals("테스터", savedUser.getName()),
                () -> assertEquals("010-1111-1111", savedUser.getPhone()),
                () -> assertEquals(LocalDate.of(1990, 1, 1), savedUser.getBirth()),
                () -> assertEquals(LoginType.LOCAL, savedUser.getLoginType()),
                () -> assertEquals(UserStatus.ACTIVE, savedUser.getStatus()),
                () -> assertEquals(0, savedUser.getLoginCount()),
                () -> assertNotNull(savedUser.getPasswordHash()),
                () -> assertNotEquals("1234", savedUser.getPasswordHash()),
                () -> assertTrue(passwordEncoder.matches("1234", savedUser.getPasswordHash())),
                () -> assertNotNull(savedUser.getNameHash()),
                () -> assertFalse(savedUser.getNameHash().isBlank())
        );
    }

    @Test
    @DisplayName("구글 회원가입 성공")
    void googleSignUpSuccessTest() {
        UserSignUpRequestDTO dto = createGoogleDto("google13@test.com", "010-2222-2222");

        userSignUpService.register(dto);

        User savedUser = userSignUpRepository.findByEmailAndLoginType("google13@test.com", LoginType.GOOGLE)
                .orElseThrow(() -> new AssertionError("구글 유저가 저장되지 않았습니다."));

        assertAll(
                () -> assertEquals("google13@test.com", savedUser.getEmail()),
                () -> assertEquals("구글테스터", savedUser.getName()),
                () -> assertEquals("010-2222-2222", savedUser.getPhone()),
                () -> assertEquals(LocalDate.of(1995, 5, 5), savedUser.getBirth()),
                () -> assertEquals(LoginType.GOOGLE, savedUser.getLoginType()),
                () -> assertEquals(UserStatus.ACTIVE, savedUser.getStatus()),
                () -> assertEquals(0, savedUser.getLoginCount()),
                () -> assertNull(savedUser.getPasswordHash()),
                () -> assertNotNull(savedUser.getNameHash()),
                () -> assertFalse(savedUser.getNameHash().isBlank())
        );
    }

    @Test
    @DisplayName("전화번호 중복이면 회원가입 실패")
    void duplicatePhoneFailTest() {
        UserSignUpRequestDTO dto1 = createLocalDto("phone1@test.com", "010-3333-3333");
        UserSignUpRequestDTO dto2 = createLocalDto("phone2@test.com", "010-3333-3333");

        userSignUpService.register(dto1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto2)
        );

        assertEquals("이미 가입된 전화번호입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("같은 이메일 + 같은 LOCAL 타입이면 회원가입 실패")
    void duplicateLocalEmailFailTest() {
        UserSignUpRequestDTO dto1 = createLocalDto("same@test.com", "010-4444-4444");
        UserSignUpRequestDTO dto2 = createLocalDto("same@test.com", "010-5555-5555");

        userSignUpService.register(dto1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto2)
        );

        assertEquals("이미 존재하는 로컬 계정입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("같은 이메일 + 같은 GOOGLE 타입이면 회원가입 실패")
    void duplicateGoogleEmailFailTest() {
        UserSignUpRequestDTO dto1 = createGoogleDto("googledup@test.com", "010-6666-6666");
        UserSignUpRequestDTO dto2 = createGoogleDto("googledup@test.com", "010-7777-7777");

        userSignUpService.register(dto1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto2)
        );

        assertEquals("이미 가입된 구글 계정입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("같은 이메일이어도 loginType이 다르면 가입 가능")
    void sameEmailDifferentLoginTypeSuccessTest() {
        UserSignUpRequestDTO localDto = createLocalDto("multi@test.com", "010-8888-8888");
        UserSignUpRequestDTO googleDto = createGoogleDto("multi@test.com", "010-9999-9999");

        userSignUpService.register(localDto);
        userSignUpService.register(googleDto);

        User localUser = userSignUpRepository.findByEmailAndLoginType("multi@test.com", LoginType.LOCAL)
                .orElseThrow(() -> new AssertionError("LOCAL 유저가 없어."));
        User googleUser = userSignUpRepository.findByEmailAndLoginType("multi@test.com", LoginType.GOOGLE)
                .orElseThrow(() -> new AssertionError("GOOGLE 유저가 없어."));

        System.out.println("========================================");
        System.out.println("가입된 LOCAL 유저: " + localUser.getEmail() + " | 해시: " + localUser.getNameHash());
        System.out.println("가입된 GOOGLE 유저: " + googleUser.getEmail() + " | 해시: " + googleUser.getNameHash());
        System.out.println("========================================");
        assertAll(
                () -> assertEquals(LoginType.LOCAL, localUser.getLoginType()),
                () -> assertEquals(LoginType.GOOGLE, googleUser.getLoginType())
        );
    }

    @Test
    @DisplayName("nameHash는 생성되어야 한다")
    void nameHashGeneratedTest() {
        UserSignUpRequestDTO dto = createLocalDto("hash@test.com", "010-1212-3434");

        userSignUpService.register(dto);

        User savedUser = userSignUpRepository.findByEmailAndLoginType("hash@test.com", LoginType.LOCAL)
                .orElseThrow(() -> new AssertionError("회원가입된 유저가 없어."));

        assertNotNull(savedUser.getNameHash());
        assertFalse(savedUser.getNameHash().isBlank());
    }

    @Test
    @DisplayName("loginType이 null이면 회원가입 실패")
    void nullLoginTypeFailTest() {
        UserSignUpRequestDTO dto = UserSignUpRequestDTO.builder()
                .email("nulltype@test.com")
                .password("1234")
                .name("테스터")
                .phone("010-1234-0000")
                .birth(LocalDate.of(1990, 1, 1))
                .profilePath("/path/to/profile.png")
                .loginType(null)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto)
        );

        assertEquals("로그인 타입은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("LOCAL 회원가입에서 비밀번호가 없으면 실패")
    void localPasswordNullFailTest() {
        UserSignUpRequestDTO dto = UserSignUpRequestDTO.builder()
                .email("nopw@test.com")
                .password(null)
                .name("테스터")
                .phone("010-1111-0000")
                .birth(LocalDate.of(1990, 1, 1))
                .profilePath("/path/to/profile.png")
                .loginType(LoginType.LOCAL)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto)
        );

        assertEquals("비밀번호는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("LOCAL 회원가입에서 이메일 미인증이면 실패")
    void localEmailNotVerifiedFailTest() {
        String email = "unverified@test.com";
        UserSignUpRequestDTO dto = createLocalDto(email, "010-2222-0000");

        when(verificationService.isVerifiedEmail(email)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto)
        );

        assertEquals("이메일 인증이 완료되지 않았습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("LOCAL 회원가입에서 이메일이 null이면 실패")
    void localEmailNullFailTest() {
        UserSignUpRequestDTO dto = UserSignUpRequestDTO.builder()
                .email(null)
                .password("1234")
                .name("테스터")
                .phone("010-3333-0000")
                .birth(LocalDate.of(1990, 1, 1))
                .profilePath("/path/to/profile.png")
                .loginType(LoginType.LOCAL)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto)
        );

        assertEquals("이메일은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("LOCAL 회원가입에서 이름이 null이면 실패")
    void localNameNullFailTest() {
        UserSignUpRequestDTO dto = UserSignUpRequestDTO.builder()
                .email("noname@test.com")
                .password("1234")
                .name(null)
                .phone("010-4444-0000")
                .birth(LocalDate.of(1990, 1, 1))
                .profilePath("/path/to/profile.png")
                .loginType(LoginType.LOCAL)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto)
        );

        assertEquals("이름은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("GOOGLE 회원가입에서 이메일이 null이면 실패")
    void googleEmailNullFailTest() {
        UserSignUpRequestDTO dto = UserSignUpRequestDTO.builder()
                .email(null)
                .password(null)
                .name("구글테스터")
                .phone("010-5555-0000")
                .birth(LocalDate.of(1995, 5, 5))
                .profilePath("/path/to/google-profile.png")
                .loginType(LoginType.GOOGLE)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto)
        );

        assertEquals("구글 계정 이메일이 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("GOOGLE 회원가입에서 이름이 null이면 실패")
    void googleNameNullFailTest() {
        UserSignUpRequestDTO dto = UserSignUpRequestDTO.builder()
                .email("google-no-name@test.com")
                .password(null)
                .name(null)
                .phone("010-6666-0000")
                .birth(LocalDate.of(1995, 5, 5))
                .profilePath("/path/to/google-profile.png")
                .loginType(LoginType.GOOGLE)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSignUpService.register(dto)
        );

        assertEquals("구글 계정 이름이 없습니다.", exception.getMessage());
    }
}