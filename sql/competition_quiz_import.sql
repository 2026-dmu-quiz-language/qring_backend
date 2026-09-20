-- 봇 컴피티션 문제 import (scripts/competition-quiz-to-sql.js 가 생성, 2026-09-20)
-- 원본: competition_quiz_en_01.json [set 01, en], competition_quiz_ja_01.json [set 01, ja], competition_quiz_zh_01.json [set 01, zh], competition_quiz_en_02.json [set 02, en], competition_quiz_ja_02.json [set 02, ja], competition_quiz_zh_02.json [set 02, zh]
-- 실행: mysql --default-character-set=utf8mb4 -h <host> -u admin -p qring_db 로 접속한 뒤  source <이 파일 경로>  (PowerShell 파이프 금지: CJK 가 ? 로 깨짐)
-- 전제: competition_quiz_detail 에 set_key 컬럼과 UNIQUE(set_key, level, origin_id) 가 있어야 함 (sql/competition_quiz_prepare.sql)
SET NAMES utf8mb4;
START TRANSACTION;

-- detail: set 01 (126건, quiz_type 은 en 파일 기준)
INSERT INTO competition_quiz_detail (set_key, level, origin_id, quiz_type) VALUES
  ('01', 1, 1, 'subjective'),
  ('01', 1, 2, 'subjective'),
  ('01', 1, 3, 'multiple_choice'),
  ('01', 1, 4, 'word_arrange'),
  ('01', 1, 5, 'subjective'),
  ('01', 1, 6, 'multiple_choice'),
  ('01', 1, 7, 'subjective'),
  ('01', 1, 8, 'word_arrange'),
  ('01', 1, 9, 'subjective'),
  ('01', 1, 10, 'word_arrange'),
  ('01', 1, 11, 'subjective'),
  ('01', 1, 12, 'multiple_choice'),
  ('01', 1, 13, 'multiple_choice'),
  ('01', 1, 14, 'word_arrange'),
  ('01', 1, 15, 'word_arrange'),
  ('01', 1, 16, 'multiple_choice'),
  ('01', 1, 17, 'multiple_choice'),
  ('01', 1, 18, 'word_arrange'),
  ('01', 1, 19, 'word_arrange'),
  ('01', 1, 20, 'subjective'),
  ('01', 1, 21, 'multiple_choice'),
  ('01', 1, 22, 'word_arrange'),
  ('01', 1, 23, 'subjective'),
  ('01', 1, 24, 'word_arrange'),
  ('01', 1, 25, 'multiple_choice'),
  ('01', 1, 26, 'word_arrange'),
  ('01', 1, 27, 'subjective'),
  ('01', 1, 28, 'multiple_choice'),
  ('01', 1, 29, 'subjective'),
  ('01', 1, 30, 'subjective'),
  ('01', 1, 31, 'word_arrange'),
  ('01', 1, 32, 'multiple_choice'),
  ('01', 1, 33, 'subjective'),
  ('01', 1, 34, 'multiple_choice'),
  ('01', 1, 35, 'subjective'),
  ('01', 1, 36, 'multiple_choice'),
  ('01', 1, 37, 'word_arrange'),
  ('01', 1, 38, 'multiple_choice'),
  ('01', 1, 39, 'subjective'),
  ('01', 1, 40, 'multiple_choice'),
  ('01', 1, 41, 'word_arrange'),
  ('01', 1, 42, 'word_arrange'),
  ('01', 2, 1, 'subjective'),
  ('01', 2, 2, 'word_arrange'),
  ('01', 2, 3, 'subjective'),
  ('01', 2, 4, 'subjective'),
  ('01', 2, 5, 'word_arrange'),
  ('01', 2, 6, 'subjective'),
  ('01', 2, 7, 'subjective'),
  ('01', 2, 8, 'multiple_choice'),
  ('01', 2, 9, 'multiple_choice'),
  ('01', 2, 10, 'word_arrange'),
  ('01', 2, 11, 'subjective'),
  ('01', 2, 12, 'multiple_choice'),
  ('01', 2, 13, 'multiple_choice'),
  ('01', 2, 14, 'word_arrange'),
  ('01', 2, 15, 'subjective'),
  ('01', 2, 16, 'multiple_choice'),
  ('01', 2, 17, 'multiple_choice'),
  ('01', 2, 18, 'subjective'),
  ('01', 2, 19, 'subjective'),
  ('01', 2, 20, 'multiple_choice'),
  ('01', 2, 21, 'word_arrange'),
  ('01', 2, 22, 'multiple_choice'),
  ('01', 2, 23, 'multiple_choice'),
  ('01', 2, 24, 'word_arrange'),
  ('01', 2, 25, 'multiple_choice'),
  ('01', 2, 26, 'word_arrange'),
  ('01', 2, 27, 'word_arrange'),
  ('01', 2, 28, 'subjective'),
  ('01', 2, 29, 'multiple_choice'),
  ('01', 2, 30, 'word_arrange'),
  ('01', 2, 31, 'word_arrange'),
  ('01', 2, 32, 'subjective'),
  ('01', 2, 33, 'subjective'),
  ('01', 2, 34, 'word_arrange'),
  ('01', 2, 35, 'word_arrange'),
  ('01', 2, 36, 'multiple_choice'),
  ('01', 2, 37, 'subjective'),
  ('01', 2, 38, 'multiple_choice'),
  ('01', 2, 39, 'multiple_choice'),
  ('01', 2, 40, 'subjective'),
  ('01', 2, 41, 'word_arrange'),
  ('01', 2, 42, 'word_arrange'),
  ('01', 3, 1, 'multiple_choice'),
  ('01', 3, 2, 'word_arrange'),
  ('01', 3, 3, 'multiple_choice'),
  ('01', 3, 4, 'subjective'),
  ('01', 3, 5, 'subjective'),
  ('01', 3, 6, 'multiple_choice'),
  ('01', 3, 7, 'subjective'),
  ('01', 3, 8, 'word_arrange'),
  ('01', 3, 9, 'multiple_choice'),
  ('01', 3, 10, 'word_arrange'),
  ('01', 3, 11, 'subjective'),
  ('01', 3, 12, 'word_arrange'),
  ('01', 3, 13, 'multiple_choice'),
  ('01', 3, 14, 'subjective'),
  ('01', 3, 15, 'multiple_choice'),
  ('01', 3, 16, 'word_arrange'),
  ('01', 3, 17, 'word_arrange'),
  ('01', 3, 18, 'subjective'),
  ('01', 3, 19, 'word_arrange'),
  ('01', 3, 20, 'multiple_choice'),
  ('01', 3, 21, 'word_arrange'),
  ('01', 3, 22, 'subjective'),
  ('01', 3, 23, 'multiple_choice'),
  ('01', 3, 24, 'subjective'),
  ('01', 3, 25, 'subjective'),
  ('01', 3, 26, 'word_arrange'),
  ('01', 3, 27, 'subjective'),
  ('01', 3, 28, 'multiple_choice'),
  ('01', 3, 29, 'word_arrange'),
  ('01', 3, 30, 'word_arrange'),
  ('01', 3, 31, 'subjective'),
  ('01', 3, 32, 'word_arrange'),
  ('01', 3, 33, 'word_arrange'),
  ('01', 3, 34, 'multiple_choice'),
  ('01', 3, 35, 'multiple_choice'),
  ('01', 3, 36, 'subjective'),
  ('01', 3, 37, 'word_arrange'),
  ('01', 3, 38, 'multiple_choice'),
  ('01', 3, 39, 'multiple_choice'),
  ('01', 3, 40, 'subjective'),
  ('01', 3, 41, 'subjective'),
  ('01', 3, 42, 'multiple_choice');

-- detail: set 02 (126건, quiz_type 은 en 파일 기준)
INSERT INTO competition_quiz_detail (set_key, level, origin_id, quiz_type) VALUES
  ('02', 1, 1, 'subjective'),
  ('02', 1, 2, 'subjective'),
  ('02', 1, 3, 'word_arrange'),
  ('02', 1, 4, 'subjective'),
  ('02', 1, 5, 'word_arrange'),
  ('02', 1, 6, 'multiple_choice'),
  ('02', 1, 7, 'word_arrange'),
  ('02', 1, 8, 'word_arrange'),
  ('02', 1, 9, 'multiple_choice'),
  ('02', 1, 10, 'word_arrange'),
  ('02', 1, 11, 'word_arrange'),
  ('02', 1, 12, 'multiple_choice'),
  ('02', 1, 13, 'subjective'),
  ('02', 1, 14, 'multiple_choice'),
  ('02', 1, 15, 'subjective'),
  ('02', 1, 16, 'multiple_choice'),
  ('02', 1, 17, 'subjective'),
  ('02', 1, 18, 'word_arrange'),
  ('02', 1, 19, 'word_arrange'),
  ('02', 1, 20, 'multiple_choice'),
  ('02', 1, 21, 'word_arrange'),
  ('02', 1, 22, 'subjective'),
  ('02', 1, 23, 'multiple_choice'),
  ('02', 1, 24, 'subjective'),
  ('02', 1, 25, 'multiple_choice'),
  ('02', 1, 26, 'word_arrange'),
  ('02', 1, 27, 'subjective'),
  ('02', 1, 28, 'multiple_choice'),
  ('02', 1, 29, 'multiple_choice'),
  ('02', 1, 30, 'subjective'),
  ('02', 1, 31, 'subjective'),
  ('02', 1, 32, 'word_arrange'),
  ('02', 1, 33, 'subjective'),
  ('02', 1, 34, 'word_arrange'),
  ('02', 1, 35, 'multiple_choice'),
  ('02', 1, 36, 'subjective'),
  ('02', 1, 37, 'multiple_choice'),
  ('02', 1, 38, 'multiple_choice'),
  ('02', 1, 39, 'word_arrange'),
  ('02', 1, 40, 'word_arrange'),
  ('02', 1, 41, 'subjective'),
  ('02', 1, 42, 'multiple_choice'),
  ('02', 2, 1, 'subjective'),
  ('02', 2, 2, 'multiple_choice'),
  ('02', 2, 3, 'subjective'),
  ('02', 2, 4, 'multiple_choice'),
  ('02', 2, 5, 'word_arrange'),
  ('02', 2, 6, 'subjective'),
  ('02', 2, 7, 'multiple_choice'),
  ('02', 2, 8, 'subjective'),
  ('02', 2, 9, 'subjective'),
  ('02', 2, 10, 'word_arrange'),
  ('02', 2, 11, 'word_arrange'),
  ('02', 2, 12, 'subjective'),
  ('02', 2, 13, 'word_arrange'),
  ('02', 2, 14, 'multiple_choice'),
  ('02', 2, 15, 'multiple_choice'),
  ('02', 2, 16, 'subjective'),
  ('02', 2, 17, 'subjective'),
  ('02', 2, 18, 'word_arrange'),
  ('02', 2, 19, 'subjective'),
  ('02', 2, 20, 'subjective'),
  ('02', 2, 21, 'word_arrange'),
  ('02', 2, 22, 'multiple_choice'),
  ('02', 2, 23, 'word_arrange'),
  ('02', 2, 24, 'multiple_choice'),
  ('02', 2, 25, 'word_arrange'),
  ('02', 2, 26, 'word_arrange'),
  ('02', 2, 27, 'multiple_choice'),
  ('02', 2, 28, 'multiple_choice'),
  ('02', 2, 29, 'word_arrange'),
  ('02', 2, 30, 'multiple_choice'),
  ('02', 2, 31, 'word_arrange'),
  ('02', 2, 32, 'multiple_choice'),
  ('02', 2, 33, 'word_arrange'),
  ('02', 2, 34, 'multiple_choice'),
  ('02', 2, 35, 'subjective'),
  ('02', 2, 36, 'word_arrange'),
  ('02', 2, 37, 'multiple_choice'),
  ('02', 2, 38, 'multiple_choice'),
  ('02', 2, 39, 'subjective'),
  ('02', 2, 40, 'word_arrange'),
  ('02', 2, 41, 'subjective'),
  ('02', 2, 42, 'subjective'),
  ('02', 3, 1, 'multiple_choice'),
  ('02', 3, 2, 'word_arrange'),
  ('02', 3, 3, 'subjective'),
  ('02', 3, 4, 'word_arrange'),
  ('02', 3, 5, 'word_arrange'),
  ('02', 3, 6, 'subjective'),
  ('02', 3, 7, 'multiple_choice'),
  ('02', 3, 8, 'subjective'),
  ('02', 3, 9, 'subjective'),
  ('02', 3, 10, 'word_arrange'),
  ('02', 3, 11, 'multiple_choice'),
  ('02', 3, 12, 'word_arrange'),
  ('02', 3, 13, 'word_arrange'),
  ('02', 3, 14, 'multiple_choice'),
  ('02', 3, 15, 'multiple_choice'),
  ('02', 3, 16, 'word_arrange'),
  ('02', 3, 17, 'multiple_choice'),
  ('02', 3, 18, 'multiple_choice'),
  ('02', 3, 19, 'subjective'),
  ('02', 3, 20, 'subjective'),
  ('02', 3, 21, 'word_arrange'),
  ('02', 3, 22, 'word_arrange'),
  ('02', 3, 23, 'subjective'),
  ('02', 3, 24, 'subjective'),
  ('02', 3, 25, 'multiple_choice'),
  ('02', 3, 26, 'multiple_choice'),
  ('02', 3, 27, 'word_arrange'),
  ('02', 3, 28, 'subjective'),
  ('02', 3, 29, 'subjective'),
  ('02', 3, 30, 'word_arrange'),
  ('02', 3, 31, 'word_arrange'),
  ('02', 3, 32, 'multiple_choice'),
  ('02', 3, 33, 'subjective'),
  ('02', 3, 34, 'multiple_choice'),
  ('02', 3, 35, 'subjective'),
  ('02', 3, 36, 'word_arrange'),
  ('02', 3, 37, 'multiple_choice'),
  ('02', 3, 38, 'word_arrange'),
  ('02', 3, 39, 'multiple_choice'),
  ('02', 3, 40, 'subjective'),
  ('02', 3, 41, 'multiple_choice'),
  ('02', 3, 42, 'subjective');

-- content: competition_quiz_en_01.json level 1 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 1), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'기다리다\' = ______', NULL, NULL, NULL, NULL, NULL, 'wait', '["wait","Wait"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 2), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'결정하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'decide', '["decide","Decide"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 3), 'en', '다음 중 \'분명한, 명백한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["optional","obvious","ordinary","original"]', 'obvious', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 4), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 아픈 척했어', '["He","be","sick","pretended","to"]', '["He","pretended","to","be","sick"]', NULL, NULL, 'He pretended to be sick', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 5), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'친절한\' = ______', NULL, NULL, NULL, NULL, NULL, 'kind', '["kind","Kind"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 6), 'en', '다음 중 \'피곤한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["tired","tough","timid","tense"]', 'tired', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 7), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'준비된\' = ______', NULL, NULL, NULL, NULL, NULL, 'ready', '["ready","Ready"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 8), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 정말 배고파', '["am","hungry","I","really"]', '["I","am","really","hungry"]', NULL, NULL, 'I am really hungry', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 9), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'걱정하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'worry', '["worry","Worry"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 10), 'en', '단어를 배열해서 문장을 완성하세요.', '그 상황은 매우 어색했어', '["awkward","The","was","very","situation"]', '["The","situation","was","very","awkward"]', NULL, NULL, 'The situation was very awkward', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 11), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'도착하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'arrive', '["arrive","Arrive"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 12), 'en', '다음 중 \'알아채다, 눈치채다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["notice","name","need","nod"]', 'notice', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 13), 'en', '다음 중 \'망설이다, 주저하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["hurry","happen","hesitate","handle"]', 'hesitate', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 14), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 시험이 무서워', '["scared","the","is","of","She","exam"]', '["She","is","scared","of","the","exam"]', NULL, NULL, 'She is scared of the exam', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 15), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 시험에 대해 걱정해', '["about","worries","She","exam","the"]', '["She","worries","about","the","exam"]', NULL, NULL, 'She worries about the exam', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 16), 'en', '다음 중 \'실수로, 우연히\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["accidentally","actively","apparently","actually"]', 'accidentally', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 17), 'en', '다음 중 \'안도한, 다행스러운\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["relaxed","relieved","reduced","reserved"]', 'relieved', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 18), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그를 기다렸어', '["him","for","waited","I"]', '["I","waited","for","him"]', NULL, NULL, 'I waited for him', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 19), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 대답하기를 망설였어', '["answer","hesitated","to","I"]', '["I","hesitated","to","answer"]', NULL, NULL, 'I hesitated to answer', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 20), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'화난\' = ______', NULL, NULL, NULL, NULL, NULL, 'angry', '["angry","Angry"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 21), 'en', '다음 중 \'점차, 서서히\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["generally","gradually","greatly","gladly"]', 'gradually', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 22), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 내 실수를 눈치챘어', '["mistake","She","noticed","my"]', '["She","noticed","my","mistake"]', NULL, NULL, 'She noticed my mistake', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 23), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'배고픈\' = ______', NULL, NULL, NULL, NULL, NULL, 'hungry', '["hungry","Hungry"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 24), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 매우 친절해', '["kind","He","very","is"]', '["He","is","very","kind"]', NULL, NULL, 'He is very kind', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 25), 'en', '다음 중 \'긴장한, 불안한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["narrow","noisy","nervous","natural"]', 'nervous', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 26), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 오늘 매우 피곤해', '["very","I","today","am","tired"]', '["I","am","very","tired","today"]', NULL, NULL, 'I am very tired today', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 27), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'슬픈\' = ______', NULL, NULL, NULL, NULL, NULL, 'sad', '["sad","Sad"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 28), 'en', '다음 중 \'놀란\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["satisfied","stressed","surprised","serious"]', 'surprised', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 29), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'설명하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'explain', '["explain","Explain"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 30), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'~인 척하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'pretend', '["pretend","Pretend"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 31), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 버스를 놓쳤어', '["the","I","bus","missed"]', '["I","missed","the","bus"]', NULL, NULL, 'I missed the bus', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 32), 'en', '다음 중 \'피하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["avoid","apply","allow","admit"]', 'avoid', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 33), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'무서운, 두려운\' = ______', NULL, NULL, NULL, NULL, NULL, 'scared', '["scared","Scared"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 34), 'en', '다음 중 \'친숙한, 낯익은\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["flexible","familiar","formal","frequent"]', 'familiar', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 35), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'어색한, 불편한\' = ______', NULL, NULL, NULL, NULL, NULL, 'awkward', '["awkward","Awkward"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 36), 'en', '다음 중 \'당황스러운, 창피한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["embarrassed","encouraged","exhausted","excited"]', 'embarrassed', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 37), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그를 피했어', '["avoided","I","him"]', '["I","avoided","him"]', NULL, NULL, 'I avoided him', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 38), 'en', '다음 중 \'행복한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["heavy","handy","happy","hasty"]', 'happy', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 39), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'잊다\' = ______', NULL, NULL, NULL, NULL, NULL, 'forget', '["forget","Forget"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 40), 'en', '다음 중 \'조용한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["quit","quick","quite","quiet"]', 'quiet', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 41), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 화가 났어', '["was","angry","I"]', '["I","was","angry"]', NULL, NULL, 'I was angry', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 42), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 내 이름을 잊었어', '["I","my","forgot","name"]', '["I","forgot","my","name"]', NULL, NULL, 'I forgot my name', NULL);

-- content: competition_quiz_en_01.json level 2 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 1), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

I need to ______ an appointment with the dentist.
(나는 치과 예약을 잡아야 한다)

\'예약을 잡다\' = ______', NULL, NULL, NULL, NULL, NULL, 'make', '["make","Make"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 2), 'en', '단어를 배열해서 문장을 완성하세요.', '그 파티는 결국 정말 재미있는 것으로 드러났다', '["fun","seemed","be","to","really","out","The","party","turned"]', '["The","party","turned","out","to","be","really","fun"]', '["seemed"]', NULL, 'The party turned out to be really fun', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 3), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'허락하다, 허용하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'permit', '["permit","Permit","allow","Allow"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 4), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'~에도 불구하고\' = ______', NULL, NULL, NULL, NULL, NULL, 'despite', '["despite","Despite"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 5), 'en', '단어를 배열해서 문장을 완성하세요.', '그 팀은 어려운 상황에도 불구하고 목표를 달성했다', '["difficult","team","their","despite","although","The","situation","achieved","the","goal"]', '["The","team","achieved","their","goal","despite","the","difficult","situation"]', '["although"]', NULL, 'The team achieved their goal despite the difficult situation', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 6), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(돈·자원 등을) 절약하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'save', '["save","Save"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 7), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(문제·어려움을) 극복하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'overcome', '["overcome","Overcome"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 8), 'en', '시험 전날 밤, 걱정이 되어 잠을 못 잘 것 같은 느낌에 가장 어울리는 단어는?', NULL, NULL, NULL, NULL, '["nervous","confused","anxious","ashamed"]', 'anxious', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 9), 'en', '다음 중 \'~에 익숙해지다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["get back to","get used to","get along with","get rid of"]', 'get used to', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 10), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 새 도시 생활에 익숙해지고 있다', '["her","is","new","life","city","She","accustomed","getting","used","to"]', '["She","is","getting","used","to","her","new","city","life"]', '["accustomed"]', NULL, 'She is getting used to her new city life', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 11), 'en', '빈칸에 알맞은 영어 표현을 쓰세요.

\'~을 포기하다\' = give ______', NULL, NULL, NULL, NULL, NULL, 'give up', '["give up","Give up","Give Up"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 12), 'en', '다음 중 \'예상치 못한 결과가 나타나다\'를 뜻하는 구동사는?', NULL, NULL, NULL, NULL, '["turn up","turn out","turn down","turn off"]', 'turn out', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 13), 'en', '다음 중 \'시간 가는 줄 모르다\'에 해당하는 표현은?', NULL, NULL, NULL, NULL, '["lose count of time","lose track of time","lose sight of time","lose touch of time"]', 'lose track of time', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 14), 'en', '단어를 배열해서 문장을 완성하세요.', '우리는 졸업 후에도 연락을 유지했다', '["touch","lost","graduation","after","kept","We","in"]', '["We","kept","in","touch","after","graduation"]', '["lost"]', NULL, 'We kept in touch after graduation', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 15), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'~을 의존하다, ~에 달려 있다\' = ______', NULL, NULL, NULL, NULL, NULL, 'rely', '["rely","Rely","depend","Depend"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 16), 'en', '다음 중 \'압도된, 감당이 안 되는\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["overwhelmed","overloaded","overrated","overlooked"]', 'overwhelmed', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 17), 'en', '다음 중 \'지속적인, 계속되는\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["consistent","convenient","considerable","confident"]', 'consistent', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 18), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

He gave a ______ speech in front of everyone.
(그는 모든 사람들 앞에서 진심 어린 연설을 했다)

\'진심 어린, 진지한\' = ______', NULL, NULL, NULL, NULL, NULL, 'sincere', '["sincere","Sincere"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 19), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'달성하다, 이루다\' = ______', NULL, NULL, NULL, NULL, NULL, 'achieve', '["achieve","Achieve"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 20), 'en', '다음 중 \'결정을 내리다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["take a decision","do a decision","have a decision","make a decision"]', 'make a decision', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 21), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 다이어트를 포기하지 않기로 결심했다', '["his","not","continue","up","to","diet","decided","He","give"]', '["He","decided","not","to","give","up","his","diet"]', '["continue"]', NULL, 'He decided not to give up his diet', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 22), 'en', '다음 중 \'어떤 문제를 해결하거나 이해하다\'를 뜻하는 구동사는?', NULL, NULL, NULL, NULL, '["figure up","figure off","figure out","figure in"]', 'figure out', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 23), 'en', '다음 중 \'의도적으로, 일부러\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["definitely","desperately","deliberately","delicately"]', 'deliberately', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 24), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 오래된 사진첩을 우연히 발견했다', '["album","came","He","across","photo","old","searched","an"]', '["He","came","across","an","old","photo","album"]', '["searched"]', NULL, 'He came across an old photo album', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 25), 'en', '다음 중 \'책임지다, 담당하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["be in front of","be in place of","be in need of","be in charge of"]', 'be in charge of', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 26), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 어제 슈퍼마켓에서 옛 친구를 우연히 만났다', '["the","old","avoided","ran","at","an","supermarket","friend","into","I"]', '["I","ran","into","an","old","friend","at","the","supermarket"]', '["avoided"]', NULL, 'I ran into an old friend at the supermarket', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 27), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 발표 도중에 감정을 참을 수 없었다', '["the","back","during","couldn\'t","He","hold","expressed","emotions","his","presentation"]', '["He","couldn\'t","hold","back","his","emotions","during","the","presentation"]', '["expressed"]', NULL, 'He couldn\'t hold back his emotions during the presentation', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 28), 'en', '빈칸에 알맞은 영어 표현을 쓰세요.

She couldn\'t ______ her anger during the meeting.
(그녀는 회의 중에 화를 참을 수 없었다)', NULL, NULL, NULL, NULL, NULL, 'hold back', '["hold back","Hold back","Hold Back"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 29), 'en', '친구를 우연히 마주쳤을 때 쓸 수 있는 표현은?', NULL, NULL, NULL, NULL, '["run into","run out","run off","run over"]', 'run into', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 30), 'en', '단어를 배열해서 문장을 완성하세요.', '그 도전을 극복하는 것이 그녀를 더 강하게 만들었다', '["her","the","made","challenge","avoiding","stronger","Overcoming"]', '["Overcoming","the","challenge","made","her","stronger"]', '["avoiding"]', NULL, 'Overcoming the challenge made her stronger', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 31), 'en', '단어를 배열해서 문장을 완성하세요.', '우리는 그 문제의 해결책을 알아냈다', '["out","to","solution","a","figured","We","problem","forgot","the"]', '["We","figured","out","a","solution","to","the","problem"]', '["forgot"]', NULL, 'We figured out a solution to the problem', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 32), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'눈에 띄는, 두드러진\' = ______', NULL, NULL, NULL, NULL, NULL, 'remarkable', '["remarkable","Remarkable"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 33), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'불평하다, 항의하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'complain', '["complain","Complain"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 34), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 그 프로젝트를 담당하고 있다', '["charge","the","She","responsible","in","is","of","project"]', '["She","is","in","charge","of","the","project"]', '["responsible"]', NULL, 'She is in charge of the project', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 35), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 일부러 그 주제를 피했다', '["deliberately","accidentally","topic","the","avoided","She"]', '["She","deliberately","avoided","the","topic"]', '["accidentally"]', NULL, 'She deliberately avoided the topic', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 36), 'en', '다음 중 \'연락을 유지하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["keep in touch","keep in line","keep in shape","keep in mind"]', 'keep in touch', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 37), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'확신하는, 자신감 있는\' = ______', NULL, NULL, NULL, NULL, NULL, 'confident', '["confident","Confident"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 38), 'en', '길을 걷다가 흥미로운 카페를 우연히 발견했을 때 쓸 수 있는 표현은?', NULL, NULL, NULL, NULL, '["come apart","come along","come about","come across"]', 'come across', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 39), 'en', '다음 중 \'미리 예상하다, 기대하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["accumulate","accelerate","anticipate","appreciate"]', 'anticipate', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 40), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

The new policy will ______ a lot of people.
(새 정책은 많은 사람들에게 영향을 미칠 것이다)

\'영향을 미치다\' = ______', NULL, NULL, NULL, NULL, NULL, 'affect', '["affect","Affect"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 41), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 마감일을 지키는 것이 중요하다고 생각한다', '["I","important","is","it","the","think","meet","to","deadline","miss"]', '["I","think","it","is","important","to","meet","the","deadline"]', '["miss"]', NULL, 'I think it is important to meet the deadline', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 42), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그 결과를 미리 예상하지 못했다', '["anticipate","the","in","I","result","expected","advance","didn\'t"]', '["I","didn\'t","anticipate","the","result","in","advance"]', '["expected"]', NULL, 'I didn\'t anticipate the result in advance', NULL);

-- content: competition_quiz_en_01.json level 3 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 1), 'en', '다음 중 \'(문제·질문 등을) 회피하다, 얼버무리다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["exceed","evade","evolve","erode"]', 'evade', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 2), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 그 비판을 대수롭지 않게 무시해버렸다', '["off","away","criticism","ignored","simply","brushed","He","the"]', '["He","simply","brushed","off","the","criticism"]', '["ignored","away"]', NULL, 'He simply brushed off the criticism', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 3), 'en', '다음 중 \'(사실·감정을) 표현하거나 말하지 않고 숨기다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["hold back","hold over","hold up","hold out"]', 'hold back', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 4), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'겉으로 드러내지 않고 속에 품은, 내재된\' = ______', NULL, NULL, NULL, NULL, NULL, 'underlying', '["underlying","Underlying"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 5), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(어떤 것을) 깊이 들여다보다, 파고들다\' = ______', NULL, NULL, NULL, NULL, NULL, 'delve', '["delve","Delve"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 6), 'en', '다음 중 \'(결과가 사실로) 드러나다, 판명되다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["turn up to be","turn around as","turn out to be","turn into being"]', 'turn out to be', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 7), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(관계·상황이) 점점 나빠지다, 악화되다\' = ______', NULL, NULL, NULL, NULL, NULL, 'deteriorate', '["deteriorate","Deteriorate"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 8), 'en', '단어를 배열해서 문장을 완성하세요.', '그 해고는 결국 변장한 축복이 되었다', '["blessing","hidden","a","in","turned","be","after","out","The","disguise","to","layoff"]', '["The","layoff","turned","out","to","be","a","blessing","in","disguise"]', '["hidden","after"]', NULL, 'The layoff turned out to be a blessing in disguise', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 9), 'en', '다음 중 \'(나쁜 상황을) 받아들이다, 감수하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["come across as","come up with","come to terms with","come down to"]', 'come to terms with', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 10), 'en', '단어를 배열해서 문장을 완성하세요.', '상황이 통제 불가능하게 되기 전에 대화를 해야 한다', '["things","get","hand","talk","worse","to","before","need","control","out","We","of"]', '["We","need","to","talk","before","things","get","out","of","hand"]', '["control","worse"]', NULL, 'We need to talk before things get out of hand', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 11), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(기회·자원 등을) 최대한 활용하다\' = make the most of ______

(위 표현에서 빈칸이 아닌, 표현 자체를 완성하세요)

\'(기회를) 최대한 활용하다\' 표현의 동사 부분 3단어: make the _______ of', NULL, NULL, NULL, NULL, NULL, 'most', '["most","Most"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 12), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 기대에 부응하기 위해 최선을 다했다', '["expectations","meet","up","best","He","with","to","live","to","his","did"]', '["He","did","his","best","to","live","up","to","expectations"]', '["meet","with"]', NULL, 'He did his best to live up to expectations', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 13), 'en', '직장에서 극심한 스트레스가 건강에 심각한 영향을 미치고 있다는 상황에서 가장 어울리는 표현은?', NULL, NULL, NULL, NULL, '["take a shot at","take a toll on","take a turn for","take a stand on"]', 'take a toll on', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 14), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'일시적인, 영구적이지 않은\' = ______', NULL, NULL, NULL, NULL, NULL, 'transient', '["transient","Transient"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 15), 'en', '다음 중 \'(기대나 기준에) 부응하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["live up to","live along with","live down to","live out of"]', 'live up to', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 16), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 어려운 질문들을 계속 회피했다', '["kept","evading","questions","hardly","difficult","the","He","avoiding"]', '["He","kept","evading","the","difficult","questions"]', '["avoiding","hardly"]', NULL, 'He kept evading the difficult questions', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 17), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 위험을 무릅쓰고 진실을 말했다', '["on","and","out","truth","told","went","He","risked","the","limb","branch","a"]', '["He","went","out","on","a","limb","and","told","the","truth"]', '["risked","branch"]', NULL, 'He went out on a limb and told the truth', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 18), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(상황이나 감정을) 악화시키다, 더 심하게 하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'exacerbate', '["exacerbate","Exacerbate"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 19), 'en', '단어를 배열해서 문장을 완성하세요.', '그 팀은 새로운 해결책을 생각해냈다', '["found","a","team","up","with","The","solution","new","down","came"]', '["The","team","came","up","with","a","new","solution"]', '["found","down"]', NULL, 'The team came up with a new solution', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 20), 'en', '다음 중 \'완전히 망연자실한, 큰 충격을 받은\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["discouraged","devastated","disoriented","disheartened"]', 'devastated', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 21), 'en', '단어를 배열해서 문장을 완성하세요.', '장기적인 스트레스는 면역 체계를 악화시킬 수 있다', '["affect","immune","exacerbate","Long-term","decline","stress","worsen","the","can","system\'s"]', '["Long-term","stress","can","exacerbate","the","immune","system\'s","decline"]', '["worsen","affect"]', NULL, 'Long-term stress can exacerbate the immune system\'s decline', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 22), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(상황이나 사람을) 조종하다, 교묘하게 다루다\' = ______', NULL, NULL, NULL, NULL, NULL, 'manipulate', '["manipulate","Manipulate"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 23), 'en', '다음 중 \'겉으로 보이는 것 이상의 숨겨진 의미를 파악하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["read against the grain","read through the dots","read over the surface","read between the lines"]', 'read between the lines', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 24), 'en', '빈칸에 알맞은 영어 표현을 쓰세요.

\'She doesn\'t want to ______ the past.\' (과거를 들추고 싶지 않다)

(빈칸 = \'들추다, 다시 꺼내다\'에 해당하는 구동사)', NULL, NULL, NULL, NULL, NULL, 'bring up', '["bring up","Bring up"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 25), 'en', '빈칸에 알맞은 영어 표현을 쓰세요.

\'위험을 무릅쓰다, 모험을 하다\' = go out on a ______', NULL, NULL, NULL, NULL, NULL, 'limb', '["limb","Limb"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 26), 'en', '단어를 배열해서 문장을 완성하세요.', '그 사건은 그녀의 정신 건강에 심각한 영향을 미쳤다', '["mental","took","a","major","her","incident","on","toll","paid","The","health"]', '["The","incident","took","a","toll","on","her","mental","health"]', '["paid","major"]', NULL, 'The incident took a toll on her mental health', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 27), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(감정·상황을) 악의 없이 대범하게 무시하다, 털어내다\' = ______

예: \'He simply ______ the criticism.\'', NULL, NULL, NULL, NULL, NULL, 'brushed off', '["brushed off","brush off","Brushed off","Brush off"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 28), 'en', '다음 중 \'(문제가) 손에 감당하기 어렵게 커지다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["get out of hand","get out of touch","get out of line","get out of shape"]', 'get out of hand', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 29), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 그 기회를 최대한 활용하기로 결심했다', '["She","decided","best","to","most","the","of","the","opportunity","use","make"]', '["She","decided","to","make","the","most","of","the","opportunity"]', '["best","use"]', NULL, 'She decided to make the most of the opportunity', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 30), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 상사의 말에 담긴 숨은 의미를 파악했다', '["words","read","through","her","lines","of","behind","She","the","boss","between"]', '["She","read","between","the","lines","of","her","boss","words"]', '["through","behind"]', NULL, 'She read between the lines of her boss words', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 31), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(결과·결론을) 도출하다, 추론하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'infer', '["infer","Infer"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 32), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 결국 자신의 실패를 받아들이게 되었다', '["with","to","accepted","finally","against","failure","terms","He","came","his"]', '["He","finally","came","to","terms","with","his","failure"]', '["accepted","against"]', NULL, 'He finally came to terms with his failure', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 33), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 감정을 억누르며 미소를 지었다', '["smiled","emotions","her","She","back","suppressed","held","over","and"]', '["She","held","back","her","emotions","and","smiled"]', '["suppressed","over"]', NULL, 'She held back her emotions and smiled', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 34), 'en', '다음 중 \'뜻밖에 좋은 결과를 가져온 불행한 일\'을 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["a blessing in disguise","a shot in the dark","a blessing in advance","a silver lining"]', 'a blessing in disguise', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 35), 'en', '다음 중 \'(아이디어·계획을) 생각해내다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["come along with","come out with","come up with","come down with"]', 'come up with', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 36), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(주장·이론 등을) 반박하다, 반증하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'refute', '["refute","Refute"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 37), 'en', '단어를 배열해서 문장을 완성하세요.', '그 연구는 그 이론의 근본적인 문제점을 드러냈다', '["revealed","the","flaws","the","of","study","underlying","hidden","theory","The","basic"]', '["The","study","revealed","the","underlying","flaws","of","the","theory"]', '["hidden","basic"]', NULL, 'The study revealed the underlying flaws of the theory', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 38), 'en', '다음 중 \'(노력 없이) 어떤 것을 그냥 따라가다, 무비판적으로 받아들이다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["go over with","go through with","go ahead with","go along with"]', 'go along with', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 39), 'en', '다음 중 \'깊이 생각하다, 심사숙고하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["contemplate","concentrate","communicate","compensate"]', 'contemplate', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 40), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(사람의 말이나 행동이) 모순되는, 앞뒤가 맞지 않는\' = ______', NULL, NULL, NULL, NULL, NULL, 'inconsistent', '["inconsistent","Inconsistent"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 41), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'(어려움·슬픔을 이겨내고) 회복하는, 탄력 있는\' = ______', NULL, NULL, NULL, NULL, NULL, 'resilient', '["resilient","Resilient"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 42), 'en', '다음 중 \'불안한, 걱정되는\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["assertive","appreciative","aggressive","apprehensive"]', 'apprehensive', NULL);

-- content: competition_quiz_ja_01.json level 1 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 1), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'준비하다\' = ______', NULL, NULL, NULL, NULL, NULL, '準備する', '["準備する","じゅんびする","準備","じゅんび"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 2), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 음악을 듣는 것을 좋아한다', '["私は","音楽を","好きです","聞くのが"]', '["私は","音楽を","聞くのが","好きです"]', NULL, NULL, '私は音楽を聞くのが好きです', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 3), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'오늘\' = ______', NULL, NULL, NULL, NULL, NULL, '今日', '["今日","きょう"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 4), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'보다\' = ______', NULL, NULL, NULL, NULL, NULL, '見る', '["見る","みる"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 5), 'ja', '다음 중 \'약속\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["用事","約束","理由","予定"]', '約束', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 6), 'ja', '타일을 배열해서 문장을 완성하세요.', '오늘은 날씨가 좋다', '["天気が","いい","今日は"]', '["今日は","天気が","いい"]', NULL, NULL, '今日は天気がいい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 7), 'ja', '타일을 배열해서 문장을 완성하세요.', '이 길은 위험하다', '["道は","危ない","この"]', '["この","道は","危ない"]', NULL, NULL, 'この道は危ない', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 8), 'ja', '다음 중 \'크다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["広い","長い","大きい","多い"]', '大きい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 9), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 친구와 약속이 있다', '["友達と","私は","ある","約束が"]', '["私は","友達と","約束が","ある"]', NULL, NULL, '私は友達と約束がある', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 10), 'ja', '다음 중 \'먹다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["作る","食べる","買う","飲む"]', '食べる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 11), 'ja', '다음 중 \'기분, 마음\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["気分","感情","心配","気持ち"]', '気持ち', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 12), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 열쇠를 방에서 찾았다', '["鍵を","部屋で","私は","探した"]', '["私は","鍵を","部屋で","探した"]', NULL, NULL, '私は鍵を部屋で探した', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 13), 'ja', '타일을 배열해서 문장을 완성하세요.', '오빠는 회사에서 일한다', '["働く","兄は","会社で"]', '["兄は","会社で","働く"]', NULL, NULL, '兄は会社で働く', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 14), 'ja', '다음 중 \'찾다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["探す","返す","消す","貸す"]', '探す', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 15), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'연습하다\' = ______', NULL, NULL, NULL, NULL, NULL, '練習する', '["練習する","れんしゅうする","練習","れんしゅう"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 16), 'ja', '다음 중 \'슬프다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["悲しい","嬉しい","寂しい","恥ずかしい"]', '悲しい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 17), 'ja', '타일을 배열해서 문장을 완성하세요.', '그녀는 겨우 집에 돌아왔다', '["彼女は","帰った","家に","やっと"]', '["彼女は","やっと","家に","帰った"]', NULL, NULL, '彼女はやっと家に帰った', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 18), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 도서관에서 공부한다', '["図書館で","私は","勉強する"]', '["私は","図書館で","勉強する"]', NULL, NULL, '私は図書館で勉強する', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 19), 'ja', '다음 중 \'서두르다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["動く","急ぐ","歩く","遊ぶ"]', '急ぐ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 20), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'시간\' = ______', NULL, NULL, NULL, NULL, NULL, '時間', '["時間","じかん"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 21), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 매일 학교에 간다', '["私は","毎日","行く","学校に"]', '["私は","毎日","学校に","行く"]', NULL, NULL, '私は毎日学校に行く', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 22), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'기쁘다\' = ______', NULL, NULL, NULL, NULL, NULL, '嬉しい', '["嬉しい","うれしい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 23), 'ja', '타일을 배열해서 문장을 완성하세요.', '역까지 서둘러 걸었다', '["歩いた","駅まで","急いで"]', '["駅まで","急いで","歩いた"]', NULL, NULL, '駅まで急いで歩いた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 24), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'책\' = ______', NULL, NULL, NULL, NULL, NULL, '本', '["本","ほん"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 25), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 지갑을 잊어버렸다', '["財布を","私は","忘れた"]', '["私は","財布を","忘れた"]', NULL, NULL, '私は財布を忘れた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 26), 'ja', '다음 중 \'잊다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["覚える","忘れる","考える","知る"]', '忘れる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 27), 'ja', '타일을 배열해서 문장을 완성하세요.', '저는 커피를 마십니다', '["コーヒーを","私は","飲みます"]', '["私は","コーヒーを","飲みます"]', NULL, NULL, '私はコーヒーを飲みます', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 28), 'ja', '다음 중 \'친구\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["友達","家族","兄弟","彼女"]', '友達', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 29), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'위험하다\' = ______', NULL, NULL, NULL, NULL, NULL, '危ない', '["危ない","あぶない"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 30), 'ja', '다음 중 \'아마도\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["もっと","きっと","やっと","たぶん"]', 'たぶん', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 31), 'ja', '다음 중 \'부끄럽다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["恥ずかしい","怖い","悲しい","眠い"]', '恥ずかしい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 32), 'ja', '타일을 배열해서 문장을 완성하세요.', '그는 아마도 늦을 것이다', '["たぶん","彼は","遅れる"]', '["彼は","たぶん","遅れる"]', NULL, NULL, '彼はたぶん遅れる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 33), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'외롭다\' = ______', NULL, NULL, NULL, NULL, NULL, '寂しい', '["寂しい","さびしい","淋しい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 34), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'편리하다\' = ______', NULL, NULL, NULL, NULL, NULL, '便利', '["便利","べんり","便利な","べんりな"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 35), 'ja', '다음 중 \'학교\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["学校","病院","図書館","会社"]', '学校', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 36), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'결석하다\' = ______', NULL, NULL, NULL, NULL, NULL, '欠席する', '["欠席する","けっせきする","欠席","けっせき"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 37), 'ja', '다음 중 \'겨우, 드디어\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["また","すぐ","もう","やっと"]', 'やっと', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 38), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'반드시, 틀림없이\' = ______', NULL, NULL, NULL, NULL, NULL, 'きっと', '["きっと"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 39), 'ja', '다음 중 \'무섭다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["痛い","眠い","辛い","怖い"]', '怖い', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 40), 'ja', '타일을 배열해서 문장을 완성하세요.', '저 가방은 큽니다', '["かばんは","あの","大きいです"]', '["あの","かばんは","大きいです"]', NULL, NULL, 'あのかばんは大きいです', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 41), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'물\' = ______', NULL, NULL, NULL, NULL, NULL, '水', '["水","みず"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 42), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'기다리다\' = ______', NULL, NULL, NULL, NULL, NULL, '待つ', '["待つ","まつ"]');

-- content: competition_quiz_ja_01.json level 2 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 1), 'ja', '타일을 배열해서 문장을 완성하세요.', '오래된 사진을 보고 어린 시절을 떠올렸다', '["思い出した","子どもの頃を","思い込んだ","古い写真を見て"]', '["古い写真を見て","子どもの頃を","思い出した"]', '["思い込んだ"]', NULL, '古い写真を見て子どもの頃を思い出した', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 2), 'ja', '\'예상 외로, 의외로\'라는 뜻을 가진 부사는?', NULL, NULL, NULL, NULL, '["意外","以外","案外","案内"]', '案外', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 3), 'ja', '다음 중 \'찾게 되다, 발견되다\'를 뜻하는 자동사는?', NULL, NULL, NULL, NULL, '["見つける","見つかる","見せる","見える"]', '見つかる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 4), 'ja', '타일을 배열해서 문장을 완성하세요.', '상황이 점점 심각해지고 있다', '["状況が","どんどん","厳しく","なっている","深刻に"]', '["状況が","どんどん","深刻に","なっている"]', '["厳しく"]', NULL, '状況がどんどん深刻になっている', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 5), 'ja', '빈칸에 알맞은 일본어를 쓰세요.

\'어릴 때 추억을 떠올렸다\' = 子どもの頃の思い出を______', NULL, NULL, NULL, NULL, NULL, '思い出した', '["思い出した","おもいだした"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 6), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

彼女は親友に秘密を______してしまった。
(그녀는 친한 친구에게 비밀을 털어놓아 버렸다.)', NULL, NULL, NULL, NULL, NULL, '打ち明け', '["打ち明け","うちあけ","打ち明ける","うちあける"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 7), 'ja', '다음 중 \'오해\'를 뜻하는 일본어 단어는?', NULL, NULL, NULL, NULL, '["解釈","誤解","解決","理解"]', '誤解', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 8), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

その映画は______が高くて、チケットがすぐ売り切れた。
(그 영화는 평판이 높아서 티켓이 금방 매진됐다.)', NULL, NULL, NULL, NULL, NULL, '評判', '["評判","ひょうばん"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 9), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

急いでいたので、財布を家に______きてしまった。
(서둘렀기 때문에 지갑을 집에 놓고 와버렸다.)', NULL, NULL, NULL, NULL, NULL, '置き忘れて', '["置き忘れて","おきわすれて","置き忘れ","おきわすれ"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 10), 'ja', '다음 중 \'생각해내다, 떠올리다\'를 뜻하는 복합동사는?', NULL, NULL, NULL, NULL, '["思い出す","思い上がる","思い切る","思い込む"]', '思い出す', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 11), 'ja', '타일을 배열해서 문장을 완성하세요.', '그는 여전히 매일 아침 운동을 한다', '["相変わらず","彼は","今でも","毎朝","運動している"]', '["彼は","相変わらず","毎朝","運動している"]', '["今でも"]', NULL, '彼は相変わらず毎朝運動している', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 12), 'ja', '타일을 배열해서 문장을 완성하세요.', '진심으로 사과하고 싶다고 그가 말했다', '["彼は","本当に","謝りたいと","心から","言った"]', '["心から","謝りたいと","彼は","言った"]', '["本当に"]', NULL, '心から謝りたいと彼は言った', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 13), 'ja', '직장에서 실수를 했을 때 \'반성하다\'는 표현으로 알맞은 것은?', NULL, NULL, NULL, NULL, '["謝罪する","後悔する","検討する","反省する"]', '反省する', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 14), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

彼女は______して、自分の意見をはっきり言った。
(그녀는 결심하고 자신의 의견을 분명히 말했다.)', NULL, NULL, NULL, NULL, NULL, '覚悟', '["覚悟","かくご"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 15), 'ja', '빈칸에 알맞은 일본어를 쓰세요.

\'문제가 드디어 해결됐다\' = 問題がやっと______', NULL, NULL, NULL, NULL, NULL, '解決した', '["解決した","かいけつした"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 16), 'ja', '어떤 일이 \'해결되다\'라는 표현으로 알맞은 것은?', NULL, NULL, NULL, NULL, '["終わらせる","完成する","片付く","解決する"]', '解決する', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 17), 'ja', '타일을 배열해서 문장을 완성하세요.', '교통사고를 주의하면서 운전해', '["交通事故に","気を付けて","運転して","気になって"]', '["交通事故に","気を付けて","運転して"]', '["気になって"]', NULL, '交通事故に気を付けて運転して', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 18), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

彼はいつも______な態度で仕事に取り組む。
(그는 항상 성실한 태도로 일에 임한다.)', NULL, NULL, NULL, NULL, NULL, '真剣', '["真剣","しんけん","真面目","まじめ"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 19), 'ja', '일부러, 고의로 한 행동에 대해 쓸 수 있는 부사는?', NULL, NULL, NULL, NULL, '["ついに","なんとか","わざと","せっかく"]', 'わざと', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 20), 'ja', '타일을 배열해서 문장을 완성하세요.', '그녀는 일부러 틀린 척했다', '["間違えた","彼女は","わざと","ふりをした","ついに"]', '["彼女は","わざと","間違えた","ふりをした"]', '["ついに"]', NULL, '彼女はわざと間違えたふりをした', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 21), 'ja', '타일을 배열해서 문장을 완성하세요.', '잃어버린 지갑이 드디어 발견됐다', '["やっと","見つけた","見つかった","なくした財布が"]', '["なくした財布が","やっと","見つかった"]', '["見つけた"]', NULL, 'なくした財布がやっと見つかった', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 22), 'ja', '친구가 갑자기 연락을 끊어서 \'신경 쓰인다\'고 할 때 알맞은 표현은?', NULL, NULL, NULL, NULL, '["気になる","気を取る","気が付く","気にする"]', '気になる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 23), 'ja', '빈칸에 알맞은 일본어를 쓰세요.

\'깊게 숨을 들이쉬고 마음을 진정시켜라\' = 深呼吸して心を______', NULL, NULL, NULL, NULL, NULL, '落ち着かせろ', '["落ち着かせろ","落ち着かせて","おちつかせろ","おちつかせて"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 24), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 그 선생님을 진심으로 존경한다', '["私は","心から","尊敬している","信頼している","その先生を"]', '["私は","その先生を","心から","尊敬している"]', '["信頼している"]', NULL, '私はその先生を心から尊敬している', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 25), 'ja', '다음 뜻에 해당하는 일본어 단어를 쓰세요.

\'어떤 일의 결과가 원인이 되어 좋지 않은 상황이 계속되는 것\' = 悪い______', NULL, NULL, NULL, NULL, NULL, '悪循環', '["悪循環","あくじゅんかん","循環","じゅんかん"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 26), 'ja', '빈칸에 알맞은 일본어 표현을 쓰세요.

彼は失敗しても______を失わなかった。
(그는 실패해도 자신감을 잃지 않았다.)', NULL, NULL, NULL, NULL, NULL, '自信', '["自信","じしん"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 27), 'ja', '다음 중 \'진심으로, 충심으로\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["気持ちで","心から","正直に","本当に"]', '心から', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 28), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 문제는 아직 해결되지 않았다', '["解決して","いない","その問題は","いなかった","まだ"]', '["その問題は","まだ","解決して","いない"]', '["いなかった"]', NULL, 'その問題はまだ解決していない', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 29), 'ja', '\'상황이 심각해지다\'는 뜻의 관용 표현은?', NULL, NULL, NULL, NULL, '["大げさになる","深刻になる","激しくなる","厳しくなる"]', '深刻になる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 30), 'ja', '\'여전히, 변함없이\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["引き続き","今でも","相変わらず","変わらずに"]', '相変わらず', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 31), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 사건은 오해에서 비롯된 것이었다', '["誤解から","生まれた","始めた","ものだった","その事件は"]', '["その事件は","誤解から","生まれた","ものだった"]', '["始めた"]', NULL, 'その事件は誤解から生まれたものだった', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 32), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 용기를 내서 그녀에게 말을 걸었다', '["彼女に","話した","私は","勇気を出して","話しかけた"]', '["私は","勇気を出して","彼女に","話しかけた"]', '["話した"]', NULL, '私は勇気を出して彼女に話しかけた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 33), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

彼女の言葉は______で、何度読んでも心に響く。
(그녀의 말은 인상적이어서 몇 번 읽어도 마음에 와닿는다.)', NULL, NULL, NULL, NULL, NULL, '印象的', '["印象的","いんしょうてき"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 34), 'ja', '타일을 배열해서 문장을 완성하세요.', '심호흡을 하자 마음이 진정됐다', '["落ち着いた","気持ちが","したら","深呼吸を","落ち込んだ"]', '["深呼吸を","したら","気持ちが","落ち着いた"]', '["落ち込んだ"]', NULL, '深呼吸をしたら気持ちが落ち着いた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 35), 'ja', '빈칸에 알맞은 일본어 표현을 쓰세요.

仕事が多すぎて、気が______。
(일이 너무 많아서 정신이 없다 / 마음이 조급하다.)', NULL, NULL, NULL, NULL, NULL, '気が焦る', '["気が焦る","きがあせる","焦る","あせる"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 36), 'ja', '누군가와의 관계에서 \'믿다, 신뢰하다\'의 뜻으로 쓰는 단어는?', NULL, NULL, NULL, NULL, '["尊敬する","依存する","信頼する","期待する"]', '信頼する', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 37), 'ja', '\'마음이 안정되다, 진정되다\'를 뜻하는 복합동사는?', NULL, NULL, NULL, NULL, '["落ち込む","落とす","落ち合う","落ち着く"]', '落ち着く', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 38), 'ja', '타일을 배열해서 문장을 완성하세요.', '실수를 하고 나서 많이 반성했다', '["してから","ミスを","後悔した","たくさん","反省した"]', '["ミスを","してから","たくさん","反省した"]', '["後悔した"]', NULL, 'ミスをしてからたくさん反省した', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 39), 'ja', '\'용기를 내다\'는 관용 표현은?', NULL, NULL, NULL, NULL, '["勇気をつける","勇気がある","勇気を出す","勇気を持つ"]', '勇気を出す', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 40), 'ja', '빈칸에 알맞은 일본어를 쓰세요.

\'나는 그를 진심으로 믿는다\' = 私は彼を______している', NULL, NULL, NULL, NULL, NULL, '信頼', '["信頼","しんらい","信頼する","しんらいする"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 41), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 음식은 예상 외로 맛있었다', '["意外と","その料理は","おいしかった","案外"]', '["その料理は","案外","おいしかった"]', '["意外と"]', NULL, 'その料理は案外おいしかった', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 42), 'ja', '빈칸에 알맞은 일본어를 쓰세요.

\'그는 항상 주의를 기울인다\' = 彼はいつも______している', NULL, NULL, NULL, NULL, NULL, '気を付ける', '["気を付ける","きをつける","気をつける"]');

-- content: competition_quiz_ja_01.json level 3 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 1), 'ja', '타일을 배열해서 문장을 완성하세요.', '갑자기 해고 통보를 받아 어찌할 바를 몰라 막막했다', '["突然","解雇を","喜んだ","告げられて","安心した","途方に暮れた"]', '["突然","解雇を","告げられて","途方に暮れた"]', '["安心した","喜んだ"]', NULL, '突然解雇を告げられて途方に暮れた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 2), 'ja', '다음 중 \'표면상·명목상으로는\'이라는 뜻의 표현은?', NULL, NULL, NULL, NULL, '["建前上は","形式上は","名目上は","表向きは"]', '名目上は', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 3), 'ja', '빈칸에 알맞은 속담을 쓰세요.

「______より産むが易し」
(걱정하는 것보다 실제로 해보면 쉽다.)', NULL, NULL, NULL, NULL, NULL, '案ずる', '["案ずる","あんずる"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 4), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 직책을 맡으려면 상당한 자질이 필요하다', '["必要だ","やる気があれば","学歴だけが","就くには","相当な","資質が","その役職に"]', '["その役職に","就くには","相当な","資質が","必要だ"]', '["学歴だけが","やる気があれば"]', NULL, 'その役職に就くには相当な資質が必要だ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 5), 'ja', '다음 중 \'아쉽다, 헤어지기 섭섭하다\'라는 뜻의 단어는?', NULL, NULL, NULL, NULL, '["儚い","名残惜しい","切ない","虚しい"]', '名残惜しい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 6), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 영화의 결말은 너무 가슴이 아파서 한동안 멍하니 있었다', '["楽しくて","その映画の","しばらく","いた","ぼんやりして","すぐに忘れた","あまりにも","結末が","切なくて"]', '["その映画の","結末が","あまりにも","切なくて","しばらく","ぼんやりして","いた"]', '["楽しくて","すぐに忘れた"]', NULL, 'その映画の結末があまりにも切なくてしばらくぼんやりしていた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 7), 'ja', '다음 중 \'가슴이 아프다, 속이 쓰리다\'는 감정의 형용사는?', NULL, NULL, NULL, NULL, '["辛い","寂しい","苦しい","切ない"]', '切ない', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 8), 'ja', '어떻게 해야 할지 몰라 완전히 막막한 상태를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["途中で終わる","先が見える","途方に暮れる","手をこまねく"]', '途方に暮れる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 9), 'ja', '빈칸에 알맞은 일본어 표현을 쓰세요.

大事な発表を前に、彼は______して眠れなかった。
(긴장이나 불안으로 \'마음이 조마조마한\' 상태를 나타내는 의태어)', NULL, NULL, NULL, NULL, NULL, 'はらはら', '["はらはら"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 10), 'ja', '다음 중 \'행동이나 태도가 어색하고 자연스럽지 않은 모양\'을 나타내는 의태어는?', NULL, NULL, NULL, NULL, '["ぎすぎす","ぎっしり","ぎりぎり","ぎこちない"]', 'ぎこちない', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 11), 'ja', '타일을 배열해서 문장을 완성하세요.', '그의 설명은 아무래도 납득이 가지 않아서 다시 물어보기로 했다', '["彼の説明は","理解できたから","聞き直すことに","納得できて","腑に落ちなくて","どうも","もう一度","した"]', '["彼の説明は","どうも","腑に落ちなくて","もう一度","聞き直すことに","した"]', '["納得できて","理解できたから"]', NULL, '彼の説明はどうも腑に落ちなくてもう一度聞き直すことにした', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 12), 'ja', '타일을 배열해서 문장을 완성하세요.', '그녀의 연설은 많은 사람들의 마음을 울렸다', '["誰にも","多くの人の","心を傷つけた","胸を打った","彼女の","スピーチは"]', '["彼女の","スピーチは","多くの人の","胸を打った"]', '["心を傷つけた","誰にも"]', NULL, '彼女のスピーチは多くの人の胸を打った', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 13), 'ja', '다음 중 \'마음을 울리다, 감동을 주다\'라는 뜻의 표현은?', NULL, NULL, NULL, NULL, '["胸が痛む","胸を張る","胸を打つ","胸をなでおろす"]', '胸を打つ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 14), 'ja', '빈칸에 알맞은 일본어 표현을 쓰세요.

그는 실패할 것을 알면서도 뻔뻔하게 도움을 요청했다. 정말 ______ 人だと思った。
(\'뻔뻔스러운, 염치없는\'을 뜻하는 な형용사)', NULL, NULL, NULL, NULL, NULL, '厚かましい', '["厚かましい","あつかましい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 15), 'ja', '다음 속담의 빈칸을 채우세요.

\'七転び______\'
(넘어져도 포기하지 않고 일어선다는 뜻의 속담)', NULL, NULL, NULL, NULL, NULL, '八起き', '["八起き","やおき","八起","やつおき"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 16), 'ja', '타일을 배열해서 문장을 완성하세요.', '졸업식 날, 친구들과 헤어지는 것이 너무 아쉬워서 눈물이 났다', '["うれしくて","名残惜しくて","涙が出た","卒業式の日","友達と","早く帰りたくて","別れるのが","あまりに"]', '["卒業式の日","友達と","別れるのが","あまりに","名残惜しくて","涙が出た"]', '["うれしくて","早く帰りたくて"]', NULL, '卒業式の日友達と別れるのがあまりに名残惜しくて涙が出た', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 17), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

彼女の言葉には、どこか______があって信用できなかった。
(\'앞뒤가 맞지 않음, 모순\'을 뜻하는 명사)', NULL, NULL, NULL, NULL, NULL, '矛盾', '["矛盾","むじゅん"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 18), 'ja', '다음 중 \'타고난 소질, 자질\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["気質","性質","資質","素質"]', '資質', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 19), 'ja', '타일을 배열해서 문장을 완성하세요.', '오래된 앨범을 보면서 학창 시절을 감회 깊게 떠올렸다', '["いらいらしながら","しみじみと","思い出した","見ながら","学生時代を","忘れようとした","古いアルバムを"]', '["古いアルバムを","見ながら","学生時代を","しみじみと","思い出した"]', '["いらいらしながら","忘れようとした"]', NULL, '古いアルバムを見ながら学生時代をしみじみと思い出した', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 20), 'ja', '다음 중 \'감회 깊게, 마음 속 깊이 느끼는 모양\'을 나타내는 의태어는?', NULL, NULL, NULL, NULL, '["しんなり","しみじみ","しくしく","しんしん"]', 'しみじみ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 21), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

彼は会議で反論されても、自分の意見を______として変えなかった。
(\'완고하게, 끝까지 굽히지 않고\'를 뜻하는 부사)', NULL, NULL, NULL, NULL, NULL, '頑として', '["頑として","がんとして"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 22), 'ja', '빈칸에 알맞은 말을 쓰세요.

その映画のラストシーンは本当に______。思わず涙が出た。
(그 영화의 마지막 장면은 정말로 ______. 나도 모르게 눈물이 났다.)

※ \'마음을 울렸다\'는 관용표현', NULL, NULL, NULL, NULL, NULL, '胸を打った', '["胸を打った","むねをうった"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 23), 'ja', '타일을 배열해서 문장을 완성하세요.', '가슴 두근거리면서 합격 발표를 기다렸다', '["ドキドキしながら","合格発表を","結果を忘れて","待った","ぼんやりしながら"]', '["ドキドキしながら","合格発表を","待った"]', '["ぼんやりしながら","結果を忘れて"]', NULL, 'ドキドキしながら合格発表を待った', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 24), 'ja', '\'걱정보다 실제로 해보면 쉽다\'는 의미의 속담은?', NULL, NULL, NULL, NULL, '["善は急げ","七転び八起き","石の上にも三年","案ずるより産むが易し"]', '案ずるより産むが易し', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 25), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

長年の努力がようやく______を結んだ。
(오랜 노력이 드디어 \'결실을 맺었다\'는 뜻 — ______に当たる単語)', NULL, NULL, NULL, NULL, NULL, '実', '["実","み"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 26), 'ja', '다음 중 \'납득이 가지 않다, 이해가 안 되다\'라는 뜻의 관용표현은?', NULL, NULL, NULL, NULL, '["目に余る","気に食わない","手に余る","腑に落ちない"]', '腑に落ちない', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 27), 'ja', '다음 뜻에 해당하는 일본어 표현을 쓰세요.

\'두루뭉술하게 넘어가다, 얼버무리다\'
예: 彼は都合の悪い質問を______にした。', NULL, NULL, NULL, NULL, NULL, 'あいまい', '["曖昧","あいまい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 28), 'ja', '다음 중 \'가슴 두근두근 긴장되거나 설레는 모양\'을 나타내는 의성어·의태어는?', NULL, NULL, NULL, NULL, '["ソワソワ","ワクワク","ドキドキ","ハラハラ"]', 'ドキドキ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 29), 'ja', '타일을 배열해서 문장을 완성하세요.', '처음 무대에 선 그녀의 움직임은 어색했지만 열정은 전해졌다', '["観客を失望させた","伝わった","初めて","彼女の動きは","ぎこちなかったが","完璧で","舞台に立った","熱意は"]', '["初めて","舞台に立った","彼女の動きは","ぎこちなかったが","熱意は","伝わった"]', '["完璧で","観客を失望させた"]', NULL, '初めて舞台に立った彼女の動きはぎこちなかったが熱意は伝わった', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 30), 'ja', '타일을 배열해서 문장을 완성하세요.', '열심히 준비했는데도 결과가 나오지 않아 허무함을 느꼈다', '["満足して","虚しさを","準備したのに","感じた","すぐに立ち直って","一生懸命","結果が出ず"]', '["一生懸命","準備したのに","結果が出ず","虚しさを","感じた"]', '["満足して","すぐに立ち直って"]', NULL, '一生懸命準備したのに結果が出ず虚しさを感じた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 31), 'ja', '다음 중 \'덧없다, 허무하다\'라는 뜻의 단어는?', NULL, NULL, NULL, NULL, '["儚い","悔しい","疎ましい","恨めしい"]', '儚い', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 32), 'ja', '타일을 배열해서 문장을 완성하세요.', '아무리 노력해도 이루어지지 않는 덧없는 꿈도 있다', '["どれだけ","努力しても","ある","現実的な","すぐに叶う","儚い夢も","叶わない"]', '["どれだけ","努力しても","叶わない","儚い夢も","ある"]', '["すぐに叶う","現実的な"]', NULL, 'どれだけ努力しても叶わない儚い夢もある', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 33), 'ja', '빈칸에 알맞은 말을 쓰세요.

どうすればいいかわからず、______いる。
(어떻게 해야 할지 몰라 막막해하고 있다.)', NULL, NULL, NULL, NULL, NULL, '途方に暮れて', '["途方に暮れて","とほうにくれて"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 34), 'ja', '빈칸에 알맞은 말을 쓰세요.

別れ際、彼女は______そうな顔をした。
(이별할 때, 그녀는 아쉬운 듯한 표정을 지었다.)

※ 형용사의 어간(い 제외)을 쓰세요.', NULL, NULL, NULL, NULL, NULL, '名残惜し', '["名残惜し","なごりおし","名残惜しい","なごりおしい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 35), 'ja', '다음 중 \'허무하다, 공허하다\'라는 뜻의 형용사는?', NULL, NULL, NULL, NULL, '["悲しい","惜しい","虚しい","恥ずかしい"]', '虚しい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 36), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 회사는 명목상으로는 NPO지만 실제로는 영리 목적이다', '["慈善活動をしている","営利目的だ","実際には","名目上は","本当に","その会社は","NPOだが"]', '["その会社は","名目上は","NPOだが","実際には","営利目的だ"]', '["本当に","慈善活動をしている"]', NULL, 'その会社は名目上はNPOだが実際には営利目的だ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 37), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

締め切りが近いのに、彼女はまだ______している。
(빈칸 = \'우물쭈물하다, 질질 끌다\'를 뜻하는 의태어)', NULL, NULL, NULL, NULL, NULL, 'のろのろ', '["のろのろ"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 38), 'ja', '타일을 배열해서 문장을 완성하세요.', '꾸물거리지 말고 지금 당장 결정해 주세요', '["今すぐ","ください","ゆっくりと","決めて","後で考えて","ぐずぐずせず"]', '["ぐずぐずせず","今すぐ","決めて","ください"]', '["ゆっくりと","後で考えて"]', NULL, 'ぐずぐずせず今すぐ決めてください', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 39), 'ja', '다음 중 \'어물어물, 꾸물꾸물 결정을 못 내리는 모양\'을 나타내는 의태어는?', NULL, NULL, NULL, NULL, '["ぼんやり","うろうろ","もたもた","ぐずぐず"]', 'ぐずぐず', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 40), 'ja', '빈칸에 알맞은 말을 쓰세요.

何時間も努力したのに成果が出なくて、______感じた。
(몇 시간이나 노력했는데 성과가 나오지 않아서 허무함을 느꼈다.)

※ \'허무한\'에 해당하는 い형용사', NULL, NULL, NULL, NULL, NULL, '虚しさを', '["虚しさを","むなしさを","虚しい","むなしい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 41), 'ja', '타일을 배열해서 문장을 완성하세요.', '걱정보다 해보면 쉽다고 했으니, 일단 시작해 보자', '["やってみよう","案ずるより","あきらめよう","産むが易しと","まず","難しいと","言うから"]', '["案ずるより","産むが易しと","言うから","まず","やってみよう"]', '["あきらめよう","難しいと"]', NULL, '案ずるより産むが易しと言うからまずやってみよう', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 42), 'ja', '빈칸에 알맞은 말을 쓰세요.

あの失恋は本当に______。今でも胸が痛い。
(그 실연은 정말 가슴 아팠어. 지금도 가슴이 아파.)

※ \'가슴이 아프게 슬프다\'는 い형용사, 과거형', NULL, NULL, NULL, NULL, NULL, '切なかった', '["切なかった","せつなかった"]');

-- content: competition_quiz_zh_01.json level 1 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 1), 'zh', '다음 중 \'어색하다, 난처하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["尴尬","别扭","陌生","奇怪"]', '尴尬', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 2), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'부끄럽다, 창피하다\' = ______', NULL, NULL, NULL, NULL, NULL, '丢脸', '["丢脸","diulian"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 3), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'후회하다\' = ______', NULL, NULL, NULL, NULL, NULL, '后悔', '["后悔","houhui"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 4), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 숙제를 잊어버렸다', '["忘记","作业了","我"]', '["我","忘记","作业了"]', NULL, NULL, '我忘记作业了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 5), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'피곤하다, 지치다\' = ______', NULL, NULL, NULL, NULL, NULL, '累', '["累","lei"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 6), 'zh', '다음 중 \'하마터면 ~할 뻔했다\'를 뜻하는 부사는?', NULL, NULL, NULL, NULL, '["几乎","差不多","将要","差点儿"]', '差点儿', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 7), 'zh', '다음 중 \'드디어, 마침내\'를 뜻하는 부사는?', NULL, NULL, NULL, NULL, '["已经","终于","终究","最终"]', '终于', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 8), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'비교하다\' = ______', NULL, NULL, NULL, NULL, NULL, '比较', '["比较","bijiao"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 9), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'결정하다, 결심하다\' = ______', NULL, NULL, NULL, NULL, NULL, '决定', '["决定","jueding"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 10), 'zh', '다음 중 \'~하는 척하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["假装","假如","装作","伪装"]', '假装', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 11), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'허락하다, 동의하다\' = ______', NULL, NULL, NULL, NULL, NULL, '同意', '["同意","tongyi"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 12), 'zh', '다음 중 \'심심하다, 지루하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["烦躁","没意思","无聊","疲倦"]', '无聊', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 13), 'zh', '다음 중 \'피하다, 회피하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["逃跑","拒绝","避免","躲避"]', '躲避', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 14), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'이해하다, 알다\' = ______', NULL, NULL, NULL, NULL, NULL, '明白', '["明白","mingbai"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 15), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 자는 척했다', '["睡觉","假装","她"]', '["她","假装","睡觉"]', NULL, NULL, '她假装睡觉', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 16), 'zh', '타일을 배열해서 문장을 완성하세요.', '오늘은 정말 심심하다', '["无聊","今天","真"]', '["今天","真","无聊"]', NULL, NULL, '今天真无聊', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 17), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'감동받다, 감동하다\' = ______', NULL, NULL, NULL, NULL, NULL, '感动', '["感动","gandong"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 18), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 갑자기 울었다', '["我","哭了","突然"]', '["我","突然","哭了"]', NULL, NULL, '我突然哭了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 19), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'걱정하다, 염려하다\' = ______', NULL, NULL, NULL, NULL, NULL, '担心', '["担心","danxin"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 20), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그를 잘 안다', '["我","很","他","熟悉"]', '["我","很","熟悉","他"]', NULL, NULL, '我很熟悉他', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 21), 'zh', '다음 중 \'갑자기\'를 뜻하는 부사는?', NULL, NULL, NULL, NULL, '["突然","马上","忽然","立刻"]', '突然', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 22), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 드디어 집에 왔다', '["回家了","他","终于"]', '["他","终于","回家了"]', NULL, NULL, '他终于回家了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 23), 'zh', '다음 중 \'이상하다, 특이하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["奇怪","怪异","奇妙","特别"]', '奇怪', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 24), 'zh', '다음 중 \'부끄럽다, 수줍다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["害怕","惭愧","不好意思","害羞"]', '害羞', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 25), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 친구를 피했다', '["朋友","她","躲避"]', '["她","躲避","朋友"]', NULL, NULL, '她躲避朋友', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 26), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 발표가 긴장된다', '["有点","紧张","演讲","我"]', '["我","演讲","有点","紧张"]', NULL, NULL, '我演讲有点紧张', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 27), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'흥미롭다, 재미있다\' = ______', NULL, NULL, NULL, NULL, NULL, '有意思', '["有意思","youyisi"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 28), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'자신 있다, 확신하다\' = ______', NULL, NULL, NULL, NULL, NULL, '自信', '["自信","zixin"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 29), 'zh', '타일을 배열해서 문장을 완성하세요.', '이 문제는 정말 이상하다', '["奇怪","问题","真","这个"]', '["这个","问题","真","奇怪"]', NULL, NULL, '这个问题真奇怪', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 30), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 매우 초조해한다', '["着急","他","非常"]', '["他","非常","着急"]', NULL, NULL, '他非常着急', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 31), 'zh', '다음 중 \'익숙하다, 잘 알다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["熟悉","明白","了解","清楚"]', '熟悉', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 32), 'zh', '다음 중 \'긴장하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["害怕","担心","着急","紧张"]', '紧张', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 33), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 시험이 긴장된다', '["我","紧张","很","考试"]', '["我","考试","很","紧张"]', NULL, NULL, '我考试很紧张', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 34), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'만족하다\' = ______', NULL, NULL, NULL, NULL, NULL, '满意', '["满意","manyi"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 35), 'zh', '다음 중 \'잊다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["遗忘","记得","忽视","忘记"]', '忘记', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 36), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'긴장을 풀다, 느긋하다\' = ______', NULL, NULL, NULL, NULL, NULL, '放松', '["放松","fangsong"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 37), 'zh', '다음 중 \'초조해하다, 서두르다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["紧张","着急","烦恼","担心"]', '着急', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 38), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 매우 슬프다', '["很","我","难过"]', '["我","很","难过"]', NULL, NULL, '我很难过', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 39), 'zh', '다음 중 \'슬프다, 마음이 아프다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["难受","痛苦","难过","伤心"]', '难过', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 40), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'화나다, 화가 나다\' = ______', NULL, NULL, NULL, NULL, NULL, '生气', '["生气","shengqi"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 41), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 하마터면 늦을 뻔했다', '["差点儿","我","迟到了"]', '["我","差点儿","迟到了"]', NULL, NULL, '我差点儿迟到了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 1 AND origin_id = 42), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 매우 부끄러워했다', '["害羞","很","他"]', '["他","很","害羞"]', NULL, NULL, '他很害羞', NULL);

-- content: competition_quiz_zh_01.json level 2 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 1), 'zh', '\'그 말을 듣고 정말 ______했어.\'
빈칸에 들어갈 \'놀라다\'의 표현은?', NULL, NULL, NULL, NULL, '["吃惊","震惊","吃苦","惊喜"]', '吃惊', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 2), 'zh', '다음 중 \'사람들과 교류하다\'를 뜻하는 고정 표현은?', NULL, NULL, NULL, NULL, '["打招呼","打交道","打交流","打电话"]', '打交道', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 3), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'서로 양보하다, 타협하다\'를 두 글자로 쓰세요. = ______', NULL, NULL, NULL, NULL, NULL, '妥协', '["妥协","tuoxie"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 4), 'zh', '\'그는 발표 전에 항상 ______해.\'
빈칸에 들어갈 \'긴장하다\'의 표현은?', NULL, NULL, NULL, NULL, '["紧张","担心","着急","害怕"]', '紧张', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 5), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 낯선 사람들과 교류하는 게 좀 어려워.', '["跟陌生人","有点","交流","打交道","难","我"]', '["我","跟陌生人","打交道","有点","难"]', '["交流"]', NULL, '我跟陌生人打交道有点难', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 6), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 일부러 그의 전화를 받지 않았어.', '["忘记","电话","我","接","他的","没有","故意"]', '["我","故意","没有","接","他的","电话"]', '["忘记"]', NULL, '我故意没有接他的电话', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 7), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'일부러, 고의로\'의 반대말 — \'우연히, 뜻밖에\' = ______', NULL, NULL, NULL, NULL, NULL, '偶然', '["偶然","ouran"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 8), 'zh', '타일을 배열해서 문장을 완성하세요.', '너 나한테 농담하는 거야?', '["开玩笑","跟我","你","吗","在","说谎"]', '["你","在","跟我","开玩笑","吗"]', '["说谎"]', NULL, '你在跟我开玩笑吗', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 9), 'zh', '타일을 배열해서 문장을 완성하세요.', '그 소식을 듣고 우리 모두 깜짝 놀랐어.', '["我们","害怕","都","听到","吃惊了","那个消息"]', '["我们","听到","那个消息","都","吃惊了"]', '["害怕"]', NULL, '我们听到那个消息都吃惊了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 10), 'zh', '다음 중 \'농담하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["开玩笑","开心情","玩笑话","说笑话"]', '开玩笑', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 11), 'zh', '다음 중 \'용기를 내다\'를 뜻하는 고정 표현은?', NULL, NULL, NULL, NULL, '["鼓足干劲","鼓起勇气","增加勇气","鼓励勇气"]', '鼓起勇气', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 12), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'(약속·마감 등을) 어기다, 지키지 못하다\' = ______约定', NULL, NULL, NULL, NULL, NULL, '违反', '["违反","weifan"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 13), 'zh', '타일을 배열해서 문장을 완성하세요.', '생각지도 못하게 그 자리에서 그를 만났어.', '["了","遇到","没想到","偶然","在那里","他"]', '["没想到","在那里","遇到","了","他"]', '["偶然"]', NULL, '没想到在那里遇到了他', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 14), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'참을 수가 없다, 견딜 수 없다\' = ______不了', NULL, NULL, NULL, NULL, NULL, '忍受', '["忍受","renshou"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 15), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 마침내 용기를 내서 그녀에게 고백했어.', '["犹豫","终于","他","表白了","鼓起勇气","向她"]', '["他","终于","鼓起勇气","向她","表白了"]', '["犹豫"]', NULL, '他终于鼓起勇气向她表白了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 16), 'zh', '빈칸에 알맞은 중국어 표현을 쓰세요.

\'용기를 내다\' = ______勇气', NULL, NULL, NULL, NULL, NULL, '鼓起', '["鼓起","guqi","gǔqǐ"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 17), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

그는 항상 다른 사람의 감정을 ______해. (배려하다, 고려하다)

他总是会______别人的感受。', NULL, NULL, NULL, NULL, NULL, '考虑', '["考虑","kaolv"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 18), 'zh', '다음 중 \'부러워하다, 선망하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["佩服","嫉妒","羡慕","崇拜"]', '羡慕', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 19), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

그 회의에서 그는 자기 의견을 ______하게 표현했다.

在那次会议上，他______地表达了自己的意见。', NULL, NULL, NULL, NULL, NULL, '大胆', '["大胆","dadan"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 20), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

이 일은 생각보다 훨씬 ______해.

这件事比想象的______多了。', NULL, NULL, NULL, NULL, NULL, '麻烦', '["麻烦","mafan"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 21), 'zh', '다음 중 \'일부러, 고의로\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["故意","故事","特意","刻意"]', '故意', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 22), 'zh', '타일을 배열해서 문장을 완성하세요.', '어린 아이들은 보통 호기심이 매우 강해.', '["通常","很强","勇气","好奇心","小孩子"]', '["小孩子","通常","好奇心","很强"]', '["勇气"]', NULL, '小孩子通常好奇心很强', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 23), 'zh', '\'시간이 ______해서 택시를 탔어.\'
빈칸에 알맞은 표현은?', NULL, NULL, NULL, NULL, '["没时间","赶不上","来不及","来得及"]', '来不及', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 24), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그 앞에서 민망해서 아무 말도 못 했어.', '["在他面前","后悔","说不出话","我","不好意思"]', '["我","在他面前","不好意思","说不出话"]', '["后悔"]', NULL, '我在他面前不好意思说不出话', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 25), 'zh', '다음 중 \'오해하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["误会","理解","误导","了解"]', '误会', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 26), 'zh', '타일을 배열해서 문장을 완성하세요.', '그들 사이에 오해가 생겼어.', '["产生了","误会","他们","矛盾","之间"]', '["他们","之间","产生了","误会"]', '["矛盾"]', NULL, '他们之间产生了误会', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 27), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

그는 실패를 두려워해서 항상 ______하려 한다.

他怕失败，总想着______。', NULL, NULL, NULL, NULL, NULL, '保守', '["保守","baoshou"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 28), 'zh', '다음 중 \'호기심\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["好奇妙","好奇怪","好奇心","好感心"]', '好奇心', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 29), 'zh', '다음 중 \'갑자기, 뜻밖에\'를 뜻하는 표현에 가장 가까운 것은?', NULL, NULL, NULL, NULL, '["没注意","没关系","没想到","没办法"]', '没想到', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 30), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그의 솔직한 태도가 마음에 들어.', '["我","他的","坦率","态度","喜欢","直接"]', '["我","喜欢","他的","坦率","态度"]', '["直接"]', NULL, '我喜欢他的坦率态度', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 31), 'zh', '타일을 배열해서 문장을 완성하세요.', '시간이 촉박해서 지하철을 탈 수가 없어.', '["坐","地铁","了","赶上","来不及"]', '["来不及","坐","地铁","了"]', '["赶上"]', NULL, '来不及坐地铁了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 32), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그녀의 생활 방식이 정말 부러워.', '["她的","嫉妒","很羡慕","我","生活方式","真的"]', '["我","真的","很羡慕","她的","生活方式"]', '["嫉妒"]', NULL, '我真的很羡慕她的生活方式', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 33), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'(상대방이) 오해할까 봐 나는 아무 말도 하지 않았다\'에서 \'오해하다\' = ______', NULL, NULL, NULL, NULL, NULL, '曲解', '["曲解","qujie"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 34), 'zh', '타일을 배열해서 문장을 완성하세요.', '너 발표할 때 긴장하지 않아?', '["不紧张","害羞","你","做报告","的时候","吗"]', '["你","做报告","的时候","不紧张","吗"]', '["害羞"]', NULL, '你做报告的时候不紧张吗', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 35), 'zh', '다음 중 \'솔직하다, 담백하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["直接","诚实","坦率","坦然"]', '坦率', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 36), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'인상을 남기다\'에서 \'인상\' = ______

그는 면접에서 좋은 ______을 남겼다.', NULL, NULL, NULL, NULL, NULL, '印象', '["印象","yinxiang"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 37), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

그녀는 칭찬을 들으면 쉽게 ______한다.

她一听到夸奖就容易______。', NULL, NULL, NULL, NULL, NULL, '害羞', '["害羞","haixiu"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 38), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 오래 망설이다가 결국 거절했어.', '["最终","考虑","很久","拒绝了","她","犹豫了"]', '["她","犹豫了","很久","最终","拒绝了"]', '["考虑"]', NULL, '她犹豫了很久最终拒绝了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 39), 'zh', '다음 중 \'부끄럽다, 미안하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["不方便","不好意思","不好意义","不舒服"]', '不好意思', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 40), 'zh', '다음 중 \'주저하다, 망설이다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["考虑","犹豫","迟到","犹如"]', '犹豫', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 41), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

그 소문은 사실이 아닐 수도 있어. 너무 ______하지 마.

那个传言不一定是真的，不要太______。', NULL, NULL, NULL, NULL, NULL, '轻信', '["轻信","qingxin"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 2 AND origin_id = 42), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'사실대로 말하다, 솔직히 말하다\' = 实话______', NULL, NULL, NULL, NULL, NULL, '实说', '["实说","shishuo"]');

-- content: competition_quiz_zh_01.json level 3 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 1), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

그는 매우 위험한 상황에서 기적적으로 살아남았다.
→ 他在极度危险的情况下______，真是奇迹。', NULL, NULL, NULL, NULL, NULL, '死里逃生', '["死里逃生","silituosheng"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 2), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

그 프로젝트는 처음엔 잘 됐지만 결국 흐지부지됐다.
→ 那个项目起初进展顺利，最终却______。', NULL, NULL, NULL, NULL, NULL, '有始无终', '["有始无终","youshiwuzhong"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 3), 'zh', '다음 중 \'겉으로만 그럴듯하고 속은 없다\'는 뜻의 성어는?', NULL, NULL, NULL, NULL, '["华而不实","表里不一","金玉其外","名不副实"]', '华而不实', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 4), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'감격하다, 감동받다\' = ______', NULL, NULL, NULL, NULL, NULL, '感激', '["感激","ganji"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 5), 'zh', '다음 중 \'경험이 많고 세상을 꿰뚫어 보다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["老谋深算","久经沙场","饱经风霜","见多识广"]', '饱经风霜', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 6), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

회사에서 갑자기 해고당한 그는 매우 ______했다.
→ 突然被公司解雇的他非常______。

\'황당하다, 어이없다\' = ______', NULL, NULL, NULL, NULL, NULL, '荒唐', '["荒唐","huangtang"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 7), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'억지로 참다, 꾹 참다\' = ______', NULL, NULL, NULL, NULL, NULL, '忍耐', '["忍耐","rennai"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 8), 'zh', '다음 중 \'재난이 오히려 복이 되다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["逢凶化吉","塞翁失马","否极泰来","因祸得福"]', '因祸得福', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 9), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 억울한 마음을 친구에게 털어놓았다', '["委屈","她","秘密","把","向","隐瞒","朋友","倾诉了"]', '["她","把","委屈","向","朋友","倾诉了"]', '["隐瞒","秘密"]', NULL, '她把委屈向朋友倾诉了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 10), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 친구 관계에서 항상 어색하고 불편한 느낌을 받는다', '["觉得","他","自在","别扭","朋友关系","很","中","轻松","总是","在"]', '["他","在","朋友关系","中","总是","觉得","很","别扭"]', '["自在","轻松"]', NULL, '他在朋友关系中总是觉得很别扭', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 11), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'(마음이) 흔들리다, 동요하다\' = ______', NULL, NULL, NULL, NULL, NULL, '动摇', '["动摇","dongyao"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 12), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

그녀는 오랫동안 준비해온 일이 무산되자 매우 ______했다.
→ 准备了很久的事情落空了，她感到非常______。

\'낙담하다, 실망하다 (심하게)\' = ______', NULL, NULL, NULL, NULL, NULL, '灰心', '["灰心","huixin"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 13), 'zh', '다음 중 \'상황이 급박하게 돌아가다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["迫切","紧迫","急促","紧张"]', '紧迫', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 14), 'zh', '타일을 배열해서 문장을 완성하세요.', '지금 시간이 매우 촉박하니까 빨리 결정해야 해', '["现在","做出","非常","决定","必须","放松","时间","紧迫","充裕","尽快"]', '["现在","时间","非常","紧迫","必须","尽快","做出","决定"]', '["充裕","放松"]', NULL, '现在时间非常紧迫必须尽快做出决定', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 15), 'zh', '다음 중 \'마음이 편치 않다, 꺼림칙하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["懊恼","惭愧","羞涩","别扭"]', '别扭', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 16), 'zh', '타일을 배열해서 문장을 완성하세요.', '사고 덕분에 그는 오히려 더 좋은 기회를 얻었다', '["错过","因为","机会","这次","他","获得了","反而","失去","更好的","事故"]', '["因为","这次","事故","他","反而","获得了","更好的","机会"]', '["失去","错过"]', NULL, '因为这次事故他反而获得了更好的机会', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 17), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 자기 성과를 매일 자화자찬하며 떠벌리고 다닌다', '["谦虚","自吹自擂","低调","自己的","每天","他","炫耀","成就"]', '["他","每天","自吹自擂","炫耀","自己的","成就"]', '["谦虚","低调"]', NULL, '他每天自吹自擂炫耀自己的成就', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 18), 'zh', '다음 중 \'자화자찬하다, 스스로를 칭찬하다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["自吹自擂","沾沾自喜","自以为是","洋洋自得"]', '自吹自擂', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 19), 'zh', '타일을 배열해서 문장을 완성하세요.', '마침 우리가 그 이야기를 하고 있었는데 그 사람이 나타났다', '["事情","曹操","出现了","他","就","正说着","他的","我们","离开"]', '["我们","正说着","他的","事情","他","就","出现了"]', '["离开","曹操"]', NULL, '我们正说着他的事情他就出现了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 20), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 일을 항상 중간에 포기해서 아무것도 이루지 못했다', '["到底","做事","没有","什么都","半途而废","坚持","他","成就","总是"]', '["他","做事","总是","半途而废","什么都","没有","成就"]', '["坚持","到底"]', NULL, '他做事总是半途而废什么都没有成就', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 21), 'zh', '다음 중 \'쓸쓸하고 아쉬운 감정\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["落寞","忧郁","惆怅","悲凉"]', '惆怅', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 22), 'zh', '친구가 불합리한 대우를 받고 억울해하고 있어. 이 감정을 나타내는 단어는?', NULL, NULL, NULL, NULL, '["沮丧","委屈","烦恼","惆怅"]', '委屈', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 23), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 이별 후 마음이 한동안 쓸쓸하고 아쉬웠다', '["惆怅","心里","开心","一直","她","之后","感到","分手","释然"]', '["她","分手","之后","心里","一直","感到","惆怅"]', '["开心","释然"]', NULL, '她分手之后心里一直感到惆怅', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 24), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

그는 작은 이익에 눈이 멀어 큰 기회를 놓쳤다.
→ 他因为______，错过了更大的机会。', NULL, NULL, NULL, NULL, NULL, '因小失大', '["因小失大","yinxiaoshida"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 25), 'zh', '타일을 배열해서 문장을 완성하세요.', '선생님은 학생들의 성장을 보며 마음이 매우 흐뭇했다', '["学生们的","看到","痛心","老师","非常","感到","成长","失望","欣慰"]', '["老师","看到","学生们的","成长","感到","非常","欣慰"]', '["失望","痛心"]', NULL, '老师看到学生们的成长感到非常欣慰', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 26), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

그는 겉으로는 친한 척하지만 속으로는 딴마음을 품고 있다.
→ 他表面上装作亲近，内心却______。', NULL, NULL, NULL, NULL, NULL, '口蜜腹剑', '["口蜜腹剑","koumifujian"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 27), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'(남을) 얕보다, 업신여기다\' = ______', NULL, NULL, NULL, NULL, NULL, '轻视', '["轻视","qingshi"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 28), 'zh', '다음 중 \'마음이 뿌듯하고 흡족하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["宽慰","欣喜","满足","欣慰"]', '欣慰', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 29), 'zh', '다음 중 \'도중에 포기하다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["前功尽弃","半途而废","功亏一篑","一蹶不振"]', '半途而废', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 30), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

그는 어떤 일이든 끝까지 해내는 사람이다.
→ 他做事______，从来不放弃。', NULL, NULL, NULL, NULL, NULL, '坚持不懈', '["坚持不懈","jianchibuixie","jianchibuxie"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 31), 'zh', '다음 중 \'빨리 하고 싶어 못 기다리다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["急不可耐","归心似箭","迫不及待","心急如焚"]', '迫不及待', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 32), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 너무 빨리 보고 싶어서 결과를 기다리지 못했다', '["耐心","等待","地","结果","想要","他","迫不及待","知道"]', '["他","迫不及待","地","想要","知道","结果"]', '["耐心","等待"]', NULL, '他迫不及待地想要知道结果', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 33), 'zh', '다음 중 \'갑자기 깨달음이 오다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["茅塞顿开","恍然大悟","幡然醒悟","豁然开朗"]', '恍然大悟', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 34), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 오랜 풍파를 겪었기 때문에 세상 이치를 꿰뚫어 봤다', '["所以","天真","百态","饱经风霜","看透了","世间","幼稚","她"]', '["她","饱经风霜","所以","看透了","世间","百态"]', '["天真","幼稚"]', NULL, '她饱经风霜所以看透了世间百态', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 35), 'zh', '말하는 도중 갑자기 그 사람이 나타났을 때 쓰는 속담은?', NULL, NULL, NULL, NULL, '["说曹操，曹操到","冤家路窄","踏破铁鞋无觅处","无巧不成书"]', '说曹操，曹操到', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 36), 'zh', '다음 중 \'원칙 없이 남의 비위를 맞추다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["曲意逢迎","阿谀奉承","溜须拍马","投其所好"]', '曲意逢迎', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 37), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 상사의 비위를 맞추기 위해 자신의 원칙을 버렸다', '["原则","曲意逢迎","坚守","为了","讨好","自己的","放弃了","他","上司"]', '["他","为了","讨好","上司","放弃了","自己的","原则"]', '["曲意逢迎","坚守"]', NULL, '他为了讨好上司放弃了自己的原则', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 38), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'(태도·입장 등이) 모호하다, 얼버무리다\' = ______', NULL, NULL, NULL, NULL, NULL, '含糊', '["含糊","hanhu"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 39), 'zh', '타일을 배열해서 문장을 완성하세요.', '설명을 듣고 나서야 나는 비로소 갑자기 모든 것을 이해했다', '["茫然","不解","我","解释","之后","才","恍然大悟","听完"]', '["听完","解释","之后","我","才","恍然大悟"]', '["茫然","不解"]', NULL, '听完解释之后我才恍然大悟', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 40), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

그녀는 이번 발표를 위해 몇 달 동안 준비했다.
→ 她为这次演讲______，准备了好几个月。', NULL, NULL, NULL, NULL, NULL, '万事俱备', '["万事俱备","wanshijubei"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 41), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'(마음이) 울적하다, 답답하다\' = ______', NULL, NULL, NULL, NULL, NULL, '郁闷', '["郁闷","yumen"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '01' AND level = 3 AND origin_id = 42), 'zh', '타일을 배열해서 문장을 완성하세요.', '그 보고서는 겉만 화려하고 내용이 전혀 없었다', '["充实","那份","报告","华而不实","内容","丰富","毫无","实质"]', '["那份","报告","华而不实","毫无","实质","内容"]', '["充实","丰富"]', NULL, '那份报告华而不实毫无实质内容', NULL);

-- content: competition_quiz_en_02.json level 1 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 1), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'조용한, 고요한\' = ______', NULL, NULL, NULL, NULL, NULL, 'quiet', '["quiet","Quiet"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 2), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'친절한, 다정한\' = ______', NULL, NULL, NULL, NULL, NULL, 'kind', '["kind","Kind"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 3), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 매우 친절하다', '["very","kind","She","is"]', '["She","is","very","kind"]', NULL, NULL, 'She is very kind', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 4), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'기억하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'remember', '["remember","Remember"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 5), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 천천히 변하고 있다', '["gradually","changing","She","is"]', '["She","is","changing","gradually"]', NULL, NULL, 'She is changing gradually', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 6), 'en', '다음 중 \'실수로, 우연히\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["actually","accurately","accidentally","actively"]', 'accidentally', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 7), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 외롭다고 느꼈다', '["lonely","felt","I"]', '["I","felt","lonely"]', NULL, NULL, 'I felt lonely', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 8), 'en', '단어를 배열해서 문장을 완성하세요.', '답은 명백하다', '["is","answer","The","obvious"]', '["The","answer","is","obvious"]', NULL, NULL, 'The answer is obvious', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 9), 'en', '다음 중 \'지루한, 따분한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["brief","broad","brave","bored"]', 'bored', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 10), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 시험이 걱정된다', '["worry","about","the","exam","I"]', '["I","worry","about","the","exam"]', NULL, NULL, 'I worry about the exam', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 11), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그것을 설명할 수 없었다', '["couldn\'t","explain","I","it"]', '["I","couldn\'t","explain","it"]', NULL, NULL, 'I couldn\'t explain it', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 12), 'en', '다음 중 \'알아차리다, 인식하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["notice","narrow","neglect","nurture"]', 'notice', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 13), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'어색한, 불편한\' = ______', NULL, NULL, NULL, NULL, NULL, 'awkward', '["awkward","Awkward"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 14), 'en', '다음 중 \'~인 척하다, 가장하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["prepare","pretend","predict","prevent"]', 'pretend', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 15), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'비교하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'compare', '["compare","Compare"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 16), 'en', '다음 중 \'긴장한, 불안한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["natural","nervous","narrow","nearby"]', 'nervous', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 17), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'안도한, 다행스러운\' = ______', NULL, NULL, NULL, NULL, NULL, 'relieved', '["relieved","Relieved"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 18), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 실수로 그것을 떨어뜨렸다', '["I","dropped","accidentally","it"]', '["I","dropped","it","accidentally"]', NULL, NULL, 'I dropped it accidentally', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 19), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 매우 화가 났다', '["He","very","was","angry"]', '["He","was","very","angry"]', NULL, NULL, 'He was very angry', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 20), 'en', '다음 중 \'피하다, 회피하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["avoid","agree","admit","argue"]', 'avoid', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 21), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 매우 배고프다', '["hungry","so","I","am"]', '["I","am","so","hungry"]', NULL, NULL, 'I am so hungry', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 22), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'무서운, 두려운\' = ______', NULL, NULL, NULL, NULL, NULL, 'scared', '["scared","Scared"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 23), 'en', '다음 중 \'화난, 짜증난\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["antsy","alive","angry","awful"]', 'angry', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 24), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'배고픈\' = ______', NULL, NULL, NULL, NULL, NULL, 'hungry', '["hungry","Hungry"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 25), 'en', '다음 중 \'명백한, 분명한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["ordinary","official","optional","obvious"]', 'obvious', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 26), 'en', '단어를 배열해서 문장을 완성하세요.', '방이 매우 조용하다', '["room","is","quiet","The","very"]', '["The","room","is","very","quiet"]', NULL, NULL, 'The room is very quiet', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 27), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'행복한\' = ______', NULL, NULL, NULL, NULL, NULL, 'happy', '["happy","Happy"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 28), 'en', '다음 중 \'자랑스러운\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["proud","polite","plain","prompt"]', 'proud', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 29), 'en', '다음 중 \'주저하다, 망설이다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["hurry","hesitate","handle","highlight"]', 'hesitate', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 30), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'설명하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'explain', '["explain","Explain"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 31), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'걱정하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'worry', '["worry","Worry"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 32), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그 얼굴이 익숙하다', '["face","familiar","me","is","to","The"]', '["The","face","is","familiar","to","me"]', NULL, NULL, 'The face is familiar to me', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 33), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'당황스러운, 창피한\' = ______', NULL, NULL, NULL, NULL, NULL, 'embarrassed', '["embarrassed","Embarrassed"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 34), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 오늘 매우 피곤하다', '["I","today","very","tired","am"]', '["I","am","very","tired","today"]', NULL, NULL, 'I am very tired today', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 35), 'en', '다음 중 \'외로운\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["lively","likely","lovely","lonely"]', 'lonely', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 36), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'결정하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'decide', '["decide","Decide"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 37), 'en', '다음 중 \'졸린, 잠이 오는\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["snoopy","sleepy","slippery","sloppy"]', 'sleepy', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 38), 'en', '다음 중 \'점차, 서서히\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["greatly","gradually","generously","generally"]', 'gradually', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 39), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그녀에게 동의하기로 결정했다', '["agree","with","decided","her","to","I"]', '["I","decided","to","agree","with","her"]', NULL, NULL, 'I decided to agree with her', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 40), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그를 피하려고 했다', '["to","I","avoid","him","tried"]', '["I","tried","to","avoid","him"]', NULL, NULL, 'I tried to avoid him', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 41), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'피곤한, 지친\' = ______', NULL, NULL, NULL, NULL, NULL, 'tired', '["tired","Tired"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 42), 'en', '다음 중 \'익숙한, 친숙한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["flexible","favorite","familiar","frequent"]', 'familiar', NULL);

-- content: competition_quiz_en_02.json level 2 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 1), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'불평하다, 불만을 말하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'complain', '["complain","Complain"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 2), 'en', '다음 중 \'예상하다, 기대하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["accelerate","appreciate","anticipate","accumulate"]', 'anticipate', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 3), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그의 행동이 ______ 하다, 즉 합리적이지 않다.\' (불합리한, 이치에 맞지 않는)', NULL, NULL, NULL, NULL, NULL, 'irrational', '["irrational","Irrational"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 4), 'en', '다음 중 \'결정을 내리다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["make a decision","do a decision","take a decision","have a decision"]', 'make a decision', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 5), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 음악을 들으면서 시간 가는 줄 몰랐다', '["I","of","forgot","music","lost","to","track","listening","time","while"]', '["I","lost","track","of","time","while","listening","to","music"]', '["forgot"]', NULL, 'I lost track of time while listening to music', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 6), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 두 사람은 서로 매우 ______ 하다.\' (닮다, 비슷하다)', NULL, NULL, NULL, NULL, NULL, 'resemble', '["resemble","Resemble","similar","Similar"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 7), 'en', '다음 중 \'신뢰할 수 있는, 믿을 만한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["reliable","respective","reluctant","relevant"]', 'reliable', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 8), 'en', '빈칸에 알맞은 영어 표현을 쓰세요.

\'처음에는 낯설었지만, 새 학교에 ______ 했다.\' (적응하다)', NULL, NULL, NULL, NULL, NULL, 'adapt to', '["adapt to","Adapt to","adjust to","Adjust to","adapted to","adjusted to"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 9), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그녀는 팀 회의에서 자신의 의견을 ______ 했다.\' (주장하다, 내세우다)', NULL, NULL, NULL, NULL, NULL, 'assert', '["assert","Assert","asserted","Asserted"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 10), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 길을 걷다가 옛 동료를 우연히 만났다', '["old","the","colleague","street","on","I","into","met","ran","an"]', '["I","ran","into","an","old","colleague","on","the","street"]', '["met"]', NULL, 'I ran into an old colleague on the street', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 11), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그 나쁜 소식에 실망했다', '["bad","disappointed","was","by","the","news","I","surprised"]', '["I","was","disappointed","by","the","bad","news"]', '["surprised"]', NULL, 'I was disappointed by the bad news', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 12), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 소식은 나를 ______ 시켰다.\' (실망시키다)', NULL, NULL, NULL, NULL, NULL, 'disappoint', '["disappoint","Disappoint","disappointed","Disappointed"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 13), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 결국 그 회의에 나타나지 않았다', '["out","She","didn\'t","up","for","turn","the","meeting"]', '["She","didn\'t","turn","up","for","the","meeting"]', '["out"]', NULL, 'She didn\'t turn up for the meeting', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 14), 'en', '다음 중 \'우연히 발견하다\'를 뜻하는 구동사는?', NULL, NULL, NULL, NULL, '["come up with","come across","come out of","come down with"]', 'come across', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 15), 'en', '다음 중 \'연락을 유지하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["keep in mind","keep in line","keep in touch","keep in shape"]', 'keep in touch', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 16), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그는 마감일을 맞추기 위해 ______ 했다.\' (고군분투하다, 힘들게 노력하다)', NULL, NULL, NULL, NULL, NULL, 'struggle', '["struggled","Struggled","struggle","Struggle"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 17), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그녀는 항상 약속을 ______ 한다.\' (지키다, 이행하다)', NULL, NULL, NULL, NULL, NULL, 'keep', '["keep","Keep","keeps","Keeps"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 18), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 새 직장 환경에 적응하는 데 시간이 걸렸다', '["workplace","to","time","new","took","It","adapt","to","adjust","her","the"]', '["It","took","her","time","to","adapt","to","the","new","workplace"]', '["adjust"]', NULL, 'It took her time to adapt to the new workplace', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 19), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'She was ______ about the job interview.\' (면접에 대해 걱정이 되고 불안했다)', NULL, NULL, NULL, NULL, NULL, 'anxious', '["anxious","Anxious","nervous","Nervous"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 20), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'애매한, 모호한\' = ______', NULL, NULL, NULL, NULL, NULL, 'ambiguous', '["ambiguous","Ambiguous"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 21), 'en', '단어를 배열해서 문장을 완성하세요.', '우리는 졸업 후에도 연락을 유지했다', '["kept","We","contact","after","touch","graduation","in"]', '["We","kept","in","touch","after","graduation"]', '["contact"]', NULL, 'We kept in touch after graduation', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 22), 'en', '다음 중 \'(일, 약속을) 미루다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["put down","put away","put up","put off"]', 'put off', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 23), 'en', '단어를 배열해서 문장을 완성하세요.', '그 기계가 신뢰할 수 있는지 확인해야 한다', '["check","We","dependable","machine","reliable","to","if","is","the","need"]', '["We","need","to","check","if","the","machine","is","reliable"]', '["dependable"]', NULL, 'We need to check if the machine is reliable', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 24), 'en', '\'anxious\'와 \'nervous\'의 차이를 잘 설명한 것은?', NULL, NULL, NULL, NULL, '["둘은 완전히 같은 뜻이다","anxious는 짧은 긴장, nervous는 만성적 불안","anxious는 흥분, nervous는 두려움","anxious는 미래의 불확실한 걱정, nervous는 특정 상황 앞의 긴장"]', 'anxious는 미래의 불확실한 걱정, nervous는 특정 상황 앞의 긴장', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 25), 'en', '단어를 배열해서 문장을 완성하세요.', '그 시험이 생각보다 쉬운 것으로 드러났다', '["to","out","be","easier","than","The","expected","seemed","turned","exam"]', '["The","exam","turned","out","to","be","easier","than","expected"]', '["seemed"]', NULL, 'The exam turned out to be easier than expected', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 26), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 그 소식을 일부러 나에게 말하지 않았다', '["didn\'t","me","deliberately","news","tell","accidentally","the","He"]', '["He","deliberately","didn\'t","tell","me","the","news"]', '["accidentally"]', NULL, 'He deliberately didn\'t tell me the news', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 27), 'en', '다음 중 \'이해하다, 알아내다\'를 뜻하는 구동사는?', NULL, NULL, NULL, NULL, '["figure up","figure out","figure in","figure on"]', 'figure out', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 28), 'en', '다음 중 \'압도된, 감당하기 힘든\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["overestimated","overloaded","overlooked","overwhelmed"]', 'overwhelmed', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 29), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그 중요한 회의를 미뤄버렸다', '["important","off","the","I","canceled","meeting","put"]', '["I","put","off","the","important","meeting"]', '["canceled"]', NULL, 'I put off the important meeting', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 30), 'en', '우연히 오래된 친구를 마주쳤을 때 쓸 수 있는 구동사는?', NULL, NULL, NULL, NULL, '["run over","run out of","run away from","run into"]', 'run into', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 31), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 그 문제를 혼자서 해결했다', '["solved","I","out","the","myself","figured","by","problem"]', '["I","figured","out","the","problem","by","myself"]', '["solved"]', NULL, 'I figured out the problem by myself', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 32), 'en', '다음 중 \'시간 가는 줄 모르다\'에 가장 가까운 표현은?', NULL, NULL, NULL, NULL, '["miss the time","lose track of time","waste the time","skip the time"]', 'lose track of time', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 33), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 마감 때문에 극심한 압박감을 느꼈다', '["excited","deadline","overwhelmed","the","I","by","felt"]', '["I","felt","overwhelmed","by","the","deadline"]', '["excited"]', NULL, 'I felt overwhelmed by the deadline', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 34), 'en', '다음 중 \'~에 익숙해지다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["be used to","used to","get used to","get use of"]', 'get used to', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 35), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'어렵고 복잡한 상황을 헤쳐 나가다\' = _______ a difficult situation', NULL, NULL, NULL, NULL, NULL, 'navigate', '["navigate","Navigate"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 36), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 결정을 서둘러 내리고 싶지 않았다', '["decision","a","want","make","She","to","take","hasty","didn\'t"]', '["She","didn\'t","want","to","make","a","hasty","decision"]', '["take"]', NULL, 'She didn\'t want to make a hasty decision', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 37), 'en', '다음 중 \'의도적으로, 고의로\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["definitely","desperately","deliberately","delicately"]', 'deliberately', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 38), 'en', '다음 중 \'결국 ~으로 드러나다, 밝혀지다\'를 뜻하는 구동사는?', NULL, NULL, NULL, NULL, '["turn down","turn up","turn off","turn out"]', 'turn out', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 39), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 프로젝트를 완료하는 데 충분한 ______ 이 없었다.\' (자원, 자산)', NULL, NULL, NULL, NULL, NULL, 'resources', '["resources","Resources","resource","Resource"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 40), 'en', '단어를 배열해서 문장을 완성하세요.', '나는 도서관에서 흥미로운 책을 우연히 발견했다', '["found","library","the","across","book","interesting","I","an","came","in"]', '["I","came","across","an","interesting","book","in","the","library"]', '["found"]', NULL, 'I came across an interesting book in the library', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 41), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 발언은 ______ 했다, 즉 논란을 일으켰다.\' (논란을 일으키다)', NULL, NULL, NULL, NULL, NULL, 'controversial', '["controversial","Controversial"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 42), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'나는 그 사실을 완전히 ______ 했다.\' (간과하다, 놓치다)', NULL, NULL, NULL, NULL, NULL, 'overlook', '["overlook","Overlook","overlooked","Overlooked"]');

-- content: competition_quiz_en_02.json level 3 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 1), 'en', '다음 중 \'(사람을) 구슬리다, 설득해서 무언가를 하게 하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["coax","concede","compel","coerce"]', 'coax', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 2), 'en', '단어를 배열해서 문장을 완성하세요.', '그 스트레스는 결국 그의 건강에 타격을 입혔다.', '["gave","a","stress","his","toll","eventually","The","took","health","serious","on"]', '["The","stress","eventually","took","a","toll","on","his","health"]', '["gave","serious"]', NULL, 'The stress eventually took a toll on his health', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 3), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 소식은 내 직장에 대한 내 ______ 을 확인시켜 줬다.\' = The news confirmed my ______ about the job.

\'직감, 예감\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'hunch', '["hunch","Hunch","hunches"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 4), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 직업을 바꾸는 것을 신중하게 고려하고 있었다.', '["career","change","was","She","her","thinking","in","contemplating","considering","a"]', '["She","was","contemplating","a","change","in","her","career"]', '["considering","thinking"]', NULL, 'She was contemplating a change in her career', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 5), 'en', '단어를 배열해서 문장을 완성하세요.', '그 감정은 회의가 끝난 후에도 오랫동안 남아 있었다.', '["long","lingered","even","after","had","the","meeting","ended","feeling","remained","The"]', '["The","feeling","lingered","long","after","the","meeting","had","ended"]', '["remained","even"]', NULL, 'The feeling lingered long after the meeting had ended', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 6), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 발표는 회의실 안에 어색한 침묵을 만들었다.\' = The announcement created an ______ silence in the meeting room.

\'어색한, 불편한 분위기의\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'palpable', '["palpable","Palpable"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 7), 'en', '다음 중 \'불안한, 염려하는\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["assertive","ambiguous","apprehensive","aggressive"]', 'apprehensive', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 8), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'우리는 결국 타협점을 찾았다.\' = We eventually reached a ______.

\'타협, 절충안\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'compromise', '["compromise","Compromise"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 9), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그녀는 어떻게 대답해야 할지 망설였다.\' = She ______ before answering.

\'망설이다, 주저하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'hesitated', '["hesitated","hesitate","Hesitated"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 10), 'en', '단어를 배열해서 문장을 완성하세요.', '그 정책 변화는 상당한 반발을 불러일으켰다.', '["negative","change","policy","caused","sparked","The","backlash","a","considerable"]', '["The","policy","change","sparked","a","considerable","backlash"]', '["caused","negative"]', NULL, 'The policy change sparked a considerable backlash', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 11), 'en', '다음 중 \'(부정적 영향을) 주다, 타격을 입히다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["take for granted","take the lead on","take issue with","take a toll on"]', 'take a toll on', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 12), 'en', '단어를 배열해서 문장을 완성하세요.', '그 해고는 결국 변장된 축복이었다.', '["be","a","disguise","in","misfortune","layoff","turned","to","blessing","out","hidden","The"]', '["The","layoff","turned","out","to","be","a","blessing","in","disguise"]', '["hidden","misfortune"]', NULL, 'The layoff turned out to be a blessing in disguise', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 13), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 그의 말 사이에 숨겨진 의미를 읽어낼 수 있었다.', '["the","his","words","lines","beyond","could","through","She","between","of","read"]', '["She","could","read","between","the","lines","of","his","words"]', '["beyond","through"]', NULL, 'She could read between the lines of his words', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 14), 'en', '다음 중 \'사실을 회피하거나 직접적으로 말하지 않다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["face the music","beat around the bush","spill the beans","cut to the chase"]', 'beat around the bush', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 15), 'en', '다음 중 \'(힘든 상황을) 받아들이다, 감수하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["come to terms with","come down with","come up against","come across as"]', 'come to terms with', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 16), 'en', '단어를 배열해서 문장을 완성하세요.', '그 회사는 엄격한 환경 기준을 고수했다.', '["followed","The","company","adhered","maintained","environmental","strict","to","standards"]', '["The","company","adhered","to","strict","environmental","standards"]', '["followed","maintained"]', NULL, 'The company adhered to strict environmental standards', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 17), 'en', '다음 중 \'신중하게 고려하다, 심사숙고하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["concentrate","contemplate","consolidate","compensate"]', 'contemplate', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 18), 'en', '다음 중 \'(비밀·감정을) 억누르다, 감추다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["surrender","subside","sustain","suppress"]', 'suppress', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 19), 'en', '빈칸에 알맞은 영어 표현을 쓰세요.

\'결국 ~로 귀결되다\' = boil ______ to', NULL, NULL, NULL, NULL, NULL, 'down', '["down","Down","boil down","boil down to"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 20), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'~를 고수하다, 지키다\' = ______ to', NULL, NULL, NULL, NULL, NULL, 'adhere', '["adhere","Adhere"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 21), 'en', '단어를 배열해서 문장을 완성하세요.', '그녀는 직접 핵심을 말하는 대신 빙빙 돌려 말했다.', '["of","avoided","around","the","bush","beat","directly","instead","She","topic","speaking"]', '["She","beat","around","the","bush","instead","of","speaking","directly"]', '["avoided","topic"]', NULL, 'She beat around the bush instead of speaking directly', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 22), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 자신의 진짜 감정을 억누르려 했다.', '["feelings","to","suppress","genuine","hide","tried","true","his","He"]', '["He","tried","to","suppress","his","true","feelings"]', '["hide","genuine"]', NULL, 'He tried to suppress his true feelings', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 23), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 결정은 많은 반발을 불러일으켰다.\' = The decision sparked considerable ______.

\'반발, 강한 반대\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'backlash', '["backlash","Backlash"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 24), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'당연하게 여기다\' = take ~ for ______', NULL, NULL, NULL, NULL, NULL, 'granted', '["granted","Granted"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 25), 'en', '다음 중 \'불행처럼 보이지만 사실은 좋은 일\'을 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["every cloud has a silver lining","a stroke of luck","a blessing in disguise","a twist of fate"]', 'a blessing in disguise', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 26), 'en', '다음 중 \'(주장·이론을) 반박하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["revise","refute","reflect","retract"]', 'refute', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 27), 'en', '단어를 배열해서 문장을 완성하세요.', '그는 실직한 현실을 받아들이는 데 오랜 시간이 걸렸다.', '["grips","a","to","terms","to","took","with","him","long","come","time","It"]', '["It","took","him","a","long","time","to","come","to","terms"]', '["with","grips"]', NULL, 'It took him a long time to come to terms', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 28), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그는 그 칭찬을 겸손하게 받아들였다.\' = He accepted the compliment with great ______.

\'겸손, 겸양\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'humility', '["humility","Humility"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 29), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그녀의 말은 뭔가 잘못됐다는 것을 암시했다.\' = Her words ______ that something was wrong.

\'암시하다, 시사하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'implied', '["implied","imply","implies","Implied"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 30), 'en', '단어를 배열해서 문장을 완성하세요.', '그 팀은 자신들의 입장을 굽히지 않고 버텼다.', '["pressure","despite","the","refused","team","firm","their","The","ground","stood"]', '["The","team","stood","their","ground","despite","the","pressure"]', '["firm","refused"]', NULL, 'The team stood their ground despite the pressure', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 31), 'en', '단어를 배열해서 문장을 완성하세요.', '너는 친구들의 지지를 당연하게 여기면 안 된다.', '["support","You","friends\'","granted","assumed","your","take","shouldn\'t","for","always"]', '["You","shouldn\'t","take","your","friends\'","support","for","granted"]', '["assumed","always"]', NULL, 'You shouldn\'t take your friends\' support for granted', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 32), 'en', '다음 중 \'기대에 부응하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["live off","live up to","live through","live out"]', 'live up to', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 33), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 영화는 내 기억에 오래 남았다.\' = The movie ______ with me for a long time.

\'(인상 등이) 남다, 맴돌다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'lingered', '["lingered","lingers","linger","Lingered"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 34), 'en', '다음 중 \'행간을 읽다, 숨은 뜻을 파악하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["get to the point","beat around the bush","read between the lines","speak your mind"]', 'read between the lines', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 35), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그는 자신의 실수를 인정하는 것을 꺼렸다.\' = He was ______ to admit his mistakes.

\'꺼리는, 내키지 않는\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'reluctant', '["reluctant","Reluctant"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 36), 'en', '단어를 배열해서 문장을 완성하세요.', '그 문제는 결국 신뢰 부족으로 귀결된다.', '["problem","boils","a","trust","ultimately","down","comes","The","of","lack","to"]', '["The","problem","boils","down","to","a","lack","of","trust"]', '["comes","ultimately"]', NULL, 'The problem boils down to a lack of trust', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 37), 'en', '다음 중 \'(결과를) 초래하다, 수반하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["enrich","ensue","entail","endorse"]', 'entail', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 38), 'en', '단어를 배열해서 문장을 완성하세요.', '그 새 직원은 상사의 기대에 부응하려고 노력했다.', '["her","to","expectations","up","to","employee","The","meet","live","tried","new"]', '["The","new","employee","tried","to","live","up","to","expectations"]', '["meet","her"]', NULL, 'The new employee tried to live up to expectations', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 39), 'en', '다음 중 \'완전히 충격받은, 망연자실한\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["disoriented","discouraged","distracted","devastated"]', 'devastated', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 40), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 발표는 그녀의 모든 노력이 헛수고였음을 의미했다.\' = The announcement meant all her efforts were ______.

\'헛된, 소용없는\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, NULL, 'futile', '["futile","Futile"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 41), 'en', '다음 중 \'(안 좋은 상황에) 굴하지 않다, 포기하지 않다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["stand one\'s ground","stand by","stand in for","stand out"]', 'stand one\'s ground', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 42), 'en', '빈칸에 알맞은 영어 단어를 쓰세요.

\'그 프로젝트를 포기하다\' = ______ on the project

\'포기하다\'를 뜻하는 구동사의 첫 단어는?', NULL, NULL, NULL, NULL, NULL, 'give up', '["give up","Give up","give up on"]');

-- content: competition_quiz_ja_02.json level 1 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 1), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 학교에 갑니다', '["行きます","学校に","私は"]', '["私は","学校に","行きます"]', NULL, NULL, '私は学校に行きます', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 2), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 물을 마십니다', '["水を","飲みます","私は"]', '["私は","水を","飲みます"]', NULL, NULL, '私は水を飲みます', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 3), 'ja', '다음 중 \'잊다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["知る","覚える","忘れる","慣れる"]', '忘れる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 4), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'기쁘다\' = ______', NULL, NULL, NULL, NULL, NULL, '嬉しい', '["嬉しい","うれしい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 5), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'위험하다\' = ______', NULL, NULL, NULL, NULL, NULL, '危ない', '["危ない","あぶない"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 6), 'ja', '다음 중 \'무섭다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["怖い","眩しい","悔しい","恥ずかしい"]', '怖い', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 7), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'외롭다\' = ______', NULL, NULL, NULL, NULL, NULL, '寂しい', '["寂しい","さびしい","さみしい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 8), 'ja', '다음 중 \'부끄럽다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["悔しい","悲しい","怖い","恥ずかしい"]', '恥ずかしい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 9), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'연습\' = ______', NULL, NULL, NULL, NULL, NULL, '練習', '["練習","れんしゅう"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 10), 'ja', '타일을 배열해서 문장을 완성하세요.', '아마도 내일 비가 온다', '["たぶん","雨が降る","明日は"]', '["たぶん","明日は","雨が降る"]', NULL, NULL, 'たぶん明日は雨が降る', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 11), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 지갑을 찾고 있다', '["探しています","私は","財布を"]', '["私は","財布を","探しています"]', NULL, NULL, '私は財布を探しています', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 12), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'선물\' = ______', NULL, NULL, NULL, NULL, NULL, '贈り物', '["贈り物","おくりもの","プレゼント"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 13), 'ja', '타일을 배열해서 문장을 완성하세요.', '그녀는 마음이 따뜻하다', '["温かい","彼女は","気持ちが"]', '["彼女は","気持ちが","温かい"]', NULL, NULL, '彼女は気持ちが温かい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 14), 'ja', '타일을 배열해서 문장을 완성하세요.', '저 가방은 비쌉니다', '["かばんは","高いです","あの"]', '["あの","かばんは","高いです"]', NULL, NULL, 'あのかばんは高いです', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 15), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'전하다, 알리다\' = ______', NULL, NULL, NULL, NULL, NULL, '伝える', '["伝える","つたえる"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 16), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 열심히 연습했다', '["私は","練習した","一生懸命"]', '["私は","一生懸命","練習した"]', NULL, NULL, '私は一生懸命練習した', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 17), 'ja', '다음 중 \'기분, 마음\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["気持ち","心","気分","感情"]', '気持ち', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 18), 'ja', '타일을 배열해서 문장을 완성하세요.', '드디어 봄이 왔다', '["春が","来た","やっと"]', '["やっと","春が","来た"]', NULL, NULL, 'やっと春が来た', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 19), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'졸업\' = ______', NULL, NULL, NULL, NULL, NULL, '卒業', '["卒業","そつぎょう"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 20), 'ja', '다음 중 \'학교\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["図書館","病院","学校","会社"]', '学校', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 21), 'ja', '타일을 배열해서 문장을 완성하세요.', '그녀는 도서관에서 공부한다', '["勉強します","図書館で","彼女は"]', '["彼女は","図書館で","勉強します"]', NULL, NULL, '彼女は図書館で勉強します', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 22), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 학생입니다', '["学生です","私は"]', '["私は","学生です"]', NULL, NULL, '私は学生です', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 23), 'ja', '다음 중 \'크다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["小さい","長い","大きい","多い"]', '大きい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 24), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'비싸다\' = ______', NULL, NULL, NULL, NULL, NULL, '高い', '["高い","たかい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 25), 'ja', '다음 중 \'슬프다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["辛い","寂しい","苦しい","悲しい"]', '悲しい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 26), 'ja', '다음 중 \'드디어, 겨우\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["なかなか","もうすぐ","やっと","たぶん"]', 'やっと', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 27), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'보다\' = ______', NULL, NULL, NULL, NULL, NULL, '見る', '["見る","みる"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 28), 'ja', '다음 중 \'서두르다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["歩く","動く","働く","急ぐ"]', '急ぐ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 29), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'춥다\' = ______', NULL, NULL, NULL, NULL, NULL, '寒い', '["寒い","さむい"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 30), 'ja', '다음 중 \'마시다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["噛む","吸う","飲む","食べる"]', '飲む', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 31), 'ja', '타일을 배열해서 문장을 완성하세요.', '그는 약속을 잊었다', '["彼は","忘れた","約束を"]', '["彼は","約束を","忘れた"]', NULL, NULL, '彼は約束を忘れた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 32), 'ja', '다음 중 \'약속\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["予定","理由","約束","用事"]', '約束', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 33), 'ja', '다음 중 \'아마(도)\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["やっと","もっと","たぶん","ずっと"]', 'たぶん', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 34), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'오다\' = ______', NULL, NULL, NULL, NULL, NULL, '来る', '["来る","くる"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 35), 'ja', '다음 중 \'찾다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["調べる","見つける","探す","拾う"]', '探す', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 36), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'기다리다\' = ______', NULL, NULL, NULL, NULL, NULL, '待つ', '["待つ","まつ"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 37), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 밥을 먹었다', '["食べた","私は","ご飯を"]', '["私は","ご飯を","食べた"]', NULL, NULL, '私はご飯を食べた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 38), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'결혼\' = ______', NULL, NULL, NULL, NULL, NULL, '結婚', '["結婚","けっこん"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 39), 'ja', '다음 중 \'친구\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["兄弟","友達","先生","家族"]', '友達', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 40), 'ja', '타일을 배열해서 문장을 완성하세요.', '오늘은 날씨가 좋다', '["天気が","今日は","いいです"]', '["今日は","天気が","いいです"]', NULL, NULL, '今日は天気がいいです', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 41), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

\'내일\' = ______', NULL, NULL, NULL, NULL, NULL, '明日', '["明日","あした","あす"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 42), 'ja', '타일을 배열해서 문장을 완성하세요.', '이 영화는 무서웠다', '["怖かった","映画は","この"]', '["この","映画は","怖かった"]', NULL, NULL, 'この映画は怖かった', NULL);

-- content: competition_quiz_ja_02.json level 2 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 1), 'ja', '빈칸에 알맞은 단어를 쓰세요.

昨日のことを急に______。
(갑자기 어제 일이 생각났다.)

\'생각났다\' = ______', NULL, NULL, NULL, NULL, NULL, '思い出した', '["思い出した","おもいだした"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 2), 'ja', '빈칸에 알맞은 단어를 쓰세요.

深呼吸して______。
(심호흡하고 진정했다.)

\'진정했다\' = ______', NULL, NULL, NULL, NULL, NULL, '落ち着いた', '["落ち着いた","おちついた"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 3), 'ja', '빈칸에 알맞은 단어를 쓰세요.

彼女はいつも______で、周りから信頼されている。
(그녀는 항상 성실해서 주변 사람들에게 신뢰받고 있다.)

\'성실한\' = ______', NULL, NULL, NULL, NULL, NULL, '真面目', '["真面目","まじめ","真面目な","まじめな"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 4), 'ja', '다음 중 \'솔직히 말하면\'에 해당하는 표현은?', NULL, NULL, NULL, NULL, '["むしろ","いわゆる","正直なところ","確かに"]', '正直なところ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 5), 'ja', '타일을 배열해서 문장을 완성하세요.', '그의 말이 계속 신경 쓰였다', '["彼の言葉が","気にして","いた","ずっと","気になって"]', '["彼の言葉が","ずっと","気になって","いた"]', '["気にして"]', NULL, '彼の言葉がずっと気になっていた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 6), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 지갑은 소파 아래에서 발견됐다', '["見つけた","見つかった","その財布は","ソファの下で"]', '["その財布は","ソファの下で","見つかった"]', '["見つけた"]', NULL, 'その財布はソファの下で見つかった', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 7), 'ja', '빈칸에 알맞은 단어를 쓰세요.

彼は______して、大切なことを言い忘れた。
(그는 멍하니 있다가 중요한 말을 하는 걸 잊었다.)

\'멍하니 실수하다\' = ______', NULL, NULL, NULL, NULL, NULL, 'うっかり', '["うっかり","うっかりして"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 8), 'ja', '빈칸에 알맞은 단어를 쓰세요.

テスト結果を見て______した。
(시험 결과를 보고 낙담했다.)

\'낙담했다\' = ______', NULL, NULL, NULL, NULL, NULL, 'がっかり', '["がっかりした","がっかり"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 9), 'ja', '빈칸에 알맞은 단어를 쓰세요.

その映画の結末が______。
(그 영화의 결말이 신경 쓰인다.)

\'신경 쓰인다\' = ______', NULL, NULL, NULL, NULL, NULL, '気になる', '["気になる","きになる"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 10), 'ja', '타일을 배열해서 문장을 완성하세요.', '시험 결과를 보고 낙담해서 집에 돌아갔다', '["のんびりして","試験の結果を見て","家に帰った","がっかりして"]', '["試験の結果を見て","がっかりして","家に帰った"]', '["のんびりして"]', NULL, '試験の結果を見てがっかりして家に帰った', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 11), 'ja', '타일을 배열해서 문장을 완성하세요.', '심호흡을 하고 마음이 진정됐다', '["落ち着いた","気持ちが","落ち込んだ","深呼吸をして"]', '["深呼吸をして","気持ちが","落ち着いた"]', '["落ち込んだ"]', NULL, '深呼吸をして気持ちが落ち着いた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 12), 'ja', '타일을 배열해서 문장을 완성하세요.', '그녀는 여전히 매일 일기를 쓰고 있다', '["だんだん","毎日","日記を書いている","彼女は","相変わらず"]', '["彼女は","相変わらず","毎日","日記を書いている"]', '["だんだん"]', NULL, '彼女は相変わらず毎日日記を書いている', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 13), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 무심코 웃음이 나왔다', '["私は","わざわざ","思わず","笑ってしまった"]', '["私は","思わず","笑ってしまった"]', '["わざわざ"]', NULL, '私は思わず笑ってしまった', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 14), 'ja', '빈칸에 알맞은 일본어 표현을 쓰세요.

締め切りに______ように、早めに準備を始めよう。
(마감에 늦지 않도록, 일찍 준비를 시작하자.)', NULL, NULL, NULL, NULL, NULL, '遅れない', '["遅れない","おくれない"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 15), 'ja', '타일을 배열해서 문장을 완성하세요.', '그는 통증을 참고 계속 일했다', '["我慢して","働き続けた","痛みを","彼は","注意して"]', '["彼は","痛みを","我慢して","働き続けた"]', '["注意して"]', NULL, '彼は痛みを我慢して働き続けた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 16), 'ja', '다음 중 \'생각해내다, 기억해내다\'를 뜻하는 복합동사는?', NULL, NULL, NULL, NULL, '["思いつく","思い出す","思い切る","思い込む"]', '思い出す', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 17), 'ja', '다음 중 \'낙담하다, 실망하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["さっぱりする","がっかりする","のんびりする","うっかりする"]', 'がっかりする', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 18), 'ja', '다음 중 \'발견되다, 찾아지다\'를 뜻하는 동사는?', NULL, NULL, NULL, NULL, '["見つける","見渡す","見つかる","見逃す"]', '見つかる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 19), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

彼女は約束を______して、みんなに謝った。
(그녀는 약속을 어겨서 모두에게 사과했다.)', NULL, NULL, NULL, NULL, NULL, 'やぶ', '["破って","やぶって"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 20), 'ja', '\'勇気を出す\'의 뜻은?', NULL, NULL, NULL, NULL, '["용기를 내다","용기가 생기다","용기를 나누다","용기를 잃다"]', '용기를 내다', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 21), 'ja', '다음 중 \'무심코, 자기도 모르게\'를 뜻하는 부사는?', NULL, NULL, NULL, NULL, '["かえって","あいにく","わざわざ","思わず"]', '思わず', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 22), 'ja', '타일을 배열해서 문장을 완성하세요.', '갑자기 어릴 때 추억이 생각났다', '["思い出を","子どもの頃の","思いついた","思い出した","急に"]', '["急に","子どもの頃の","思い出を","思い出した"]', '["思いついた"]', NULL, '急に子どもの頃の思い出を思い出した', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 23), 'ja', '빈칸에 알맞은 단어를 쓰세요.

彼は痛みを______して、最後まで走り続けた。
(그는 통증을 참고, 끝까지 계속 달렸다.)

\'참고\' = ______', NULL, NULL, NULL, NULL, NULL, '我慢', '["我慢して","がまんして","我慢","がまん"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 24), 'ja', '다음 중 \'일부러, 고의로\'를 뜻하는 부사는?', NULL, NULL, NULL, NULL, '["きっと","やっと","ずっと","わざと"]', 'わざと', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 25), 'ja', '다음 중 \'~덕분에\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["おかげで","おもわず","おかまいなく","おそらく"]', 'おかげで', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 26), 'ja', '타일을 배열해서 문장을 완성하세요.', '그는 고의로 늦게 도착했다', '["彼は","到着した","きっと","遅れて","わざと"]', '["彼は","わざと","遅れて","到着した"]', '["きっと"]', NULL, '彼はわざと遅れて到着した', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 27), 'ja', '빈칸에 알맞은 단어를 쓰세요.

先生の______で、試験に合格できた。
(선생님 덕분에 시험에 합격할 수 있었다.)

\'덕분에\' = ______', NULL, NULL, NULL, NULL, NULL, 'おかげ', '["おかげ","おかげで"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 28), 'ja', '타일을 배열해서 문장을 완성하세요.', '그녀는 성실해서 모두에게 신뢰받고 있다', '["みんなに","信頼されている","真面目なので","彼女は","わがままなので"]', '["彼女は","真面目なので","みんなに","信頼されている"]', '["わがままなので"]', NULL, '彼女は真面目なのでみんなに信頼されている', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 29), 'ja', '타일을 배열해서 문장을 완성하세요.', '선생님 덕분에 시험에 합격할 수 있었다', '["おかげで","合格できた","試験に","せいで","先生の"]', '["先生の","おかげで","試験に","合格できた"]', '["せいで"]', NULL, '先生のおかげで試験に合格できた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 30), 'ja', '빈칸에 알맞은 단어를 쓰세요.

失くしたと思っていた財布が______。
(잃어버렸다고 생각했던 지갑이 발견됐다.)

\'발견됐다\' = ______', NULL, NULL, NULL, NULL, NULL, '見つかった', '["見つかった","みつかった"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 31), 'ja', '다음 중 \'여전히, 변함없이\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["だんだん","相変わらず","それなりに","いつのまにか"]', '相変わらず', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 32), 'ja', '타일을 배열해서 문장을 완성하세요.', '그것은 오해야. 나는 거짓말하지 않았어', '["私は","嘘をついていない","誤解だよ","それは","理解だよ"]', '["それは","誤解だよ","私は","嘘をついていない"]', '["理解だよ"]', NULL, 'それは誤解だよ私は嘘をついていない', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 33), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 영화는 의외로 무서웠다', '["怖かった","案外","その映画は","やはり"]', '["その映画は","案外","怖かった"]', '["やはり"]', NULL, 'その映画は案外怖かった', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 34), 'ja', '타일을 배열해서 문장을 완성하세요.', '나는 용기를 내서 그녀에게 말을 걸었다', '["私は","話した","勇気を出して","話しかけた","彼女に"]', '["私は","勇気を出して","彼女に","話しかけた"]', '["話した"]', NULL, '私は勇気を出して彼女に話しかけた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 35), 'ja', '다음 중 \'의외로, 생각보다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["案内","意外","以外","案外"]', '案外', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 36), 'ja', '빈칸에 알맞은 단어를 쓰세요.

彼はわざと失敗したふりをした。
이 문장에서 \'わざと\'의 한국어 의미는?', NULL, NULL, NULL, NULL, NULL, '일부러', '["일부러","고의로","의도적으로"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 37), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

このレポートには______な情報が含まれているので、取り扱いに注意してください。
(이 보고서에는 기밀 정보가 포함되어 있으니 취급에 주의하세요.)', NULL, NULL, NULL, NULL, NULL, '機密的', '["機密的","きみつてき","機密","きみつ"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 38), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

彼女は怒っていたが、しばらくして______してくれた。
(그녀는 화가 났지만, 잠시 후 용서해 주었다.)', NULL, NULL, NULL, NULL, NULL, '許して', '["許して","ゆるして","許す","ゆるす"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 39), 'ja', '다음 중 \'참다, 견디다\'를 뜻하는 동사는?', NULL, NULL, NULL, NULL, '["遠慮する","集中する","注意する","我慢する"]', '我慢する', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 40), 'ja', '다음 중 \'진정되다, 안정되다\'를 뜻하는 복합동사는?', NULL, NULL, NULL, NULL, '["落ちこむ","落ち着く","落とす","落ちる"]', '落ち着く', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 41), 'ja', '\'気になる\'와 \'気にする\'의 차이를 잘 설명한 것은?', NULL, NULL, NULL, NULL, '["気になる는 자연스럽게 신경 쓰이다, 気にする는 의식적으로 신경 쓰다","気になる는 무시하다, 気にする는 기억하다","気になる는 화나다, 気にする는 걱정하다","둘 다 똑같은 의미로 바꿔 쓸 수 있다"]', '気になる는 자연스럽게 신경 쓰이다, 気にする는 의식적으로 신경 쓰다', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 42), 'ja', '다음 중 \'오해\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["解説","理解","誤解","解決"]', '誤解', NULL);

-- content: competition_quiz_ja_02.json level 3 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 1), 'ja', '\'案ずるより産むが易し\'의 뜻에 가장 가까운 한국 속담은?', NULL, NULL, NULL, NULL, '["해보면 별거 아니다 / 기우에 불과했다","호랑이도 제 말 하면 온다","돌다리도 두드려 보고 건너라","시작이 반이다"]', '해보면 별거 아니다 / 기우에 불과했다', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 2), 'ja', '어떻게 해야 할지 몰라 막막한 상태를 나타내는 표현은?', NULL, NULL, NULL, NULL, '["途方に暮れる","先が見えない","道に迷う","途中で倒れる"]', '途方に暮れる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 3), 'ja', '빈칸에 알맞은 단어를 쓰세요.

애틋하고 가슴이 저리는 감정 = ______', NULL, NULL, NULL, NULL, NULL, '切ない', '["切ない","せつない"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 4), 'ja', '자신의 이익을 위해 남을 희생시키거나 이용하는 사람을 가리키는 표현은?', NULL, NULL, NULL, NULL, '["我田引水","他力本願","人任せ","以心伝心"]', '我田引水', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 5), 'ja', '타일을 배열해서 문장을 완성하세요.', '갑자기 회사를 그만두게 되어 어찌할 바를 몰라 막막했다', '["途方に","暮れた","会社を","困り果てて","迷ってしまった","突然","辞めることになり"]', '["突然","会社を","辞めることになり","途方に","暮れた"]', '["困り果てて","迷ってしまった"]', NULL, '突然会社を辞めることになり途方に暮れた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 6), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

졸업식 날, 친구들과의 이별이 너무나 ______。(헤어지기 너무 아쉬웠다)', NULL, NULL, NULL, NULL, NULL, '名残惜しかった', '["名残惜しかった","なごりおしかった"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 7), 'ja', '문제가 복잡하게 얽혀서 해결하기 어려운 상태를 나타내는 표현은?', NULL, NULL, NULL, NULL, '["堂々巡りをする","袋小路に入る","泥沼にはまる","板挟みになる"]', '泥沼にはまる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 8), 'ja', '겉으로는 친한 척하지만 속으로는 서로 적대적인 관계를 나타내는 표현은?', NULL, NULL, NULL, NULL, '["呉越同舟","羊頭狗肉","面従腹背","玉石混交"]', '面従腹背', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 9), 'ja', '타일을 배열해서 문장을 완성하세요.', '졸업식이 끝나고 친구들과 헤어지는 게 너무 아쉬워서 눈물이 났다', '["懐かしくて","涙が出た","寂しいので","卒業式が終わり","名残惜しくて","友達との","別れが"]', '["卒業式が終わり","友達との","別れが","名残惜しくて","涙が出た"]', '["懐かしくて","寂しいので"]', NULL, '卒業式が終わり友達との別れが名残惜しくて涙が出た', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 10), 'ja', '빈칸에 알맞은 단어를 쓰세요.

医師はその難病患者に______を投げた。(의사는 그 난치병 환자에게 포기를 선언했다)', NULL, NULL, NULL, NULL, NULL, 'さじ', '["さじ","匙"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 11), 'ja', '빈칸에 알맞은 히라가나 의태어를 쓰세요.

初めて人前でスピーチをして、動作が______なってしまった。(처음으로 사람들 앞에서 발표를 해서 동작이 어색해져 버렸다)', NULL, NULL, NULL, NULL, NULL, 'ぎこちなく', '["ぎこちなく"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 12), 'ja', '타일을 배열해서 문장을 완성하세요.', '그의 갑작스러운 부탁을 그녀는 차갑게 단호하게 거절했다', '["彼女は","優しく","断った","けんもほろろに","少し迷ってから","彼の突然の頼みを"]', '["彼の突然の頼みを","彼女は","けんもほろろに","断った"]', '["優しく","少し迷ってから"]', NULL, '彼の突然の頼みを彼女はけんもほろろに断った', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 13), 'ja', '다음 뜻에 해당하는 일본어 관용표현을 쓰세요.

\'발이 묶이다, 오도 가도 못하다\' (교통·사정 등으로 이동할 수 없게 된 상태)', NULL, NULL, NULL, NULL, NULL, '足止めを食う', '["足止めを食う","あしどめをくう","足止めをくう","あしどめを食う"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 14), 'ja', '빈칸에 알맞은 표현을 쓰세요.

彼女は______になって、上司と部下の間で板挟みになっていた。
(그녀는 진퇴양난에 빠져 상사와 부하 사이에서 샌드위치 신세가 됐다)

\'상사와 부하 사이에 끼다\'를 나타내는 표현에서 빈칸에 들어갈 말은?', NULL, NULL, NULL, NULL, NULL, '板挟み', '["板挟み","いたばさみ"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 15), 'ja', '가슴을 뭉클하게 하다, 감동을 주다는 뜻의 표현은?', NULL, NULL, NULL, NULL, '["胸を張る","胸が痛む","胸を借りる","胸を打つ"]', '胸を打つ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 16), 'ja', '타일을 배열해서 문장을 완성하세요.', '상사와 부하 사이에서 이러지도 저러지도 못하는 상황에 빠졌다', '["上司と部下の間で","ジレンマに","困り果てた","どうすることも","板挟みの","できない","状況に陥った"]', '["上司と部下の間で","どうすることも","できない","板挟みの","状況に陥った"]', '["ジレンマに","困り果てた"]', NULL, '上司と部下の間でどうすることもできない板挟みの状況に陥った', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 17), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 프로젝트는 문제가 복잡하게 얽혀 수렁에 빠져버렸다', '["泥沼に","はまって","解決できずに","問題が複雑に絡み合い","そのプロジェクトは","しまった","行き詰まって"]', '["そのプロジェクトは","問題が複雑に絡み合い","泥沼に","はまって","しまった"]', '["行き詰まって","解決できずに"]', NULL, 'そのプロジェクトは問題が複雑に絡み合い泥沼にはまってしまった', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 18), 'ja', '빈칸에 알맞은 일본어 표현을 쓰세요.

彼女の言葉は聞く人の心に深く______。
(그녀의 말은 듣는 사람의 마음에 깊이 스며들었다.)', NULL, NULL, NULL, NULL, NULL, '染み渡った', '["染み渡った","しみわたった","染みわたった"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 19), 'ja', '물음이나 부탁을 단호하게 거절하다는 뜻의 표현은?', NULL, NULL, NULL, NULL, '["けしかける","けちをつける","けんもほろろ","けなす"]', 'けんもほろろ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 20), 'ja', '타일을 배열해서 문장을 완성하세요.', '그는 자신의 이익을 위해 항상 억지 논리를 펴는 사람이다', '["人だ","身勝手な","彼は","いつも","我田引水な","自分の利益のために","他力本願な"]', '["彼は","いつも","自分の利益のために","我田引水な","人だ"]', '["他力本願な","身勝手な"]', NULL, '彼はいつも自分の利益のために我田引水な人だ', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 21), 'ja', '타일을 배열해서 문장을 완성하세요.', '오랜만에 고향에 돌아와 지난 시절을 깊이 감회에 젖어 떠올렸다', '["なつかしんで","しみじみと","思い出した","過ぎた日々を","故郷に戻り","久しぶりに","ひしひしと"]', '["久しぶりに","故郷に戻り","過ぎた日々を","しみじみと","思い出した"]', '["なつかしんで","ひしひしと"]', NULL, '久しぶりに故郷に戻り過ぎた日々をしみじみと思い出した', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 22), 'ja', '일이 잘 풀리지 않아 허탈하고 공허한 감정을 나타내는 단어는?', NULL, NULL, NULL, NULL, '["物悲しい","心細い","虚しい","切ない"]', '虚しい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 23), 'ja', '동작이나 태도가 어색하고 자연스럽지 않은 모양을 나타내는 의태어는?', NULL, NULL, NULL, NULL, '["ぐずぐず","ぎこちない","ぎょっと","ぎりぎり"]', 'ぎこちない', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 24), 'ja', '다음 속담의 뜻을 이해하고, 빈칸에 알맞은 단어를 쓰세요.

「______より産むが易し」(걱정하는 것보다 막상 해보면 쉽다)', NULL, NULL, NULL, NULL, NULL, '案ずる', '["案ずる","あんずる"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 25), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 의사는 손을 쓸 수 없다고 판단하여 포기를 선언했다', '["手の施しようが","ないと判断し","諦めて","投げた","手を引いた","その医師は","さじを"]', '["その医師は","手の施しようが","ないと判断し","さじを","投げた"]', '["諦めて","手を引いた"]', NULL, 'その医師は手の施しようがないと判断しさじを投げた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 26), 'ja', '타일을 배열해서 문장을 완성하세요.', '그는 겉으로는 따르는 척하면서 속으로는 배반할 기회를 노리고 있었다', '["彼は","内心では","表面上は従いながら","心から賛成して","裏切る機会を狙っていた","面従腹背で","忠実に従い"]', '["彼は","面従腹背で","表面上は従いながら","内心では","裏切る機会を狙っていた"]', '["忠実に従い","心から賛成して"]', NULL, '彼は面従腹背で表面上は従いながら内心では裏切る機会を狙っていた', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 27), 'ja', '타일을 배열해서 문장을 완성하세요.', '그녀는 처음 만나는 사람 앞에서 태도가 항상 어색하다', '["彼女は","不自然になる","態度が","いつも","初対面の人の前では","ぎこちない","うまくいかない"]', '["彼女は","初対面の人の前では","いつも","態度が","ぎこちない"]', '["不自然になる","うまくいかない"]', NULL, '彼女は初対面の人の前ではいつも態度がぎこちない', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 28), 'ja', '타일을 배열해서 문장을 완성하세요.', '그의 설명은 아무래도 납득이 가지 않아서 다시 물어봤다', '["もう一度","彼の説明は","腑に落ちなくて","理解して","聞き直した","納得できて","どうも"]', '["彼の説明は","どうも","腑に落ちなくて","もう一度","聞き直した"]', '["納得できて","理解して"]', NULL, '彼の説明はどうも腑に落ちなくてもう一度聞き直した', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 29), 'ja', '직장 상사에게 \'폐를 끼쳐 죄송합니다\'라고 할 때 \'폐\'에 해당하는 단어는?', NULL, NULL, NULL, NULL, '["苦労","不便","邪魔","迷惑"]', '迷惑', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 30), 'ja', '빈칸에 알맞은 일본어 단어를 쓰세요.

오랫동안 만나지 못한 친구와 재회했을 때 느끼는 왠지 모를 그리움과 애틋함을 일본어로 뭐라고 할까요?

(힌트: \'호로리\'한 느낌, 눈물이 날 것 같은 따뜻하고 슬픈 감정)', NULL, NULL, NULL, NULL, NULL, 'ほろ苦い', '["ほろ苦い","ほろにがい","ほろ苦さ","ほろにがさ"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 31), 'ja', '도저히 납득이 가지 않아서 답답한 상황을 표현할 때 쓰는 말은?', NULL, NULL, NULL, NULL, '["気が置けない","目に余る","腑に落ちない","手に余る"]', '腑に落ちない', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 32), 'ja', '타일을 배열해서 문장을 완성하세요.', '그 영화의 마지막 장면은 보는 사람의 가슴을 뭉클하게 했다', '["その映画の","胸を","打った","心に刺さった","感動させた","最後の場面は","見る人の"]', '["その映画の","最後の場面は","見る人の","胸を","打った"]', '["感動させた","心に刺さった"]', NULL, 'その映画の最後の場面は見る人の胸を打った', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 33), 'ja', '이별할 때 헤어지기 아쉬운 감정을 나타내는 단어는?', NULL, NULL, NULL, NULL, '["切ない","名残惜しい","懐かしい","虚しい"]', '名残惜しい', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 34), 'ja', '타일을 배열해서 문장을 완성하세요.', '처음에는 걱정했지만 막상 해보니 별거 아니었다', '["心配したが","産むが","最初は","案ずるより","やはり無理だった","易しだった","難しかった"]', '["最初は","心配したが","案ずるより","産むが","易しだった"]', '["難しかった","やはり無理だった"]', NULL, '最初は心配したが案ずるより産むが易しだった', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 35), 'ja', '빈칸에 알맞은 단어를 쓰세요.

問題が______にはまって、なかなか解決できない。(문제가 수렁에 빠져서 좀처럼 해결되지 않는다)', NULL, NULL, NULL, NULL, NULL, '泥沼', '["泥沼","どろぬま"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 36), 'ja', '타일을 배열해서 문장을 완성하세요.', '부장님께 폐를 끼쳐서 진심으로 죄송합니다', '["申し訳ございません","おかけして","失礼いたしました","ご迷惑を","ご不便を","部長に","誠に"]', '["部長に","ご迷惑を","おかけして","誠に","申し訳ございません"]', '["ご不便を","失礼いたしました"]', NULL, '部長にご迷惑をおかけして誠に申し訳ございません', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 37), 'ja', '빈칸에 알맞은 표현을 쓰세요.

どう対応すればいいかわからず、______に暮れた。(어떻게 대응해야 할지 몰라서 막막했다)', NULL, NULL, NULL, NULL, NULL, '途方', '["途方","とほう"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 38), 'ja', '빈칸에 알맞은 표현을 쓰세요.

彼の提案はどうも______ない。(그의 제안은 아무래도 납득이 가지 않는다)', NULL, NULL, NULL, NULL, NULL, '腑に落ち', '["腑に落ち","ふにおち"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 39), 'ja', '어떤 일을 하다가 완전히 그만두어 버린다는 뜻의 표현은?', NULL, NULL, NULL, NULL, '["幕を引く","さじを投げる","首を切る","手を引く"]', 'さじを投げる', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 40), 'ja', '빈칸에 알맞은 일본어 속담을 쓰세요.

\'원숭이도 나무에서 떨어진다\'는 뜻으로, 아무리 능숙한 사람도 실수할 수 있다는 의미의 속담은?', NULL, NULL, NULL, NULL, NULL, '猿も木から落ちる', '["猿も木から落ちる","さるもきからおちる","猿もきから落ちる","さるも木からおちる"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 41), 'ja', '빈칸에 알맞은 단어를 쓰세요.

彼女の歌声は聴く人の______を打った。(그녀의 노랫소리는 듣는 사람의 가슴을 뭉클하게 했다)', NULL, NULL, NULL, NULL, NULL, '胸', '["胸","むね"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 42), 'ja', '감동이나 추억을 느끼며 깊이 감회에 잠기는 모양을 나타내는 부사는?', NULL, NULL, NULL, NULL, '["ひしひし","じんわり","しみじみ","しんみり"]', 'しみじみ', NULL);

-- content: competition_quiz_zh_02.json level 1 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 1), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 그 길에 익숙하다', '["那条路","对","很","他","熟悉"]', '["他","对","那条路","很","熟悉"]', NULL, NULL, '他对那条路很熟悉', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 2), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'화가 나다, 생기다(화)\' = ______

예: 他很______，不想说话。(그는 매우 화가 나서 말하고 싶지 않다.)', NULL, NULL, NULL, NULL, NULL, '生气', '["生气","shengqi"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 3), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 지금 매우 조급하다', '["现在","着急","很","我"]', '["我","现在","很","着急"]', NULL, NULL, '我现在很着急', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 4), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'긴장을 풀다, 편안히 쉬다\' = ______', NULL, NULL, NULL, NULL, NULL, '放松', '["放松","fangsong"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 5), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'부럽다, 선망하다\' = ______

예: 我很______她的生活。(나는 그녀의 생활이 정말 부럽다.)', NULL, NULL, NULL, NULL, NULL, '羡慕', '["羡慕","xianmu"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 6), 'zh', '다음 중 \'갑자기\'를 뜻하는 부사는?', NULL, NULL, NULL, NULL, '["突然","差点儿","终于","已经"]', '突然', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 7), 'zh', '타일을 배열해서 문장을 완성하세요.', '이 상황은 정말 이상하다', '["很","奇怪","情况","这个","真的"]', '["这个","情况","真的","很","奇怪"]', NULL, NULL, '这个情况真的很奇怪', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 8), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그 상황이 너무 어색했다', '["让","很","情况","那个","尴尬","我"]', '["那个","情况","让","我","很","尴尬"]', NULL, NULL, '那个情况让我很尴尬', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 9), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'실망하다\' = ______

예: 他没来，我很______。(그가 오지 않아서 나는 많이 실망했다.)', NULL, NULL, NULL, NULL, NULL, '失望', '["失望","shiwang"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 10), 'zh', '다음 중 \'부끄럽다, 수줍다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["害羞","无聊","难过","害怕"]', '害羞', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 11), 'zh', '다음 중 \'조급하다, 급하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["害怕","紧张","无聊","着急"]', '着急', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 12), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 요즘 너무 심심하다', '["太","无聊","我","最近","了"]', '["我","最近","太","无聊","了"]', NULL, NULL, '我最近太无聊了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 13), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 매우 긴장된다', '["很","紧张","我"]', '["我","很","紧张"]', NULL, NULL, '我很紧张', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 14), 'zh', '다음 중 \'지루하다, 심심하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["难过","无奈","无聊","着急"]', '无聊', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 15), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'걱정하다\' = ______

예: 妈妈总是______我。(엄마는 항상 나를 걱정한다.)', NULL, NULL, NULL, NULL, NULL, '担心', '["担心","danxin"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 16), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'후회하다\' = ______

예: 他______当初的决定。(그는 처음의 결정을 후회한다.)', NULL, NULL, NULL, NULL, NULL, '后悔', '["后悔","houhui"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 17), 'zh', '다음 중 \'드디어, 마침내\'를 뜻하는 부사는?', NULL, NULL, NULL, NULL, '["还是","终于","差点儿","突然"]', '终于', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 18), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 이미 숙제를 잊어버렸다', '["作业","他","忘记","了"]', '["他","忘记","作业","了"]', NULL, NULL, '他忘记作业了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 19), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'참다, 견디다\' = ______

예: 她______住了眼泪。(그녀는 눈물을 참았다.)', NULL, NULL, NULL, NULL, NULL, '忍', '["忍","ren"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 20), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 나를 피했다', '["了","躲避","她","我"]', '["她","躲避","了","我"]', NULL, NULL, '她躲避了我', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 21), 'zh', '다음 중 \'슬프다, 마음이 아프다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["害羞","紧张","难过","难受"]', '难过', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 22), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'행복하다, 즐겁다\' = ______', NULL, NULL, NULL, NULL, NULL, '快乐', '["快乐","kuaile"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 23), 'zh', '다음 중 \'긴장하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["紧张","难过","害怕","着急"]', '紧张', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 24), 'zh', '다음 중 \'이상하다, 기이하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["奇怪","特别","奇特","不同"]', '奇怪', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 25), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'이해하다, 알다\' = ______

예: 你______我的意思吗？(내 말뜻을 이해하니?)', NULL, NULL, NULL, NULL, NULL, '明白', '["明白","mingbai"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 26), 'zh', '다음 중 \'익숙하다, 잘 알다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["知道","了解","熟悉","明白"]', '熟悉', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 27), 'zh', '다음 중 \'~인 척하다, 가장하다\'를 뜻하는 동사는?', NULL, NULL, NULL, NULL, '["表演","假设","假装","装扮"]', '假装', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 28), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'감동받다, 감동시키다\' = ______

예: 这个故事很______我。(이 이야기는 나를 매우 감동시켰다.)', NULL, NULL, NULL, NULL, NULL, '感动', '["感动","gandong"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 29), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'웃다\' = ______

예: 她一直在______。(그녀는 계속 웃고 있었다.)', NULL, NULL, NULL, NULL, NULL, '笑', '["笑","xiao"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 30), 'zh', '다음 중 \'어색하다, 난처하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["无奈","紧张","奇怪","尴尬"]', '尴尬', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 31), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 매우 부끄러워한다', '["害羞","她","很"]', '["她","很","害羞"]', NULL, NULL, '她很害羞', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 32), 'zh', '다음 중 \'잊다, 잊어버리다\'를 뜻하는 동사는?', NULL, NULL, NULL, NULL, '["了解","熟悉","忘记","记得"]', '忘记', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 33), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'격려하다, 응원하다\' = ______

예: 老师______我们努力学习。(선생님은 우리에게 열심히 공부하라고 격려했다.)', NULL, NULL, NULL, NULL, NULL, '鼓励', '["鼓励","guli"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 34), 'zh', '다음 중 \'하마터면 ~할 뻔했다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["突然","差点儿","已经","终于"]', '差点儿', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 35), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 모르는 척했다', '["假装","我","不知道"]', '["我","假装","不知道"]', NULL, NULL, '我假装不知道', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 36), 'zh', '다음 중 \'피하다, 회피하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["拒绝","避免","躲避","逃跑"]', '躲避', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 37), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 하마터면 넘어질 뻔했다', '["摔倒","了","我","差点儿"]', '["我","差点儿","摔倒","了"]', NULL, NULL, '我差点儿摔倒了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 38), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 갑자기 그녀를 기억했다', '["突然","她","想起","我"]', '["我","突然","想起","她"]', NULL, NULL, '我突然想起她', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 39), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'피곤하다, 지치다\' = ______', NULL, NULL, NULL, NULL, NULL, '累', '["累","lei"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 40), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 갑자기 울기 시작했다', '["开始","突然","她","哭"]', '["她","突然","开始","哭"]', NULL, NULL, '她突然开始哭', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 41), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 드디어 그를 찾았다', '["终于","找到","他","我","了"]', '["我","终于","找到","他","了"]', NULL, NULL, '我终于找到他了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 1 AND origin_id = 42), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'불만스럽다, 만족스럽지 않다\' = ______

예: 他对结果很______。(그는 결과에 매우 불만스러워했다.)', NULL, NULL, NULL, NULL, NULL, '不满意', '["不满意","bu manyi","bumanyi"]');

-- content: competition_quiz_zh_02.json level 2 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 1), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 용기를 내서 말했다', '["害怕","她","说出来","鼓起勇气","了"]', '["她","鼓起勇气","说出来","了"]', '["害怕"]', NULL, '她鼓起勇气说出来了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 2), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그가 오해할까봐 걱정됐다', '["会","原谅","误会","我","他","担心"]', '["我","担心","他","会","误会"]', '["原谅"]', NULL, '我担心他会误会', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 3), 'zh', '다음 중 \'부끄럽다, 미안하다\'는 감정을 나타내는 표현은?', NULL, NULL, NULL, NULL, '["不高兴","不满意","不好意思","不在乎"]', '不好意思', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 4), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'인상이 깊다\' = 印象______', NULL, NULL, NULL, NULL, NULL, '深刻', '["深刻","shenke","shēn kè"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 5), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

그는 화가 나서 ______을 냈다. (\'화를 내다\'에서 핵심 단어)', NULL, NULL, NULL, NULL, NULL, '脾气', '["脾气","piqi","pí qì"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 6), 'zh', '시험 전날 긴장해서 잠을 못 잤다. 여기서 \'긴장하다\'에 가장 알맞은 단어는?', NULL, NULL, NULL, NULL, '["担心","着急","害怕","紧张"]', '紧张', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 7), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 일부러 나에게 알리지 않았다', '["忘记","故意","没有","他","告诉","我"]', '["他","故意","没有","告诉","我"]', '["忘记"]', NULL, '他故意没有告诉我', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 8), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그와 일하며 교류한 적이 있다', '["我","打过","交道","跟他","曾经","吵架"]', '["我","曾经","跟他","打过","交道"]', '["吵架"]', NULL, '我曾经跟他打过交道', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 9), 'zh', '다음 중 \'호기심\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["兴趣心","求知欲","好感情","好奇心"]', '好奇心', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 10), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'유머가 있다, 재미있다\' = ______', NULL, NULL, NULL, NULL, NULL, '幽默', '["幽默","youmo","yōu mò"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 11), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'불평하다, 투덜거리다\' = ______', NULL, NULL, NULL, NULL, NULL, '抱怨', '["抱怨","baoyuan","bào yuàn"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 12), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'~에 적응하다\' = 适______', NULL, NULL, NULL, NULL, NULL, '适应', '["适应","shiying","shì yìng"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 13), 'zh', '다음 중 \'~와 교류하다, 왕래하다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["打算法","打招呼","打电话","打交道"]', '打交道', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 14), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그에게 농담을 했다', '["跟他","开了","一个","认真","我","玩笑"]', '["我","跟他","开了","一个","玩笑"]', '["认真"]', NULL, '我跟他开了一个玩笑', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 15), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'솔직히 말하면, 사실은\' = ______', NULL, NULL, NULL, NULL, NULL, '其实', '["其实","qishi","qí shí"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 16), 'zh', '다음 중 \'오해하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["误会","理解","解释","明白"]', '误会', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 17), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'칭찬하다\' = ______', NULL, NULL, NULL, NULL, NULL, '表扬', '["表扬","biaoyang","biǎo yáng"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 18), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'책임지다\' = 负______', NULL, NULL, NULL, NULL, NULL, '负责', '["负责","fuze","fù zé"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 19), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 항상 다른 사람을 위로해 준다', '["批评","别人","她","安慰","总是"]', '["她","总是","安慰","别人"]', '["批评"]', NULL, '她总是安慰别人', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 20), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'몰랐다, 생각지도 못했다\' = ______', NULL, NULL, NULL, NULL, NULL, '没想到', '["没想到","mei xiang dao","méi xiǎng dào"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 21), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'부러워하다, 羡慕하다\' = ______', NULL, NULL, NULL, NULL, NULL, '羡慕', '["羡慕","xianmu","xiàn mù"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 22), 'zh', '다음 중 \'감동받다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["感激","感觉","感动","感受"]', '感动', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 23), 'zh', '다음 중 \'망설이다, 주저하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["迷惑","徘徊","犹豫","困惑"]', '犹豫', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 24), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'갑자기 깨닫다\' = 突然______', NULL, NULL, NULL, NULL, NULL, '意识到', '["意识到","yishidao","yì shí dào"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 25), 'zh', '다음 중 \'고의로, 일부러\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["竟然","故意","突然","居然"]', '故意', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 26), 'zh', '친구가 농담을 할 때 쓰는 표현으로 가장 알맞은 것은?', NULL, NULL, NULL, NULL, '["说笑话","发脾气","开心情","开玩笑"]', '开玩笑', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 27), 'zh', '타일을 배열해서 문장을 완성하세요.', '버스가 이미 떠나서 따라잡을 시간이 없었다', '["公共汽车","追了","来不及","等一下","已经走了"]', '["公共汽车","已经走了","来不及","追了"]', '["等一下"]', NULL, '公共汽车已经走了来不及追了', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 28), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 내가 생각지도 못한 답을 말했다', '["说出了","没想到的","答案","他","我","早就"]', '["他","说出了","我","没想到的","答案"]', '["早就"]', NULL, '他说出了我没想到的答案', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 29), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'거절하다\' = ______', NULL, NULL, NULL, NULL, NULL, '拒绝', '["拒绝","jujue","jù jué"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 30), 'zh', '타일을 배열해서 문장을 완성하세요.', '선생님이 그를 공개적으로 칭찬했다', '["批评","表扬了","当众","老师","他"]', '["老师","当众","表扬了","他"]', '["批评"]', NULL, '老师当众表扬了他', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 31), 'zh', '타일을 배열해서 문장을 완성하세요.', '사실 그는 나를 부러워하고 있었다', '["其实","一直","我","羡慕","讨厌","他"]', '["其实","他","一直","羡慕","我"]', '["讨厌"]', NULL, '其实他一直羡慕我', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 32), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'위로하다\' = ______', NULL, NULL, NULL, NULL, NULL, '安慰', '["安慰","anwei","ān wèi"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 33), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그 소식을 듣고 깜짝 놀랐다', '["非常","吃惊","听到","我","那个消息","高兴"]', '["我","听到","那个消息","非常","吃惊"]', '["高兴"]', NULL, '我听到那个消息非常吃惊', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 34), 'zh', '다음 중 \'솔직하다, 직접적이다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["直白","简单","直接","坦率"]', '直接', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 35), 'zh', '타일을 배열해서 문장을 완성하세요.', '나는 그의 부탁을 거절했다', '["我","拒绝了","他的","请求","接受"]', '["我","拒绝了","他的","请求"]', '["接受"]', NULL, '我拒绝了他的请求', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 36), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'참가하다, 참여하다\' = ______', NULL, NULL, NULL, NULL, NULL, '参加', '["参加","canjia","cān jiā"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 37), 'zh', '다음 중 \'깜짝 놀라다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["吃力","吃苦","吃亏","吃惊"]', '吃惊', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 38), 'zh', '다음 중 \'~할 틈이 없다, 시간이 모자라다\'를 뜻하는 표현은?', NULL, NULL, NULL, NULL, '["来得及","来不及","顾不上","赶不上"]', '来不及', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 39), 'zh', '다음 중 \'참을성 있게 기다리다\'와 관련된 표현으로 알맞은 것은?', NULL, NULL, NULL, NULL, '["开心","耐心","细心","用心"]', '耐心', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 40), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 결과에 불만을 토로했다', '["抱怨了","对结果","满意","很多","她"]', '["她","对结果","抱怨了","很多"]', '["满意"]', NULL, '她对结果抱怨了很多', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 41), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 새 환경에 빠르게 적응했다', '["很快","环境","适应了","逃避","他","新的"]', '["他","很快","适应了","新的","环境"]', '["逃避"]', NULL, '他很快适应了新的环境', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 2 AND origin_id = 42), 'zh', '다음 중 \'용기를 내다\'와 짝을 이루는 표현으로 알맞은 것은?', NULL, NULL, NULL, NULL, '["信心","精神","力气","勇气"]', '勇气', NULL);

-- content: competition_quiz_zh_02.json level 3 (42건)
INSERT INTO competition_quiz_content (quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers) VALUES
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 1), 'zh', '타일을 배열해서 문장을 완성하세요.', '그녀는 설명을 듣고 나서야 비로소 깨달았다(恍然大悟).', '["恍然大悟","才","突然","她","明白了","之后","解释","听完"]', '["她","听完","解释","之后","才","恍然大悟"]', '["突然","明白了"]', NULL, '她听完解释之后才恍然大悟', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 2), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'그 계획은 현실적으로 ______(실행 가능하다)하지 않다.\' = ______', NULL, NULL, NULL, NULL, NULL, '可行', '["可行","kexing"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 3), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 자신이 한 말을 곧 후회했다(懊悔).', '["感到","懊悔","他","很快","高兴","骄傲","自己说的话","为","就"]', '["他","很快","就","为","自己说的话","感到","懊悔"]', '["高兴","骄傲"]', NULL, '他很快就为自己说的话感到懊悔', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 4), 'zh', '타일을 배열해서 문장을 완성하세요.', '그들은 표면적으로는 협력하지만 속으로는 암투를(明争暗斗) 벌이고 있다.', '["互相信任","在","同心协力","他们","明争暗斗","在","合作","实际上","却","表面上"]', '["他们","表面上","在","合作","实际上","却","在","明争暗斗"]', '["同心协力","互相信任"]', NULL, '他们表面上在合作实际上却在明争暗斗', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 5), 'zh', '재앙이 오히려 복이 되다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["塞翁失马","绝处逢生","因祸得福","否极泰来"]', '因祸得福', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 6), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'그는 항상 남들보다 앞서 나가려는 ______(공명심, 명예욕)이 강하다.\' = ______', NULL, NULL, NULL, NULL, NULL, '功利', '["功利","gongli"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 7), 'zh', '타일을 배열해서 문장을 완성하세요.', '그 아이는 선생님 앞에서 자신의 생각을 잘 표현하지(表达) 못한다.', '["理解","想法","面前","孩子","掩饰","在","能","表达","自己的","老师","不太","那个"]', '["那个","孩子","在","老师","面前","不太","能","表达","自己的","想法"]', '["理解","掩饰"]', NULL, '那个孩子在老师面前不太能表达自己的想法', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 8), 'zh', '다음 중 \'몹시 원통하고 억울한 감정\'을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["郁闷","沮丧","懊悔","委屈"]', '委屈', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 9), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'그는 자신의 감정을 잘 ______(표현하다, 드러내다)하지 못한다.\'에서 빈칸 = ______', NULL, NULL, NULL, NULL, NULL, '表达', '["表达","biaoda"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 10), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'그는 ______(모호하게, 애매하게) 대답하며 확실한 입장을 밝히지 않았다.\' = ______', NULL, NULL, NULL, NULL, NULL, '含糊', '["含糊","hanhu"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 11), 'zh', '다음 중 \'자신의 실력을 숨기고 때를 기다리다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["忍辱负重","卧薪尝胆","韬光养晦","厚积薄发"]', '韬光养晦', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 12), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 어려운 상황에서도 반도폐(半途而废)하지 않고 끝까지 버텼다.', '["坚持到了","半途而废","没有","在","情况下","轻易","放弃了","他","困难的","最后"]', '["他","在","困难的","情况下","没有","半途而废","坚持到了","最后"]', '["放弃了","轻易"]', NULL, '他在困难的情况下没有半途而废坚持到了最后', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 13), 'zh', '다음 중 \'참을 수 없을 만큼 서둘러 하고 싶다\'는 뜻의 성어는?', NULL, NULL, NULL, NULL, '["急于求成","跃跃欲试","迫不及待","心急如焚"]', '迫不及待', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 14), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

\'이미 저지른 일은 돌이킬 수 없다\' = ______', NULL, NULL, NULL, NULL, NULL, '覆水难收', '["覆水难收","fushui nan shou"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 15), 'zh', '다음 중 \'어떤 사실을 증명하거나 뒷받침하다\'를 뜻하는 동사는?', NULL, NULL, NULL, NULL, '["核实","证明","证实","验证"]', '证实', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 16), 'zh', '타일을 배열해서 문장을 완성하세요.', '이 프로젝트는 대충 마무리(草草了事)할 수 없다.', '["不能","项目","这个","草草了事","认真对待","随便"]', '["这个","项目","不能","草草了事"]', '["随便","认真对待"]', NULL, '这个项目不能草草了事', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 17), 'zh', '타일을 배열해서 문장을 완성하세요.', '이 두 가지 문제는 서로 밀접하게 연관되어(息息相关) 있다.', '["两个","息息相关","的","这","完全不同","问题","是","毫无关系"]', '["这","两个","问题","是","息息相关","的"]', '["完全不同","毫无关系"]', NULL, '这两个问题是息息相关的', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 18), 'zh', '다음 중 \'마음이 서글프고 우울한 감정\'을 나타내는 단어는?', NULL, NULL, NULL, NULL, '["惆怅","委屈","忧郁","烦躁"]', '惆怅', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 19), 'zh', '다음 중 \'남의 비위를 맞추려고 아첨하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["迎合","奉承","谄媚","讨好"]', '谄媚', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 20), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 완고해서(顽固) 누구의 의견도 받아들이려 하지 않는다.', '["接受","开明","愿意","不","意见","他","非常","任何人的","顽固","谦虚"]', '["他","非常","顽固","不","愿意","接受","任何人的","意见"]', '["谦虚","开明"]', NULL, '他非常顽固不愿意接受任何人的意见', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 21), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'그 소식을 듣고 모두가 ______(분개하다)했다.\' = ______', NULL, NULL, NULL, NULL, NULL, '愤怒', '["愤怒","fennu"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 22), 'zh', '타일을 배열해서 문장을 완성하세요.', '이 정책은 현실적으로 실행 가능하지(可行) 않다고 생각한다.', '["认为","现实中","政策","我","完善","可行","合理","这个","在","不"]', '["我","认为","这个","政策","在","现实中","不","可行"]', '["合理","完善"]', NULL, '我认为这个政策在现实中不可行', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 23), 'zh', '타일을 배열해서 문장을 완성하세요.', '그는 재능을 숨기고(韬光养晦) 10년 동안 기회를 기다렸다.', '["的","锋芒毕露","韬光养晦","一鸣惊人","十年","他","了","等待","机会"]', '["他","韬光养晦","等待","了","十年","的","机会"]', '["锋芒毕露","一鸣惊人"]', NULL, '他韬光养晦等待了十年的机会', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 24), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

\'서로 없어서는 안 될 밀접한 관계\' = ______', NULL, NULL, NULL, NULL, NULL, '息息相关', '["息息相关","xixi xiangguan"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 25), 'zh', '다음 중 \'(자신이 한 행동을) 매우 후회하다\'를 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["惭愧","遗憾","后悔","懊悔"]', '懊悔', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 26), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

\'서로 공개적으로 경쟁하고 실력을 겨루다\' = ______', NULL, NULL, NULL, NULL, NULL, '明争暗斗', '["明争暗斗","mingzheng andou"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 27), 'zh', '다음 중 \'말하는 도중 바로 그 사람이 나타나다\'라는 속담은?', NULL, NULL, NULL, NULL, '["说曹操曹操到","无巧不成书","冤家路窄","不是冤家不聚头"]', '说曹操曹操到', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 28), 'zh', '타일을 배열해서 문장을 완성하세요.', '두 사람의 관계는 겉으로는 화목해 보이지만 실제로는 貌合神离이다.', '["关系","貌合神离","看起来","表面上","形影不离","实际上","的","两人","真心相待","很和谐"]', '["两人","的","关系","表面上","看起来","很和谐","实际上","貌合神离"]', '["真心相待","形影不离"]', NULL, '两人的关系表面上看起来很和谐实际上貌合神离', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 29), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'그의 말은 앞뒤가 맞지 않아 ______(모순이다)이었다.\' = ______', NULL, NULL, NULL, NULL, NULL, '矛盾', '["矛盾","maodun"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 30), 'zh', '다음 중 \'일을 중간에 그만두다, 용두사미가 되다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["一事无成","功亏一篑","浅尝辄止","半途而废"]', '半途而废', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 31), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'그녀는 사람들 앞에서 실수해서 몹시 ______(창피하다, 쑥스럽다)했다.\' = ______', NULL, NULL, NULL, NULL, NULL, '难堪', '["难堪","nankan"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 32), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

\'하나를 들으면 열을 안다, 매우 총명하다\' = ______', NULL, NULL, NULL, NULL, NULL, '闻一知十', '["闻一知十","wen yi zhi shi"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 33), 'zh', '타일을 배열해서 문장을 완성하세요.', '그의 태도가 너무 모호해서(含糊) 나는 어떻게 해야 할지 몰랐다.', '["了","态度","含糊","我","果断","他的","明确","该怎么办","太","不知道"]', '["他的","态度","太","含糊","了","我","不知道","该怎么办"]', '["明确","果断"]', NULL, '他的态度太含糊了我不知道该怎么办', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 34), 'zh', '타일을 배열해서 문장을 완성하세요.', '말하자마자 그가 나타났다, 정말 말하면 오는구나(说曹操曹操到).', '["他","真是","提到","他","恰好","刚","来了","说曹操曹操到","就","碰巧"]', '["刚","提到","他","他","就","来了","真是","说曹操曹操到"]', '["恰好","碰巧"]', NULL, '刚提到他他就来了真是说曹操曹操到', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 35), 'zh', '다음 중 \'겉으로만 화목한 척하다, 속으로는 사이가 나쁘다\'를 뜻하는 성어는?', NULL, NULL, NULL, NULL, '["口是心非","同床异梦","阳奉阴违","貌合神离"]', '貌合神离', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 36), 'zh', '빈칸에 알맞은 중국어 성어를 쓰세요.

\'대충 넘어가다, 마무리를 적당히 흐지부지하다\' = ______', NULL, NULL, NULL, NULL, NULL, '草草了事', '["草草了事","caocao liaoshi"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 37), 'zh', '다음 중 \'(윗사람이 아랫사람에게) 마음이 흐뭇하고 대견하다\'는 감정을 뜻하는 단어는?', NULL, NULL, NULL, NULL, '["欣喜","欣慰","满足","感慨"]', '欣慰', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 38), 'zh', '갑자기 모든 것이 이해되어 \'아, 그렇구나!\'라고 깨달았을 때 쓰는 성어는?', NULL, NULL, NULL, NULL, '["茅塞顿开","豁然开朗","如梦初醒","恍然大悟"]', '恍然大悟', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 39), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'글이나 말이 지나치게 길고 복잡해서 핵심을 파악하기 어렵다\' = ______', NULL, NULL, NULL, NULL, NULL, '累赘', '["累赘","leizhui"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 40), 'zh', '타일을 배열해서 문장을 완성하세요.', '그의 이야기는 앞뒤가 맞지 않아 모두가 의심했다(矛盾).', '["前后","大家","都","说法","疑心","信服","的","他","一致","矛盾","起了"]', '["他","的","说法","前后","矛盾","大家","都","起了","疑心"]', '["一致","信服"]', NULL, '他的说法前后矛盾大家都起了疑心', NULL),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 41), 'zh', '빈칸에 알맞은 중국어 단어를 쓰세요.

\'그는 매우 ______(완고하다, 고집스럽다)한 사람이라 어떤 충고도 받아들이지 않는다.\' = ______', NULL, NULL, NULL, NULL, NULL, '顽固', '["顽固","wangu"]'),
  ((SELECT quiz_id FROM competition_quiz_detail WHERE set_key = '02' AND level = 3 AND origin_id = 42), 'zh', '직장에서 \'업무 능력이 뛰어나고 출중하다\'는 뜻의 단어는?', NULL, NULL, NULL, NULL, '["出众","出色","卓越","优秀"]', '出众', NULL);

COMMIT;

-- 확인: select d.set_key, c.lang_code, count(*) from competition_quiz_detail d join competition_quiz_content c on c.quiz_id = d.quiz_id group by d.set_key, c.lang_code;
--       기대: 2세트 × 언어별 126건
