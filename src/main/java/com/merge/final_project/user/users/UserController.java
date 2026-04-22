package com.merge.final_project.user.users;

import com.merge.final_project.blockchain.entity.Transaction;

import com.merge.final_project.blockchain.service.TransactionService;
import com.merge.final_project.donation.donations.DonationService;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.user.users.dto.MicroTrackingDTO;
import com.merge.final_project.user.users.dto.UserTransactionResponseDTO;
import com.merge.final_project.user.users.dto.UserWalletResponseDTO;
import com.merge.final_project.user.users.dto.support.*;
import com.merge.final_project.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        Object details = authentication.getDetails();
        Long userNo;
        if (details instanceof Long) {
            userNo = (Long) details;
        } else {
            // details가 Long이 아닌 경우 (예: OAuth2User 또는 다른 필터에서 설정된 경우)
            // principal에서 email을 가져와서 DB에서 userNo를 찾습니다.
            String email = authentication.getName();
            User user = userRepository.findByEmailAndLoginType(email, LoginType.LOCAL)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            userNo = user.getUserNo();
        }
        
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
    //마이크로트래킹도
    @GetMapping("/see")
    public ResponseEntity<MicroTrackingDTO> showMicro(@RequestParam Long campaignNo){

        MicroTrackingDTO microTracking=userService.showMicroTracking(campaignNo);
        return ResponseEntity.ok(microTracking);

    }
    // 나의 지갑 조회하기
    @GetMapping("/user/wallet")
    public ResponseEntity<UserWalletResponseDTO> showUserWallet(Authentication authentication){
        Long userNo = (Long) authentication.getDetails();
        UserWalletResponseDTO dto=userService.showUserWalletInfo(userNo);
        return ResponseEntity.ok(dto);
    }

    //나의 지갑 기준으로 토큰 거래 내역 조회해오기
    @GetMapping("/user/wallet/token/transactions")
    public ResponseEntity<List<UserTransactionResponseDTO>> showUserTransactional(Authentication authentication, Long walletNo){
       Long userNo = (Long) authentication.getDetails();
       List<UserTransactionResponseDTO> transactionDTO=userService.showWalletTokenTrans(userNo);
        return ResponseEntity.ok(transactionDTO);
    }


}
