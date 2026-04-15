package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.dto.AdminTransactionDTO;
import com.merge.final_project.admin.dto.AdminWalletInfoDTO;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.wallet.HotWalletResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    private final HotWalletResolver hotWalletResolver;
    private final TransactionRepository transactionRepository;

    @Value("${blockchain.wallet.hot-address:}")
    private String configuredHotWalletAddress;

    @GetMapping
    public ResponseEntity<AdminWalletInfoDTO> getHotWalletInfo() {
        return ResponseEntity.ok(
                AdminWalletInfoDTO.from(hotWalletResolver.resolve(configuredHotWalletAddress))
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<AdminTransactionDTO>> getHotWalletTransactions(Pageable pageable) {
        String hotWalletAddress = hotWalletResolver.resolve(configuredHotWalletAddress).getWalletAddress();
        return ResponseEntity.ok(
                transactionRepository.findByWalletAddressPaged(hotWalletAddress, pageable)
                        .map(AdminTransactionDTO::from)
        );
    }
}
