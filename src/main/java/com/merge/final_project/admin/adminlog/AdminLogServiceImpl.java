package com.merge.final_project.admin.adminlog;

import com.merge.final_project.admin.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl implements AdminLogService{

    private final AdminLogRepository adminLogRepository;

    // 관리자의 활동 이력을 저장하는 메서드. description은 호출하는 쪽에서 조합할 예정. Admin 객체는 호출하는 곳에서 시큐리티 컨텍스트에서 id 넘겨서 조회할 것임.
    @Override
    public void log(ActionType actionType, TargetType targetType, Long targetNo, String description, Admin admin) {
        AdminLog log = AdminLog.builder()
                .actionType(actionType)
                .targetType(targetType)
                .targetNo(targetNo)
                .description(description)
                .admin(admin)
                .build();
        adminLogRepository.save(log);

    }

    //필터링적용한 목록 조회 기능. 각 타입들이 있으면 해당 조건으로 필터링 걸 것이고, null이면 제외. 즉 필터 조건이 다 null이면 전체 조회.
    //adminId fetch가 lazy이기에 문제가 발생하는 걸 트랜잭션을 걸어줘서 DTO 변환 시점까지 lazy loading 가능하게 수정함.
    @Transactional(readOnly = true)
    @Override
    public Page<AdminLogResponseDTO> getLogsWithFilter(ActionType actionType, TargetType targetType, LocalDateTime startDate, LocalDateTime endDate, String keyword, Pageable pageable) {
        return adminLogRepository.findByFilter(
                        actionType != null ? actionType.name() : null,
                        targetType != null ? targetType.name() : null,
                        startDate,
                        endDate,
                        keyword != null ? keyword : "",
                        pageable)
                .map(AdminLogResponseDTO::from);
    }

}
