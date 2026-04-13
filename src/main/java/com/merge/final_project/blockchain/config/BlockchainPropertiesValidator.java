package com.merge.final_project.blockchain.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlockchainPropertiesValidator {

    @Value("${blockchain.stub.enabled:false}")
    private boolean stubEnabled;

    @Value("${blockchain.rpc.url:}")
    private String rpcUrl;

    @Value("${blockchain.chain-id:137}")
    private long chainId;

    @Value("${blockchain.contract.donation-token-address:}")
    private String donationTokenAddress;

    @Value("${blockchain.wallet.hot-address:}")
    private String hotWalletAddress;

    @Value("${blockchain.contract.owner-address:}")
    private String contractOwnerAddress;

    @PostConstruct
    void validate() {
        if (stubEnabled) {
            return;
        }

        requireNotBlank(rpcUrl, "blockchain.rpc.url");
        requireNotBlank(donationTokenAddress, "blockchain.contract.donation-token-address");
        requireNotBlank(hotWalletAddress, "blockchain.wallet.hot-address");

        if (chainId <= 0) {
            throw new IllegalStateException("blockchain.chain-id must be positive");
        }

        validateHttpUrl(rpcUrl, "blockchain.rpc.url");
        validateAddress(donationTokenAddress, "blockchain.contract.donation-token-address");
        validateAddress(hotWalletAddress, "blockchain.wallet.hot-address");

        if (!contractOwnerAddress.isBlank()) {
            validateAddress(contractOwnerAddress, "blockchain.contract.owner-address");
        }
    }

    private void requireNotBlank(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must not be blank");
        }
    }

    private void validateHttpUrl(String value, String propertyName) {
        if (!(value.startsWith("http://") || value.startsWith("https://"))) {
            throw new IllegalStateException(propertyName + " must start with http:// or https://");
        }
    }

    private void validateAddress(String value, String propertyName) {
        if (!value.matches("^0x[a-fA-F0-9]{40}$")) {
            throw new IllegalStateException(propertyName + " must be a valid EVM address");
        }
    }
}
