# 로컬 수동 API 테스트 체크리스트 (2단계)

## 목적
- 회원가입 기능 없이도 현재 블록체인 API 흐름을 수동으로 검증한다.
- 검증 대상:
  - `POST /blockchain/payments/complete` (SERVER/HOT -> USER 지급)
  - `POST /blockchain/donations` (USER -> CAMPAIGN 기부)
  - `token_transaction` 이벤트 기반 적재(`TokenAllocated`, `DonationSent`)

## 0. 사전 설정 확인
- `application.properties`에서 `.env` 값이 정상 로딩되는지 확인
- 최소 필수 환경값:
  - `BLOCKCHAIN_STUB_ENABLED` (`true`/`false`)
  - `BLOCKCHAIN_RPC_URL`
  - `BLOCKCHAIN_CHAIN_ID=137`
  - `BLOCKCHAIN_CONTRACT_ADDRESS`
  - `BLOCKCHAIN_CONTRACT_OWNER_ADDRESS`
  - `BLOCKCHAIN_TOKEN_DECIMALS=18`

권장:
- 1차 점검은 `BLOCKCHAIN_STUB_ENABLED=true` (호출/DB 흐름만 검증)
- 2차 점검은 `BLOCKCHAIN_STUB_ENABLED=false` (실체인 소액 검증)

## 1. DB 사전 조건 점검 SQL
아래 쿼리는 현재 코드 기준 테이블/컬럼명으로 작성.

```sql
-- 1) 컨트랙트 owner 지갑 존재 확인
select wallet_no, owner_type, owner_no, wallet_address, encrypted_private_key
from wallet
where lower(wallet_address) = lower(:contract_owner_address);

-- 2) HOT 지갑 존재 확인
select wallet_no, owner_type, owner_no, wallet_address, encrypted_private_key
from wallet
where owner_type = 'SERVER' and owner_no = 'HOT';

-- 3) 테스트 대상 USER 지갑 확인
select wallet_no, owner_type, owner_no, wallet_address, encrypted_private_key
from wallet
where owner_type = 'USER' and owner_no = :user_no_text;

-- 4) 테스트 대상 CAMPAIGN 매핑 확인
select c.campaign_no, c.camp_wallet_no, w.wallet_address
from campaign c
left join wallet w on w.wallet_no = c.camp_wallet_no
where c.campaign_no = :campaign_no;
```

중요:
- owner/hot/user 지갑의 `encrypted_private_key`는 `null`이면 안 됨
- `campaign.camp_wallet_no`가 실제 `wallet.wallet_no`와 연결되어야 함

## 2. 서버 실행
```powershell
./gradlew.bat bootRun
```

## 3. 결제 완료 API 수동 호출
```powershell
$body = @{
  userNo = 101
  amount = 100
  donationId = 900001
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8090/blockchain/payments/complete" `
  -ContentType "application/json" `
  -Body $body
```

응답 체크:
- `transactionNo` 존재
- `txHash` 존재(실체인 모드에서 유효 해시)
- `status`가 `SUCCESS` 또는 실패 사유 확인 가능 상태
- `eventType`이 `TokenAllocated` (또는 이벤트 미검출 시 기존 값)

DB 체크:
```sql
select transaction_no, tx_hash, status, event_type, from_wallet_no, to_wallet_no, amount, block_num, created_at
from token_transaction
order by created_at desc
limit 5;
```

## 4. 기부 API 수동 호출
```powershell
$body = @{
  userNo = 101
  campaignNo = 777
  amount = 55
  donationId = 900002
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8090/blockchain/donations" `
  -ContentType "application/json" `
  -Body $body
```

응답 체크:
- `transactionNo` 존재
- `txHash` 존재
- `status` 확인
- `eventType`이 `DonationSent` (또는 이벤트 미검출 시 기존 값)

DB 체크:
```sql
select transaction_no, tx_hash, status, event_type, from_wallet_no, to_wallet_no, amount, block_num, created_at
from token_transaction
order by created_at desc
limit 10;
```

## 5. 실패 시 우선 점검 포인트
- `user wallet not found`: `wallet.owner_type='USER' and owner_no={userNo}` 확인
- `server HOT wallet not found`: HOT 지갑 row 확인
- `contract owner wallet not found in DB`: `BLOCKCHAIN_CONTRACT_OWNER_ADDRESS`와 `wallet.wallet_address` 일치 확인
- `campaign wallet is not assigned`: `campaign.camp_wallet_no` 확인
- 개인키 복호화 오류: `encrypted_private_key` 값/암호화 키(`wallet.crypto.secret`) 확인

## 6. 테스트 결과 기록 템플릿
- 실행 일시:
- 모드(stub/real):
- userNo / campaignNo:
- payments API 결과:
- donations API 결과:
- token_transaction 반영 결과:
- 이슈/조치:
