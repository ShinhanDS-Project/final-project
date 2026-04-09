CREATE INDEX IF NOT EXISTS idx_token_tx_status_sent_at_no
ON public.token_transaction (status, sent_at DESC, transaction_no DESC);

CREATE INDEX IF NOT EXISTS idx_token_tx_status_tx_hash_lower
ON public.token_transaction (status, LOWER(tx_hash), transaction_no DESC);

CREATE INDEX IF NOT EXISTS idx_token_tx_status_from_wallet_sent_at
ON public.token_transaction (status, from_wallet_no, sent_at DESC, transaction_no DESC);

CREATE INDEX IF NOT EXISTS idx_token_tx_status_to_wallet_sent_at
ON public.token_transaction (status, to_wallet_no, sent_at DESC, transaction_no DESC);

CREATE INDEX IF NOT EXISTS idx_wallet_address_lower
ON public.wallet (LOWER(wallet_address));

CREATE INDEX IF NOT EXISTS idx_campaign_wallet_no
ON public.campaign (wallet_no);

CREATE INDEX IF NOT EXISTS idx_campaign_title_lower
ON public.campaign (LOWER(title));

CREATE INDEX IF NOT EXISTS idx_foundation_name_lower
ON public.foundation (LOWER(foundation_name));
