-- 봇 컴피티션 문제 교체 준비 (2026-09-20). competition_quiz_import.sql 보다 먼저, 한 번만 실행.
-- 1) 기존 문제 행 삭제 (자식 → 부모 순서). 테이블은 지우지 않는다 — 외래키가 걸려 있고,
--    drop 하면 quiz_content_id 가 1 부터 다시 시작해 옛 competition_match_answer 가 엉뚱한 문제를 가리키게 된다.
-- 2) 세트 키 컬럼 추가. _01, _02 파일의 id 가 둘 다 1~42 라 (level, origin_id) 만으로는 구분이 안 된다.
SET NAMES utf8mb4;

DELETE FROM competition_wrong_answer;
DELETE FROM competition_quiz_content;
DELETE FROM competition_quiz_detail;

ALTER TABLE competition_quiz_detail
  ADD COLUMN set_key VARCHAR(30) NOT NULL DEFAULT 'default' AFTER quiz_id;
-- 아래 이름이 다르면 SHOW CREATE TABLE competition_quiz_detail 의 UNIQUE KEY 이름으로 바꿔서 실행
ALTER TABLE competition_quiz_detail
  DROP INDEX uq_level_origin;
ALTER TABLE competition_quiz_detail
  ADD UNIQUE KEY uq_set_level_origin (set_key, level, origin_id);

SHOW CREATE TABLE competition_quiz_detail;
