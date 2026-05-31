-- =============================================
-- [STEP 1] quiz_detail 컬럼 추가 (먼저 실행!)
-- =============================================
ALTER TABLE quiz_detail
  ADD COLUMN question TEXT,
  ADD COLUMN options JSON,
  ADD COLUMN difficulty INT,
  ADD COLUMN acceptable_answers JSON,
  ADD COLUMN hint TEXT;

-- =============================================
-- [STEP 2] 아래 qring_insert.sql 실행
-- =============================================
-- =============================================
-- qring_db INSERT 스크립트
-- =============================================

SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. difficulty_level
-- =============================================
INSERT INTO difficulty_level (level_code, level_name, level_desc) VALUES
  (1, '쉬움', '기초 영어 단어 수준'),
  (2, '보통', '문장 완성 수준'),
  (3, '어려움', '문맥 이해 수준');

-- =============================================
-- 2. content_category
-- =============================================
INSERT INTO content_category (category_id, category_name, display_order) VALUES
  (1, '짝사랑', 1),
  (2, '드라마', 2),
  (3, '스릴러', 3),
  (4, '추리물', 4),
  (5, '특이한연애썰', 5),
  (6, '연애갈등', 6);

-- =============================================
-- 3. content, chapter, script, quiz_detail
-- =============================================
-- ALTER TABLE quiz_detail ADD COLUMN question TEXT;
-- ALTER TABLE quiz_detail ADD COLUMN options JSON;
-- ALTER TABLE quiz_detail ADD COLUMN difficulty INT;
-- ALTER TABLE quiz_detail ADD COLUMN acceptable_answers JSON;

-- ----- [짝사랑] 도서관 좌석번호 64번 -----
INSERT INTO content (content_id, title, category_id, level_code, total_chapters, status, thumbnail_url) VALUES
  (1, '도서관 좌석번호 64번', 1, 2, 1, 'ACTIVE', NULL);

INSERT INTO chapter (chapter_id, chapter_num, chapter_title, required_points, content_id) VALUES
  (1, 1, '도서관 좌석번호 64번', 0, 1);

INSERT INTO script (script_id, script_content, sequence_num, has_options, character_name, chapter_id, option_id) VALUES
  (1, '나 대학교 다닐 때 시험기간마다 도서관 3층 64번 자리에만 앉았음.', 1, b'0', NULL, 1, NULL),
  (2, '창가 자리라서 햇빛도 잘 들고 에어컨 바람도 안 와서 딱 좋았거든.', 2, b'0', NULL, 1, NULL),
  (3, '근데 어느 날부터 그 자리에 항상 먼저 와있는 애가 있는 거임.', 3, b'0', NULL, 1, NULL),
  (4, '65번 자리에 앉아서 책 읽고 있더라고.', 4, b'0', NULL, 1, NULL),
  (5, '처음엔 짜증났음. 내 자리 옆에 왜 앉냐고.', 5, b'0', NULL, 1, NULL),
  (6, '그런데 그 애가 책상에 A4 용지 하나를 올려두고 가는 거야.', 6, b'0', NULL, 1, NULL),
  (7, '거기에 연필로 ''64번 자리 비워둘게요'' 이렇게 적어둔 거임.', 7, b'0', NULL, 1, NULL),
  (8, '뭔가 이상하다 싶었는데 일단 고맙긴 해서 그냥 앉았음.', 8, b'0', NULL, 1, NULL),
  (9, '다음 날도 똑같더라고. 65번에 그 애 있고, 64번엔 쪽지.', 9, b'0', NULL, 1, NULL),
  (10, '근데 이번엔 ''오늘 비 온대요. 우산 챙기세요'' 이렇게 적혀있음.', 10, b'0', NULL, 1, NULL),
  (11, '이게 뭐지? 싶어서 슬쩍 옆을 봤는데 그 애가 책에 집중하고 있어서 말 걸기 애매했음.', 11, b'0', NULL, 1, NULL),
  (12, '그날 정말 비 왔음. 우산 안 가져갔으면 비 맞을 뻔했어.', 12, b'0', NULL, 1, NULL),
  (13, '그 다음 날부터 호기심이 생기기 시작함.', 13, b'0', NULL, 1, NULL),
  (14, '쪽지 내용이 매일 달라지거든.', 14, b'0', NULL, 1, NULL),
  (15, '''오늘 학식 돈까스 맛없다고 하네요'', ''3층 화장실 휴지 떨어졌어요''', 15, b'0', NULL, 1, NULL),
  (16, '이런 식으로 은근 유용한 정보들이었음.', 16, b'0', NULL, 1, NULL),
  (17, '한 달 정도 지났을 때 그 애 얼굴을 제대로 본 적이 없다는 걸 깨달았어.', 17, b'0', NULL, 1, NULL),
  (18, '항상 내가 올 때는 이미 책 보고 있고, 내가 자리 뜰 때는 아직 앉아있고.', 18, b'0', NULL, 1, NULL),
  (19, '궁금해서 일부러 일찍 가봤는데도 이미 앉아있더라.', 19, b'0', NULL, 1, NULL),
  (20, '도대체 몇 시에 오는 거야?', 20, b'0', NULL, 1, NULL),
  (21, '어느 날 쪽지에 ''시험 잘 보세요. 항상 열심히 하시는 모습 멋있어요'' 이렇게 적혀있는 거임.', 21, b'0', NULL, 1, NULL),
  (22, '이때 뭔가 심장이 두근거렸어.', 22, b'0', NULL, 1, NULL),
  (23, '그래서 용기내서 쪽지 밑에 작은 글씨로 ''고마워요'' 이렇게 적어뒀음.', 23, b'0', NULL, 1, NULL),
  (24, '다음 날 가보니까 내 글씨 옆에 이모티콘 하나 그려져 있었어.', 24, b'0', NULL, 1, NULL),
  (25, '웃는 얼굴이었는데 되게 귀엽게 그렸더라고.', 25, b'0', NULL, 1, NULL),
  (26, '그날부터 우리 쪽지 주고받기 시작함.', 26, b'0', NULL, 1, NULL),
  (27, '근데 진짜 대화는 안 해. 계속 쪽지로만.', 27, b'0', NULL, 1, NULL),
  (28, '''오늘 뭐 공부해요?'' ''전공이 뭐예요?'' 이런 것들.', 28, b'0', NULL, 1, NULL),
  (29, '알고 보니까 그 애는 미술과였음.', 29, b'0', NULL, 1, NULL),
  (30, '그래서 쪽지에 작은 그림도 그려주고 글씨도 예쁘게 썼던 거더라.', 30, b'0', NULL, 1, NULL),
  (31, '나는 경영학과라고 했더니 다음 날 쪽지에 작은 계산기 그림이 그려져 있었어 ㅋㅋㅋ', 31, b'0', NULL, 1, NULL),
  (32, '시험이 끝나고 방학이 되니까 도서관에 갈 일이 없어짐.', 32, b'0', NULL, 1, NULL),
  (33, '근데 그 애가 궁금한 거야.', 33, b'0', NULL, 1, NULL),
  (34, '그래서 개강하고 바로 도서관 갔는데 65번 자리가 비어있더라.', 34, b'0', NULL, 1, NULL),
  (35, '64번에 쪽지도 없고.', 35, b'0', NULL, 1, NULL),
  (36, '혹시 시간표가 바뀐 건가 싶어서 계속 다녀봤는데 한 주 내내 없었어.', 36, b'0', NULL, 1, NULL),
  (37, '그러다가 일주일 뒤에 64번 자리에 쪽지 하나가 놓여있는 거 발견함.', 37, b'0', NULL, 1, NULL),
  (38, '''미안해요. 교환학생으로 가게 됐어요. 1년 후에 돌아와요''', 38, b'0', NULL, 1, NULL),
  (39, '그 밑에 작은 비행기 그림하나 그려져 있었음.', 39, b'0', NULL, 1, NULL),
  (40, '진짜 허무했어. 얼굴도 제대로 못 본 채로 헤어진 거잖아.', 40, b'0', NULL, 1, NULL),
  (41, '그 뒤로 도서관 가면 65번 자리만 계속 쳐다봤어.', 41, b'0', NULL, 1, NULL),
  (42, '1년이 지나고 그 애가 돌아왔는지 확인하러 갔는데 또 없더라.', 42, b'0', NULL, 1, NULL),
  (43, '근데 며칠 뒤에 도서관 앞 게시판에 전시회 포스터 하나가 붙어있는 거 봤어.', 43, b'0', NULL, 1, NULL),
  (44, '미술과 졸업전시회였는데 포스터 한쪽 모서리에 작은 글씨로 ''64''라고 적혀있었음.', 44, b'0', NULL, 1, NULL),
  (45, '그 글씨체가 쪽지랑 똑같았어.', 45, b'0', NULL, 1, NULL),
  (46, '전시회 가봤는데 작품 중에 도서관 그림이 하나 있더라고.', 46, b'0', NULL, 1, NULL),
  (47, '3층 창가 자리를 그린 건데 64번 책상 위에 작은 쪽지들이 쌓여있는 그림이었어.', 47, b'0', NULL, 1, NULL),
  (48, '그림 제목이 ''말 못한 인사''였음.', 48, b'0', NULL, 1, NULL),
  (49, '그때 옆에서 누군가 말을 거는 거야. ''그 자리 아세요?''', 49, b'0', NULL, 1, NULL),
  (50, '돌아보니까 익숙한데 낯선 얼굴이었어.', 50, b'0', NULL, 1, NULL),
  (51, '1년 동안 매일 옆에 앉았던 그 애였음.', 51, b'0', NULL, 1, NULL),
  (52, '''1년 내내 인사하고 싶었는데 용기가 안 났어요''라고 하더라.', 52, b'0', NULL, 1, NULL),
  (53, '나도 똑같았다고 했어.', 53, b'0', NULL, 1, NULL),
  (54, '그 애가 웃으면서 ''이제 쪽지 말고 진짜 대화할 수 있을까요?'' 이러는데 목소리가 떨리고 있었음.', 54, b'0', NULL, 1, NULL),
  (55, '나도 떨렸어.', 55, b'0', NULL, 1, NULL),
  (56, '지금도 그 애랑 만나고 있음.', 56, b'0', NULL, 1, NULL),
  (57, '아직도 가끔 쪽지로 대화해 ㅋㅋㅋ', 57, b'0', NULL, 1, NULL);

INSERT INTO quiz_detail (quiz_id, correct_answer, explanation, quiz_type, content_id, script_id, question, options, difficulty, acceptable_answers, hint) VALUES
  (1, 'annoyed', '''annoyed''는 무언가에 짜증이 나거나 성가실 때 쓰는 표현이야. 내 자리 옆에 누가 앉아서 기분이 별로였던 상황이지.', 'multiple_choice', 1, 11, '다음 중 ''짜증나다, 성가시다''의 영어 표현으로 알맞은 것은?', '["annoyed", "amazed", "admired", "attended"]', 1, '["annoyed"]', 'a로 시작하는 7글자 단어'),
  (2, 'left behind', '''leave behind''는 무언가를 그 자리에 남겨두고 떠난다는 뜻의 구동사야.', 'subjective', 1, 11, '''~을 남겨두다/놓고 가다'' = l____ b______

She ______ ______ a note on my desk.
(뜻: 그녀가 내 책상에 쪽지를 남겨두고 갔다)', NULL, 2, '["left behind", "Left behind"]', '떠나다(leave) + 뒤에(behind), 뭔가를 그 자리에 두고 떠날 때 써'),
  (3, 'awkward to start up', '''strike up a conversation''이나 ''start up a conversation''은 대화를 시작한다는 뜻이야. 상황이 어색해서 말 걸기 힘들 때 쓰는 표현이지.', 'subjective', 1, 11, '''말 걸기 애매했다''

She was so focused on her book that it felt ______ to ______ a conversation.
(뜻: 대화를 시작하다)', NULL, 3, '["awkward to start up", "awkward to strike up", "uncomfortable to start up", "uncomfortable to strike up", "weird to start", "strange to start"]', 's___ u_ (2단어, 엔진을 시동 걸 때도 쓰는 표현)'),
  (4, 'curiosity', '''curiosity''는 호기심을 뜻해. 쪽지 내용이 궁금해서 생긴 마음이지.', 'multiple_choice', 1, 22, '다음 중 ''호기심''을 뜻하는 영어 단어는?', '["curiosity", "confusion", "confidence", "consideration"]', 1, '["curiosity"]', 'c로 시작하는 9글자, 고양이를 죽인다는 속담에 나오는 단어'),
  (5, 'realized', '''realize''는 깨닫다, 알아차리다라는 뜻이야. 영국식으로는 ''realise''라고 쓰기도 해.', 'subjective', 1, 22, '''깨닫다'' = r______

I ______ that I had never seen her face properly.
(뜻: 그녀의 얼굴을 제대로 본 적이 없다는 걸 깨달았다)', NULL, 2, '["realized", "realised", "Realized", "Realised"]', 're로 시작, ''알아차리다/인식하다''라는 뜻 ?'),
  (6, 'on purpose', '''on purpose''는 의도적으로, 일부러라는 뜻이야. deliberately나 intentionally도 같은 의미로 쓸 수 있어.', 'subjective', 1, 22, '''일부러 일찍 가봤다''

I went to the library ______ ______ to see when she arrived.
(뜻: 의도적으로, 특별히)', NULL, 3, '["on purpose", "deliberately", "intentionally"]', 'o_ p_______ (2단어, 목적을 가지고 할 때 쓰는 표현)'),
  (7, 'gather courage', '''gather courage''는 용기를 모아내다, 용기내다라는 뜻이야. 쪽지에 글을 쓰기 위해 용기를 낸 상황이지.', 'multiple_choice', 1, 33, '다음 중 ''용기를 내다''의 영어 표현으로 알맞은 것은?', '["gather courage", "collect money", "receive support", "follow advice"]', 1, '["gather courage"]', 'g로 시작하는 단어와 함께 쓰이는 표현'),
  (8, 'exchanging', '''exchange''는 서로 주고받다라는 뜻으로, 메시지나 정보를 상호 교환할 때 쓰는 자연스러운 표현이야.', 'subjective', 1, 33, '''메시지를 주고받다'' = e_______ messages

They started _______ notes from that day on.
(뜻: 그날부터 쪽지를 주고받기 시작했다)', NULL, 2, '["exchanging", "Exchanging"]', 'e로 시작, ''change''와 비슷한 뜻이지만 서로 주고받는다는 의미'),
  (9, 'exchanging', '''exchange notes''는 쪽지를 주고받는다는 뜻이야. passing, trading도 비슷한 의미로 쓸 수 있어.', 'subjective', 1, 33, '''쪽지 주고받기''

We started _______ _______ notes.
(뜻: ~을 주고받다)', NULL, 3, '["exchanging", "passing", "trading", "sharing"]', 'e_______ (시소처럼 서로 주고받는 느낌)'),
  (10, 'disappointed', '''disappointed''는 기대했던 일이 이뤄지지 않아서 허무하고 실망스러울 때 쓰는 표현이야.', 'multiple_choice', 1, 44, '다음 중 ''허무한, 실망스러운''을 뜻하는 영어 표현은?', '["disappointed", "determined", "delighted", "developed"]', 1, '["disappointed"]', 'd로 시작하는 11글자 단어'),
  (11, 'hollow', '''hollow''는 물리적으로 속이 비었다는 뜻뿐만 아니라 감정적으로 공허하고 허무하다는 뜻으로도 쓰여.', 'subjective', 1, 44, '''허무한, 공허한'' = h______

It felt so ______ to part ways without even seeing each other properly.
(뜻: 제대로 서로를 보지도 못하고 헤어져서 너무 허무했다)', NULL, 2, '["hollow", "Hollow"]', 'h로 시작, ''속이 빈'' 나무를 떠올려봐 ?'),
  (12, 'empty', '''empty''는 허무하고 공허한 감정을 표현할 때 쓰여. hollow, disappointed도 비슷한 의미로 사용할 수 있어.', 'subjective', 1, 44, '''허무했다''는 감정을 영어로 표현해보세요.

I felt so ______.
(뜻: 공허하고 실망스러운 감정)', NULL, 3, '["empty", "hollow", "disappointed", "deflated", "let down"]', 'e_____ (6글자, ''빈''이라는 뜻도 있는 단어)'),
  (13, 'occasionally', '''occasionally''는 가끔, 때때로라는 뜻으로 정기적이지 않고 이따금씩 일어나는 상황을 표현해.', 'multiple_choice', 1, 57, '다음 중 ''가끔, 때때로''를 의미하는 영어 표현으로 알맞은 것은?', '["occasionally", "obviously", "officially", "originally"]', 1, '["occasionally"]', 'o로 시작하는 부사, ''once in a while''과 비슷한 뜻'),
  (14, 'occasionally', '''occasionally''는 ''가끔, 때때로''라는 뜻으로 빈도를 나타내는 부사야.', 'subjective', 1, 57, '''가끔씩'' = o_________

We still ________ communicate through notes.
(뜻: 우리는 아직도 가끔 쪽지로 소통해)', NULL, 2, '["occasionally", "Occasionally"]', '한 번씩, 때때로... o로 시작하는 부사'),
  (15, 'exchange', '''exchange notes''는 쪽지를 주고받는다는 뜻이야. pass notes, send notes도 자연스러운 표현이고.', 'subjective', 1, 57, '''가끔 쪽지로 대화한다''

We still ______ ______ notes sometimes.
(뜻: 서로 주고받다)', NULL, 3, '["exchange", "pass", "send", "write", "share"]', 'e_______');

-- ----- [드라마] 3번 관람석 -----
INSERT INTO content (content_id, title, category_id, level_code, total_chapters, status, thumbnail_url) VALUES
  (2, '3번 관람석', 2, 2, 1, 'ACTIVE', NULL);

INSERT INTO chapter (chapter_id, chapter_num, chapter_title, required_points, content_id) VALUES
  (2, 1, '3번 관람석', 0, 2);

INSERT INTO script (script_id, script_content, sequence_num, has_options, character_name, chapter_id, option_id) VALUES
  (58, '연우는 동네 서점에서 일했다.', 1, b'0', NULL, 2, NULL),
  (59, '작은 서점이라 손님이 많지 않았다.', 2, b'0', NULL, 2, NULL),
  (60, '그런데 한 명만 매일 왔다.', 3, b'0', NULL, 2, NULL),
  (61, '학교 끝나고 오는 고등학생.', 4, b'0', NULL, 2, NULL),
  (62, '항상 같은 자리에 앉았다.', 5, b'0', NULL, 2, NULL),
  (63, '창가 쪽 3번 관람석이라고 적힌 의자.', 6, b'0', NULL, 2, NULL),
  (64, '책을 읽는 게 아니라 숙제를 했다.', 7, b'0', NULL, 2, NULL),
  (65, '수학 문제집을 펼쳐놓고 연필로 끄적거렸다.', 8, b'0', NULL, 2, NULL),
  (66, '가끔 머리를 쥐어뜯기도 했다.', 9, b'0', NULL, 2, NULL),
  (67, '연우는 그 학생을 ''3번 학생''이라고 불렀다.', 10, b'0', NULL, 2, NULL),
  (68, '혼자서.', 11, b'0', NULL, 2, NULL),
  (69, '어느 날 그 학생이 울고 있었다.', 12, b'0', NULL, 2, NULL),
  (70, '조용히 훌쩍거리면서 문제집을 보고 있었다.', 13, b'0', NULL, 2, NULL),
  (71, '연우가 티슈를 갖다 줬다.', 14, b'0', NULL, 2, NULL),
  (72, '학생이 고맙다고 작게 말했다.', 15, b'0', NULL, 2, NULL),
  (73, '그 다음 날부터 인사를 했다.', 16, b'0', NULL, 2, NULL),
  (74, '들어올 때 고개를 살짝 숙이고 나갈 때도.', 17, b'0', NULL, 2, NULL),
  (75, '한 달쯤 지났을까.', 18, b'0', NULL, 2, NULL),
  (76, '그 학생이 안 왔다.', 19, b'0', NULL, 2, NULL),
  (77, '3번 자리가 텅 비어 있었다.', 20, b'0', NULL, 2, NULL),
  (78, '이틀째도 안 왔다.', 21, b'0', NULL, 2, NULL),
  (79, '연우는 자꾸 창가를 쳐다봤다.', 22, b'0', NULL, 2, NULL),
  (80, '일주일이 지나서야 왔다.', 23, b'0', NULL, 2, NULL),
  (81, '그런데 뭔가 달랐다.', 24, b'0', NULL, 2, NULL),
  (82, '머리를 짧게 잘랐고 교복이 아닌 검은 옷을 입고 있었다.', 25, b'0', NULL, 2, NULL),
  (83, '평소처럼 3번 자리에 앉았는데 숙제를 안 꺼냈다.', 26, b'0', NULL, 2, NULL),
  (84, '그냥 멍하니 창밖만 보고 있었다.', 27, b'0', NULL, 2, NULL),
  (85, '연우가 다가가서 물었다.', 28, b'0', NULL, 2, NULL),
  (86, '괜찮냐고.', 29, b'0', NULL, 2, NULL),
  (87, '학생이 고개를 들었다.', 30, b'0', NULL, 2, NULL),
  (88, '눈이 빨갛게 부어 있었다.', 31, b'0', NULL, 2, NULL),
  (89, '아버지가 돌아가셨다고 말했다.', 32, b'0', NULL, 2, NULL),
  (90, '갑자기 쓰러지셨다고.', 33, b'0', NULL, 2, NULL),
  (91, '연우는 뭐라고 말해야 할지 몰랐다.', 34, b'0', NULL, 2, NULL),
  (92, '그냥 옆에 앉았다.', 35, b'0', NULL, 2, NULL),
  (93, '학생이 또 말했다.', 36, b'0', NULL, 2, NULL),
  (94, '여기서 숙제하면서 아버지 생각이 많이 났다고.', 37, b'0', NULL, 2, NULL),
  (95, '아버지도 수학을 좋아하셨다고.', 38, b'0', NULL, 2, NULL),
  (96, '연우가 물었다.', 39, b'0', NULL, 2, NULL),
  (97, '그래서 수학을 하는 거냐고.', 40, b'0', NULL, 2, NULL),
  (98, '학생이 고개를 저었다.', 41, b'0', NULL, 2, NULL),
  (99, '사실 수학을 제일 싫어한다고.', 42, b'0', NULL, 2, NULL),
  (100, '그런데 아버지가 수학 선생님이셨다고.', 43, b'0', NULL, 2, NULL),
  (101, '아버지 책상에서 이 문제집을 찾았다고.', 44, b'0', NULL, 2, NULL),
  (102, '첫 페이지에 편지가 끼워져 있었다고.', 45, b'0', NULL, 2, NULL),
  (103, '학생이 가방에서 편지를 꺼냈다.', 46, b'0', NULL, 2, NULL),
  (104, '연우에게 보여줬다.', 47, b'0', NULL, 2, NULL),
  (105, '''수학이 어려워도 포기하지 마. 아빠가 항상 응원할게. - 사랑하는 딸 지은이에게''', 48, b'0', NULL, 2, NULL),
  (106, '편지 아래쪽에 작은 글씨로 또 적혀 있었다.', 49, b'0', NULL, 2, NULL),
  (107, '''3번 자리에 앉으면 집중이 잘 될 거야.''', 50, b'0', NULL, 2, NULL),
  (108, '연우가 깨달았다.', 51, b'0', NULL, 2, NULL),
  (109, '아버지가 이 서점을 알고 있었다는 걸.', 52, b'0', NULL, 2, NULL),
  (110, '지은이가 여기서 공부한다는 걸 아셨다는 걸.', 53, b'0', NULL, 2, NULL),
  (111, '지은이도 같은 생각이었나 보다.', 54, b'0', NULL, 2, NULL),
  (112, '눈물을 흘리면서 말했다.', 55, b'0', NULL, 2, NULL),
  (113, '아버지가 여기까지 찾아오셨을지도 모른다고.', 56, b'0', NULL, 2, NULL),
  (114, '딸이 어디서 공부하는지 궁금해서.', 57, b'0', NULL, 2, NULL),
  (115, '연우는 그제서야 기억했다.', 58, b'0', NULL, 2, NULL),
  (116, '한 달 전쯤 한 번 온 중년 남자를.', 59, b'0', NULL, 2, NULL),
  (117, '지은이와 닮은 얼굴이었다.', 60, b'0', NULL, 2, NULL),
  (118, '3번 자리를 유심히 보더니 아무 말 없이 나갔던.', 61, b'0', NULL, 2, NULL);

INSERT INTO quiz_detail (quiz_id, correct_answer, explanation, quiz_type, content_id, script_id, question, options, difficulty, acceptable_answers, hint) VALUES
  (16, 'occasionally', '''occasionally''는 가끔씩, 때때로라는 뜻으로 정기적이지 않고 이따금씩 일어나는 상황을 표현해.', 'multiple_choice', 2, 69, '다음 중 ''가끔, 때때로''를 뜻하는 영어 표현은?', '["occasionally", "obviously", "officially", "originally"]', 1, '["occasionally"]', 'o로 시작하는 12글자 표현'),
  (17, 'scribbling', '''scribble''은 급히 또는 대충 끄적거리며 쓰는 것을 의미해. 학생이 숙제하며 연필로 끄적거리는 상황에 딱 맞는 표현이야.', 'subjective', 2, 69, '''끄적거리다'' = s______

He was ______ notes in the margin.
(뜻: 그는 여백에 끄적거리고 있었다)', NULL, 2, '["scribbling", "Scribbling", "scrawling", "Scrawling"]', 's로 시작, ''긁다''라는 뜻도 있어 ?'),
  (18, 'pulling', '''pulling his hair out''은 스트레스나 좌절감에 머리를 쥐어뜯는다는 관용적 표현이야. tearing이나 tugging도 비슷한 의미로 쓸 수 있어.', 'subjective', 2, 69, '수학 문제가 어려워서 ''머리를 쥐어뜯었다''

He was ______ his hair out over the difficult math problems.
(뜻: 스트레스를 받아 머리를 뜯다)', NULL, 3, '["pulling", "tearing", "tugging"]', 'p_____ (털이나 머리카락을 뽑는다는 동사)'),
  (19, 'empty', '''empty''는 텅 비어있다는 뜻이야. 학생이 안 와서 자리가 완전히 비어있는 상황이지.', 'multiple_choice', 2, 81, '다음 중 ''완전히 비어 있는''을 뜻하는 영어 표현은?', '["empty", "early", "equal", "eager"]', 1, '["empty"]', 'e로 시작하는 5글자 단어'),
  (20, 'empty', '''empty''는 아무것도 없이 완전히 비어있는 상태를 나타내는 기본적인 형용사야.', 'subjective', 2, 81, '''텅 빈'' = e______

The seat was completely ______.
(뜻: 그 자리는 완전히 비어있었다)', NULL, 2, '["empty", "Empty"]', 'e로 시작, ''빈 집''을 영어로 하면 _____ house'),
  (21, 'glancing', '''glance''는 빠르고 짧게 힐끗 보는 걸 뜻해. 계속 창가 쪽을 신경 쓰며 보는 상황에 딱 맞아.', 'subjective', 2, 81, '''자꾸 창가를 쳐다봤다''

Yeon-woo kept ______ toward the window.
(뜻: 힐끗힐끗 보다, 슬쩍 보다)', NULL, 3, '["glancing", "looking", "gazing", "staring", "peeking"]', 'g_____ (6글자, 빠르고 짧게 보는 동작)'),
  (22, 'swollen', '''swollen''은 부은 상태를 나타내는 말이야. 울어서 눈이 부었을 때 쓰지.', 'multiple_choice', 2, 93, '다음 중 ''부은, 붓다''의 영어 표현으로 알맞은 것은?', '["swollen", "sleepy", "smooth", "serious"]', 1, '["swollen"]', 's로 시작하는 단어, 벌에 쏘이면 이렇게 됨'),
  (23, 'swollen', '''swollen''은 부어오른 상태를 나타내는 형용사야. 울거나 다쳤을 때 자주 쓰이지.', 'subjective', 2, 93, '''부어오른'' = s______

Her eyes were ______ from crying.
(뜻: 그녀의 눈은 울어서 부어 있었다)', NULL, 2, '["swollen", "Swollen"]', 's로 시작, 벌에 쏘였을 때도 이렇게 돼 ?'),
  (24, 'collapsed down', '''collapse''나 ''pass out''은 갑작스럽게 의식을 잃고 쓰러지는 걸 표현할 때 쓰는 자연스러운 영어야.', 'subjective', 2, 93, '''갑자기 쓰러지다''

He suddenly ______ ______.
(뜻: 의식을 잃고 넘어지다)', NULL, 3, '["collapsed down", "passed out", "fell down", "fainted away", "collapsed", "blacked out"]', 'c_______ d___ (의식과 관련된 표현, 기절하다)'),
  (25, 'give up', '''give up''은 포기하다라는 뜻이야. 아버지가 딸에게 수학이 어려워도 포기하지 말라고 격려하는 상황이지.', 'multiple_choice', 2, 105, '다음 중 ''포기하다''의 영어 표현으로 알맞은 것은?', '["give up", "get up", "grow up", "go up"]', 1, '["give up"]', 'g로 시작하는 구동사 (give + up)'),
  (26, 'tucked', '''tuck''은 무언가를 살살 끼워 넣거나 집어넣는다는 뜻이야. ''tucked between''은 사이에 끼워져 있다는 표현이지.', 'subjective', 2, 105, '''끼워 넣다'' = t______

A letter was ______ between the pages.
(뜻: 편지가 페이지 사이에 끼워져 있었다)', NULL, 2, '["tucked", "Tucked"]', 't로 시작, ''집어넣다/꽂다''라는 뜻'),
  (27, 'give up', '''give up''은 포기하다라는 뜻이야. quit이나 surrender도 같은 의미로 쓸 수 있어.', 'subjective', 2, 105, '''포기하지 마''

Don''t ______ ______.
(뜻: ~을 그만두다, 포기하다)', NULL, 3, '["give up", "quit", "surrender"]', 'g___ u_ (2단어, 담배를 끊을 때도 쓰는 표현)'),
  (28, 'carefully', '''carefully''는 주의 깊게, 유심히 관찰하거나 행동할 때 쓰는 부사야. 아버지가 딸의 자리를 신중하게 살펴본 상황이지.', 'multiple_choice', 2, 118, '다음 중 ''유심히, 주의 깊게''를 뜻하는 영어 부사는?', '["carefully", "certainly", "currently", "completely"]', 1, '["carefully"]', 'c로 시작하는 9글자 부사'),
  (29, 'inspected', '''inspect''는 세심하게 관찰하다/살펴보다라는 뜻이야. 아버지가 딸의 자리를 조용히 확인하는 감동적인 장면을 표현할 때 쓸 수 있어.', 'subjective', 2, 118, '''유심히 관찰하다'' = i______

He ______ the seat #3 carefully and left without saying a word.
(뜻: 그는 3번 자리를 유심히 살펴보고는 아무 말 없이 떠났다)', NULL, 2, '["inspected", "examined", "observed", "studied"]', '눈으로 자세히 들여다본다는 뜻, ''in-''로 시작해'),
  (30, 'gazing intently', '''gaze intently''는 관심을 가지고 유심히 바라본다는 뜻이야. stare intently, look intently도 같은 의미로 쓸 수 있어.', 'subjective', 2, 118, '''유심히 보다''

He was _______ _______ at seat number 3, then left without saying a word.
(뜻: 관심 있게 자세히 보다)', NULL, 3, '["gazing intently", "staring intently", "looking intently", "gazing carefully", "staring carefully", "looking carefully", "gazing closely", "staring closely", "looking closely"]', 'g___ at (2단어, 응시하다/뚫어져라 보다)');

-- ----- [스릴러] 끊어진 전화선 -----
INSERT INTO content (content_id, title, category_id, level_code, total_chapters, status, thumbnail_url) VALUES
  (3, '끊어진 전화선', 3, 2, 1, 'ACTIVE', NULL);

INSERT INTO chapter (chapter_id, chapter_num, chapter_title, required_points, content_id) VALUES
  (3, 1, '끊어진 전화선', 0, 3);

INSERT INTO script (script_id, script_content, sequence_num, has_options, character_name, chapter_id, option_id) VALUES
  (119, '민혜는 콜센터에서 일한다.', 1, b'0', NULL, 3, NULL),
  (120, '야간 근무를 맡은 지 3개월째.', 2, b'0', NULL, 3, NULL),
  (121, '새벽 시간대라 전화가 거의 안 온다.', 3, b'0', NULL, 3, NULL),
  (122, '대부분 술 취한 사람들의 장난 전화나 잘못 건 전화.', 4, b'0', NULL, 3, NULL),
  (123, '그런데 지난주부터 이상한 전화가 오기 시작했다.', 5, b'0', NULL, 3, NULL),
  (124, '새벽 2시 47분. 정확히 같은 시각.', 6, b'0', NULL, 3, NULL),
  (125, '전화벨이 울린다.', 7, b'0', NULL, 3, NULL),
  (126, '받으면 숨소리만 들린다.', 8, b'0', NULL, 3, NULL),
  (127, '"안녕하세요, 고객센터입니다" 말해도 대답이 없다.', 9, b'0', NULL, 3, NULL),
  (128, '그냥 후-후- 하는 숨소리.', 10, b'0', NULL, 3, NULL),
  (129, '처음에는 장난 전화려니 했다.', 11, b'0', NULL, 3, NULL),
  (130, '근데 매일 같은 시간이었다.', 12, b'0', NULL, 3, NULL),
  (131, '2시 47분. 정확히.', 13, b'0', NULL, 3, NULL),
  (132, '오늘도 그 시간이 다가온다.', 14, b'0', NULL, 3, NULL),
  (133, '시계를 보니 2시 46분.', 15, b'0', NULL, 3, NULL),
  (134, '민혜는 손에 땀이 났다.', 16, b'0', NULL, 3, NULL),
  (135, '왜 이렇게 긴장하고 있는 걸까?', 17, b'0', NULL, 3, NULL),
  (136, '그냥 끊어버리면 되는데.', 18, b'0', NULL, 3, NULL),
  (137, '2시 47분. 벨이 울린다.', 19, b'0', NULL, 3, NULL),
  (138, '손이 떨렸지만 받았다.', 20, b'0', NULL, 3, NULL),
  (139, '"안녕하세요..."', 21, b'0', NULL, 3, NULL),
  (140, '역시 숨소리만 들린다.', 22, b'0', NULL, 3, NULL),
  (141, '그런데 오늘은 뭔가 달랐다.', 23, b'0', NULL, 3, NULL),
  (142, '숨소리 뒤로 다른 소리가 섞여 있었다.', 24, b'0', NULL, 3, NULL),
  (143, '뚝뚝뚝.', 25, b'0', NULL, 3, NULL),
  (144, '물 떨어지는 소리? 아니다.', 26, b'0', NULL, 3, NULL),
  (145, '타자 치는 소리였다.', 27, b'0', NULL, 3, NULL),
  (146, '누군가 키보드를 치고 있었다.', 28, b'0', NULL, 3, NULL),
  (147, '"거기 누구세요?" 민혜가 물었다.', 29, b'0', NULL, 3, NULL),
  (148, '타자 소리가 멈췄다.', 30, b'0', NULL, 3, NULL),
  (149, '그리고 목소리가 들렸다.', 31, b'0', NULL, 3, NULL),
  (150, '"도와줘."', 32, b'0', NULL, 3, NULL),
  (151, '여자 목소리였다. 떨리고 있었다.', 33, b'0', NULL, 3, NULL),
  (152, '민혜는 등에 소름이 돋았다.', 34, b'0', NULL, 3, NULL),
  (153, '"어디에 계세요? 무슨 일이에요?"', 35, b'0', NULL, 3, NULL),
  (154, '"찾지 마. 위험해."', 36, b'0', NULL, 3, NULL),
  (155, '그리고 전화가 끊어졌다.', 37, b'0', NULL, 3, NULL),
  (156, '발신번호를 확인했다.', 38, b'0', NULL, 3, NULL),
  (157, '표시되지 않음.', 39, b'0', NULL, 3, NULL),
  (158, '민혜는 팀장에게 보고했다.', 40, b'0', NULL, 3, NULL),
  (159, '"그냥 장난 전화겠지. 신경 쓰지 마."', 41, b'0', NULL, 3, NULL),
  (160, '하지만 신경 쓰이지 않을 수가 없었다.', 42, b'0', NULL, 3, NULL),
  (161, '그 여자의 목소리가 계속 맴돌았다.', 43, b'0', NULL, 3, NULL),
  (162, '다음 날 밤.', 44, b'0', NULL, 3, NULL),
  (163, '2시 47분. 또 전화가 왔다.', 45, b'0', NULL, 3, NULL),
  (164, '이번에는 바로 그 목소리가 들렸다.', 46, b'0', NULL, 3, NULL),
  (165, '"왜 찾으려고 해?"', 47, b'0', NULL, 3, NULL),
  (166, '민혜는 당황했다.', 48, b'0', NULL, 3, NULL),
  (167, '"찾는 게 아니라... 도와주려고..."', 49, b'0', NULL, 3, NULL),
  (168, '"내 뒤에 있어. 지금."', 50, b'0', NULL, 3, NULL),
  (169, '민혜는 뒤를 돌아봤다.', 51, b'0', NULL, 3, NULL),
  (170, '아무도 없었다.', 52, b'0', NULL, 3, NULL),
  (171, '텅 빈 사무실.', 53, b'0', NULL, 3, NULL),
  (172, '"장난하지 마세요."', 54, b'0', NULL, 3, NULL),
  (173, '"장난이 아니야. 정말로 뒤에 있어."', 55, b'0', NULL, 3, NULL),
  (174, '전화기 너머로 또 다른 소리가 들렸다.', 56, b'0', NULL, 3, NULL),
  (175, '의자 바퀴 굴러가는 소리.', 57, b'0', NULL, 3, NULL),
  (176, '민혜는 고개를 들었다.', 58, b'0', NULL, 3, NULL),
  (177, '자신의 의자가 천천히 움직이고 있었다.', 59, b'0', NULL, 3, NULL),
  (178, '아무도 건드리지 않았는데.', 60, b'0', NULL, 3, NULL),
  (179, '전화기에서 웃음소리가 들렸다.', 61, b'0', NULL, 3, NULL),
  (180, '"이제 알겠지?"', 62, b'0', NULL, 3, NULL),
  (181, '민혜는 전화기를 내려놓으려 했다.', 63, b'0', NULL, 3, NULL),
  (182, '하지만 손이 말을 듣지 않았다.', 64, b'0', NULL, 3, NULL),
  (183, '전화기가 귀에 붙어 떨어지지 않았다.', 65, b'0', NULL, 3, NULL),
  (184, '목소리가 다시 들렸다.', 66, b'0', NULL, 3, NULL),
  (185, '"3개월 전에 죽었어. 이 자리에서."', 67, b'0', NULL, 3, NULL),
  (186, '민혜는 기억났다.', 68, b'0', NULL, 3, NULL),
  (187, '전임자가 갑자기 그만뒀다고 들었다.', 69, b'0', NULL, 3, NULL),
  (188, '아니다. 그만둔 게 아니었다.', 70, b'0', NULL, 3, NULL),
  (189, '"야간 근무 중에 심장마비로..."', 71, b'0', NULL, 3, NULL),
  (190, '팀장의 말이 떠올랐다.', 72, b'0', NULL, 3, NULL),
  (191, '"넌 내 자리에 앉아 있어."', 73, b'0', NULL, 3, NULL),
  (192, '민혜는 의자에서 일어나려 했다.', 74, b'0', NULL, 3, NULL),
  (193, '몸이 움직이지 않았다.', 75, b'0', NULL, 3, NULL),
  (194, '전화기 너머로 속삭임이 들렸다.', 76, b'0', NULL, 3, NULL),
  (195, '"이제 네 차례야."', 77, b'0', NULL, 3, NULL),
  (196, '민혜의 심장이 빠르게 뛰기 시작했다.', 78, b'0', NULL, 3, NULL),
  (197, '숨이 점점 가빠졌다.', 79, b'0', NULL, 3, NULL),
  (198, '가슴이 아팠다.', 80, b'0', NULL, 3, NULL),
  (199, '전화기에서 마지막 목소리가 들렸다.', 81, b'0', NULL, 3, NULL),
  (200, '"고마워. 이제 나는 자유야."', 82, b'0', NULL, 3, NULL),
  (201, '민혜는 의자에 고개를 떨어뜨렸다.', 83, b'0', NULL, 3, NULL),
  (202, '다음 날 아침, 팀장이 발견했다.', 84, b'0', NULL, 3, NULL),
  (203, '심장마비였다.', 85, b'0', NULL, 3, NULL),
  (204, '그리고 한 달 후.', 86, b'0', NULL, 3, NULL),
  (205, '새로운 야간 근무자가 들어왔다.', 87, b'0', NULL, 3, NULL),
  (206, '새벽 2시 47분.', 88, b'0', NULL, 3, NULL),
  (207, '전화벨이 울린다.', 89, b'0', NULL, 3, NULL);

INSERT INTO quiz_detail (quiz_id, correct_answer, explanation, quiz_type, content_id, script_id, question, options, difficulty, acceptable_answers, hint) VALUES
  (31, 'nervous', '''nervous''는 긴장하거나 불안할 때 쓰는 표현이야. 손에 땀이 날 정도로 긴장한 상황이지.', 'multiple_choice', 3, 135, '다음 중 ''긴장한, 초조한''을 뜻하는 영어 단어는?', '["nervous", "natural", "nothing", "normal"]', 1, '["nervous"]', 'n으로 시작하는 7글자, ''신경''과 관련된 단어'),
  (32, 'night shift', '''night shift''는 야간 근무, 밤 교대를 뜻하는 표현이야.', 'subjective', 3, 135, '''야간 근무'' = n____ s____

She''s been working the ______ ______ for three months.
(뜻: 밤 근무를 맡고 있다)', NULL, 2, '["night shift", "Night shift", "night-shift", "nightshift"]', '밤(night)과 교대근무를 뜻하는 s로 시작하는 단어'),
  (33, 'palms', '''palms were getting sweaty''는 긴장해서 손에 땀이 날 때 쓰는 자연스러운 표현이야. hands도 맞지만 palms가 더 정확해.', 'subjective', 3, 135, '''손에 땀이 났다''

Her ______ were getting sweaty.
(긴장하거나 불안할 때 손이 축축해지는 상황)', NULL, 3, '["palms", "hands"]', 'p___ (4글자, 손바닥을 뜻하는 단어)'),
  (34, 'tremble', '''tremble''은 무서워서나 긴장해서 몸이나 목소리가 떨리는 걸 표현할 때 써.', 'multiple_choice', 3, 152, '다음 중 ''떨다, 진동하다''의 영어 표현으로 알맞은 것은?', '["tremble", "trouble", "terrible", "trigger"]', 1, '["tremble"]', 't로 시작하는 7글자 단어'),
  (35, 'goosebumps', '''get goosebumps''는 소름이 돋다, 오싹하다는 뜻의 표현이야. 무서울 때나 감동할 때 쓸 수 있어.', 'subjective', 3, 152, '''소름이 돋다'' = get g______

She got ______ when she heard the trembling voice.
(뜻: 떨리는 목소리를 듣고 소름이 돋았다)', NULL, 2, '["goosebumps", "goose bumps", "Goosebumps"]', '거위 살이 돋는다고도 하지 ?'),
  (36, 'ran up', '''goosebumps ran up''은 소름이 등골을 타고 올라간다는 뜻이야. crept up이나 crawled up도 같은 의미로 쓸 수 있어.', 'subjective', 3, 152, '''등에 소름이 돋았다''

Goosebumps ______ ______ her back.
(뜻: ~에 소름이 돋다)', NULL, 3, '["ran up", "crept up", "crawled up", "went up"]', 'r___ u_ (2단어, 달리기할 때도 쓰는 표현)'),
  (37, 'confused', '''confused''는 갑작스러운 상황에 당황하고 혼란스러운 상태를 뜻해.', 'multiple_choice', 3, 169, '다음 중 ''당황한, 혼란스러운''의 영어 표현으로 알맞은 것은?', '["confused", "confident", "creative", "curious"]', 1, '["confused"]', 'c로 시작하는 8글자 단어'),
  (38, 'panicked', '''panic''은 갑작스런 상황에 당황하거나 공포에 빠지다라는 뜻이야.', 'subjective', 3, 169, '''당황하다'' = p______

She ______ when she heard the voice.
(뜻: 그녀는 목소리를 듣고 당황했다)', NULL, 2, '["panicked", "Panicked"]', 'p로 시작, 갑작스런 상황에 놀라서 어쩔 줄 모르는 상태 ?'),
  (39, 'flustered', '''flustered''는 예상치 못한 상황에 당황해서 어쩔 줄 모르는 상태를 표현해. bewildered, perplexed도 비슷한 의미야.', 'subjective', 3, 169, '''당황했다''

Minhye was ______.
(뜻: 예상하지 못한 상황에 놀라서 어쩔 줄 모르는 상태)', NULL, 3, '["flustered", "bewildered", "perplexed", "confused", "rattled"]', 'f_______d (8글자, 당황스러운 상황을 뜻하는 형용사)'),
  (40, 'empty', '''empty''는 아무것도 없이 텅 빈 상태를 말해. 사무실에 아무도 없는 상황이지.', 'multiple_choice', 3, 186, '다음 중 ''텅 빈, 비어있는''을 뜻하는 영어 단어는?', '["empty", "early", "equal", "exact"]', 1, '["empty"]', 'e로 시작하는 5글자 단어'),
  (41, 'stuck', '''be stuck to''는 ~에 달라붙어 있다는 뜻이야. 여기서는 전화기가 귀에서 떨어지지 않는 상황을 표현해.', 'subjective', 3, 186, '''~에 붙어 있다/달라붙다'' = s____

The phone was ______ to her ear.
(뜻: 전화기가 그녀 귀에 붙어 있었다)', NULL, 2, '["stuck", "sticking"]', '끈적끈적한 테이프처럼 달라붙는 그 단어'),
  (42, 'obey', '''obey''는 명령이나 의지에 따르다라는 뜻이야. 몸이 마음대로 안 될 때 자주 쓰는 표현이야.', 'subjective', 3, 186, '''손이 말을 듣지 않았다''

My hands wouldn''t ______ me.
(뜻: 내 뜻대로 움직이지 않다)', NULL, 3, '["obey", "listen to", "respond to", "follow"]', 'o___ (동사, 명령에 따르다)'),
  (43, 'ring', '''ring''은 전화벨이 울리거나 종이 울린다는 뜻이야. 새로운 희생자에게 걸려오는 무서운 전화벨 소리지.', 'multiple_choice', 3, 207, '다음 중 ''전화벨이 울리다''를 영어로 표현할 때 알맞은 것은?', '["ring", "roll", "rise", "rush"]', 1, '["ring"]', 'r로 시작하는 4글자 동사'),
  (44, 'ringing', '''ring''은 전화벨이 울리다라는 뜻으로 가장 자연스럽게 쓰이는 표현이야.', 'subjective', 3, 207, '''벨이 울리다'' = The phone is r______

At 2:47 AM, the phone started ______.
(뜻: 새벽 2시 47분, 전화벨이 울리기 시작했다)', NULL, 2, '["ringing", "Ringing"]', 'r로 시작, 종이나 벨이 내는 소리를 표현할 때 써 ?'),
  (45, 'is ringing', '''is ringing''이 가장 기본적인 표현이고, keeps/starts/begins ringing도 상황에 맞게 쓸 수 있어.', 'subjective', 3, 207, '이야기 마지막에서 ''전화벨이 울린다''는 상황

The phone ______ ______.
(현재진행형으로 표현)', NULL, 3, '["is ringing", "keeps ringing", "starts ringing", "begins ringing"]', 'r___ (4글자) - 벨이나 알람이 계속 울릴 때 쓰는 동사');

-- ----- [추리물] 빨간 구두 -----
INSERT INTO content (content_id, title, category_id, level_code, total_chapters, status, thumbnail_url) VALUES
  (4, '빨간 구두', 4, 2, 1, 'ACTIVE', NULL);

INSERT INTO chapter (chapter_id, chapter_num, chapter_title, required_points, content_id) VALUES
  (4, 1, '빨간 구두', 0, 4);

INSERT INTO script (script_id, script_content, sequence_num, has_options, character_name, chapter_id, option_id) VALUES
  (208, '학교 청소부 박미란은 새벽 6시에 출근했다.', 1, b'0', NULL, 4, NULL),
  (209, '1층 복도를 걸어가다가 발견했다.', 2, b'0', NULL, 4, NULL),
  (210, '빨간 구두 한 짝.', 3, b'0', NULL, 4, NULL),
  (211, '여학생 실내화가 아니었다. 성인용 하이힐이었다.', 4, b'0', NULL, 4, NULL),
  (212, '미란은 주변을 살펴봤다.', 5, b'0', NULL, 4, NULL),
  (213, '복도는 어젯밤에 깨끗이 청소했던 그대로였다.', 6, b'0', NULL, 4, NULL),
  (214, '아무도 없는 새벽 학교에 누가?', 7, b'0', NULL, 4, NULL),
  (215, '구두는 3학년 4반 교실 문 앞에 놓여 있었다.', 8, b'0', NULL, 4, NULL),
  (216, '한 짝만. 오른쪽.', 9, b'0', NULL, 4, NULL),
  (217, '미란이 교실 문을 열었다.', 10, b'0', NULL, 4, NULL),
  (218, '칠판에 분필로 뭔가가 적혀 있었다.', 11, b'0', NULL, 4, NULL),
  (219, '"7번 책상을 봐."', 12, b'0', NULL, 4, NULL),
  (220, '7번 자리는 비어 있었다. 원래 결석이 많은 학생이었다.', 13, b'0', NULL, 4, NULL),
  (221, '책상 위에 종이 한 장이 접혀 있었다.', 14, b'0', NULL, 4, NULL),
  (222, '"체육관으로 와. 혼자."', 15, b'0', NULL, 4, NULL),
  (223, '미란은 휴대폰으로 경비실에 연락했다.', 16, b'0', NULL, 4, NULL),
  (224, '"김 아저씨, 어젯밤에 학교에 누가 들어왔나요?"', 17, b'0', NULL, 4, NULL),
  (225, '"아뇨. 저 밤새 깨어있었는데 아무도 안 왔어요."', 18, b'0', NULL, 4, NULL),
  (226, '그런데 보안 기록을 확인해보니 이상한 점이 있었다.', 19, b'0', NULL, 4, NULL),
  (227, '어젯밤 11시 47분에 정문이 열렸다가 닫혔다.', 20, b'0', NULL, 4, NULL),
  (228, '하지만 다시 나간 기록은 없었다.', 21, b'0', NULL, 4, NULL),
  (229, '누군가 아직 학교 안에 있다는 뜻이었다.', 22, b'0', NULL, 4, NULL),
  (230, '체육관으로 가는 길에 미란은 또 발견했다.', 23, b'0', NULL, 4, NULL),
  (231, '왼쪽 구두.', 24, b'0', NULL, 4, NULL),
  (232, '체육관 입구 앞에 떨어져 있었다.', 25, b'0', NULL, 4, NULL),
  (233, '맨발로 들어간 거였다.', 26, b'0', NULL, 4, NULL),
  (234, '체육관 안은 고요했다.', 27, b'0', NULL, 4, NULL),
  (235, '미란이 전등을 켰다.', 28, b'0', NULL, 4, NULL),
  (236, '농구대 밑에 누군가 앉아 있었다.', 29, b'0', NULL, 4, NULL),
  (237, '20대 여성이었다. 긴 머리, 교복이 아닌 정장 차림.', 30, b'0', NULL, 4, NULL),
  (238, '맨발이었다.', 31, b'0', NULL, 4, NULL),
  (239, '"누구세요? 여기서 뭐 하는 거예요?"', 32, b'0', NULL, 4, NULL),
  (240, '여자가 고개를 들었다.', 33, b'0', NULL, 4, NULL),
  (241, '"전 이 학교 졸업생이에요. 17년 전에."', 34, b'0', NULL, 4, NULL),
  (242, '"밤에 왜 학교에?"', 35, b'0', NULL, 4, NULL),
  (243, '"3학년 4반 7번 자리에 앉던 학생이었어요."', 36, b'0', NULL, 4, NULL),
  (244, '미란이 깨달았다.', 37, b'0', NULL, 4, NULL),
  (245, '구두를 벗고 들어온 이유.', 38, b'0', NULL, 4, NULL),
  (246, '"선생님한테 혼나지 않으려고 맨발로 다녔던 거군요."', 39, b'0', NULL, 4, NULL),
  (247, '여자가 고개를 끄덕였다.', 40, b'0', NULL, 4, NULL),
  (248, '"그때처럼 조용히 들어오고 싶었어요."', 41, b'0', NULL, 4, NULL),
  (249, '"왜 하필 지금?"', 42, b'0', NULL, 4, NULL),
  (250, '여자가 가방에서 신문을 꺼냈다.', 43, b'0', NULL, 4, NULL),
  (251, '부고란이 펼쳐져 있었다.', 44, b'0', NULL, 4, NULL),
  (252, '"담임 선생님이 돌아가셨대요."', 45, b'0', NULL, 4, NULL),
  (253, '미란은 그제야 이해했다.', 46, b'0', NULL, 4, NULL),
  (254, '"마지막 인사를 하려고 온 거구나."', 47, b'0', NULL, 4, NULL),
  (255, '여자가 일어났다.', 48, b'0', NULL, 4, NULL),
  (256, '"선생님이 항상 말씀하셨어요. 신발 소리 나지 않게 조용히 다니라고."', 49, b'0', NULL, 4, NULL),
  (257, '"지금도 그 습관이 남아 있어서."', 50, b'0', NULL, 4, NULL),
  (258, '미란이 빨간 구두를 주워왔다.', 51, b'0', NULL, 4, NULL),
  (259, '"이거 신고 나가세요."', 52, b'0', NULL, 4, NULL),
  (260, '여자가 구두를 신더니 한 번 돌아봤다.', 53, b'0', NULL, 4, NULL),
  (261, '"고마워요. 그리고 죄송해요."', 54, b'0', NULL, 4, NULL),
  (262, '정문이 열리고 닫히는 소리가 들렸다.', 55, b'0', NULL, 4, NULL),
  (263, '미란은 3학년 4반으로 돌아가서 칠판을 지웠다.', 56, b'0', NULL, 4, NULL);

INSERT INTO quiz_detail (quiz_id, correct_answer, explanation, quiz_type, content_id, script_id, question, options, difficulty, acceptable_answers, hint) VALUES
  (46, 'look around', '''look around''는 주변을 둘러보거나 살펴본다는 뜻이야. 미란이 이상한 상황에서 주위를 확인하는 거지.', 'multiple_choice', 4, 218, '다음 중 ''주변을 둘러보다, 살펴보다''를 뜻하는 영어 표현은?', '["look around", "look after", "look forward", "look up"]', 1, '["look around"]', 'l로 시작하는 단어, ''보다''의 다른 표현'),
  (47, 'around', '''look around''는 주변을 둘러보다, 살펴보다라는 뜻의 구동사야.', 'subjective', 4, 218, '''주변을 살펴보다'' = look a______

She looked ______ to see if anyone was there.
(뜻: 그녀는 누군가 있는지 주변을 살펴봤다)', NULL, 2, '["around", "Around"]', 'a로 시작하는 ''둘러, 주위'' 의미하는 단어'),
  (48, 'untouched', '''untouched''는 아무도 손대지 않아서 원래 그대로인 상태를 나타내. unchanged, intact, undisturbed도 비슷한 의미로 쓸 수 있어.', 'subjective', 4, 218, '''그대로였다'' (변화 없이 원래 상태)

The hallway was ______ as it had been after last night''s cleaning.
(뜻: 변화하지 않은, 손대지 않은)', NULL, 3, '["untouched", "unchanged", "intact", "undisturbed"]', 'un____ed (과거분사 형태, touch + ed)'),
  (49, 'still', '''still''은 어떤 상황이 계속되고 있을 때 ''아직, 여전히''라는 뜻으로 써.', 'multiple_choice', 4, 229, '다음 중 ''아직, 여전히''를 뜻하는 영어 단어는?', '["still", "start", "study", "speak"]', 1, '["still"]', 's로 시작하는 5글자 단어'),
  (50, 'record', '''record''는 기록이라는 뜻으로, 보안 시스템의 출입 기록을 말할 때 자주 써.', 'subjective', 4, 229, '''기록'' = r______

There was no ______ of anyone leaving.
(뜻: 아무도 나간 기록이 없었다)', NULL, 2, '["record", "Record"]', 'r로 시작, ''녹음하다''는 동사로도 쓰이는 그 단어 ?'),
  (51, 'still inside', '''still inside''는 누군가가 아직도 그 장소 안에 있다는 걸 표현할 때 쓰는 자연스러운 표현이야.', 'subjective', 4, 229, '''아직 학교 안에 있다는 뜻이었다''

It meant someone was ______ ______ the school.
(뜻: 여전히 ~안에 있다)', NULL, 3, '["still inside", "still in", "still within", "still at"]', 's___ i_____ (2단어, 아직도 안에)'),
  (52, 'quiet', '''quiet''은 소리가 없이 고요하고 조용한 상태를 말해. 아무도 없는 체육관의 고요한 분위기를 표현한 거야.', 'multiple_choice', 4, 240, '다음 중 ''고요한, 조용한''의 영어 표현으로 알맞은 것은?', '["quiet", "quick", "quite", "queen"]', 1, '["quiet"]', 'q로 시작하는 5글자 단어'),
  (53, 'entrance', '''entrance''는 건물이나 장소의 입구를 뜻하는 기본 단어야.', 'subjective', 4, 240, '''입구'' = e_______

The shoe was lying at the _______ of the gym.
(뜻: 신발이 체육관 입구에 떨어져 있었다)', NULL, 2, '["entrance", "Entrance"]', 'exit의 반대말, e로 시작하는 8글자'),
  (54, 'silent', '''silent''는 완전히 조용한 상태를 표현해. quiet, still, hushed도 비슷한 의미로 쓸 수 있어.', 'subjective', 4, 240, '''고요했다''

The gymnasium was completely ______.
(뜻: 조용한, 아무 소리가 없는)', NULL, 3, '["silent", "quiet", "still", "hushed"]', 's____ (동물 조용히 하라고 할 때도 쓰는 말)'),
  (55, 'realize', '''realize''는 갑자기 뭔가를 깨닫거나 이해하게 될 때 쓰는 표현이야. 미란이 구두를 벗은 이유를 알아챈 상황이지.', 'multiple_choice', 4, 251, '다음 중 ''깨닫다, 알아차리다''의 영어 표현으로 알맞은 것은?', '["realize", "receive", "require", "replace"]', 1, '["realize"]', 'r로 시작하는 7글자 단어'),
  (56, 'obituary', '''obituary section''은 신문의 부고란을 뜻하는 표현이야. 돌아가신 분들의 소식을 전하는 지면이지.', 'subjective', 4, 251, '''부고란'' = o_______ section

She opened the newspaper to the ______ section.
(뜻: 그녀는 신문의 부고란을 펼쳤다)', NULL, 2, '["obituary", "Obituary"]', 'o로 시작, ''bite''나 ''chew''의 반대말이기도 해 ?'),
  (57, 'obituary', '''obituary''는 신문의 부고란이나 부고 기사를 뜻해. 사망한 사람의 생애를 기리는 글이기도 하지.', 'subjective', 4, 251, '신문의 ''부고란''을 영어로 뭐라고 할까?

She took out a newspaper from her bag. The ______ section was open.
(뜻: 사망 소식을 알리는 신문 란)', NULL, 3, '["obituary", "obituaries", "death notice", "death notices"]', 'o_______ (8글자, ''죽음''을 뜻하는 라틴어에서 온 말)'),
  (58, 'erase', '''erase''는 칠판이나 글씨를 지우다라는 뜻이야. 미란이 칠판을 지운 상황이지.', 'multiple_choice', 4, 263, '다음 중 ''지우다, 없애다''의 영어 표현으로 알맞은 것은?', '["erase", "empty", "enter", "enjoy"]', 1, '["erase"]', 'e로 시작하는 5글자 단어'),
  (59, 'erased', '''erase the board''는 칠판을 지우다라는 뜻이야. 이야기의 마지막 장면에서 미란이 모든 상황을 정리하며 칠판을 지우는 행동을 나타내는 표현이지.', 'subjective', 4, 263, '''칠판을 지우다'' = e_____ the board

After the lesson, the teacher ______ the board.
(뜻: 수업 후 선생님이 칠판을 지웠다)', NULL, 2, '["erased", "erase"]', '지우개는 eraser, 그럼 ''지우다''는 동사는? e로 시작해'),
  (60, 'erased', '''erase''는 칠판이나 화이트보드의 글씨를 지울 때 쓰는 가장 자연스러운 표현이야. clean, wipe, clear도 비슷한 의미로 쓸 수 있어.', 'subjective', 4, 263, '''칠판을 지웠다''

Miran went back to classroom 3-4 and ______ the blackboard.
(뜻: 칠판/화이트보드의 글씨를 지우다)', NULL, 3, '["erased", "cleaned", "wiped", "cleared"]', 'e___ (4글자, 컴퓨터에서 파일 삭제할 때도 쓰는 단어)');

-- ----- [특이한연애썰] 남자친구가 제 꿈을 예측해요 -----
INSERT INTO content (content_id, title, category_id, level_code, total_chapters, status, thumbnail_url) VALUES
  (5, '남자친구가 제 꿈을 예측해요', 5, 2, 1, 'ACTIVE', NULL);

INSERT INTO chapter (chapter_id, chapter_num, chapter_title, required_points, content_id) VALUES
  (5, 1, '남자친구가 제 꿈을 예측해요', 0, 5);

INSERT INTO script (script_id, script_content, sequence_num, has_options, character_name, chapter_id, option_id) VALUES
  (264, '저는 23살 여자고요, 남자친구랑 사귄 지 6개월 됐어요.', 1, b'0', NULL, 5, NULL),
  (265, '남자친구가 제 꿈을 미리 알아요.', 2, b'0', NULL, 5, NULL),
  (266, '처음 설명할게요. 남자친구는 예술대학에서 심리학을 복수전공하는 애예요.', 3, b'0', NULL, 5, NULL),
  (267, '성격이 되게 신비로운 타입이거든요.', 4, b'0', NULL, 5, NULL),
  (268, '첫 만남에서부터 뭔가 특이했어요.', 5, b'0', NULL, 5, NULL),
  (269, '"오늘 밤에 파란색 나비 꿈을 꿀 것 같은데요?" 이렇게 말하더라고요.', 6, b'0', NULL, 5, NULL),
  (270, '저는 그냥 "아 네...ㅋㅋㅋ" 이랬거든요.', 7, b'0', NULL, 5, NULL),
  (271, '그런데 진짜로 그날 밤에 파란 나비가 나오는 꿈을 꿨어요.', 8, b'0', NULL, 5, NULL),
  (272, '우연이겠지 하고 넘어갔는데.', 9, b'0', NULL, 5, NULL),
  (273, '두 번째 만남에서 또 말하는 거예요.', 10, b'0', NULL, 5, NULL),
  (274, '"혹시 어릴 때 살던 집 나오는 꿈 꾸지 않았어요?"', 11, b'0', NULL, 5, NULL),
  (275, '맞았어요. 진짜 초등학교 때 살던 빌라가 나왔어요.', 12, b'0', NULL, 5, NULL),
  (276, '세 번째 만남에서는 "계단에서 떨어지는 꿈 조심하세요" 이러더라고요.', 13, b'0', NULL, 5, NULL),
  (277, '그날 밤에 진짜 꿈에서 계단을 굴러떨어졌어요.', 14, b'0', NULL, 5, NULL),
  (278, '이게 우연일까요?', 15, b'0', NULL, 5, NULL),
  (279, '사귀고 나서도 계속됐어요.', 16, b'0', NULL, 5, NULL),
  (280, '매일 자기 전에 카톡이 와요.', 17, b'0', NULL, 5, NULL),
  (281, '"오늘은 물 관련 꿈일 것 같아" "검은 고양이 나올 거야" 이런 식으로.', 18, b'0', NULL, 5, NULL),
  (282, '정확도가 80% 정도 돼요.', 19, b'0', NULL, 5, NULL),
  (283, '처음엔 신기했는데 점점 무서워지기 시작했어요.', 20, b'0', NULL, 5, NULL),
  (284, '어떻게 이런 게 가능한 거예요?', 21, b'0', NULL, 5, NULL),
  (285, '남자친구한테 직접 물어봤어요.', 22, b'0', NULL, 5, NULL),
  (286, '"야 솔직히 말해. 너 뭐야? 초능력자야?"', 23, b'0', NULL, 5, NULL),
  (287, '그랬더니 웃으면서 "그냥 관찰하는 거야" 이러더라고요.', 24, b'0', NULL, 5, NULL),
  (288, '"관찰이 뭔데?"', 25, b'0', NULL, 5, NULL),
  (289, '"네가 하루 종일 뭘 했는지, 뭘 먹었는지, 무슨 영화 봤는지 들으면 꿈이 예측돼"', 26, b'0', NULL, 5, NULL),
  (290, '"심리학으로 배운 거야. 무의식이랑 꿈의 연관성 같은 거"', 27, b'0', NULL, 5, NULL),
  (291, '그래도 이상했어요.', 28, b'0', NULL, 5, NULL),
  (292, '그런데 한 달 전에 충격적인 걸 발견했어요.', 29, b'0', NULL, 5, NULL),
  (293, '남자친구 집에 갔는데 책상에 노트가 있었어요.', 30, b'0', NULL, 5, NULL),
  (294, '제목이 "○○이 꿈 패턴 분석"이었어요. 제 이름이 들어간.', 31, b'0', NULL, 5, NULL),
  (295, '펼쳐보니까 날짜별로 제 하루 일과랑 그날 밤 꿈이 정리되어 있었어요.', 32, b'0', NULL, 5, NULL),
  (296, '"3월 5일: 매운 음식 + 스트레스 → 쫓기는 꿈 (적중)"', 33, b'0', NULL, 5, NULL),
  (297, '"3월 12일: 옛날 사진 봄 + 향수 → 고향 관련 꿈 (적중)"', 34, b'0', NULL, 5, NULL),
  (298, '이런 식으로 3개월치가 빼곡히 적혀있었어요.', 35, b'0', NULL, 5, NULL),
  (299, '그리고 맨 뒤 페이지에 "꿈 조작 실험"이라고 써 있었어요.', 36, b'0', NULL, 5, NULL),
  (300, '뭐냐면, 제가 특정 꿈을 꿀 만한 상황을 일부러 만드는 거였어요.', 37, b'0', NULL, 5, NULL),
  (301, '데이트할 때 일부러 파란색 옷을 입고 나비 장식품을 보여준다든지.', 38, b'0', NULL, 5, NULL),
  (302, '옛날 이야기를 꺼낸다든지.', 39, b'0', NULL, 5, NULL),
  (303, '제 꿈을 예측하는 게 아니라 조종하고 있었던 거예요.', 40, b'0', NULL, 5, NULL),
  (304, '남자친구한테 따졌더니 "미안해, 근데 네 반응이 너무 귀여워서" 이러더라고요.', 41, b'0', NULL, 5, NULL),
  (305, '"그리고 이거 덕분에 네 심리 상태도 더 잘 알게 되고"', 42, b'0', NULL, 5, NULL),
  (306, '"나쁜 의도는 아니었어"', 43, b'0', NULL, 5, NULL),
  (307, '이 사람 저를 실험용 쥐로 본 건가요?', 44, b'0', NULL, 5, NULL),
  (308, '근데 또 나쁜 사람은 아닌 것 같아요.', 45, b'0', NULL, 5, NULL),
  (309, '제가 스트레스받을 때 미리 알고 챙겨주거든요.', 46, b'0', NULL, 5, NULL),
  (310, '어떻게 해야 할까요...?', 47, b'0', NULL, 5, NULL);

INSERT INTO quiz_detail (quiz_id, correct_answer, explanation, quiz_type, content_id, script_id, question, options, difficulty, acceptable_answers, hint) VALUES
  (61, 'mysterious', '''mysterious''는 신비롭고 이해하기 어려운 성격이나 상황을 나타낼 때 쓰는 말이야.', 'multiple_choice', 5, 272, '다음 중 ''신비로운, 수수께끼 같은''을 뜻하는 영어 단어는?', '["mysterious", "magnificent", "mechanical", "meaningful"]', 1, '["mysterious"]', 'm으로 시작하는 10글자 단어'),
  (62, 'coincidence', '''coincidence''는 우연의 일치를 뜻하는 단어야. 예측 불가능한 일이 일어났을 때 자주 쓰는 표현이지.', 'subjective', 5, 272, '''우연'' = c________

It must have been just a ________.
(뜻: 그냥 우연이었을 거야)', NULL, 2, '["coincidence", "Coincidence"]', '동전을 던질 때 쓰는 단어와 같아 ?'),
  (63, 'shrugged off', '''shrug off''는 대수롭지 않게 여기고 넘어간다는 뜻이야. brush off, pass off도 비슷한 의미로 쓸 수 있어.', 'subjective', 5, 272, '''우연이겠지 하고 넘어갔다''

I just ______ it ______ as a coincidence.
(뜻: ~로 여기고 넘어가다)', NULL, 3, '["shrugged off", "brushed off", "passed off", "wrote off"]', 's___ o__ (2단어, 해고할 때도 쓰는 표현)'),
  (64, 'coincidence', '''coincidence''는 우연의 일치를 뜻해. 남자친구가 꿈을 정확히 맞춰서 우연인지 의심하는 상황이지.', 'multiple_choice', 5, 281, '다음 중 ''우연, 우연의 일치''를 뜻하는 영어 단어는?', '["coincidence", "consequence", "convenience", "conference"]', 1, '["coincidence"]', 'c로 시작하는 11글자 단어'),
  (65, 'coincidence', '''coincidence''는 우연의 일치나 우연한 사건을 뜻하는 단어야.', 'subjective', 5, 281, '''우연'' = c_________

Is this just a ________?
(뜻: 이게 그냥 우연일까?)', NULL, 2, '["coincidence", "Coincidence"]', '동전을 던질 때도 쓰는 단어, c로 시작해'),
  (66, 'tumbled down', '''tumble down the stairs''는 계단에서 구르며 떨어지는 걸 표현해. roll down이나 fell down도 비슷한 의미로 쓸 수 있어.', 'subjective', 5, 281, '''계단을 굴러떨어지다''

I ______ ______ the stairs in my dream.
(뜻: 계단에서 굴러 떨어지다)', NULL, 3, '["tumbled down", "rolled down", "fell down", "tumbled down"]', 't___ d___ (구르면서 떨어질 때 쓰는 표현)'),
  (67, 'gradually', '''gradually''는 시간이 지나면서 점점, 서서히 변화하는 걸 표현할 때 써.', 'multiple_choice', 5, 290, '다음 중 ''점점, 서서히''를 뜻하는 영어 표현은?', '["gradually", "generally", "gracefully", "gratefully"]', 1, '["gradually", "Gradually"]', 'g로 시작하는 부사, ''등급''과 같은 어원'),
  (68, 'unconscious', '''unconscious''는 심리학에서 무의식을 뜻하는 단어야. ''subconscious''도 비슷한 의미로 쓰여.', 'subjective', 5, 290, '''무의식'' = u__________

Freud studied the human __________ and its influence on behavior.
(뜻: 프로이드는 인간의 무의식과 행동에 미치는 영향을 연구했다)', NULL, 2, '["unconscious", "Unconscious", "subconscious", "Subconscious"]', 'un으로 시작, ''의식하지 못하는'' 마음의 영역 ?'),
  (69, 'connection', '''connection''은 두 가지 사이의 연관성이나 관계를 나타내는 가장 자연스러운 표현이야. correlation, relationship 등도 비슷한 의미로 쓸 수 있어.', 'subjective', 5, 290, '''무의식이랑 꿈의 연관성''

The ______ between the unconscious mind and dreams
(뜻: 두 가지 사이의 관련성, 연결)', NULL, 3, '["connection", "correlation", "relationship", "link", "association"]', 'c_______ (9글자, connect의 명사형)'),
  (70, 'shocking', '''shocking''은 충격적이고 놀라운 일을 발견했을 때 쓰는 표현이야.', 'multiple_choice', 5, 299, '다음 중 ''충격적인, 깜짝 놀라게 하는''을 뜻하는 영어 단어는?', '["shocking", "shaking", "sharing", "shouting"]', 1, '["shocking"]', 's로 시작하는 8글자, ''쇼크''와 비슷한 단어'),
  (71, 'manipulate', '''manipulate''는 조작하다, 교묘히 다루다라는 뜻으로 심리적 조작에도 많이 쓰이는 단어야.', 'subjective', 5, 299, '''조작하다, 다루다'' = m________

He was trying to _______ her dreams.
(뜻: 그는 그녀의 꿈을 조작하려고 하고 있었다)', NULL, 2, '["manipulate", "Manipulate"]', 'm으로 시작, ''손으로 다루다''에서 나온 단어 ?'),
  (72, 'experimenting with', '''experiment with/on''은 누군가를 대상으로 실험하다라는 뜻이야. test with/on도 비슷한 의미로 쓸 수 있어.', 'subjective', 5, 299, '비밀리에 누군가를 실험 대상으로 삼아 연구한다

He was secretly ______ ______ her dreams.
(뜻: ~을 대상으로 실험하다)', NULL, 3, '["experimenting with", "experimenting on", "testing with", "testing on"]', 'e____ w___ (2단어, 과학자들이 하는 행동)'),
  (73, 'confused', '''confused''는 어떻게 해야 할지 몰라서 혼란스러운 상태를 표현할 때 쓰는 단어야.', 'multiple_choice', 5, 310, '다음 중 ''혼란스러워하다, 어찌할 바를 모르다''의 영어 표현으로 알맞은 것은?', '["confused", "complete", "continue", "consider"]', 1, '["confused"]', 'c로 시작하는 8글자 단어'),
  (74, 'do', '''what to do''는 ''무엇을 해야 하는지, 어떻게 해야 할지''를 나타내는 기본적이면서도 중요한 표현이야.', 'subjective', 5, 310, '''무엇을 해야 하는지'' = what to ___

I don''t know what to ___ about this situation.
(뜻: 이 상황에 대해 어떻게 해야 할지 모르겠다)', NULL, 2, '["do", "Do"]', 'd로 시작, ''행동하다''라는 뜻 ?'),
  (75, 'should do', '''What should I do?''는 어떻게 해야 할지 모를 때 조언을 구하는 가장 자연스러운 표현이야.', 'subjective', 5, 310, '고민이 있을 때 ''어떻게 해야 할까요?''라고 조언을 구할 때

What ______ I ______?
(뜻: 어떻게 해야 할까요?)', NULL, 3, '["should do", "should I do", "am I supposed to do", "ought to do"]', 's_____ d_ (조언 구할 때 쓰는 정중한 표현)');

-- ----- [연애갈등] 수학 과외 선생님과 내 여자친구 -----
INSERT INTO content (content_id, title, category_id, level_code, total_chapters, status, thumbnail_url) VALUES
  (6, '수학 과외 선생님과 내 여자친구', 6, 2, 1, 'ACTIVE', NULL);

INSERT INTO chapter (chapter_id, chapter_num, chapter_title, required_points, content_id) VALUES
  (6, 1, '수학 과외 선생님과 내 여자친구', 0, 6);

INSERT INTO script (script_id, script_content, sequence_num, has_options, character_name, chapter_id, option_id) VALUES
  (311, '고3 여자친구랑 8개월 사귀고 있음.', 1, b'0', NULL, 6, NULL),
  (312, '내가 대학교 1학년이고 걔가 고등학교 3학년인데 동네 선후배 사이였음.', 2, b'0', NULL, 6, NULL),
  (313, '원래 공부 잘하는 애가 아니라서 부모님이 과외 선생님을 붙여줬다더라.', 3, b'0', NULL, 6, NULL),
  (314, '수학 과외인데 대학생 형이 온다고 함.', 4, b'0', NULL, 6, NULL),
  (315, '처음에는 그냥 좋았음. 성적이 올라가면 나도 뿌듯하니까.', 5, b'0', NULL, 6, NULL),
  (316, '근데 몇 주 지나더니 과외 얘기를 자주 하기 시작함.', 6, b'0', NULL, 6, NULL),
  (317, '"오빠가 문제 이렇게 설명해줘서 이해했어" 이런 식으로.', 7, b'0', NULL, 6, NULL),
  (318, '뭔가 밝아진 느낌? 원래는 공부 얘기만 하면 짜증내던 애가.', 8, b'0', NULL, 6, NULL),
  (319, '그래도 처음에는 공부에 흥미가 생긴 건가 싶었음.', 9, b'0', NULL, 6, NULL),
  (320, '근데 하루는 데이트하는데 문자가 와서 "잠깐만" 하면서 답장하는 거임.', 10, b'0', NULL, 6, NULL),
  (321, '누구냐고 물어봤더니 "과외 선생님이 숙제 관련해서 물어봤어" 이러더라.', 11, b'0', NULL, 6, NULL),
  (322, '주말에도 과외와 관련된 문자를 주고받나?', 12, b'0', NULL, 6, NULL),
  (323, '이상했지만 그냥 넘어갔음. 공부 열심히 하는 건 좋은 일이니까.', 13, b'0', NULL, 6, NULL),
  (324, '그런데 이번 주에 걔 친구를 만났는데.', 14, b'0', NULL, 6, NULL),
  (325, '"요즘 과외 어때?" 물어봤더니 친구가 뭔가 어색하게 웃더라.', 15, b'0', NULL, 6, NULL),
  (326, '"그냥... 열심히 하는 것 같아" 이러는데 말투가 이상함.', 16, b'0', NULL, 6, NULL),
  (327, '그래서 "과외 선생님은 어떤 분이야?" 물어봤더니.', 17, b'0', NULL, 6, NULL),
  (328, '친구가 잠깐 멈추더니 "잘 모르겠어, 직접 물어봐" 이러고 화제를 돌리더라고.', 18, b'0', NULL, 6, NULL),
  (329, '그날부터 신경이 쓰이기 시작함.', 19, b'0', NULL, 6, NULL),
  (330, '과외 하는 날을 유심히 보니까 평소보다 화장을 진하게 함.', 20, b'0', NULL, 6, NULL),
  (331, '원래는 민낯으로 다니는 애였는데.', 21, b'0', NULL, 6, NULL),
  (332, '그리고 과외 끝나고 연락하면 답장이 늦어짐.', 22, b'0', NULL, 6, NULL),
  (333, '"피곤해서 잠깐 잤어" 이런 핑계를 대는데.', 23, b'0', NULL, 6, NULL),
  (334, '어느 날 과외 하는 날에 집 앞에 가봤음.', 24, b'0', NULL, 6, NULL),
  (335, '걔 집 근처 카페에서 기다리면서 언제 나오나 봤거든.', 25, b'0', NULL, 6, NULL),
  (336, '과외는 보통 2시간 한다고 했는데 3시간이 넘어도 안 나와.', 26, b'0', NULL, 6, NULL),
  (337, '그래서 전화했더니 "아 지금 좀 어려운 문제 풀고 있어서 조금 늦어질 것 같아" 이러더라.', 27, b'0', NULL, 6, NULL),
  (338, '전화 끊고 30분 더 기다리니까 드디어 나옴.', 28, b'0', NULL, 6, NULL),
  (339, '그런데 과외 선생님도 같이 나오는 거임.', 29, b'0', NULL, 6, NULL),
  (340, '멀어서 정확히는 안 보이는데 둘이 뭔가 얘기하면서 웃고 있더라.', 30, b'0', NULL, 6, NULL),
  (341, '그리고 헤어지기 전에 잠깐 손을 마주쳤음. 악수인지 뭔지는 모르겠지만.', 31, b'0', NULL, 6, NULL),
  (342, '집에 와서 "과외 어땠어?" 물어봤더니 "그냥 평소대로" 이러는 거임.', 32, b'0', NULL, 6, NULL),
  (343, '"오늘 좀 늦게 끝났네?" 했더니 "응, 모르는 게 많아서" 이러더라.', 33, b'0', NULL, 6, NULL),
  (344, '그런데 목소리가 뭔가 들떠 있었음.', 34, b'0', NULL, 6, NULL),
  (345, '며칠 뒤에 걔 엄마한테 안부 인사차 전화했음.', 35, b'0', NULL, 6, NULL),
  (346, '"과외 효과 있나요?" 물어봤더니 뭔가 이상한 말씀을 하시더라.', 36, b'0', NULL, 6, NULL),
  (347, '"요즘 공부보다는 다른 걸 배우는 것 같아요" 이러시는 거임.', 37, b'0', NULL, 6, NULL),
  (348, '무슨 뜻이냐고 했더니 "글쎄요, 성적은 그대로인데 기분이 좋아 보여요" 이러시더라.', 38, b'0', NULL, 6, NULL),
  (349, '그래서 어제 과외 선생님 번호를 알아냈음.', 39, b'0', NULL, 6, NULL),
  (350, '걔한테 "과외 선생님 연락처 좀 달라, 나도 과외받고 싶어" 이렇게 말해서.', 40, b'0', NULL, 6, NULL),
  (351, '의심 안 하고 바로 줬는데.', 41, b'0', NULL, 6, NULL),
  (352, '전화해서 "안녕하세요, 과외 받고 있는 학생 남자친구입니다" 이렇게 시작했음.', 42, b'0', NULL, 6, NULL),
  (353, '그랬더니 목소리가 확 달라지더라. 뭔가 당황하는 느낌?', 43, b'0', NULL, 6, NULL),
  (354, '"아... 네, 안녕하세요" 이러는데 어색함.', 44, b'0', NULL, 6, NULL),
  (355, '"혹시 시간 되실 때 한 번 뵙고 과외 방식에 대해 상담받을 수 있을까요?" 했더니.', 45, b'0', NULL, 6, NULL),
  (356, '"그게... 요즘 좀 바빠서... 나중에 연락드릴게요" 이러고 전화를 끊어버림.', 46, b'0', NULL, 6, NULL),
  (357, '보통 과외 선생님이면 학부모나 지인이 연락하면 반가워해야 하는 거 아님?', 47, b'0', NULL, 6, NULL),
  (358, '지금 여자친구한테 말해야 할지 그냥 넘어가야 할지 모르겠음.', 48, b'0', NULL, 6, NULL),
  (359, '확실한 건 아무것도 없는데 괜히 의심하는 건가 싶기도 하고.', 49, b'0', NULL, 6, NULL),
  (360, '근데 뭔가 이상한 건 분명함.', 50, b'0', NULL, 6, NULL);

INSERT INTO quiz_detail (quiz_id, correct_answer, explanation, quiz_type, content_id, script_id, question, options, difficulty, acceptable_answers, hint) VALUES
  (76, 'brighter', '''brighter''는 더 밝아진, 더 활기찬 상태를 말해. 여자친구가 과외 후 달라진 모습을 표현한 거야.', 'multiple_choice', 6, 320, '다음 중 ''밝아진, 더 활기찬''을 뜻하는 영어 표현으로 알맞은 것은?', '["brighter", "bothered", "busiest", "bragged"]', 1, '["brighter"]', 'b로 시작하는 7글자 단어, 전구가 켜진 것처럼!'),
  (77, 'interest', '''develop an interest in''은 어떤 것에 흥미나 관심을 갖게 되다는 뜻의 표현이야.', 'subjective', 6, 320, '''흥미를 갖다'' = develop an i_______ in

She seems to have developed an _______ in studying.
(뜻: 그녀는 공부에 흥미가 생긴 것 같다)', NULL, 2, '["interest", "Interest"]', '관심이나 호기심을 뜻하는 i로 시작하는 명사'),
  (78, 'irate', '''get irate''는 짜증나거나 화가 나는 상태를 표현해. irritated, annoyed, frustrated도 같은 맥락에서 쓸 수 있어.', 'subjective', 6, 320, '''공부 얘기만 하면 짜증을 냈다''

She used to get ______ whenever we talked about studying.
(뜻: 짜증나다, 화나다)', NULL, 3, '["irate", "irritated", "annoyed", "frustrated", "upset", "angry"]', 'i___ (4글자, irritated와 비슷한 감정)'),
  (79, 'awkwardly', '''awkwardly''는 어색하거나 불편한 방식으로 행동할 때 쓰는 부사야. 친구가 뭔가 숨기는 듯 어색하게 웃는 상황이지.', 'multiple_choice', 6, 330, '다음 중 ''어색하게, 불편하게''라는 뜻의 영어 표현은?', '["awkwardly", "apparently", "absolutely", "actually"]', 1, '["awkwardly"]', 'a로 시작하는 부사, ''어색한'' 형용사에 -ly를 붙인 것'),
  (80, 'change the subject', '''change the subject''는 화제를 바꾸다라는 뜻의 기본적인 영어 표현이야. 불편한 주제를 피할 때 자주 쓰지.', 'subjective', 6, 330, '''화제를 바꾸다/돌리다'' = c_______ the s______

She tried to ______ the ______ when I asked about it.
(뜻: 내가 그것에 대해 물어보자 그녀는 화제를 돌리려 했다)', NULL, 2, '["change the subject", "Change the subject", "changed the subject"]', '바꾸다는 ''c'', 주제는 ''s''로 시작해. 대화 방향을 바꿀 때 쓰는 표현!'),
  (81, 'changed the subject', '''change the subject''는 화제를 바꾸다는 뜻이야. switch나 shift도 같은 의미로 쓸 수 있어.', 'subjective', 6, 330, '''화제를 돌리다''

She _______ the _______ to something else.
(뜻: 대화 주제를 바꾸다)', NULL, 3, '["changed the subject", "switched the subject", "shifted the subject", "changed subject", "switched subject", "shifted subject"]', 'c_____ the s_____ (바꾸다 + 주제)'),
  (82, 'excuse', '''excuse''는 핑계나 변명을 뜻해. 피곤해서 잤다는 건 늦은 답장에 대한 핑계였던 거지.', 'multiple_choice', 6, 340, '다음 중 ''핑계, 변명''을 뜻하는 영어 단어는?', '["excuse", "escape", "expect", "extend"]', 1, '["excuse", "Excuse"]', 'e로 시작하는 6글자 단어'),
  (83, 'delayed', '''be delayed''는 예정된 시간보다 늦어지거나 지연되다는 뜻이야.', 'subjective', 6, 340, '''지연되다, 늦어지다'' = d______

The meeting will be ______ due to technical issues.
(뜻: 회의가 기술적 문제로 지연될 예정이다)', NULL, 2, '["delayed", "Delayed"]', 'd로 시작, ''미루다''라는 뜻도 있어 ?'),
  (84, 'over three', '''over three hours''는 3시간을 넘어서/초과해서라는 뜻이야. more than, past, beyond도 비슷한 의미로 쓸 수 있어.', 'subjective', 6, 340, '''3시간이 넘어도 안 나와''

Even after ______ ______ three hours, she didn''t come out.
(뜻: ~이 넘다/지나다)', NULL, 3, '["over three", "more than", "past three", "beyond three"]', 'o___ (전치사 + 형용사, 끝나다의 over)'),
  (85, 'excited', '''excited''는 기분이 들뜨고 흥분된 상태를 말해. 여자친구 목소리가 평소와 달리 들떠있던 상황이지.', 'multiple_choice', 6, 350, '다음 중 ''들뜬, 흥분한''의 영어 표현으로 알맞은 것은?', '["excited", "exhausted", "expected", "extended"]', 1, '["excited"]', 'e로 시작하는 7글자 단어'),
  (86, 'ask', '''ask after someone''은 누군가의 안부를 묻다라는 뜻의 구동사야.', 'subjective', 6, 350, '''안부를 묻다'' = ask after someone

I called to ______ after your daughter.
(뜻: 따님 안부를 묻기 위해 전화했어요)', NULL, 2, '["ask", "Ask"]', 'a로 시작하는 단어, ''질문하다''의 기본형'),
  (87, 'check up', '''check up on someone''은 누군가의 안부나 상황을 확인한다는 뜻이야. follow up, catch up도 비슷한 상황에서 쓸 수 있어.', 'subjective', 6, 350, '''안부 인사차 전화하다''

I called her mom to ______ ______ on her.
(뜻: 안부를 묻다, 근황을 확인하다)', NULL, 3, '["check up", "follow up", "catch up"]', 'c___ u_ (건강이나 상태를 확인할 때 쓰는 표현)'),
  (88, 'obvious', '''obvious''는 뭔가 분명하고 명백하다는 뜻이야. 확실하지는 않지만 뭔가 이상한 게 분명하다는 상황이지.', 'multiple_choice', 6, 360, '다음 중 ''분명한, 확실한''을 뜻하는 영어 표현은?', '["obvious", "optional", "opposite", "ordinary"]', 1, '["obvious"]', 'o로 시작하는 7글자 단어'),
  (89, 'definitely', '''definitely''는 ''분명히, 확실히''라는 뜻으로 확신을 나타낼 때 쓰는 부사야.', 'subjective', 6, 360, '''분명한, 확실한'' = d______

Something is ______ wrong here.
(뜻: 여기 뭔가 확실히 잘못됐어)', NULL, 2, '["definitely", "Definitely"]', 'd로 시작, ''명확하게''라는 부사로도 쓰여 ?'),
  (90, 'definitely something', '''definitely something''은 ''분명히 뭔가''라는 의미로, 확신을 표현할 때 쓰는 자연스러운 영어 표현이야.', 'subjective', 6, 360, '''뭔가 이상한 게 분명하다''

But there''s ______ ______ something strange going on.
(뜻: 분명히, 확실히)', NULL, 3, '["definitely something", "clearly something", "certainly something", "obviously something"]', 'd___ _____ (첫 번째 단어는 확실함, 두 번째는 no의 반대)');

SET FOREIGN_KEY_CHECKS = 1;

-- 완료!