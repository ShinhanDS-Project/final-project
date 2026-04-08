package com.merge.final_project.admin.adminlog;

import com.merge.final_project.admin.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    @Override
    public Page<AdminLogResponseDTO> getLogsWithFilter(ActionType actionType, TargetType targetType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return adminLogRepository.findByFilter(
                        actionType != null ? actionType.name() : null,
                        targetType != null ? targetType.name() : null,
                        startDate,
                        endDate,
                        pageable)
                .map(AdminLogResponseDTO::from);
    }

}
