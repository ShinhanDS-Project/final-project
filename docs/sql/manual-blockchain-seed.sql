-- Manual API seed for local blockchain flow tests
-- Target endpoints:
--   POST /blockchain/payments/complete
--   POST /blockchain/donations
--
-- Safe behavior:
-- - Re-runnable (idempotent)
-- - Inserts only missing rows
-- - Reuses existing SERVER/HOT wallet if already present
--
-- Tested SQL dialect assumptions: PostgreSQL

BEGIN;

DO $$
DECLARE
    v_user_no INTEGER := 91001;
    v_foundation_no INTEGER := 91001;
    v_campaign_no INTEGER := 91001;

    v_user_email TEXT := 'manual-api-user+91001@example.com';
    v_user_name_hash TEXT := 'seed-name-hash-91001';
    v_user_phone TEXT := '010-9100-1001';
    v_foundation_email TEXT := 'manual-api-foundation+91001@example.com';

    v_hot_seed_address TEXT := '0x1000000000000000000000000000000000000001';
    v_user_seed_address TEXT := '0x2000000000000000000000000000000000000002';
    v_campaign_seed_address TEXT := '0x3000000000000000000000000000000000000003';

    -- 64-hex raw private keys. Works with current "decrypt fail -> raw fallback" logic.
    v_hot_seed_private_key TEXT := '1111111111111111111111111111111111111111111111111111111111111111';
    v_user_seed_private_key TEXT := '2222222222222222222222222222222222222222222222222222222222222222';
    v_campaign_seed_private_key TEXT := '3333333333333333333333333333333333333333333333333333333333333333';

    v_hot_key_no BIGINT;
    v_user_key_no BIGINT;
    v_campaign_key_no BIGINT;

    v_hot_wallet_no BIGINT;
    v_user_wallet_no BIGINT;
    v_campaign_wallet_no BIGINT;

    v_now TIMESTAMP := NOW();
BEGIN
    -- 1) Ensure user row for payment test target
    INSERT INTO users (
        user_no, email, password_hash, name_hash, "name", phone, birth,
        profile_path, status, created_at, updated_at, wallet_no, privatekey_no,
        login_type, login_count
    )
    VALUES (
        v_user_no,
        v_user_email,
        'seed-password-hash',
        v_user_name_hash,
        'Manual API User',
        v_user_phone,
        DATE '1990-01-01',
        NULL,
        'ACTIVE',
        v_now,
        v_now,
        NULL,
        NULL,
        'LOCAL',
        0
    )
    ON CONFLICT (user_no) DO UPDATE
        SET email = EXCLUDED.email,
            name_hash = EXCLUDED.name_hash,
            phone = EXCLUDED.phone,
            status = EXCLUDED.status,
            login_type = EXCLUDED.login_type,
            login_count = EXCLUDED.login_count,
            updated_at = EXCLUDED.updated_at;

    -- 2) Ensure foundation row (campaign.foundation_no prerequisite)
    INSERT INTO foundation (
        foundation_no, foundation_email, foundation_name,
        account_status, review_status, created_at, updated_at
    )
    VALUES (
        v_foundation_no,
        v_foundation_email,
        'SEED-MANUAL-FOUNDATION',
        'ACTIVE',
        'APPROVED',
        v_now,
        v_now
    )
    ON CONFLICT (foundation_no) DO UPDATE
        SET updated_at = EXCLUDED.updated_at;

    -- 3) Ensure campaign row for donation test target
    INSERT INTO campaign (
        campaign_no, title, description, image_path, start_at, end_at,
        usage_start_at, usage_end_at, target_amount, achieved_at, current_amount,
        category, approval_status, approved_at, campaign_status, beneficiary_no,
        created_at, updated_at, reject_reason, foundation_no, wallet_no
    )
    VALUES (
        v_campaign_no,
        'SEED-MANUAL-BLOCKCHAIN-CAMPAIGN',
        'Manual blockchain API test campaign',
        NULL,
        CAST(v_now AS TIMESTAMP),
        CAST(v_now + INTERVAL '30 days' AS TIMESTAMP),
        CAST(v_now AS TIMESTAMP),
        CAST(v_now + INTERVAL '60 days' AS TIMESTAMP),
        1000000,
        NULL,
        0,
        'ETC',
        'APPROVED',
        CAST(v_now AS TIMESTAMP),
        'IN_PROGRESS',
        NULL,
        NOW(),
        NOW(),
        NULL,
        v_foundation_no,
        NULL
    )
    ON CONFLICT (campaign_no) DO UPDATE
        SET foundation_no = EXCLUDED.foundation_no,
            updated_at = EXCLUDED.updated_at;

    -- 4) Ensure key rows
    SELECT key_no INTO v_hot_key_no
    FROM "key"
    WHERE private_key = v_hot_seed_private_key
    LIMIT 1;
    IF v_hot_key_no IS NULL THEN
        INSERT INTO "key" (private_key, aes_key)
        VALUES (v_hot_seed_private_key, NULL)
        RETURNING key_no INTO v_hot_key_no;
    END IF;

    SELECT key_no INTO v_user_key_no
    FROM "key"
    WHERE private_key = v_user_seed_private_key
    LIMIT 1;
    IF v_user_key_no IS NULL THEN
        INSERT INTO "key" (private_key, aes_key)
        VALUES (v_user_seed_private_key, NULL)
        RETURNING key_no INTO v_user_key_no;
    END IF;

    SELECT key_no INTO v_campaign_key_no
    FROM "key"
    WHERE private_key = v_campaign_seed_private_key
    LIMIT 1;
    IF v_campaign_key_no IS NULL THEN
        INSERT INTO "key" (private_key, aes_key)
        VALUES (v_campaign_seed_private_key, NULL)
        RETURNING key_no INTO v_campaign_key_no;
    END IF;

    -- 5) Ensure SERVER/HOT wallet
    SELECT w.wallet_no
    INTO v_hot_wallet_no
    FROM wallet w
    WHERE w.wallet_type = 'SERVER'
      AND w.owner_no = 'HOT'
    ORDER BY w.wallet_no DESC
    LIMIT 1;

    IF v_hot_wallet_no IS NULL THEN
        INSERT INTO wallet (
            key_no, wallet_type, owner_no, wallet_address, balance, wallet_hash,
            status, created_at, last_used_at, campaign_no
        )
        VALUES (
            v_hot_key_no, 'SERVER', 'HOT', v_hot_seed_address, 0, NULL,
            'ACTIVE', v_now, v_now, NULL
        )
        RETURNING wallet_no INTO v_hot_wallet_no;
    END IF;

    -- 6) Ensure USER wallet (owner_no = user_no text)
    SELECT w.wallet_no
    INTO v_user_wallet_no
    FROM wallet w
    WHERE w.wallet_type = 'USER'
      AND w.owner_no = v_user_no::TEXT
    ORDER BY w.wallet_no DESC
    LIMIT 1;

    IF v_user_wallet_no IS NULL THEN
        INSERT INTO wallet (
            key_no, wallet_type, owner_no, wallet_address, balance, wallet_hash,
            status, created_at, last_used_at, campaign_no
        )
        VALUES (
            v_user_key_no, 'USER', v_user_no::TEXT, v_user_seed_address, 0, NULL,
            'ACTIVE', v_now, v_now, NULL
        )
        RETURNING wallet_no INTO v_user_wallet_no;
    END IF;

    -- 7) Ensure CAMPAIGN wallet
    SELECT w.wallet_no
    INTO v_campaign_wallet_no
    FROM wallet w
    WHERE w.wallet_type = 'CAMPAIGN'
      AND w.owner_no = 'SEED_CAMPAIGN_91001'
    ORDER BY w.wallet_no DESC
    LIMIT 1;

    IF v_campaign_wallet_no IS NULL THEN
        INSERT INTO wallet (
            key_no, wallet_type, owner_no, wallet_address, balance, wallet_hash,
            status, created_at, last_used_at, campaign_no
        )
        VALUES (
            v_campaign_key_no, 'CAMPAIGN', 'SEED_CAMPAIGN_91001', v_campaign_seed_address, 0, NULL,
            'ACTIVE', v_now, v_now, v_campaign_no
        )
        RETURNING wallet_no INTO v_campaign_wallet_no;
    END IF;

    -- 8) Connect user/campaign references required by service flow
    UPDATE users
    SET wallet_no = v_user_wallet_no::INTEGER,
        privatekey_no = v_user_key_no::INTEGER,
        updated_at = NOW()
    WHERE user_no = v_user_no;

    UPDATE campaign
    SET wallet_no = v_campaign_wallet_no::INTEGER,
        updated_at = NOW()
    WHERE campaign_no = v_campaign_no;

    RAISE NOTICE 'Seed ready. user_no=%, campaign_no=%, hot_wallet_no=%, user_wallet_no=%, campaign_wallet_no=%',
        v_user_no, v_campaign_no, v_hot_wallet_no, v_user_wallet_no, v_campaign_wallet_no;
END $$;

COMMIT;
