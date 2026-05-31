-- =============================================
-- [STEP 1] quiz_detail 컬럼 추가 (먼저 실행!)
-- =============================================
ALTER TABLE quiz_detail
  ADD COLUMN IF NOT EXISTS question TEXT,
  ADD COLUMN IF NOT EXISTS options JSON,
  ADD COLUMN IF NOT EXISTS difficulty INT,
  ADD COLUMN IF NOT EXISTS acceptable_answers JSON,
  ADD COLUMN IF NOT EXISTS hint TEXT;

-- =============================================
-- [STEP 2] 아래 qring_insert.sql 실행
-- =============================================
