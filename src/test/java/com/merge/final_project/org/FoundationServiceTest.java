package com.merge.final_project.org;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.utils.FileUtil;
import com.merge.final_project.notification.email.event.FoundationApprovedEvent;
import com.merge.final_project.notification.email.event.FoundationRejectedEvent;
import com.merge.final_project.org.dto.FoundationApplyRequestDTO;
import com.merge.final_project.org.illegalfoundation.IllegalFoundation;
import com.merge.final_project.org.illegalfoundation.IllegalFoundationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoundationServiceTest {

    @InjectMocks
    private FoundationServiceImpl foundationService;

    @Mock
    private FoundationRepository foundationRepository;

    @Mock
    private IllegalFoundationRepository illegalFoundationRepository;

    @Mock
    private FileUtil upload;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Foundation foundationOf(ReviewStatus reviewStatus, String rejectReason) {
        return Foundation.builder()
                .foundationEmail("test@test.com")
                .foundationName("테스트단체")
                .businessRegistrationNumber("123-45-67890")
                .representativeName("대표자")
                .description("설명")
                .contactPhone("010-0000-0000")
                .account("111-222-333")
                .feeRate(BigDecimal.valueOf(0.05))
                .bankName("국민은행")
                .foundationType(FoundationType.COMPANY)
                .accountStatus(AccountStatus.PRE_REGISTERED)
                .reviewStatus(reviewStatus)
                .rejectReason(rejectReason)
                .build();
    }

    // apply 테스트

    @Test
    @DisplayName("불법단체 해당 없는 정상 신청 시 CLEAN 상태로 저장된다")
    void 정상_가입신청_CLEAN() {
        FoundationApplyRequestDTO dto = mock(FoundationApplyRequestDTO.class);
        when(dto.getBusinessRegistrationNumber()).thenReturn("123-45-67890");
        when(dto.getFoundationName()).thenReturn("정상단체");
        when(dto.getRepresentativeName()).thenReturn("홍길동");
        when(dto.getFoundationEmail()).thenReturn("정상단체@test.com");
        when(dto.getContactPhone()).thenReturn("010-1234-5678");
        when(dto.getDescription()).thenReturn("설명");
        when(dto.getAccount()).thenReturn("123-456-789");
        when(dto.getBankName()).thenReturn("국민은행");
        when(dto.getFeeRate()).thenReturn(BigDecimal.valueOf(0.05));
        when(dto.getFoundationType()).thenReturn(FoundationType.COMPANY);

        when(foundationRepository.existsByBusinessRegistrationNumber("123-45-67890")).thenReturn(false);
        when(illegalFoundationRepository.findByNameAndRepresentative("정상단체", "홍길동")).thenReturn(Optional.empty());
        when(illegalFoundationRepository.findByNameContaining("정상단체")).thenReturn(List.of());

        foundationService.apply(dto, null);

        ArgumentCaptor<Foundation> captor = ArgumentCaptor.forClass(Foundation.class);
        verify(foundationRepository).save(captor.capture());
        assertThat(captor.getValue().getReviewStatus()).isEqualTo(ReviewStatus.CLEAN);
    }

    @Test
    @DisplayName("이미 등록된 사업자등록번호로 신청 시 DUPLICATE_BUSINESS_REGISTRATION 예외가 발생한다")
    void 사업자등록번호_중복_예외() {
        FoundationApplyRequestDTO dto = mock(FoundationApplyRequestDTO.class);
        when(dto.getBusinessRegistrationNumber()).thenReturn("123-45-67890");

        when(foundationRepository.existsByBusinessRegistrationNumber("123-45-67890")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> foundationService.apply(dto, null));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.DUPLICATE_BUSINESS_REGISTRATION.getMessage());
    }

    @Test
    @DisplayName("불법단체와 정확히 일치하는 단체 신청 시 ILLEGAL 상태와 사유가 저장된다")
    void 불법단체_정확일치_ILLEGAL() {
        FoundationApplyRequestDTO dto = mock(FoundationApplyRequestDTO.class);
        when(dto.getBusinessRegistrationNumber()).thenReturn("123-45-67890");
        when(dto.getFoundationName()).thenReturn("불법단체명");
        when(dto.getRepresentativeName()).thenReturn("불법대표자");
        when(dto.getFoundationEmail()).thenReturn("불법단체명@test.com");
        when(dto.getContactPhone()).thenReturn("010-1234-5678");
        when(dto.getDescription()).thenReturn("설명");
        when(dto.getAccount()).thenReturn("123-456-789");
        when(dto.getBankName()).thenReturn("국민은행");
        when(dto.getFeeRate()).thenReturn(BigDecimal.valueOf(0.05));
        when(dto.getFoundationType()).thenReturn(FoundationType.COMPANY);

        when(foundationRepository.existsByBusinessRegistrationNumber("123-45-67890")).thenReturn(false);

        IllegalFoundation illegalFoundation = mock(IllegalFoundation.class);
        when(illegalFoundation.getIllegalNo()).thenReturn(1L);
        when(illegalFoundation.getReason()).thenReturn("기부금 횡령");
        when(illegalFoundationRepository.findByNameAndRepresentative("불법단체명", "불법대표자"))
                .thenReturn(Optional.of(illegalFoundation));
        when(illegalFoundationRepository.findByNameContaining("불법단체명")).thenReturn(List.of(illegalFoundation));

        foundationService.apply(dto, null);

        ArgumentCaptor<Foundation> captor = ArgumentCaptor.forClass(Foundation.class);
        verify(foundationRepository).save(captor.capture());
        assertThat(captor.getValue().getReviewStatus()).isEqualTo(ReviewStatus.ILLEGAL);
        assertThat(captor.getValue().getRejectReason()).isEqualTo("기부금 횡령");
    }

    @Test
    @DisplayName("유사한 불법단체가 존재하는 경우 SIMILAR 상태로 저장된다")
    void 유사단체_존재_SIMILAR() {
        FoundationApplyRequestDTO dto = mock(FoundationApplyRequestDTO.class);
        when(dto.getBusinessRegistrationNumber()).thenReturn("123-45-67890");
        when(dto.getFoundationName()).thenReturn("유사단체명");
        when(dto.getRepresentativeName()).thenReturn("홍길동");
        when(dto.getFoundationEmail()).thenReturn("유사단체명@test.com");
        when(dto.getContactPhone()).thenReturn("010-1234-5678");
        when(dto.getDescription()).thenReturn("설명");
        when(dto.getAccount()).thenReturn("123-456-789");
        when(dto.getBankName()).thenReturn("국민은행");
        when(dto.getFeeRate()).thenReturn(BigDecimal.valueOf(0.05));
        when(dto.getFoundationType()).thenReturn(FoundationType.COMPANY);

        when(foundationRepository.existsByBusinessRegistrationNumber("123-45-67890")).thenReturn(false);
        when(illegalFoundationRepository.findByNameAndRepresentative("유사단체명", "홍길동")).thenReturn(Optional.empty());

        IllegalFoundation similarFoundation = mock(IllegalFoundation.class);
        when(illegalFoundationRepository.findByNameContaining("유사단체명")).thenReturn(List.of(similarFoundation));

        foundationService.apply(dto, null);

        ArgumentCaptor<Foundation> captor = ArgumentCaptor.forClass(Foundation.class);
        verify(foundationRepository).save(captor.capture());
        assertThat(captor.getValue().getReviewStatus()).isEqualTo(ReviewStatus.SIMILAR);
    }

    // 기부단체 approve 테스트

    @Test
    @DisplayName("정상 단체를 승인하면 APPROVED 상태가 되고 승인 이벤트가 발행된다")
    void 단체_승인_성공() {
        Foundation foundation = foundationOf(ReviewStatus.CLEAN, null);
        when(foundationRepository.findById(1L)).thenReturn(Optional.of(foundation));
        when(passwordEncoder.encode(any())).thenReturn("encodedTempPassword");

        foundationService.approveFoundation(1L);

        assertThat(foundation.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);
        assertThat(foundation.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(eventPublisher).publishEvent(any(FoundationApprovedEvent.class));
    }

    @Test
    @DisplayName("불법단체를 승인 시도하면 CANNOT_APPROVE_ILLEGAL_FOUNDATION 예외가 발생한다")
    void 불법단체_승인_예외() {
        Foundation foundation = foundationOf(ReviewStatus.ILLEGAL, "기부금 횡령");
        when(foundationRepository.findById(1L)).thenReturn(Optional.of(foundation));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> foundationService.approveFoundation(1L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.CANNOT_APPROVE_ILLEGAL_FOUNDATION.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 단체 승인 시도 시 FOUNDATION_NOT_FOUND 예외가 발생한다")
    void 존재하지않는_단체_승인_예외() {
        when(foundationRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> foundationService.approveFoundation(999L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.FOUNDATION_NOT_FOUND.getMessage());
    }

    // 기부단체 reject 테스트

    @Test
    @DisplayName("단체를 반려하면 REJECTED 상태가 되고 반려 이벤트가 발행된다")
    void 단체_반려_성공() {
        Foundation foundation = foundationOf(ReviewStatus.SIMILAR, "유사단체 존재");
        when(foundationRepository.findById(1L)).thenReturn(Optional.of(foundation));

        foundationService.rejectFoundationForIllegal(1L);

        assertThat(foundation.getReviewStatus()).isEqualTo(ReviewStatus.REJECTED);
        assertThat(foundation.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
        verify(eventPublisher).publishEvent(any(FoundationRejectedEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 단체 반려 시도 시 FOUNDATION_NOT_FOUND 예외가 발생한다")
    void 존재하지않는_단체_반려_예외() {
        when(foundationRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> foundationService.rejectFoundationForIllegal(999L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.FOUNDATION_NOT_FOUND.getMessage());
    }
}
