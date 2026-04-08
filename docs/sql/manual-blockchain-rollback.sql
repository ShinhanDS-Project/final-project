-- Rollback only test data created by docs/sql/manual-blockchain-seed.sql
-- Safety guard: deletes rows only when marker values match.

BEGIN;

-- 1) Delete donation rows first (FK to token_transaction.transaction_no)
DELETE FROM donation
WHERE transaction_no IN (
    SELECT transaction_no
    FROM token_transaction
    WHERE from_wallet_no IN (
        SELECT wallet_no::INTEGER
        FROM wallet
        WHERE (wallet_type = 'SERVER' AND owner_no = 'HOT' AND wallet_address = '0x1000000000000000000000000000000000000001')
           OR (wallet_type = 'USER' AND owner_no = '91001' AND wallet_address = '0x2000000000000000000000000000000000000002')
           OR (wallet_type = 'CAMPAIGN' AND owner_no = 'SEED_CAMPAIGN_91001' AND wallet_address = '0x3000000000000000000000000000000000000003')
    )
       OR to_wallet_no IN (
        SELECT wallet_no::INTEGER
        FROM wallet
        WHERE (wallet_type = 'SERVER' AND owner_no = 'HOT' AND wallet_address = '0x1000000000000000000000000000000000000001')
           OR (wallet_type = 'USER' AND owner_no = '91001' AND wallet_address = '0x2000000000000000000000000000000000000002')
           OR (wallet_type = 'CAMPAIGN' AND owner_no = 'SEED_CAMPAIGN_91001' AND wallet_address = '0x3000000000000000000000000000000000000003')
    )
)
   OR (user_no = '91001' AND campaign_no = 91001);

-- 2) Delete token_transaction rows tied to seed wallets
DELETE FROM token_transaction
WHERE from_wallet_no IN (
    SELECT wallet_no::INTEGER
    FROM wallet
    WHERE (wallet_type = 'SERVER' AND owner_no = 'HOT' AND wallet_address = '0x1000000000000000000000000000000000000001')
       OR (wallet_type = 'USER' AND owner_no = '91001' AND wallet_address = '0x2000000000000000000000000000000000000002')
       OR (wallet_type = 'CAMPAIGN' AND owner_no = 'SEED_CAMPAIGN_91001' AND wallet_address = '0x3000000000000000000000000000000000000003')
)
   OR to_wallet_no IN (
    SELECT wallet_no::INTEGER
    FROM wallet
    WHERE (wallet_type = 'SERVER' AND owner_no = 'HOT' AND wallet_address = '0x1000000000000000000000000000000000000001')
       OR (wallet_type = 'USER' AND owner_no = '91001' AND wallet_address = '0x2000000000000000000000000000000000000002')
       OR (wallet_type = 'CAMPAIGN' AND owner_no = 'SEED_CAMPAIGN_91001' AND wallet_address = '0x3000000000000000000000000000000000000003')
);

-- 3) Disconnect seeded references on retained rows
UPDATE users
SET wallet_no = NULL,
    privatekey_no = NULL,
    updated_at = NOW()
WHERE user_no = 91001
  AND email = 'manual-api-user+91001@example.com';

UPDATE campaign
SET wallet_no = NULL,
    updated_at = NOW()
WHERE campaign_no = 91001
  AND title = 'SEED-MANUAL-BLOCKCHAIN-CAMPAIGN';

-- 4) Delete seeded wallets only
DELETE FROM wallet
WHERE (wallet_type = 'USER' AND owner_no = '91001' AND wallet_address = '0x2000000000000000000000000000000000000002')
   OR (wallet_type = 'CAMPAIGN' AND owner_no = 'SEED_CAMPAIGN_91001' AND wallet_address = '0x3000000000000000000000000000000000000003')
   OR (wallet_type = 'SERVER' AND owner_no = 'HOT' AND wallet_address = '0x1000000000000000000000000000000000000001');

-- 5) Delete seeded campaign/user/foundation rows only
DELETE FROM campaign
WHERE campaign_no = 91001
  AND title = 'SEED-MANUAL-BLOCKCHAIN-CAMPAIGN';

DELETE FROM users
WHERE user_no = 91001
  AND email = 'manual-api-user+91001@example.com';

DELETE FROM foundation
WHERE foundation_no = 91001
  AND foundation_email = 'manual-api-foundation+91001@example.com';

-- 6) Delete seeded keys if no wallet references remain
DELETE FROM "key" k
WHERE k.private_key IN (
    '1111111111111111111111111111111111111111111111111111111111111111',
    '2222222222222222222222222222222222222222222222222222222222222222',
    '3333333333333333333333333333333333333333333333333333333333333333'
)
  AND NOT EXISTS (
      SELECT 1
      FROM wallet w
      WHERE w.key_no = k.key_no
  );

COMMIT;
