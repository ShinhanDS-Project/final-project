package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.service.AdminUserService;
import com.merge.final_project.user.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [가빈] 관리자 회원 활성화/비활성화 컨트롤러
// 회원 관련 나머지 기능(조회 등)은 이채원 팀원 코드 참고
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 회원 활성화
    @PatchMapping("/{userNo}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long userNo) {
        adminUserService.activateUser(userNo);
        return ResponseEntity.ok().build();
    }

    // 회원 비활성화
    @PatchMapping("/{userNo}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long userNo) {
        adminUserService.deactivateUser(userNo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getUsers(Pageable pageable) {

    }
}
