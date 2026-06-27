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

-- =============================================
-- [STEP 3] 결과 저장 언어 기록용 컬럼 추가
-- =============================================
ALTER TABLE quiz_result
  ADD COLUMN IF NOT EXISTS lang_code VARCHAR(5);

ALTER TABLE user_study_log
  ADD COLUMN IF NOT EXISTS lang_code VARCHAR(5);
