package com.merge.final_project.wallet;

/**
 * 서버 지갑 생성/조회 시 외부에서 사용하는 타입.
 * WalletType의 HOT/COLD와 1:1 매핑된다.
 */
public enum ServerWalletType {
    // 운영 송신/가스 공급용 지갑
    HOT,
    // 장기 보관용 지갑
    COLD
}
