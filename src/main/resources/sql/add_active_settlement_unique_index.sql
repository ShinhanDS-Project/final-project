CREATE UNIQUE INDEX IF NOT EXISTS uq_settlement_active_campaign
ON public.settlement (campaign_no)
WHERE status IN ('PENDING', 'PROCESSING', 'COMPLETED');
