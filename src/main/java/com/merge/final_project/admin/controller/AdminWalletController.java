package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.dto.AdminTransactionDTO;
import com.merge.final_project.admin.dto.AdminWalletInfoDTO;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 지갑 관리", description = "관리자 플랫폼 HOT 지갑 조회 API")
@RestController
@RequestMapping("/admin/wallet")
@RequiredArgsConstructor
public class AdminWalletController {

    private final WalletLookupRepository walletLookupRepository;
    private final TransactionRepository transactionRepository;

    @Operation(summary = "HOT 지갑 정보 조회", description = "플랫폼 HOT 지갑의 주소와 잔액을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "HOT 지갑 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping
    public ResponseEntity<AdminWalletInfoDTO> getHotWalletInfo() {
        return ResponseEntity.ok(AdminWalletInfoDTO.from(
                walletLookupRepository.findFirstByWalletType(WalletType.HOT)
                        .orElseThrow(() -> new IllegalStateException("HOT 지갑 정보를 찾을 수 없습니다."))
        ));
    }

    @Operation(summary = "HOT 지갑 거래 내역 조회", description = "플랫폼 HOT 지갑의 블록체인 거래 내역을 페이징으로 조회합니다. (예: ?page=0&size=20&sort=sentAt,desc)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "HOT 지갑 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/transactions")
    public ResponseEntity<Page<AdminTransactionDTO>> getHotWalletTransactions(Pageable pageable) {
        String hotWalletAddress = walletLookupRepository.findFirstByWalletType(WalletType.HOT)
                .orElseThrow(() -> new IllegalStateException("HOT 지갑 정보를 찾을 수 없습니다."))
                .getWalletAddress();
        return ResponseEntity.ok(
                transactionRepository.findByWalletAddressPaged(hotWalletAddress, pageable)
                        .map(AdminTransactionDTO::from)
        );
    }
}
