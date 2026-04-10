package com.merge.final_project.blockchain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

@Component
// Spring Bean으로 등록되어 다른 서비스에서 주입받아 사용되는 가스 설정 클래스
public class ConfigurableContractGasProvider implements ContractGasProvider {

    // 트랜잭션 실행 시 사용할 가스 가격 (wei 단위)
    private final BigInteger gasPrice;
    // 트랜잭션 최대 가스 사용량 제한
    private final BigInteger gasLimit;

    public ConfigurableContractGasProvider(
            @Value("${blockchain.gas.price}") String gasPrice,
            @Value("${blockchain.gas.limit}") String gasLimit
    ) {
        // application.yml 또는 환경변수에서 읽어온 값을 BigInteger로 변환
        this.gasPrice = new BigInteger(gasPrice);
        this.gasLimit = new BigInteger(gasLimit);
    }

    // 특정 컨트랙트 함수 호출 시 사용할 gasPrice 반환 (함수별 분기 없이 동일 값 사용)
    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return gasPrice;
    }

    // 기본 gasPrice 반환
    @Override
    public BigInteger getGasPrice() {
        return gasPrice;
    }

    // 특정 함수 호출 시 사용할 gasLimit 반환 (함수별 분기 없이 동일 값 사용)
    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return gasLimit;
    }

    // 기본 gasLimit 반환
    @Override
    public BigInteger getGasLimit() {
        return gasLimit;
    }
}
