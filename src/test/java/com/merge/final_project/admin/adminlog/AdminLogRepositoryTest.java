package com.merge.final_project.admin.adminlog;

import com.merge.final_project.admin.Admin;
import com.merge.final_project.admin.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AdminLogRepositoryTest {

    @Autowired
    private AdminLogRepository adminLogRepository;

    @Autowired
    private AdminRepository adminRepository;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = adminRepository.save(Admin.builder()
                .adminId("test-admin")
                .password("pw")
                .name("테스트관리자")
                .adminRole("ROLE_ADMIN")
                .build());
    }

    private AdminLog log(ActionType actionType, TargetType targetType, Long targetNo) {
        return AdminLog.builder()
                .actionType(actionType)
                .targetType(targetType)
                .targetNo(targetNo)
                .description(actionType + " " + targetType)
                .admin(admin)
                .build();
    }

    @Test
    @DisplayName("필터 없이 전체 조회 시 저장된 로그가 모두 반환된다")
    void 전체_조회() {
        adminLogRepository.save(log(ActionType.APPROVE, TargetType.FOUNDATION, 1L));
        adminLogRepository.save(log(ActionType.REJECT, TargetType.CAMPAIGN, 2L));

        Page<AdminLog> result = adminLogRepository.findByFilter(null, null, null, null, "", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("actionType 필터 적용 시 해당 타입만 반환된다")
    void actionType_필터() {
        adminLogRepository.save(log(ActionType.APPROVE, TargetType.FOUNDATION, 1L));
        adminLogRepository.save(log(ActionType.REJECT, TargetType.FOUNDATION, 2L));

        Page<AdminLog> result = adminLogRepository.findByFilter("APPROVE", null, null, null, "", PageRequest.of(0, 10));

        assertThat(result.getContent()).allMatch(l -> l.getActionType() == ActionType.APPROVE);
    }

    @Test
    @DisplayName("targetType 필터 적용 시 해당 타입만 반환된다")
    void targetType_필터() {
        adminLogRepository.save(log(ActionType.APPROVE, TargetType.FOUNDATION, 1L));
        adminLogRepository.save(log(ActionType.APPROVE, TargetType.CAMPAIGN, 2L));

        Page<AdminLog> result = adminLogRepository.findByFilter(null, "FOUNDATION", null, null, "", PageRequest.of(0, 10));

        assertThat(result.getContent()).allMatch(l -> l.getTargetType() == TargetType.FOUNDATION);
    }

    @Test
    @DisplayName("날짜 범위 필터 적용 시 범위 내 로그만 반환된다")
    void 날짜_범위_필터() {
        adminLogRepository.save(log(ActionType.APPROVE, TargetType.FOUNDATION, 1L));

        LocalDateTime start = LocalDateTime.now().minusMinutes(1);
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);

        Page<AdminLog> result = adminLogRepository.findByFilter(null, null, start, end, "", PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("날짜 범위 밖의 로그는 조회되지 않는다")
    void 날짜_범위_밖_제외() {
        adminLogRepository.save(log(ActionType.APPROVE, TargetType.FOUNDATION, 1L));

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        Page<AdminLog> result = adminLogRepository.findByFilter(null, null, start, end, "", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("actionType과 targetType 동시 필터 시 두 조건 모두 만족하는 로그만 반환된다")
    void actionType_targetType_복합_필터() {
        adminLogRepository.save(log(ActionType.APPROVE, TargetType.FOUNDATION, 1L));
        adminLogRepository.save(log(ActionType.APPROVE, TargetType.CAMPAIGN, 2L));
        adminLogRepository.save(log(ActionType.REJECT, TargetType.FOUNDATION, 3L));

        Page<AdminLog> result = adminLogRepository.findByFilter("APPROVE", "FOUNDATION", null, null, "", PageRequest.of(0, 10));

        assertThat(result.getContent()).allMatch(l ->
                l.getActionType() == ActionType.APPROVE && l.getTargetType() == TargetType.FOUNDATION
        );
    }
}
