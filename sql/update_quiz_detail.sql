SET FOREIGN_KEY_CHECKS=0;
DELETE FROM user_study_log;
TRUNCATE TABLE quiz_detail;
SET FOREIGN_KEY_CHECKS=1;

-- =============================================
-- quiz_detail (최신 버전)
-- =============================================
INSERT INTO quiz_detail (quiz_id, correct_answer, explanation, quiz_type, content_id, script_id, question, options, difficulty, acceptable_answers, hint) VALUES
  (1, 'annoyed', '''annoyed''는 뭔가 때문에 짜증나거나 화날 때 쓰는 표현이야. 자리 문제로 짜증난 상황이지.', 'multiple_choice', 1, 11, '다음 중 ''짜증나는, 화나는''의 영어 표현으로 알맞은 것은?', '["annoyed", "awkward", "anxious", "amazing"]', 1, NULL, 'annoy(짜증나게 하다)에 -ed, 모기 앵앵거릴 때 느끼는 그 감정 🦟'),
  (2, 'slyly', '''slyly glance''는 몰래 살짝 쳐다본다는 뜻이야. 상황을 파악하려고 조심스럽게 볼 때 쓰는 표현이지.', 'subjective', 1, 11, '''살짝/몰래'' = s______

I ______ glanced at her while she was reading.
(뜻: 그녀가 책을 읽는 동안 나는 살짝 쳐다봤다)', NULL, 2, '["slyly", "Slyly", "sly"]', 's로 시작하는 5글자, ''미끄러지다''는 뜻도 있어 🕵️'),
  (3, 'awkward', '''awkward''는 어색하고 불편한 상황을 표현할 때 쓰는 단어야. uncomfortable, weird도 비슷한 의미로 쓸 수 있어.', 'subjective', 1, 11, '''말 걸기 애매했다''

She was so focused on her book that it felt ______ to strike up a conversation.
(뜻: 어색한, 민망한)', NULL, 3, '["awkward", "uncomfortable", "weird", "strange"]', 'a_____ (어색하고 불편한 상황을 표현, 7글자)'),
  (4, 'curiosity', '''curiosity''는 궁금해하는 마음, 호기심을 뜻해. 쪽지 때문에 호기심이 생긴 상황이지.', 'multiple_choice', 1, 22, '다음 중 ''호기심''을 뜻하는 영어 단어는?', '["confusion", "creativity", "confidence", "curiosity"]', 1, NULL, 'Curiosity killed the cat(호기심이 고양이를 죽였다) 🐱 그 단어!'),
  (5, 'curiosity', '''curiosity''는 호기심이라는 뜻이야. ''curiosity killed the cat''이라는 유명한 속담도 있어.', 'subjective', 1, 22, '''호기심'' = c______

My ______ about the mysterious notes kept growing.
(뜻: 그 신비한 쪽지들에 대한 호기심이 계속 커졌다)', NULL, 2, '["curiosity", "Curiosity"]', 'c로 시작하는 9글자 단어, 고양이를 죽인다는 유명한 속담의 주인공 🐱'),
  (6, 'on purpose', '''on purpose''는 일부러, 의도적으로 한다는 뜻이야. intentionally, deliberately도 같은 의미로 쓸 수 있어.', 'subjective', 1, 22, '''일부러 일찍 가봤다''

I went there ______ ______ out of curiosity.
(뜻: 의도적으로, 일부러)', NULL, 3, '["on purpose", "intentionally", "deliberately"]', 'on p_______ (2단어, ''목적''이란 뜻의 단어)'),
  (7, 'courage', '''courage''는 용기를 뜻해. 무서워도 해내는 담력을 말하지.', 'multiple_choice', 1, 33, '다음 중 ''용기, 담력''을 뜻하는 영어 단어는?', '["careful", "courage", "comfort", "curious"]', 1, NULL, 'encourage(격려하다)에서 en을 빼 보면 💪'),
  (8, 'exchanging', '''exchange''는 서로 주고받다라는 뜻으로, 쪽지를 주고받는 상황에 딱 맞는 표현이야.', 'fill_in_blank', 1, 33, 'From that day on, we started ______ notes back and forth.', '["sharing", "writing", "sending", "exchanging"]', 2, NULL, 'e로 시작, ''서로 바꾸다''라는 뜻이야'),
  (9, 'exchanging', '''exchange''는 서로 주고받는다는 뜻이야. passing, trading, swapping도 비슷한 의미로 쓸 수 있어.', 'subjective', 1, 33, '''쪽지를 주고받다''

We started _______ notes back and forth.
(뜻: 서로 주고받다)', NULL, 3, '["exchanging", "passing", "trading", "swapping"]', 'e_______ (8글자, ''교환하다''라는 뜻)'),
  (10, 'empty', '''empty''는 물리적으로 비어있다는 뜻뿐만 아니라 마음이 허무하고 공허할 때도 쓸 수 있어.', 'multiple_choice', 1, 44, '다음 중 ''허무한, 공허한''을 뜻하는 영어 단어는?', '["early", "eager", "empty", "exact"]', 1, NULL, '''빈''이라는 뜻도 있어!'),
  (11, 'empty', '''feel empty''는 허무하고 공허한 감정을 표현할 때 쓰는 자연스러운 영어 표현이야.', 'subjective', 1, 44, '''허무한, 공허한'' = e______

I felt ______ after waiting for nothing.
(뜻: 아무것도 얻지 못하고 기다린 후 허무했다)', NULL, 2, '["empty", "Empty"]', 'e로 시작, ''비다''라는 뜻의 형용사에서 나온 감정 표현'),
  (12, 'hollow', '''empty''는 허무하고 공허한 감정을 표현할 때 자주 써. ''hollow''나 ''deflated''도 비슷한 의미로 쓸 수 있어.', 'subjective', 1, 44, '''허무했다''는 감정을 표현할 때

I felt so ______.
(뜻: 텅 빈 듯한, 공허한 느낌)', NULL, 3, '["empty", "hollow", "deflated", "let down"]', 'h____ (6글자, ''쑥 들어간'' 뜻의 형용사)'),
  (13, 'occasionally', '''occasionally''는 가끔씩, 때때로라는 뜻이야. 아직도 가끔 쪽지로 대화한다는 상황을 표현할 때 써.', 'multiple_choice', 1, 57, '다음 중 ''가끔, 때때로''를 뜻하는 영어 표현은?', '["occasionally", "officially", "ordinarily", "obviously"]', 1, NULL, 'occasion(특별한 날)의 부사형 — 항상은 아니고 가끔만 일어난다는 뉘앙스'),
  (14, 'occasionally', '''occasionally''는 ''가끔, 때때로''라는 뜻의 부사로 일상 대화에서 자주 쓰이는 표현이야.', 'subjective', 1, 57, '''가끔'' = o_________

We __________ still communicate through notes.
(뜻: 우리는 가끔 여전히 쪽지로 소통해)', NULL, 2, '["occasionally", "Occasionally"]', 'o로 시작하는 부사, ''특별한 occasion''할 때 그 단어의 부사 버전 🎉'),
  (15, 'now and then', '''now and then''은 ''가끔씩, 때때로''라는 뜻이야. from time to time, once in a while도 같은 의미로 쓸 수 있어.', 'subjective', 1, 57, '''가끔씩, 때때로''

We still communicate through notes ______ and ______.
(뜻: 가끔씩)', NULL, 3, '["now and then", "from time to time", "every now and then", "once in a while", "occasionally"]', 'n__ a__ t___ (이제와 그때, 시간 표현)'),
  (16, 'occasionally', '''occasionally''는 가끔씩, 때때로라는 뜻이야. 학생이 가끔 머리를 쥐어뜯는 상황을 표현할 때 쓸 수 있어.', 'multiple_choice', 2, 69, '다음 중 ''가끔, 때때로''를 뜻하는 영어 표현은?', '["officially", "occasionally", "obviously", "originally"]', 1, NULL, 'once in a while과 같은 뜻, ''occasion(기회)''에서 나온 단어'),
  (17, 'scribbling', '''scribble''은 급하게 쓰거나 끄적거린다는 뜻이야. 학생이 숙제하면서 연필로 끄적이는 모습을 표현할 때 딱이지.', 'subjective', 2, 69, '''낙서하다/끄적거리다'' = s______

He was ______ notes in the margin.
(뜻: 그는 여백에 끄적거리고 있었다)', NULL, 2, '["scribbling", "Scribbling", "scribble"]', 's로 시작하는 동사, 긁는다는 뜻도 있어 ✏️'),
  (18, 'tug at', '''tug at his hair''는 좌절이나 스트레스로 머리카락을 잡아당기는 걸 표현해. pull at도 같은 의미로 쓸 수 있어.', 'subjective', 2, 69, '수학 문제가 어려워서 ''머리를 쥐어뜯었다''

He would sometimes ______ his hair in frustration.
(뜻: 머리카락을 잡아당기다, 좌절감에 빠지다)', NULL, 3, '["tug at", "pull at", "grab at", "clutch at"]', '줄다리기 = tug of war, ''잡아당기다'' + at 🪢'),
  (19, 'empty', '''empty''는 비어있다는 뜻이야. 학생이 없어서 자리가 텅 빈 상황을 표현할 때 써.', 'multiple_choice', 2, 81, '다음 중 ''비어있는, 텅 빈''을 뜻하는 영어 단어는?', '["early", "equal", "enjoy", "empty"]', 1, NULL, 'e로 시작하는 5글자, 반대말은 full'),
  (20, 'sniffling', '''sniffling''은 코를 훌쩍거리는 소리를 나타내는 표현이야. 울고 난 후나 감정이 북받칠 때 자주 쓰지.', 'subjective', 2, 81, '''훌쩍거리다'' = s______

She was quietly ______ while reading her workbook.
(뜻: 그녀는 문제집을 보며 조용히 훌쩍거리고 있었다)', NULL, 2, '["sniffling", "Sniffling", "sniffing"]', '코감기 걸렸을 때 내는 소리 💧'),
  (21, 'glancing over', '''glance over''는 자꾸 힐끗힐끗 쳐다보는 걸 표현해. keep -ing와 함께 쓰면 반복적인 행동을 나타내지.', 'subjective', 2, 81, '''자꾸 창가를 쳐다봤다''

Yeon-woo kept ______ ______ at the window.
(뜻: 자꾸 힐끗힐끗 보다)', NULL, 3, '["glancing over", "looking over", "peeking over", "gazing over", "staring over"]', '''at a glance(한눈에)'' 할 때 그 단어 + over 👀'),
  (22, 'swollen', '''swollen''은 울거나 다쳐서 부어있는 상태를 표현할 때 써. 눈이 빨갛게 부은 모습이지.', 'multiple_choice', 2, 93, '다음 중 ''부어있는, 팽창한''을 뜻하는 영어 표현은?', '["swollen", "serious", "smooth", "sleepy"]', 1, NULL, '풍선처럼 부풀어 오른 상태, swell'),
  (23, 'swollen', '''swollen''은 부어오른 상태를 나타내는 형용사야. 울고 난 후 눈이 부은 모습을 표현할 때 자주 쓰여.', 'fill_in_blank', 2, 93, 'His eyes were red and ______.', '["watery", "swollen", "tired", "burning"]', 2, NULL, 's로 시작하는 7글자 단어, 벌에 쏘이면?'),
  (24, 'collapsed down', '''collapsed''나 ''passed out''은 갑자기 의식을 잃고 쓰러지는 걸 표현해. ''fainted''도 같은 의미로 쓸 수 있어.', 'subjective', 2, 93, '''갑자기 쓰러지다''

He suddenly ______ ______.
(뜻: 의식을 잃고 넘어지다)', NULL, 3, '["collapsed down", "passed out", "fell down", "collapsed", "fainted"]', 'c_______ d___ (의식을 잃다 + 아래로)'),
  (25, 'give up', '''give up''은 포기하다라는 뜻이야. 아버지가 딸에게 어려워도 포기하지 말라고 격려하는 상황이지.', 'multiple_choice', 2, 105, '다음 중 ''포기하다''의 영어 표현으로 알맞은 것은?', '["give off", "give out", "give away", "give up"]', 1, NULL, '항복할 때 손을 위로 올리는 모습을 떠올려봐'),
  (26, 'tucked', '''tuck''은 무언가를 사이나 틈에 끼워넣는다는 뜻이야. ''tucked between''은 ~사이에 끼워져 있다는 표현이지.', 'subjective', 2, 105, '''끼우다/사이에 넣다'' = t______

A letter was ______ between the pages.
(뜻: 편지가 페이지 사이에 끼워져 있었다)', NULL, 2, '["tucked", "Tucked"]', 't로 시작, ''집어넣다''라는 뜻. 셔츠를 바지에 넣을 때도 써'),
  (27, 'tucked', '''tucked''는 무언가가 사이에 끼워져 있거나 숨겨져 있는 상태를 표현해. inserted나 placed도 같은 의미로 쓸 수 있어.', 'subjective', 2, 105, '''편지가 끼워져 있었다''

A letter was ______ between the pages.
(뜻: ~이 사이에 끼워지다)', NULL, 3, '["tucked", "inserted", "placed", "stuck", "wedged"]', 't_____ (6글자, 갇혀있다는 뜻)'),
  (28, 'closely', '''closely''는 유심히, 주의 깊게 관찰하거나 보는 것을 의미해. 아버지가 딸의 자리를 자세히 살펴보는 상황이야.', 'multiple_choice', 2, 118, '다음 중 ''유심히, 주의 깊게''를 뜻하는 영어 부사는?', '["calmly", "closely", "cleverly", "clearly"]', 1, NULL, '''닫다''라는 단어와 연관된 부사'),
  (29, 'carefully', '''carefully watched''나 ''closely watched''는 유심히, 주의깊게 관찰한다는 뜻이야.', 'subjective', 2, 118, '''유심히 보다'' = c_______ watched

He ______ watched seat number 3 before leaving without a word.
(뜻: 그는 3번 자리를 유심히 보더니 아무 말 없이 나갔다)', NULL, 2, '["carefully", "closely", "Carefully", "Closely"]', 'c로 시작하는 ''주의깊게''라는 부사'),
  (30, 'intently', '''intently''는 집중해서 유심히 보는 걸 표현해. closely, carefully, attentively도 비슷한 의미로 쓸 수 있어.', 'subjective', 2, 118, '''유심히 보다''

He looked at seat #3 ______ and left without saying a word.
(뜻: 세심하게, 주의 깊게)', NULL, 3, '["intently", "closely", "carefully", "attentively"]', 'i___ly (부사, intense의 부사형)'),
  (31, 'nervous', '''nervous''는 긴장되고 불안할 때 쓰는 표현이야. 이상한 전화 때문에 민혜가 느끼는 감정이지.', 'multiple_choice', 3, 135, '다음 중 ''긴장한, 불안한''의 영어 표현으로 알맞은 것은?', '["nervous", "negative", "numerous", "natural"]', 1, NULL, 'nerve(신경) + ous, 신경이 곤두설 때의 느낌'),
  (32, 'prank', '''prank call''은 장난 전화라는 뜻으로, 콜센터에서 자주 받게 되는 성가신 전화를 말해.', 'subjective', 3, 135, '''장난 전화'' = p____ call

Most calls were just ______ calls from drunk people.
(뜻: 대부분의 전화는 술 취한 사람들의 장난 전화였다)', NULL, 2, '["prank", "Prank"]', 'p로 시작, ''허위의/가짜의''라는 뜻도 있어 📞'),
  (33, 'palms', '''palms''는 손바닥을 뜻해. 긴장하면 손바닥에 땀이 나는 걸 ''sweaty palms''라고 표현해.', 'subjective', 3, 135, '''손에 땀이 났다''

Her ______ were sweaty.
(뜻: 손바닥이 땀으로 젖었다)', NULL, 3, '["palms", "hands"]', 'p___ (5글자, 손바닥을 뜻하는 단어)'),
  (34, 'shake', '''shake''는 손이 떨리거나 진동하는 것을 표현할 때 쓰는 단어야. 무서워서 손이 떨리는 상황이지.', 'multiple_choice', 3, 152, '다음 중 ''떨리다, 진동하다''의 영어 표현으로 알맞은 것은?', '["shine", "share", "shake", "smile"]', 1, NULL, '지진이 일어날 때도 사용해'),
  (35, 'goosebumps', '''get goosebumps''는 무서울 때나 감동받을 때 피부에 소름이 돋는 걸 표현하는 영어 관용구야.', 'subjective', 3, 152, '''소름이 돋다'' = get g______

When I heard the strange voice, I got ______ all over my back.
(뜻: 이상한 목소리를 들었을 때 등에 소름이 돋았다)', NULL, 2, '["goosebumps", "goose bumps"]', '거위의 피부처럼 오돌토돌해진다고 해서 붙은 이름 🪿'),
  (36, 'ran up', '''chills ran up/down one''s spine''은 무서워서 등골이 오싹한 느낌을 표현할 때 쓰는 표현이야.', 'subjective', 3, 152, '''등에 소름이 돋았다''

Chills ______ ______ her spine.
(뜻: 등골이 오싹하다)', NULL, 3, '["ran up", "went up", "ran down", "went down", "shot up"]', 'r___ u_ (2단어, 달리다 + 위)'),
  (37, 'shocked', '''shocked''는 갑작스런 상황에 놀라고 당황한 상태를 말해. 예상치 못한 전화에 민혜가 당황한 거지.', 'multiple_choice', 3, 169, '다음 중 ''당황한, 놀란''의 영어 표현으로 알맞은 것은?', '["serious", "silly", "sleepy", "shocked"]', 1, NULL, '''쇼크 받았다''할 때 그 shock의 과거분사 ⚡'),
  (38, 'panicked', '''panic''은 당황하다, 공포에 빠지다라는 뜻이야. 과거형은 ''panicked''로 써.', 'subjective', 3, 169, '''당황하다'' = p______

She was ______ by the unexpected call.
(뜻: 그녀는 예상치 못한 전화에 당황했다)', NULL, 2, '["panicked", "Panicked"]', 'p로 시작, ''공황''이라는 단어와 친척 관계 😰'),
  (39, 'flustered', '''flustered''는 갑작스러운 상황에 당황하고 혼란스러워하는 감정을 표현해. bewildered, confused도 비슷한 의미야.', 'subjective', 3, 169, '''당황했다''

Minhye was ______.
(뜻: 혼란스럽고 어찌할 바를 모르는 상태)', NULL, 3, '["flustered", "bewildered", "confused", "perplexed", "rattled"]', 'f로 시작하는 7글자, flutter(파닥파닥)처럼 마음이 흔들려서 어쩔 줄 모르는 상태 😵'),
  (40, 'empty', '''empty''는 아무것도 없이 텅 비어있다는 뜻이야. 사무실에 아무도 없는 상황을 표현할 때 쓰지.', 'multiple_choice', 3, 186, '다음 중 ''텅 빈, 비어있는''을 뜻하는 영어 표현은?', '["eager", "exact", "empty", "early"]', 1, NULL, '''MT''랑 발음이 같아'),
  (41, 'froze', '''freeze''는 얼다는 뜻뿐만 아니라 공포나 충격으로 몸이 굳어질 때도 써. ''froze''는 과거형이야.', 'subjective', 3, 186, '''얼어붙다, 마비되다'' = f______

Her hands ______ and wouldn''t move.
(뜻: 그녀의 손이 얼어붙어서 움직이지 않았다)', NULL, 2, '["froze", "Froze"]', '겨울왕국 영어 제목의 과거형! 🧊'),
  (42, 'wouldn''t obey', '''wouldn''t obey''는 몸이 내 의지대로 움직이지 않을 때 쓰는 표현이야. wouldn''t listen이나 wouldn''t respond도 같은 의미로 쓸 수 있어.', 'subjective', 3, 186, '''손이 말을 듣지 않는다''

My hands ______ ______ to me.
(뜻: 내 의지대로 움직이지 않다)', NULL, 3, '["wouldn''t obey", "won''t obey", "wouldn''t listen", "won''t listen", "wouldn''t respond", "won''t respond"]', 'w___''t o___ (거부하다, 복종하지 않다)'),
  (43, 'ring', '''ring''은 전화벨이나 종이 울리는 소리를 표현할 때 쓰는 동사야. 새로운 야간 근무자에게도 같은 일이 반복되는 무서운 결말이지.', 'multiple_choice', 3, 207, '다음 중 ''(전화벨이) 울리다''를 뜻하는 영어 표현은?', '["ring", "rock", "roll", "rise"]', 1, NULL, '''반지, 고리''랑 같은 단어야'),
  (44, 'ringing', '전화벨이 울린다는 ''the phone is ringing''으로 표현해. 이야기의 무한 반복을 암시하는 마지막 장면이야.', 'fill_in_blank', 3, 207, 'At 2:47 AM, the phone started ______ again.', '["sounding", "ringing", "buzzing", "calling"]', 2, NULL, 'r으로 시작, 전화가 울릴 때 쓰는 동사'),
  (45, 'is ringing', '''ring''은 전화벨이나 종이 울릴 때 쓰는 동사야. 현재 진행형으로 ''is ringing''이 가장 자연스러워.', 'subjective', 3, 207, '''전화벨이 울린다''

The phone ______ ______.
(현재 진행되는 상황을 표현)', NULL, 3, '["is ringing", "rings", "starts ringing", "begins ringing"]', 'r___ (4글자) - 종이나 벨이 소리낼 때 쓰는 동사'),
  (46, 'look around', '''look around''는 주변을 둘러보거나 살펴본다는 뜻이야. 미란이 이상한 상황에서 주변을 확인하는 거지.', 'multiple_choice', 4, 218, '다음 중 ''주변을 살펴보다, 둘러보다''의 영어 표현으로 알맞은 것은?', '["look into", "look around", "look forward", "look after"]', 1, '["look around"]', 'l로 시작하는 동사, ''보다''의 의미'),
  (47, 'dawn', '''dawn''은 새벽, 여명을 뜻하는 단어로 해가 떠오르기 시작하는 이른 아침 시간을 나타내.', 'subjective', 4, 218, '''새벽'' = d____

She arrived at ______ to avoid the crowd.
(뜻: 그녀는 사람들을 피하려고 새벽에 도착했다)', NULL, 2, '["dawn", "Dawn"]', 'd로 시작, 해가 뜨기 직전의 그 시간대 🌅'),
  (48, 'looked around', '''look around''는 주변을 둘러보며 살펴본다는 뜻이야. glance around, peer around도 비슷한 의미로 쓸 수 있어.', 'subjective', 4, 218, '''주변을 살펴보다''

Miran ______ ______ to see if anyone was around.
(뜻: 주위를 둘러보다, 살피다)', NULL, 3, '["looked around", "glanced around", "peered around", "gazed around"]', 'l___ a_____ (2단어, 돌아보면서 확인한다는 뜻)'),
  (49, 'empty', '''empty''는 자리나 공간이 비어있다는 뜻이야. 7번 자리에 학생이 없어서 빈 상태였다는 거지.', 'multiple_choice', 4, 229, '다음 중 ''비어 있는, 빈''을 뜻하는 영어 단어는?', '["equal", "empty", "exact", "early"]', 1, '["empty"]', 'e로 시작하는 5글자 단어'),
  (50, 'record', '''record''는 기록이라는 뜻으로, 보안 시스템의 출입 기록을 말할 때 자주 쓰이는 단어야.', 'subjective', 4, 229, '''기록, 녹화, 녹음하다'' = r______

There was no ______ of anyone leaving.
(뜻: 누군가 나간 기록이 없었다)', NULL, 2, '["record", "Record"]', 'r로 시작하는 6글자, 음악 앨범을 만들 때도 쓰는 단어 🎵'),
  (51, 'meant', '''This meant that~''은 ''이것은 ~을 의미했다''라는 뜻으로, 상황을 분석하고 결론을 내릴 때 자주 쓰는 표현이야.', 'subjective', 4, 229, '''~라는 뜻이다''를 영어로 표현하면?

This ______ that someone was still inside the school.
(뜻: ~를 의미하다)', NULL, 3, '["meant", "means", "indicated", "suggested", "implied"]', 'm_____ (6글자, 평균을 나타내는 수학 용어와 같은 단어)'),
  (52, 'quiet', '''quiet''는 조용하고 고요한 상태를 나타내는 단어야. 체육관 안이 아무 소리 없이 고요했다는 뜻이지.', 'multiple_choice', 4, 240, '다음 중 ''고요한, 조용한''을 뜻하는 영어 단어는?', '["quite", "quick", "quiet", "quest"]', 1, '["quiet"]', 'q로 시작하는 5글자 단어'),
  (53, 'entrance', '''entrance''는 입구라는 뜻으로, 건물이나 장소로 들어가는 문이나 통로를 말해.', 'subjective', 4, 240, '''입구'' = e_______

There was a security guard at the ______ of the building.
(뜻: 건물 입구에 경비원이 있었다)', NULL, 2, '["entrance", "Entrance"]', 'e로 시작, ''exit''의 반대말! 들어가는 곳 🚪'),
  (54, 'lying', '''lying''은 물건이 바닥에 떨어져 있는 상태를 표현해. dropped, left, sitting도 비슷한 의미로 쓸 수 있어.', 'subjective', 4, 240, '''떨어져 있다'' (물건이 바닥에)

The shoe was ______ on the ground in front of the gym entrance.
(뜻: 바닥에 떨어져 있다)', NULL, 3, '["lying", "dropped", "left", "sitting"]', 'l_____ (과거분사, 누워있다는 뜻도 있음)'),
  (55, 'realize', '''realize''는 갑자기 뭔가를 깨달아서 이해하게 되는 걸 말해. 미란이가 여자의 행동 이유를 깨달은 상황이지.', 'multiple_choice', 4, 251, '다음 중 ''깨닫다, 알아차리다''의 영어 표현으로 알맞은 것은?', '["reflect", "realize", "receive", "require"]', 1, '["realize"]', 'r로 시작하는 7글자 단어'),
  (56, 'obituary', '''obituary''는 신문의 부고란이나 부고 기사를 뜻하는 단어야.', 'subjective', 4, 251, '''부고'' = o______

She opened the newspaper to the ______ section.
(뜻: 그녀는 신문의 부고란을 펼쳤다)', NULL, 2, '["obituary", "obituaries", "Obituary"]', '죽음을 알리는 신문 섹션, ''ob''로 시작해'),
  (57, 'obituary', '''obituary section''은 신문의 부고란을 뜻해. obituary는 사망자의 생애를 기리는 기사를 말하지.', 'subjective', 4, 251, '''부고란''을 영어로?

______ section
(뜻: 신문에서 사망 소식을 알리는 란)', NULL, 3, '["obituary", "obituaries", "death notice", "death notices"]', 'o_______ (사망자를 기리는 글, 기억하다는 뜻의 동사와 관련)'),
  (58, 'erase', '''erase''는 칠판의 글씨나 자국을 지우다는 뜻이야. 이야기의 마지막에 미란이 칠판을 지우며 하루를 마무리하는 장면이지.', 'multiple_choice', 4, 263, '다음 중 ''지우다, 없애다''의 영어 표현으로 알맞은 것은?', '["escape", "erase", "expect", "excuse"]', 1, '["erase"]', 'e로 시작하는 5글자 단어'),
  (59, 'erased', '''erase the board''는 칠판을 지우다라는 뜻으로, 학교에서 자주 쓰이는 표현이야.', 'subjective', 4, 263, '''칠판을 지우다'' = e______ the board

After class, the teacher ______ the board.
(뜻: 수업 후에 선생님이 칠판을 지웠다)', NULL, 2, '["erased", "Erased"]', 'e로 시작, ''삭제하다/완전히 없애다''라는 뜻 📝'),
  (60, 'wiped off', '''wipe off''는 칠판이나 표면의 글씨를 닦아서 지운다는 뜻이야. erase나 clean off도 같은 의미로 쓸 수 있어.', 'subjective', 4, 263, '이야기 마지막 장면에서 ''칠판을 지웠다''

Miran went back to classroom 3-4 and ______ ______ the blackboard.
(뜻: 칠판의 글씨를 지우다)', NULL, 3, '["wiped off", "erased", "cleaned off", "wiped clean", "cleared"]', 'w___ o__ (2단어, 운동이나 연습할 때도 쓰는 표현)'),
  (61, 'mysterious', '''mysterious''는 신비롭고 수수께끼 같은 느낌을 주는 성격을 표현할 때 쓰는 단어야.', 'multiple_choice', 5, 272, '다음 중 ''신비로운, 불가사의한''의 영어 표현으로 알맞은 것은?', '["mechanical", "miraculous", "magnificent", "mysterious"]', 1, NULL, 'mystery(미스터리)의 형용사형'),
  (62, 'coincidence', '''coincidence''는 우연의 일치라는 뜻으로, 예상치 못한 일이 동시에 일어날 때 쓰는 단어야.', 'subjective', 5, 272, '''우연'' = c______

It was probably just a ______.
(뜻: 아마 그냥 우연이었을 거야)', NULL, 2, '["coincidence", "Coincidence"]', '동전을 던질 때 쓰는 그 단어와 같아 🪙'),
  (63, 'double majoring', '''double majoring''은 두 개의 전공을 동시에 하는 것을 뜻해. dual majoring도 같은 의미로 쓸 수 있어.', 'subjective', 5, 272, '''복수전공하다''

He is ______ ______ in psychology at art university.
(뜻: 두 개의 전공을 동시에 하다)', NULL, 3, '["double majoring", "dual majoring", "pursuing double major", "doing double major"]', 'd_____ m_____ (첫 번째 단어는 ''두 배''라는 뜻)'),
  (64, 'coincidence', '''coincidence''는 우연히 동시에 일어나는 일을 말해. 꿈 예측이 계속 맞는 상황이 우연인지 궁금해하는 거지.', 'multiple_choice', 5, 281, '다음 중 ''우연, 우연의 일치''를 뜻하는 영어 단어는?', '["consequence", "convenience", "coincidence", "conference"]', 1, NULL, 'coin(동전)이 들어있는 단어 — 동전 던지기처럼 우연히 겹치는 일 🪙'),
  (65, 'tumbled', '''tumble down''은 굴러떨어지다라는 뜻으로, 계단에서 굴러떨어지는 상황을 표현할 때 자주 쓰는 표현이야.', 'fill_in_blank', 5, 281, 'I ______ down the stairs in my dream that night.', '["walked", "jumped", "tumbled", "slipped"]', 2, NULL, 't으로 시작, 종양도 이 단어를 써서 표현해'),
  (66, 'tumbled down', '''tumble down the stairs''는 계단에서 굴러떨어지다라는 뜻이야. roll down이나 fall down도 비슷한 의미로 쓸 수 있어.', 'subjective', 5, 281, '''계단을 굴러떨어지다''

I ______ ______ the stairs in my dream.
(뜻: 계단에서 굴러서 떨어지다)', NULL, 3, '["tumbled down", "rolled down", "fell down", "tumbled off", "rolled off"]', 't_____ d___ (넘어져서 아래로 굴러간다는 뜻)'),
  (67, 'gradually', '''gradually''는 점점, 서서히 변화하는 걸 표현할 때 쓰는 부사야.', 'multiple_choice', 5, 290, '다음 중 ''점점, 서서히''를 뜻하는 영어 표현은?', '["gracefully", "generally", "greatly", "gradually"]', 1, NULL, 'grade(등급)와 같은 어원 — 한 단계씩 천천히 📶'),
  (68, 'unconscious', '''unconscious''는 무의식을 뜻하는 심리학 용어야. 꿈과 깊은 연관이 있다고 알려져 있어.', 'subjective', 5, 290, '''무의식'' = u_________

The _________ mind affects our dreams.
(뜻: 무의식이 우리 꿈에 영향을 준다)', NULL, 2, '["unconscious", "Unconscious"]', 'conscious의 반대, 프로이드가 연구한 의식 아래 숨겨진 마음의 영역 🧠'),
  (69, 'connection', '''connection''은 두 가지 사이의 연관성이나 연결을 나타내는 단어야. correlation, relationship도 같은 맥락에서 쓸 수 있어.', 'subjective', 5, 290, '''무의식이랑 꿈의 연관성''

The ______ between the unconscious mind and dreams
(뜻: 두 가지 사이의 관련성, 연결고리)', NULL, 3, '["connection", "correlation", "relationship", "link", "association"]', 'c_______ (9글자, connect의 명사형)'),
  (70, 'shocking', '''shocking''은 충격적이고 놀라운 일을 발견했을 때 쓰는 표현이야.', 'multiple_choice', 5, 299, '다음 중 ''충격적인, 놀라운''을 뜻하는 영어 단어는?', '["shocking", "sparkling", "sleeping", "speaking"]', 1, NULL, '우리나라에서도 놀랄 때 흔히 쓰이는 단어야'),
  (71, 'manipulate', '''manipulate''는 조작하다, 다루다라는 뜻으로 꿈을 인위적으로 조작한다는 맥락에 딱 맞는 표현이야.', 'subjective', 5, 299, '''조작하다/다루다'' = m______

He tried to ______ the situation to his advantage.
(뜻: 그는 자신에게 유리하도록 상황을 조작하려고 했다)', NULL, 2, '["manipulate", "Manipulate"]', 'manual(수동의)과 같은 어원 mani(손) — 손으로 교묘히 다루다 ✋'),
  (72, 'according to', '''according to''는 ''~에 따라, ~별로''라는 뜻으로 정보를 분류할 때 자주 써. ''by''도 같은 의미로 쓸 수 있어.', 'subjective', 5, 299, '''날짜별로 정리되어 있다''

The dreams were organized ______ ______ date.
(뜻: ~에 따라, ~별로)', NULL, 3, '["according to", "by", "arranged by", "organized by"]', 'a________ t_ (전치사구, ''~에 따르면''이란 뜻도 있어)'),
  (73, 'wonder', '''wonder''는 뭔가에 대해 고민하거나 궁금해할 때 쓰는 표현이야. 어떻게 해야 할지 고민하는 상황이지.', 'multiple_choice', 5, 310, '다음 중 ''고민하다, 궁금해하다''를 뜻하는 영어 표현은?', '["welcome", "wander", "whisper", "wonder"]', 1, NULL, '''I wonder why~(왜일까~)'' 할 때 쓰는 동사, wonderful의 원형 ✨'),
  (74, 'should', '''What should I do?''는 어떻게 해야 할지 모를 때 조언을 구하는 표현이야. ''Should I~?''로 선택에 대한 고민을 나타낼 수 있어.', 'subjective', 5, 310, '''어떻게 해야 할까요?'' = What s______ I do?

______ I break up with him or give him another chance?
(뜻: 그와 헤어져야 할까요, 아니면 한 번 더 기회를 줘야 할까요?)', NULL, 2, '["should", "Should"]', '선택의 순간에 쓰는 단어, ''shall''과 비슷해'),
  (75, 'should do', '''What should I do?''는 어떻게 해야 할지 모르는 상황에서 조언을 구할 때 쓰는 가장 일반적인 표현이야.', 'subjective', 5, 310, '이야기 마지막에서 화자가 고민하며 조언을 구하는 상황

What ______ I ______ ?
(뜻: 어떻게 해야 할까요?)', NULL, 3, '["should do", "shall do", "ought to do"]', 's_____ d_ (조언을 구할 때 가장 기본적인 표현)'),
  (76, 'brighten', '''brighten''은 밝아지거나 활기차게 되는 걸 의미해. 여자친구가 과외 후 표정이 밝아진 상황이지.', 'multiple_choice', 6, 320, '다음 중 ''밝아지다, 활기차게 되다''를 뜻하는 영어 표현은?', '["bother", "broaden", "brighten", "burden"]', 1, NULL, '전구가 ''밝아질'' 때도 써요'),
  (77, 'tutor', '''tutor''는 과외선생님이나 개인교습을 해주는 사람을 뜻해.', 'subjective', 6, 320, '''과외'' = t______

Her parents hired a ______ to help with her math.
(뜻: 부모님이 수학을 도와줄 과외선생님을 고용했다)', NULL, 2, '["tutor", "Tutor"]', 't로 시작, ''teach''와 같은 어근을 가진 단어'),
  (78, 'hired', '''hire''는 돈을 주고 사람을 고용한다는 뜻이야. got, found, arranged도 이 맥락에서 쓸 수 있어.', 'subjective', 6, 320, '''과외 선생님을 붙여주다''

Her parents ______ a private tutor for her.
(뜻: ~을 고용하다, 채용하다)', NULL, 3, '["hired", "got", "found", "arranged", "provided"]', 'h___ (4글자, 고용하다라는 뜻도 있음)'),
  (79, 'awkward', '''awkward''는 상황이 어색하거나 불편할 때 쓰는 표현이야. 친구가 뭔가 이상하게 웃는 어색한 분위기를 말하는 거지.', 'multiple_choice', 6, 330, '다음 중 ''어색한, 불편한''의 영어 표현으로 알맞은 것은?', '["awkward", "annoying", "amazing", "anxious"]', 1, NULL, '4개 중 발음이 어려울 거 같은 단어! 😬'),
  (80, 'change the subject', '''change the subject''는 화제를 바꾸다라는 뜻의 기본 표현이야. 어색한 상황에서 자주 쓰이지.', 'subjective', 6, 330, '''화제를 바꾸다/돌리다'' = c______ the s______

She tried to ______ the ______ when I asked about her boyfriend.
(뜻: 남자친구에 대해 물어보자 그녀는 화제를 돌리려 했다)', NULL, 2, '["change the subject", "change subject", "Change the subject", "Change subject"]', '바꾸다 c + 주제/제목을 뜻하는 s로 시작하는 단어'),
  (81, 'changed', '''change the subject''는 화제를 돌리다라는 뜻이야. switch나 shift도 같은 의미로 쓸 수 있어.', 'subjective', 6, 330, '''화제를 돌리다''

She ______ the subject when I asked about the tutor.
(뜻: 대화 주제를 바꾸다)', NULL, 3, '["changed", "switched", "shifted", "diverted"]', 'c_____ (전환하다, 바꾸다라는 뜻의 동사)'),
  (82, 'delayed', '''delayed''는 예정보다 늦어지거나 지연되는 걸 의미해. 과외가 늦어지는 상황에 딱 맞는 표현이야.', 'multiple_choice', 6, 340, '다음 중 ''지연되다, 늦어지다''의 영어 표현으로 알맞은 것은?', '["decided", "delayed", "deleted", "detailed"]', 1, NULL, '게임에서 ''딜레이 걸렸다''할 때 그 delay의 과거형 ✈️'),
  (83, 'excuses', '''make excuses''는 핑계를 대다라는 뜻의 자연스러운 영어 표현이야.', 'fill_in_blank', 6, 340, 'She always makes ______ when she''s late.', '["explanations", "excuses", "reasons", "stories"]', 2, NULL, '''Excuse me'' 할 때 그 단어의 복수형, make와 짝꿍으로 쓰여'),
  (84, 'making up', '''make up excuses''는 핑계나 변명을 지어내다라는 뜻이야. come up with도 비슷한 의미로 쓸 수 있어.', 'subjective', 6, 340, '''핑계를 대다''

She started ______ ______ excuses like "I was tired and took a nap."
(뜻: 핑계나 변명을 늘어놓다)', NULL, 3, '["making up", "coming up with", "giving", "making"]', 'm___ u_ (2단어, 만들다ing + 위로)'),
  (85, 'excited', '''excited''는 기분이 좋아서 들뜨거나 신난 상태를 말해. 목소리에서 설렘이 느껴지는 상황이지.', 'multiple_choice', 6, 350, '다음 중 ''들뜬, 신난''을 뜻하는 영어 표현으로 알맞은 것은?', '["exhausted", "excited", "experienced", "embarrassed"]', 1, NULL, 'e로 시작하는 7글자 단어, ''흥분한'' 느낌'),
  (86, 'well-being', '''ask about someone''s well-being''은 안부를 묻다는 뜻의 정중한 표현이야.', 'subjective', 6, 350, '''안부를 묻다'' = ask about someone''s w______

I called to ask about her ______.
(뜻: 그녀의 안부를 묻기 위해 전화했다)', NULL, 2, '["well-being", "wellbeing", "welfare"]', '건강과 행복을 뜻하는 단어, w로 시작해'),
  (87, 'check up', '''check up on''은 누군가의 안부나 상황을 확인한다는 뜻이야. check in이나 follow up도 비슷한 의미로 쓸 수 있어.', 'subjective', 6, 350, '''안부 인사차 전화하다''

I called her mom to ______ ______ on how she was doing.
(뜻: ~의 안부를 묻다)', NULL, 3, '["check up", "check in", "follow up"]', 'c___ u_ (2단어, 전화로 상황 확인할 때 쓰는 표현)'),
  (88, 'certain', '''certain''은 뭔가가 분명하거나 확실할 때 쓰는 표현이야. 의심스러운 상황에서 확신을 나타낼 때 사용해.', 'multiple_choice', 6, 360, '다음 중 ''분명한, 확실한''을 뜻하는 영어 표현으로 알맞은 것은?', '["central", "careful", "certain", "curious"]', 1, NULL, 'cert(증명서) + ain, certificate(자격증)과 같은 어원'),
  (89, 'definitely', '''definitely''는 ''분명히, 확실히''라는 뜻으로 확신을 표현할 때 쓰는 부사야.', 'subjective', 6, 360, '''분명한, 확실한'' = d______

Something is ______ wrong here.
(뜻: 뭔가 분명히 잘못됐어)', NULL, 2, '["definitely", "Definitely"]', 'd로 시작, ''명백히/분명히''라는 부사로도 쓰여'),
  (90, 'smells fishy', '''Something smells fishy''는 뭔가 수상하다는 뜻이야. 생선이 상하면 냄새가 나듯이, 상황이 의심스러울 때 쓰는 관용 표현이지.', 'subjective', 6, 360, '''뭔가 수상하다''

Something ______ ______.
(뜻: 뭔가 수상한 냄새가 난다)', NULL, 3, '["smells fishy", "Smells fishy", "smells off", "seems fishy"]', 's_____ f____ (2단어, 생선 비린내를 떠올려봐 🐟)');
