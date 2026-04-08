# Local Manual Blockchain Seed Runbook

This runbook prepares minimal DB data for manual testing of:
- `POST /blockchain/payments/complete`
- `POST /blockchain/donations`

## 1) Apply seed

Run:

```sql
\i docs/sql/manual-blockchain-seed.sql
```

## 2) Verify seed

Run:

```sql
\i docs/sql/manual-blockchain-verify.sql
```

Expected IDs for API test:
- `userNo = 91001`
- `campaignNo = 91001`

## 3) Manual API calls

PowerShell examples:

```powershell
$payment = @{
  userNo = 91001
  amount = 100
  donationId = 900001
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8090/blockchain/payments/complete" `
  -ContentType "application/json" `
  -Body $payment
```

```powershell
$donation = @{
  userNo = 91001
  campaignNo = 91001
  amount = 55
  donationId = 900002
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8090/blockchain/donations" `
  -ContentType "application/json" `
  -Body $donation
```

Re-run verify query:

```sql
\i docs/sql/manual-blockchain-verify.sql
```

Check `token_transaction.event_type` for:
- `TokenAllocated`
- `DonationSent`

## 4) Rollback test data

Run:

```sql
\i docs/sql/manual-blockchain-rollback.sql
```

## Notes

- Seed is idempotent and re-runnable.
- Rollback removes only rows with seed marker values (email/title/address/private_key).
- Current keys are stored as raw hex private keys for local manual testing compatibility.
