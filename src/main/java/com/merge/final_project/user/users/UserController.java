package com.merge.final_project.user.users;

import com.merge.final_project.donation.donations.DonationService;
import com.merge.final_project.user.users.dto.support.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/support")
public class UserController {
    //부가기능 또는 마이페이지 전용 컨트롤러
    @Autowired
    private UserService userService;
    @Autowired
    private DonationService donationService;

    // 이메일 찾기
    @PostMapping("/email")
    public ResponseEntity<EmailResponseDTO> email(@Valid @RequestBody EmailRequestDTO request) {
        EmailResponseDTO response=userService.findEmail(request.getPhone(),request.getName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/password/reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody ChangePasswordRequestDTO dto) {
        userService.requestPasswordReset(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset/verify")
    public ResponseEntity<Void> confirmPasswordCode(@RequestParam String email, @RequestParam String code) {
        userService.confirmPasswordCode(email, code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset/confirm")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ChangeResetPasswordRequestDTO dto) {
        userService.resetPassword(dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> editPassword(
            Authentication authentication,
            @Valid @RequestBody EditPasswordDTO dto
    ) {
        Long userNo = (Long) authentication.getDetails();
        userService.editPassword(userNo, dto);
        return ResponseEntity.ok().build();
    }
    // 정보 수정 및 출력(아이디- 이름 -번호는 변경 불가 )
    @GetMapping("/mypage/my")
    public ResponseEntity<MyInfoResponseDTO> getMyInfo(Authentication authentication){
        Long userNo = (Long) authentication.getDetails();

        MyInfoResponseDTO response = userService.getMyInfo(userNo);
        return ResponseEntity.ok(response);
    }
    @PatchMapping(value = "/mypage/my", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateMyInfo(
            Authentication authentication,
            @Valid @ModelAttribute UpdateMyInfoRequestDTO dto
    ) {
        Long userNo = (Long) authentication.getDetails();
        userService.updateMyInfo(userNo, dto);
        return ResponseEntity.ok().build();
    }
    // 나의 기부 내역-> DonationController에 존재
    //마이크로트래킹도 ->


//    // 증서
//    @GetMapping("/certificates/{donationNo}")
//    public ResponseEntity<DonationCertificateResponseDTO> getDonationCertificate(
//            Authentication authentication,
//            @PathVariable Long donationNo
//    ) {
//        Long userNo = (Long) authentication.getDetails();
//        DonationCertificateResponseDTO response = userService.getDonationCertificate(userNo, donationNo);
//        return ResponseEntity.ok(response);
//    }
//    // 기부금 영수증 신청
//    @PostMapping("/receipts")
//    public ResponseEntity<Void> requestDonationReceipt(
//            Authentication authentication,
//            @Valid @RequestBody DonationReceiptRequestDTO dto
//    ) {
//        Long userNo = (Long) authentication.getDetails();
//        userService.requestDonationReceipt(userNo, dto);
//        return ResponseEntity.ok().build();
//    }
}
