package com.merge.final_project.blockchain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

@Component
public class ConfigurableContractGasProvider implements ContractGasProvider {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurableContractGasProvider.class);
    private final Web3j web3j;
    private final BigInteger minGasPrice;
    private final BigInteger gasLimit;

    public ConfigurableContractGasProvider(
            Web3j web3j,
            @Value("${blockchain.gas.price}") String gasPrice,
            @Value("${blockchain.gas.limit}") String gasLimit
    ) {
        this.web3j = web3j;
        this.minGasPrice = new BigInteger(gasPrice);
        this.gasLimit = new BigInteger(gasLimit);
    }

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return getGasPrice();
    }

    @Override
    public BigInteger getGasPrice() {
        try {
            BigInteger networkGasPrice = web3j.ethGasPrice().send().getGasPrice();
            if (networkGasPrice == null) {
                return minGasPrice;
            }
            return networkGasPrice.max(minGasPrice);
        } catch (Exception e) {
            log.warn("Failed to fetch network gas price, falling back to minGasPrice", e);
            return minGasPrice;
        }
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return gasLimit;
    }

    @Override
    public BigInteger getGasLimit() {
        return gasLimit;
    }
}
