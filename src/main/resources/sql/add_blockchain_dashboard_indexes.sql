-- 상태 + 전송시각 정렬 기반 목록 조회 최적화
CREATE INDEX IF NOT EXISTS idx_token_tx_status_sent_at_no
ON public.token_transaction (status, sent_at DESC, transaction_no DESC);

-- tx_hash 검색(대소문자 무시) + 상태 필터 최적화
CREATE INDEX IF NOT EXISTS idx_token_tx_status_tx_hash_lower
ON public.token_transaction (status, LOWER(tx_hash), transaction_no DESC);

-- 특정 송신 지갑(from_wallet_no) 기준 목록 조회 최적화
CREATE INDEX IF NOT EXISTS idx_token_tx_status_from_wallet_sent_at
ON public.token_transaction (status, from_wallet_no, sent_at DESC, transaction_no DESC);

-- 특정 수신 지갑(to_wallet_no) 기준 목록 조회 최적화
CREATE INDEX IF NOT EXISTS idx_token_tx_status_to_wallet_sent_at
ON public.token_transaction (status, to_wallet_no, sent_at DESC, transaction_no DESC);

-- 지갑 주소 검색(대소문자 무시) 최적화
CREATE INDEX IF NOT EXISTS idx_wallet_address_lower
ON public.wallet (LOWER(wallet_address));

-- campaign.wallet_no 기반 역조회 최적화
CREATE INDEX IF NOT EXISTS idx_campaign_wallet_no
ON public.campaign (wallet_no);

-- 캠페인 제목 검색(대소문자 무시) 최적화
CREATE INDEX IF NOT EXISTS idx_campaign_title_lower
ON public.campaign (LOWER(title));

-- 단체명 검색(대소문자 무시) 최적화
CREATE INDEX IF NOT EXISTS idx_foundation_name_lower
ON public.foundation (LOWER(foundation_name));
