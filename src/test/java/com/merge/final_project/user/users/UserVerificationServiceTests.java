//package com.merge.final_project.user.users;
//
//import com.merge.final_project.user.signUp.UserSignUpRepository;
//import com.merge.final_project.user.users.LoginType;
//import com.merge.final_project.user.verify.EmailVerification;
//import com.merge.final_project.user.verify.EmailVerificationRepository;
//import com.merge.final_project.user.verify.VerificationIssuer;
//import com.merge.final_project.user.verify.VerificationServiceImpl;
//import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
//import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//public class UserVerificationServiceTests {
//    @InjectMocks
//    private VerificationServiceImpl verificationService;
//
//    @Mock
//    private EmailVerificationRepository emailVerificationRepository;
//
//    @Mock
//    private UserSignUpRepository userSignUpRepository;
//
//    @Mock
//    private VerificationIssuer verificationIssuer;
//
//    private UserVerifyRequestDTO requestDTO;
//    private EmailVerification verification;
//
//    @BeforeEach
//    void setUp() {
//        requestDTO = new UserVerifyRequestDTO("test@gmail.com", LoginType.LOCAL);
//
//        verification = EmailVerification.builder()
//                .emailVerifyNo(1L)
//                .email("test@gmail.com")
//                .verificationCode("123456")
//                .expiredAt(LocalDateTime.now().plusMinutes(5))
//                .verified(false)
//                .requestCount(0)
//                .attemptCount(0)
//                .build();
//    }
//
//    @Nested
//    @DisplayName("sendVerificationCode")
//    class SendVerificationCodeTest {
//
//        @Test
//        @DisplayName("성공: 가입되지 않은 이메일이면 인증번호를 발송한다")
//        void sendVerificationCodeSuccess() {
//            given(userSignUpRepository.existsByEmailAndLoginType("test@gmail.com", LoginType.LOCAL))
//                    .willReturn(false);
//
//            UserVerifyResponseDTO response = verificationService.sendVerificationCode(requestDTO);
//
//            assertThat(response.isSuccess()).isTrue();
//            assertThat(response.getMessage()).isEqualTo("인증번호가 발송되었습니다.");
//            verify(verificationIssuer).issue(
//                    eq("test@gmail.com"),
//                    eq("[giveNtoken] 회원가입 인증 번호입니다."),
//                    anyString()
//            );
//        }
//
//        @Test
//        @DisplayName("실패: 이미 가입된 이메일이면 예외가 발생한다")
//        void sendVerificationCodeAlreadyRegistered() {
//            given(userSignUpRepository.existsByEmailAndLoginType("test@gmail.com", LoginType.LOCAL))
//                    .willReturn(true);
//
//            IllegalStateException ex = assertThrows(
//                    IllegalStateException.class,
//                    () -> verificationService.sendVerificationCode(requestDTO)
//            );
//
//            assertThat(ex.getMessage()).isEqualTo("이미 가입된 이메일입니다.");
//        }
//    }
//
//    @Nested
//    @DisplayName("verifyCode")
//    class VerifyCodeTest {
//
//        @Test
//        @DisplayName("실패: 인증 요청 이력이 없는 이메일")
//        void verifyCodeNotFound() {
//            given(emailVerificationRepository.findByEmailForUpdate("test@gmail.com"))
//                    .willReturn(Optional.empty());
//
//            IllegalArgumentException ex = assertThrows(
//                    IllegalArgumentException.class,
//                    () -> verificationService.verifyCode("test@gmail.com", "123456")
//            );
//
//            assertThat(ex.getMessage()).isEqualTo("인증 요청 이력이 없는 이메일입니다.");
//        }
//
//        @Test
//        @DisplayName("실패: 이미 인증 완료된 이메일")
//        void verifyCodeAlreadyVerified() {
//            verification.setVerified(true);
//            given(emailVerificationRepository.findByEmailForUpdate("test@gmail.com"))
//                    .willReturn(Optional.of(verification));
//
//            IllegalStateException ex = assertThrows(
//                    IllegalStateException.class,
//                    () -> verificationService.verifyCode("test@gmail.com", "123456")
//            );
//
//            assertThat(ex.getMessage()).isEqualTo("이미 인증이 완료된 이메일입니다.");
//        }
//
//        @Test
//        @DisplayName("실패: 만료된 인증번호")
//        void verifyCodeExpired() {
//            verification.setExpiredAt(LocalDateTime.now().minusMinutes(1));
//            given(emailVerificationRepository.findByEmailForUpdate("test@gmail.com"))
//                    .willReturn(Optional.of(verification));
//
//            IllegalArgumentException ex = assertThrows(
//                    IllegalArgumentException.class,
//                    () -> verificationService.verifyCode("test@gmail.com", "123456")
//            );
//
//            assertThat(ex.getMessage()).isEqualTo("인증시간이 만료되었습니다. 다시 시도해주세요.");
//        }
//
//        @Test
//        @DisplayName("실패: 인증 시도 횟수 5회 초과")
//        void verifyCodeAttemptExceeded() {
//            verification.setAttemptCount(5);
//            given(emailVerificationRepository.findByEmailForUpdate("test@gmail.com"))
//                    .willReturn(Optional.of(verification));
//
//            IllegalArgumentException ex = assertThrows(
//                    IllegalArgumentException.class,
//                    () -> verificationService.verifyCode("test@gmail.com", "123456")
//            );
//
//            assertThat(ex.getMessage()).isEqualTo("인증 실패 횟수(5회)를 초과하여 무효 처리되었습니다.");
//        }
//
//        @Test
//        @DisplayName("실패: 인증번호가 틀리면 시도 횟수가 1 증가한다")
//        void verifyCodeMismatch() {
//            given(emailVerificationRepository.findByEmailForUpdate("test@gmail.com"))
//                    .willReturn(Optional.of(verification));
//
//            IllegalArgumentException ex = assertThrows(
//                    IllegalArgumentException.class,
//                    () -> verificationService.verifyCode("test@gmail.com", "000000")
//            );
//
//            assertThat(ex.getMessage()).contains("인증번호가 일치하지 않습니다.");
//            assertThat(verification.getAttemptCount()).isEqualTo(1);
//            assertThat(verification.isVerified()).isFalse();
//        }
//
//        @Test
//        @DisplayName("실패: 5번째 틀리면 만료 처리된다")
//        void verifyCodeMismatchFifthTime() {
//            verification.setAttemptCount(4);
//            given(emailVerificationRepository.findByEmailForUpdate("test@gmail.com"))
//                    .willReturn(Optional.of(verification));
//
//            IllegalArgumentException ex = assertThrows(
//                    IllegalArgumentException.class,
//                    () -> verificationService.verifyCode("test@gmail.com", "000000")
//            );
//
//            assertThat(ex.getMessage()).isEqualTo("인증번호가 5회 틀려 무효 처리되었습니다.");
//            assertThat(verification.getAttemptCount()).isEqualTo(5);
//            assertThat(verification.getExpiredAt()).isBeforeOrEqualTo(LocalDateTime.now());
//            assertThat(verification.isVerified()).isFalse();
//        }
//
//        @Test
//        @DisplayName("성공: 인증번호가 일치하면 verified=true")
//        void verifyCodeSuccess() {
//            given(emailVerificationRepository.findByEmailForUpdate("test@gmail.com"))
//                    .willReturn(Optional.of(verification));
//
//            boolean result = verificationService.verifyCode("test@gmail.com", "123456");
//
//            assertThat(result).isTrue();
//            assertThat(verification.isVerified()).isTrue();
//            assertThat(verification.getAttemptCount()).isEqualTo(0);
//        }
//    }
//
//    @Nested
//    @DisplayName("기타 메서드")
//    class HelperMethodsTest {
//
//        @Test
//        @DisplayName("isVerifiedEmail: 인증된 이메일이면 true")
//        void isVerifiedEmailTrue() {
//            verification.setVerified(true);
//            given(emailVerificationRepository.findByEmail("test@gmail.com"))
//                    .willReturn(Optional.of(verification));
//
//            boolean result = verificationService.isVerifiedEmail("test@gmail.com");
//
//            assertThat(result).isTrue();
//        }
//
//        @Test
//        @DisplayName("isVerifiedEmail: 없으면 false")
//        void isVerifiedEmailFalse() {
//            given(emailVerificationRepository.findByEmail("test@gmail.com"))
//                    .willReturn(Optional.empty());
//
//            boolean result = verificationService.isVerifiedEmail("test@gmail.com");
//
//            assertThat(result).isFalse();
//        }
//
//        @Test
//        @DisplayName("deleteVerification: 이메일로 삭제 요청")
//        void deleteVerification() {
//            verificationService.deleteVerification("test@gmail.com");
//
//            verify(emailVerificationRepository).deleteByEmail("test@gmail.com");
//        }
//
//        @Test
//        @DisplayName("sendPasswordResetCode: 인증번호를 발송한다")
//        void sendPasswordResetCode() {
//            verificationService.sendPasswordResetCode("test@gmail.com");
//
//            verify(verificationIssuer).issue(
//                    eq("test@gmail.com"),
//                    eq("[giveNtoken] 비밀번호 재설정 인증 번호입니다."),
//                    anyString()
//            );
//        }
//    }
//}
