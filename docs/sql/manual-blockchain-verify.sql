-- Verify data for local manual blockchain API tests
-- Expected test payload:
--   payment:  { "userNo": 91001, "amount": 100, "donationId": 900001 }
--   donation: { "userNo": 91001, "campaignNo": 91001, "amount": 55, "donationId": 900002 }

-- 1) Wallet prerequisites
SELECT wallet_no, key_no, wallet_type, owner_no, wallet_address, status, created_at
FROM wallet
WHERE (wallet_type = 'SERVER' AND owner_no = 'HOT')
   OR (wallet_type = 'USER' AND owner_no = '91001')
   OR (wallet_type = 'CAMPAIGN' AND owner_no = 'SEED_CAMPAIGN_91001')
ORDER BY wallet_type, wallet_no;

-- 2) User to wallet/key linkage
SELECT user_no, email, wallet_no, privatekey_no, status, login_type, updated_at
FROM users
WHERE user_no = 91001;

-- 3) Campaign to wallet linkage
SELECT campaign_no, title, wallet_no, foundation_no, approval_status, campaign_status, updated_at
FROM campaign
WHERE campaign_no = 91001;

-- 3-1) Foundation prerequisite row
SELECT foundation_no, foundation_email, foundation_name, account_status, review_status, updated_at
FROM foundation
WHERE foundation_no = 91001;

-- 4) Key row presence
SELECT key_no, private_key
FROM "key"
WHERE private_key IN (
    '1111111111111111111111111111111111111111111111111111111111111111',
    '2222222222222222222222222222222222222222222222222222222222222222',
    '3333333333333333333333333333333333333333333333333333333333333333'
)
ORDER BY key_no;

-- 5) Latest token transactions produced by API calls
SELECT transaction_no, transaction_code, tx_hash, status, event_type, from_wallet_no, to_wallet_no, amount, block_num, created_at
FROM token_transaction
WHERE from_wallet_no IN (
    SELECT wallet_no FROM wallet
    WHERE (wallet_type = 'SERVER' AND owner_no = 'HOT')
       OR (wallet_type = 'USER' AND owner_no = '91001')
)
   OR to_wallet_no IN (
    SELECT wallet_no FROM wallet
    WHERE (wallet_type = 'USER' AND owner_no = '91001')
       OR (wallet_type = 'CAMPAIGN' AND owner_no = 'SEED_CAMPAIGN_91001')
)
ORDER BY created_at DESC
LIMIT 20;

-- 6) Fallback quick check (no filter)
SELECT transaction_no, tx_hash, status, event_type, from_wallet_no, to_wallet_no, created_at
FROM token_transaction
ORDER BY transaction_no DESC
LIMIT 20;
