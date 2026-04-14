package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.dto.AdminTransactionDTO;
import com.merge.final_project.admin.dto.AdminWalletInfoDTO;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/wallet")
@RequiredArgsConstructor
public class AdminWalletController {

    private final WalletLookupRepository walletLookupRepository;
    private final TransactionRepository transactionRepository;

    // 관리자 — 플랫폼 HOT 지갑 주소 + 잔액 조회
    @GetMapping
    public ResponseEntity<AdminWalletInfoDTO> getHotWalletInfo() {
        return ResponseEntity.ok(AdminWalletInfoDTO.from(
                walletLookupRepository.findFirstByWalletType(WalletType.HOT)
                        .orElseThrow(() -> new IllegalStateException("HOT 지갑 정보를 찾을 수 없습니다."))
        ));
    }

    // 관리자 — HOT 지갑 거래 내역 조회 (페이징, 최신순)
    // 예: GET /admin/wallet/transactions?page=0&size=20&sort=sentAt,desc
    @GetMapping("/transactions")
    public ResponseEntity<Page<AdminTransactionDTO>> getHotWalletTransactions(Pageable pageable) {
        // 핫 타입 상태값을 통해 지갑 주소를 찾아오고, 해당 주소로 거래 내역을 반환한다.
        String hotWalletAddress = walletLookupRepository.findFirstByWalletType(WalletType.HOT)
                .orElseThrow(() -> new IllegalStateException("HOT 지갑 정보를 찾을 수 없습니다."))
                .getWalletAddress();
        return ResponseEntity.ok(
                transactionRepository.findByWalletAddressPaged(hotWalletAddress, pageable)
                        .map(AdminTransactionDTO::from)
        );
    }
}
