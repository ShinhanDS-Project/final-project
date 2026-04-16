package com.merge.final_project.user.users;

import com.merge.final_project.blockchain.entity.Transaction;

import com.merge.final_project.blockchain.service.TransactionService;
import com.merge.final_project.donation.donations.DonationService;
import com.merge.final_project.user.users.dto.MicroTrackingDTO;
import com.merge.final_project.user.users.dto.UserTransactionResponseDTO;
import com.merge.final_project.user.users.dto.UserWalletResponseDTO;
import com.merge.final_project.user.users.dto.support.*;
import com.merge.final_project.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@io.swagger.v3.oas.annotations.tags.Tag(name = "일반 사용자 마이페이지", description = "사용자 마이페이지 부가기능 API")
@RestController
@RequestMapping("/users/support")
public class UserController {
    //부가기능 또는 마이페이지 전용 컨트롤러
    @Autowired
    private UserService userService;
    @Autowired
    private DonationService donationService;
    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "이메일 찾기", description = "이름·전화번호로 가입된 이메일을 찾습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "일치하는 계정 없음")
    })
    @PostMapping("/email")
    public ResponseEntity<EmailResponseDTO> email(@Valid @RequestBody EmailRequestDTO request) {
        EmailResponseDTO response=userService.findEmail(request.getPhone(),request.getName());
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "비밀번호 재설정 요청", description = "이메일로 비밀번호 재설정 코드를 발송합니다.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "발송 성공") })
    @PostMapping("/password/reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody ChangePasswordRequestDTO dto) {
        userService.requestPasswordReset(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 재설정 코드 확인", description = "이메일로 발송된 재설정 코드의 유효성을 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코드 확인 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 코드")
    })
    @PostMapping("/password/reset/verify")
    public ResponseEntity<Void> confirmPasswordCode(@RequestParam String email, @RequestParam String code) {
        userService.confirmPasswordCode(email, code);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 재설정 확정", description = "코드 확인 후 새 비밀번호로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류")
    })
    @PostMapping("/password/reset/confirm")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ChangeResetPasswordRequestDTO dto) {
        userService.resetPassword(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경 (마이페이지)", description = "로그인 상태에서 현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PatchMapping("/password")
    public ResponseEntity<Void> editPassword(
            Authentication authentication,
            @Valid @RequestBody EditPasswordDTO dto
    ) {
        Long userNo = (Long) authentication.getDetails();
        userService.editPassword(userNo, dto);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "내 정보 조회 (마이페이지)", description = "로그인한 사용자의 마이페이지 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/mypage/my")
    public ResponseEntity<MyInfoResponseDTO> getMyInfo(Authentication authentication){
        Long userNo = (Long) authentication.getDetails();

        MyInfoResponseDTO response = userService.getMyInfo(userNo);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "내 정보 수정 (마이페이지)", description = "프로필 이미지·소개 등 수정 가능한 정보를 업데이트합니다. 아이디·이름·전화번호는 변경 불가.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
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
    //마이크로트래킹도
    @Operation(summary = "캠페인 마이크로 트래킹 조회", description = "특정 캠페인의 기부금 사용 마이크로 트래킹 정보를 조회합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "캠페인을 찾을 수 없음")
    })
    @GetMapping("/see")
    public ResponseEntity<MicroTrackingDTO> showMicro(
            @Parameter(description = "캠페인 번호", example = "1") Long campaignNo){

        MicroTrackingDTO microTracking=userService.showMicroTracking(campaignNo);
        return ResponseEntity.ok(microTracking);

    }
    @Operation(summary = "내 지갑 조회", description = "로그인한 사용자의 블록체인 지갑 주소·잔액 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/user/wallet")
    public ResponseEntity<UserWalletResponseDTO> showUserWallet(Authentication authentication){
        Long userNo = (Long) authentication.getDetails();
        UserWalletResponseDTO dto=userService.showUserWalletInfo(userNo);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "내 토큰 거래 내역 조회", description = "로그인한 사용자의 지갑 기준 토큰 거래 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/user/wallet/token/transactions")
    public ResponseEntity<List<UserTransactionResponseDTO>> showUserTransactional(Authentication authentication, Long walletNo){
       Long userNo = (Long) authentication.getDetails();
       List<UserTransactionResponseDTO> transactionDTO=userService.showWalletTokenTrans(userNo);
        return ResponseEntity.ok(transactionDTO);
    }


}
