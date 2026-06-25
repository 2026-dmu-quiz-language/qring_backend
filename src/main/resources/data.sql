-- =============================================
-- qring_db INSERT 스크립트
-- =============================================

SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. difficulty_level
-- =============================================
INSERT IGNORE INTO
    difficulty_level (
        level_code,
        level_name,
        level_desc
    )
VALUES (1, '쉬움', '기초 영어 단어 수준'),
    (2, '보통', '문장 완성 수준'),
    (3, '어려움', '문맥 이해 수준');

-- =============================================
-- 2. content_category
-- =============================================
INSERT IGNORE INTO
    content_category (
        category_id,
        category_name,
        display_order
    )
VALUES (1, '로맨스', 1),
    (2, '스토리', 2);

-- =============================================
-- 3. content,  script, quiz_detail
-- =============================================
-- ALTER TABLE quiz_detail ADD COLUMN IF NOT EXISTS question TEXT;
-- ALTER TABLE quiz_detail ADD COLUMN IF NOT EXISTS options JSON;
-- ALTER TABLE quiz_detail ADD COLUMN IF NOT EXISTS difficulty INT;
-- ALTER TABLE quiz_detail ADD COLUMN IF NOT EXISTS acceptable_answers JSON;

-- ----- [짝사랑] 도서관 좌석번호 64번 -----
INSERT IGNORE INTO
    content (
        content_id,
        title,
        category_id,
        status,
        thumbnail_url
    )
VALUES (
        1,
        '도서관 좌석번호 64번',
        1,
        'ACTIVE',
        '/images/짝사랑_썸네일.png'
    );

INSERT IGNORE INTO
    script (
        script_id,
        script_content,
        sequence_num,
        has_options,
        character_name,
        content_id,
        option_id
    )
VALUES (
        1,
        '나 대학교 다닐 때 시험기간마다 도서관 3층 64번 자리에만 앉았음.',
        1,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        2,
        '창가 자리라서 햇빛도 잘 들고 에어컨 바람도 안 와서 딱 좋았거든.',
        2,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        3,
        '근데 어느 날부터 그 자리에 항상 먼저 와있는 애가 있는 거임.',
        3,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        4,
        '65번 자리에 앉아서 책 읽고 있더라고.',
        4,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        5,
        '처음엔 짜증났음. 내 자리 옆에 왜 앉냐고.',
        5,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        6,
        '그런데 그 애가 책상에 A4 용지 하나를 올려두고 가는 거야.',
        6,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        7,
        '거기에 연필로 ''64번 자리 비워둘게요'' 이렇게 적어둔 거임.',
        7,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        8,
        '뭔가 이상하다 싶었는데 일단 고맙긴 해서 그냥 앉았음.',
        8,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        9,
        '다음 날도 똑같더라고. 65번에 그 애 있고, 64번엔 쪽지.',
        9,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        10,
        '근데 이번엔 ''오늘 비 온대요. 우산 챙기세요'' 이렇게 적혀있음.',
        10,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        11,
        '이게 뭐지? 싶어서 슬쩍 옆을 봤는데 그 애가 책에 집중하고 있어서 말 걸기 애매했음.',
        11,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        12,
        '그날 정말 비 왔음. 우산 안 가져갔으면 비 맞을 뻔했어.',
        12,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        13,
        '그 다음 날부터 호기심이 생기기 시작함.',
        13,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        14,
        '쪽지 내용이 매일 달라지거든.',
        14,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        15,
        '''오늘 학식 돈까스 맛없다고 하네요'', ''3층 화장실 휴지 떨어졌어요''',
        15,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        16,
        '이런 식으로 은근 유용한 정보들이었음.',
        16,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        17,
        '한 달 정도 지났을 때 그 애 얼굴을 제대로 본 적이 없다는 걸 깨달았어.',
        17,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        18,
        '항상 내가 올 때는 이미 책 보고 있고, 내가 자리 뜰 때는 아직 앉아있고.',
        18,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        19,
        '궁금해서 일부러 일찍 가봤는데도 이미 앉아있더라.',
        19,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        20,
        '도대체 몇 시에 오는 거야?',
        20,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        21,
        '어느 날 쪽지에 ''시험 잘 보세요. 항상 열심히 하시는 모습 멋있어요'' 이렇게 적혀있는 거임.',
        21,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        22,
        '이때 뭔가 심장이 두근거렸어.',
        22,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        23,
        '그래서 용기내서 쪽지 밑에 작은 글씨로 ''고마워요'' 이렇게 적어뒀음.',
        23,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        24,
        '다음 날 가보니까 내 글씨 옆에 이모티콘 하나 그려져 있었어.',
        24,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        25,
        '웃는 얼굴이었는데 되게 귀엽게 그렸더라고.',
        25,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        26,
        '그날부터 우리 쪽지 주고받기 시작함.',
        26,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        27,
        '근데 진짜 대화는 안 해. 계속 쪽지로만.',
        27,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        28,
        '''오늘 뭐 공부해요?'' ''전공이 뭐예요?'' 이런 것들.',
        28,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        29,
        '알고 보니까 그 애는 미술과였음.',
        29,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        30,
        '그래서 쪽지에 작은 그림도 그려주고 글씨도 예쁘게 썼던 거더라.',
        30,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        31,
        '나는 경영학과라고 했더니 다음 날 쪽지에 작은 계산기 그림이 그려져 있었어 ㅋㅋㅋ',
        31,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        32,
        '시험이 끝나고 방학이 되니까 도서관에 갈 일이 없어짐.',
        32,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        33,
        '근데 그 애가 궁금한 거야.',
        33,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        34,
        '그래서 개강하고 바로 도서관 갔는데 65번 자리가 비어있더라.',
        34,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        35,
        '64번에 쪽지도 없고.',
        35,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        36,
        '혹시 시간표가 바뀐 건가 싶어서 계속 다녀봤는데 한 주 내내 없었어.',
        36,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        37,
        '그러다가 일주일 뒤에 64번 자리에 쪽지 하나가 놓여있는 거 발견함.',
        37,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        38,
        '''미안해요. 교환학생으로 가게 됐어요. 1년 후에 돌아와요''',
        38,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        39,
        '그 밑에 작은 비행기 그림하나 그려져 있었음.',
        39,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        40,
        '진짜 허무했어. 얼굴도 제대로 못 본 채로 헤어진 거잖아.',
        40,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        41,
        '그 뒤로 도서관 가면 65번 자리만 계속 쳐다봤어.',
        41,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        42,
        '1년이 지나고 그 애가 돌아왔는지 확인하러 갔는데 또 없더라.',
        42,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        43,
        '근데 며칠 뒤에 도서관 앞 게시판에 전시회 포스터 하나가 붙어있는 거 봤어.',
        43,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        44,
        '미술과 졸업전시회였는데 포스터 한쪽 모서리에 작은 글씨로 ''64''라고 적혀있었음.',
        44,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        45,
        '그 글씨체가 쪽지랑 똑같았어.',
        45,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        46,
        '전시회 가봤는데 작품 중에 도서관 그림이 하나 있더라고.',
        46,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        47,
        '3층 창가 자리를 그린 건데 64번 책상 위에 작은 쪽지들이 쌓여있는 그림이었어.',
        47,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        48,
        '그림 제목이 ''말 못한 인사''였음.',
        48,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        49,
        '그때 옆에서 누군가 말을 거는 거야. ''그 자리 아세요?''',
        49,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        50,
        '돌아보니까 익숙한데 낯선 얼굴이었어.',
        50,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        51,
        '1년 동안 매일 옆에 앉았던 그 애였음.',
        51,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        52,
        '''1년 내내 인사하고 싶었는데 용기가 안 났어요''라고 하더라.',
        52,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        53,
        '나도 똑같았다고 했어.',
        53,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        54,
        '그 애가 웃으면서 ''이제 쪽지 말고 진짜 대화할 수 있을까요?'' 이러는데 목소리가 떨리고 있었음.',
        54,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        55,
        '나도 떨렸어.',
        55,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        56,
        '지금도 그 애랑 만나고 있음.',
        56,
        b'0',
        NULL,
        1,
        NULL
    ),
    (
        57,
        '아직도 가끔 쪽지로 대화해 ㅋㅋㅋ',
        57,
        b'0',
        NULL,
        1,
        NULL
    );

INSERT IGNORE INTO
    quiz_detail (
        quiz_id,
        quiz_type,
        content_id,
        script_id,
        difficulty
    )
VALUES (1, 'multiple_choice', 1, 9, 1),
    (2, 'fill_in_blank', 1, 9, 2),
    (3, 'subjective', 1, 9, 3),
    (
        4,
        'multiple_choice',
        1,
        18,
        1
    ),
    (5, 'subjective', 1, 18, 2),
    (6, 'subjective', 1, 18, 3),
    (
        7,
        'multiple_choice',
        1,
        27,
        1
    ),
    (8, 'fill_in_blank', 1, 27, 2),
    (9, 'subjective', 1, 27, 3),
    (
        10,
        'multiple_choice',
        1,
        36,
        1
    ),
    (11, 'fill_in_blank', 1, 36, 2),
    (12, 'subjective', 1, 36, 3),
    (
        13,
        'multiple_choice',
        1,
        45,
        1
    ),
    (14, 'fill_in_blank', 1, 45, 2),
    (15, 'subjective', 1, 45, 3),
    (
        16,
        'multiple_choice',
        1,
        57,
        1
    ),
    (17, 'fill_in_blank', 1, 57, 2),
    (18, 'subjective', 1, 57, 3);

INSERT IGNORE INTO
    quiz_content (
        quiz_id,
        lang_code,
        question,
        options,
        correct_answer,
        explanation,
        hint,
        acceptable_answers
    )
VALUES (
        1,
        'EN',
        '다음 중 ''짜증나다, 화나다''의 영어 표현으로 알맞은 것은?',
        '["annoyed", "amused", "amazed", "ashamed"]',
        'annoyed',
        '''annoyed''는 짜증나거나 화가 날 때 쓰는 표현이야. 자기 자리 옆에 누가 앉아서 기분이 나쁜 상황이지.',
        'annoy(짜증나게 하다)에 -ed, 모기 앵앵거릴 때 느끼는 그 감정 ?',
        '["annoyed"]'
    ),
    (
        2,
        'EN',
        'During exam period, I would always ______ the same seat in the library.',
        '["occupy", "reserve", "claim", "book"]',
        'occupy',
        '''occupy a seat''는 자리를 차지하다/앉다라는 뜻으로, 습관적으로 같은 자리에 앉는 상황에 자연스러운 표현이야.',
        's로 시작하는 5글자, ''미끄러지다''는 뜻도 있어 ??',
        '["occupy"]'
    ),
    (
        3,
        'EN',
        '''시험기간마다''

I always sat in the same spot ______ ______ season.
(뜻: 시험 기간 동안)',
        '[]',
        'during exam',
        '''during exam season''은 시험기간 동안이라는 뜻이야. throughout나 in도 같은 의미로 쓸 수 있어.',
        'a_____ (어색하고 불편한 상황을 표현, 7글자)',
        '["during exam", "during examination", "throughout exam", "throughout examination", "in exam", "in examination"]'
    ),
    (
        4,
        'EN',
        '다음 중 ''호기심''을 뜻하는 영어 단어는?',
        '["curiosity", "courage", "concern", "confusion"]',
        'curiosity',
        '''curiosity''는 뭔가 궁금해하고 알고 싶어하는 호기심을 뜻해. 쪽지 때문에 그 사람에 대해 궁금해지기 시작한 거야.',
        'Curiosity killed the cat(호기심이 고양이를 죽였다) ? 그 단어!',
        '["curiosity"]'
    ),
    (
        5,
        'EN',
        '''호기심'' = c______

My ______ was piqued by the mysterious notes.
(뜻: 신비한 쪽지들 때문에 호기심이 생겼다)',
        '[]',
        'curiosity',
        '''curiosity가 piqued되다''는 호기심이 자극받다, 생기다라는 뜻의 자연스러운 영어 표현이야.',
        'c로 시작하는 9글자 단어, 고양이를 죽인다는 유명한 속담의 주인공 ?',
        '["curiosity", "Curiosity"]'
    ),
    (
        6,
        'EN',
        '''제대로 본 적이 없다는 걸 깨달았다''

I realized that I had never ______ a proper ______ at her face.
(뜻: ~을 제대로 보다)',
        '[]',
        'gotten look',
        '''get a proper look at''은 어떤 것을 제대로 자세히 보다라는 뜻이야. take a look at도 비슷하게 쓸 수 있어.',
        'on p_______ (2단어, ''목적''이란 뜻의 단어)',
        '["gotten look", "had look", "taken look", "got look"]'
    ),
    (
        7,
        'EN',
        '다음 중 ''심장이 빨리 뛰다, 두근거리다''를 뜻하는 영어 표현은?',
        '["pound", "pause", "push", "point"]',
        'pound',
        '''pound''는 심장이 두근거릴 때 쓰는 표현이야. ''My heart was pounding''처럼 사용해.',
        'encourage(격려하다)에서 en을 빼 보면 ?',
        '["pound"]'
    ),
    (
        8,
        'EN',
        'At that moment, my heart started to ______ with excitement.',
        '["flutter", "bounce", "shake", "tremble"]',
        'flutter',
        '''heart flutter''는 심장이 두근거린다는 뜻으로, 설렘이나 흥분 상태를 자연스럽게 표현하는 말이야.',
        'e로 시작, ''서로 바꾸다''라는 뜻이야',
        '["flutter"]'
    ),
    (
        9,
        'EN',
        '''용기를 내서''

I ______ ______ the courage and wrote ''Thank you'' in small letters below the note.
(뜻: ~할 용기를 모으다, 용기를 내다)',
        '[]',
        'worked up',
        '''work up the courage''는 용기를 내다라는 뜻이야. muster up, pluck up, gather up도 같은 의미로 쓸 수 있어.',
        'e_______ (8글자, ''교환하다''라는 뜻)',
        '["worked up", "mustered up", "gathered up", "plucked up", "summoned up"]'
    ),
    (
        10,
        'EN',
        '다음 중 ''비어있는, 빈''을 뜻하는 영어 표현은?',
        '["empty", "early", "eager", "equal"]',
        'empty',
        '''empty''는 자리나 공간이 비어있다는 뜻이야. 65번 자리에 아무도 없었던 상황이지.',
        '''빈''이라는 뜻도 있어!',
        '["empty"]'
    ),
    (
        11,
        'EN',
        'When the new semester started, I went straight to the library but seat 65 was ______.',
        '["vacant", "broken", "moved", "changed"]',
        'vacant',
        '''vacant''는 자리가 비어있다는 뜻으로, 빈 좌석을 표현할 때 자주 쓰이는 단어야.',
        'e로 시작, ''비다''라는 뜻의 형용사에서 나온 감정 표현',
        '["vacant"]'
    ),
    (
        12,
        'EN',
        '''계속 다녀보다''를 영어로 표현하면?

I ______ ______ going to the library thinking maybe her schedule had changed.
(뜻: 계속 ~하다, 지속하다)',
        '[]',
        'kept on',
        '''kept on''은 어떤 행동을 계속 반복한다는 뜻이야. continue나 carry on도 같은 의미로 쓸 수 있어.',
        'h____ (6글자, ''쑥 들어간'' 뜻의 형용사)',
        '["kept on", "continued", "carried on", "went on"]'
    ),
    (
        13,
        'EN',
        '다음 중 ''허무한, 실망스러운''의 영어 표현으로 알맞은 것은?',
        '["disappointed", "determined", "delighted", "developed"]',
        'disappointed',
        '''disappointed''는 기대했던 일이 이루어지지 않아서 허무하고 실망스러울 때 쓰는 표현이야.',
        'occasion(특별한 날)의 부사형 ? 항상은 아니고 가끔만 일어난다는 뉘앙스',
        '["disappointed"]'
    ),
    (
        14,
        'EN',
        'I''m going abroad as an ______ student and will be back in a year.',
        '["exchange", "foreign", "overseas", "international"]',
        'exchange',
        '''exchange student''는 교환학생이라는 뜻이야. 다른 나라 학교와 학생을 교환하는 프로그램을 말해.',
        'o로 시작하는 부사, ''특별한 occasion''할 때 그 단어의 부사 버전 ?',
        '["exchange"]'
    ),
    (
        15,
        'EN',
        '''허무했다''는 감정을 영어로 표현하면?

I felt so ______.
(뜻: 공허하고 실망스러운 감정)',
        '[]',
        'empty',
        '''empty''는 허무하고 공허한 감정을 표현할 때 쓰는 자연스러운 영어야. hollow, deflated도 비슷한 뉘앙스로 사용돼.',
        'n__ a__ t___ (이제와 그때, 시간 표현)',
        '["empty", "hollow", "deflated", "let down", "disappointed"]'
    ),
    (
        16,
        'EN',
        '다음 중 ''용기, 담력''을 뜻하는 영어 단어는?',
        '["courage", "culture", "curious", "corner"]',
        'courage',
        '''courage''는 용기, 담력을 뜻해. 무언가를 하고 싶지만 용기가 나지 않을 때 쓰는 단어야.',
        'once in a while과 같은 뜻, ''occasion(기회)''에서 나온 단어',
        '["courage"]'
    ),
    (
        17,
        'EN',
        'I wanted to say hi all year, but I didn''t have the ______ to do it.',
        '["courage", "strength", "power", "energy"]',
        'courage',
        '''have the courage to do''는 ~할 용기가 있다는 뜻의 자연스러운 영어 표현이야.',
        's로 시작하는 동사, 긁는다는 뜻도 있어 ??',
        '["courage"]'
    ),
    (
        18,
        'EN',
        '''용기가 안 났다''

I couldn''t ______ ______ the courage to say hello.
(뜻: ~할 용기를 내다)',
        '[]',
        'work up',
        '''work up the courage''는 용기를 내다라는 뜻이야. muster up, pluck up도 같은 의미로 쓸 수 있어.',
        '줄다리기 = tug of war, ''잡아당기다'' + at ?',
        '["work up", "muster up", "build up", "gather up", "pluck up"]'
    );

-- ----- [드라마] 3번 관람석 -----
INSERT IGNORE INTO
    content (
        content_id,
        title,
        category_id,
        status,
        thumbnail_url
    )
VALUES (
        2,
        '3번 관람석',
        2,
        'ACTIVE',
        '/images/드라마_썸네일.png'
    );

INSERT IGNORE INTO
    script (
        script_id,
        script_content,
        sequence_num,
        has_options,
        character_name,
        content_id,
        option_id
    )
VALUES (
        58,
        '연우는 동네 서점에서 일했다.',
        1,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        59,
        '작은 서점이라 손님이 많지 않았다.',
        2,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        60,
        '그런데 한 명만 매일 왔다.',
        3,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        61,
        '학교 끝나고 오는 고등학생.',
        4,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        62,
        '항상 같은 자리에 앉았다.',
        5,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        63,
        '창가 쪽 3번 관람석이라고 적힌 의자.',
        6,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        64,
        '책을 읽는 게 아니라 숙제를 했다.',
        7,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        65,
        '수학 문제집을 펼쳐놓고 연필로 끄적거렸다.',
        8,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        66,
        '가끔 머리를 쥐어뜯기도 했다.',
        9,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        67,
        '연우는 그 학생을 ''3번 학생''이라고 불렀다.',
        10,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        68,
        '혼자서.',
        11,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        69,
        '어느 날 그 학생이 울고 있었다.',
        12,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        70,
        '조용히 훌쩍거리면서 문제집을 보고 있었다.',
        13,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        71,
        '연우가 티슈를 갖다 줬다.',
        14,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        72,
        '학생이 고맙다고 작게 말했다.',
        15,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        73,
        '그 다음 날부터 인사를 했다.',
        16,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        74,
        '들어올 때 고개를 살짝 숙이고 나갈 때도.',
        17,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        75,
        '한 달쯤 지났을까.',
        18,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        76,
        '그 학생이 안 왔다.',
        19,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        77,
        '3번 자리가 텅 비어 있었다.',
        20,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        78,
        '이틀째도 안 왔다.',
        21,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        79,
        '연우는 자꾸 창가를 쳐다봤다.',
        22,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        80,
        '일주일이 지나서야 왔다.',
        23,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        81,
        '그런데 뭔가 달랐다.',
        24,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        82,
        '머리를 짧게 잘랐고 교복이 아닌 검은 옷을 입고 있었다.',
        25,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        83,
        '평소처럼 3번 자리에 앉았는데 숙제를 안 꺼냈다.',
        26,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        84,
        '그냥 멍하니 창밖만 보고 있었다.',
        27,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        85,
        '연우가 다가가서 물었다.',
        28,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        86,
        '괜찮냐고.',
        29,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        87,
        '학생이 고개를 들었다.',
        30,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        88,
        '눈이 빨갛게 부어 있었다.',
        31,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        89,
        '아버지가 돌아가셨다고 말했다.',
        32,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        90,
        '갑자기 쓰러지셨다고.',
        33,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        91,
        '연우는 뭐라고 말해야 할지 몰랐다.',
        34,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        92,
        '그냥 옆에 앉았다.',
        35,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        93,
        '학생이 또 말했다.',
        36,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        94,
        '여기서 숙제하면서 아버지 생각이 많이 났다고.',
        37,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        95,
        '아버지도 수학을 좋아하셨다고.',
        38,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        96,
        '연우가 물었다.',
        39,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        97,
        '그래서 수학을 하는 거냐고.',
        40,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        98,
        '학생이 고개를 저었다.',
        41,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        99,
        '사실 수학을 제일 싫어한다고.',
        42,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        100,
        '그런데 아버지가 수학 선생님이셨다고.',
        43,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        101,
        '아버지 책상에서 이 문제집을 찾았다고.',
        44,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        102,
        '첫 페이지에 편지가 끼워져 있었다고.',
        45,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        103,
        '학생이 가방에서 편지를 꺼냈다.',
        46,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        104,
        '연우에게 보여줬다.',
        47,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        105,
        '''수학이 어려워도 포기하지 마. 아빠가 항상 응원할게. - 사랑하는 딸 지은이에게''',
        48,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        106,
        '편지 아래쪽에 작은 글씨로 또 적혀 있었다.',
        49,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        107,
        '''3번 자리에 앉으면 집중이 잘 될 거야.''',
        50,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        108,
        '연우가 깨달았다.',
        51,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        109,
        '아버지가 이 서점을 알고 있었다는 걸.',
        52,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        110,
        '지은이가 여기서 공부한다는 걸 아셨다는 걸.',
        53,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        111,
        '지은이도 같은 생각이었나 보다.',
        54,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        112,
        '눈물을 흘리면서 말했다.',
        55,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        113,
        '아버지가 여기까지 찾아오셨을지도 모른다고.',
        56,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        114,
        '딸이 어디서 공부하는지 궁금해서.',
        57,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        115,
        '연우는 그제서야 기억했다.',
        58,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        116,
        '한 달 전쯤 한 번 온 중년 남자를.',
        59,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        117,
        '지은이와 닮은 얼굴이었다.',
        60,
        b'0',
        NULL,
        2,
        NULL
    ),
    (
        118,
        '3번 자리를 유심히 보더니 아무 말 없이 나갔던.',
        61,
        b'0',
        NULL,
        2,
        NULL
    );

INSERT IGNORE INTO
    quiz_detail (
        quiz_id,
        quiz_type,
        content_id,
        script_id,
        difficulty
    )
VALUES (
        19,
        'multiple_choice',
        2,
        67,
        1
    ),
    (20, 'fill_in_blank', 2, 67, 2),
    (21, 'subjective', 2, 67, 3),
    (
        22,
        'multiple_choice',
        2,
        77,
        1
    ),
    (23, 'fill_in_blank', 2, 77, 2),
    (24, 'subjective', 2, 77, 3),
    (
        25,
        'multiple_choice',
        2,
        87,
        1
    ),
    (26, 'fill_in_blank', 2, 87, 2),
    (27, 'subjective', 2, 87, 3),
    (
        28,
        'multiple_choice',
        2,
        97,
        1
    ),
    (29, 'fill_in_blank', 2, 97, 2),
    (30, 'subjective', 2, 97, 3),
    (
        31,
        'multiple_choice',
        2,
        107,
        1
    ),
    (
        32,
        'fill_in_blank',
        2,
        107,
        2
    ),
    (33, 'subjective', 2, 107, 3),
    (
        34,
        'multiple_choice',
        2,
        118,
        1
    ),
    (
        35,
        'fill_in_blank',
        2,
        118,
        2
    ),
    (36, 'subjective', 2, 118, 3);

INSERT IGNORE INTO
    quiz_content (
        quiz_id,
        lang_code,
        question,
        options,
        correct_answer,
        explanation,
        hint,
        acceptable_answers
    )
VALUES (
        19,
        'EN',
        '다음 중 ''가끔, 때때로''를 뜻하는 영어 표현은?',
        '["occasionally", "obviously", "officially", "originally"]',
        'occasionally',
        '''occasionally''는 가끔씩, 때때로라는 뜻이야. 학생이 가끔 머리를 쥐어뜯는 상황을 표현할 때 쓸 수 있어.',
        'e로 시작하는 5글자, 반대말은 full',
        '["occasionally"]'
    ),
    (
        20,
        'EN',
        'He ______ down some notes with his pencil while working on math problems.',
        '["scribbled", "wrote", "marked", "drew"]',
        'scribbled',
        '''scribble''은 끄적거리다, 갈겨쓰다라는 뜻으로 연필로 대충 쓰는 모습을 자연스럽게 표현해.',
        '코감기 걸렸을 때 내는 소리 ?',
        '["scribbled"]'
    ),
    (
        21,
        'EN',
        '''머리를 쥐어뜯다'' (좌절하거나 고민할 때 하는 행동)

Sometimes he would ______ his hair in frustration.
(뜻: 머리카락을 잡아당기다)',
        '[]',
        'pull',
        '''pull one''s hair''는 스트레스나 좌절감으로 머리카락을 잡아당기는 행동을 표현해. tug나 grab도 같은 의미로 쓸 수 있어.',
        '''at a glance(한눈에)'' 할 때 그 단어 + over ?',
        '["pull", "tug", "grab", "clutch", "pull at", "tug at"]'
    ),
    (
        22,
        'EN',
        '다음 중 ''조용히 코를 훌쩍거리며 우는 소리''를 뜻하는 영어 표현은?',
        '["sniffling", "shouting", "smiling", "sleeping"]',
        'sniffling',
        '''sniffling''은 울 때 코를 훌쩍거리는 소리를 말해. 조용히 우는 상황에 딱 맞는 표현이야.',
        '풍선처럼 부풀어 오른 상태, swell',
        '["sniffling"]'
    ),
    (
        23,
        'EN',
        'She was quietly ______ while looking at her textbook.',
        '["sniffling", "whimpering", "sobbing", "weeping"]',
        'sniffling',
        '''sniffling''은 코를 훌쩍거리는 소리를 내며 조용히 우는 것을 표현할 때 써.',
        's로 시작하는 7글자 단어, 벌에 쏘이면?',
        '["sniffling"]'
    ),
    (
        24,
        'EN',
        '''고개를 살짝 숙이며 인사하다''

He would give a slight ______ of his head when coming in and leaving.
(뜻: 고개를 끄덕이는 인사)',
        '[]',
        'nod',
        '''nod''는 고개를 끄덕이는 인사를 뜻해. ''bow''나 ''dip''도 고개를 숙이는 인사로 쓸 수 있어.',
        'c_______ d___ (의식을 잃다 + 아래로)',
        '["nod", "bow", "dip"]'
    ),
    (
        25,
        'EN',
        '다음 중 ''멍하니, 정신없이''를 뜻하는 영어 표현은?',
        '["blankly", "briefly", "blindly", "bravely"]',
        'blankly',
        '''blankly''는 멍하니 정신없이 바라보는 상태를 말해. 학생이 창밖을 멍하니 보고 있는 상황이지.',
        '항복할 때 손을 위로 올리는 모습을 떠올려봐',
        '["blankly"]'
    ),
    (
        26,
        'EN',
        'He was just ______ out the window blankly.',
        '["staring", "looking", "watching", "gazing"]',
        'staring',
        '''stare''는 멍하니 또는 뚫어지게 바라본다는 의미로, ''stare out the window''는 창밖을 멍하니 바라본다는 뜻이야.',
        't로 시작, ''집어넣다''라는 뜻. 셔츠를 바지에 넣을 때도 써',
        '["staring"]'
    ),
    (
        27,
        'EN',
        '''멍하니 바라보다''

He was just ______ ______ out the window.
(뜻: 정신없이 또는 빈 표정으로 바라보다)',
        '[]',
        'staring blankly',
        '''staring blankly''는 정신이 나간 것처럼 멍하니 바라본다는 뜻이야. gazing blankly나 vacantly도 같은 의미로 쓸 수 있어.',
        't_____ (6글자, 갇혀있다는 뜻)',
        '["staring blankly", "gazing blankly", "looking blankly", "staring vacantly", "gazing vacantly"]'
    ),
    (
        28,
        'EN',
        '다음 중 ''갑자기, 예상하지 못하게''를 뜻하는 영어 표현은?',
        '["suddenly", "silently", "seriously", "secretly"]',
        'suddenly',
        '''suddenly''는 갑자기 예상하지 못한 일이 일어날 때 쓰는 표현이야.',
        '''닫다''라는 단어와 연관된 부사',
        '["suddenly"]'
    ),
    (
        29,
        'EN',
        'He suddenly ______ and had to be rushed to the hospital.',
        '["collapsed", "stumbled", "slipped", "dropped"]',
        'collapsed',
        '''collapse''는 갑자기 쓰러지다, 건강상 문제로 넘어지다는 뜻이야. 의료 응급상황에서 자주 쓰이는 표현이지.',
        'c로 시작하는 ''주의깊게''라는 부사',
        '["collapsed"]'
    ),
    (
        30,
        'EN',
        '''뭐라고 말해야 할지 모르겠다''

I didn''t know ______ ______ ______.
(상황: 위로의 말을 어떻게 해야 할지 모를 때)',
        '[]',
        'what to say',
        '''what to say''는 어떤 말을 해야 할지 모를 때 쓰는 자연스러운 표현이야. 위로가 필요한 상황에서 자주 쓰여.',
        'i___ly (부사, intense의 부사형)',
        '["what to say", "how to respond", "what to tell", "how to comfort", "what words", "what response"]'
    ),
    (
        31,
        'EN',
        '다음 중 ''포기하다''의 영어 표현으로 알맞은 것은?',
        '["give up", "give out", "give in", "give away"]',
        'give up',
        '''give up''은 포기하다라는 뜻이야. 아버지가 딸에게 수학이 어려워도 포기하지 말라고 하는 상황이지.',
        'nerve(신경) + ous, 신경이 곤두설 때의 느낌',
        '["give up"]'
    ),
    (
        32,
        'EN',
        'Don''t give up even if math is difficult. I''ll always ______ you on.',
        '["cheer", "push", "keep", "move"]',
        'cheer',
        '''cheer someone on''은 누군가를 응원하다라는 뜻의 구동사야. 아버지가 딸을 격려하는 상황에 딱 맞아.',
        'p로 시작, ''허위의/가짜의''라는 뜻도 있어 ?',
        '["cheer"]'
    ),
    (
        33,
        'EN',
        '''포기하지 마''

Don''t ______ ______ on math even if it''s difficult.
(뜻: ~을 포기하다)',
        '[]',
        'give up',
        '''give up on''은 ~을 포기하다라는 뜻이야. quit이나 abandon도 같은 의미로 쓸 수 있어.',
        'p___ (5글자, 손바닥을 뜻하는 단어)',
        '["give up", "quit", "abandon"]'
    ),
    (
        34,
        'EN',
        '다음 중 ''깨닫다, 알아차리다''의 영어 표현으로 알맞은 것은?',
        '["realize", "receive", "recover", "respond"]',
        'realize',
        '''realize''는 뭔가를 깨닫거나 알아차릴 때 쓰는 표현이야. 연우가 아버지의 진짜 의도를 깨달은 상황이지.',
        '지진이 일어날 때도 사용해',
        '["realize"]'
    ),
    (
        35,
        'EN',
        'He looked at seat number 3 ______ and left without saying a word.',
        '["intently", "briefly", "casually", "nervously"]',
        'intently',
        '''유심히 보다''는 ''look intently''로 표현해. 집중해서 자세히 본다는 뜻이야.',
        '거위의 피부처럼 오돌토돌해진다고 해서 붙은 이름 ?',
        '["intently"]'
    ),
    (
        36,
        'EN',
        '''유심히 보다''

He looked ______ at seat number 3 and left without saying anything.
(뜻: 관심을 가지고 자세히 보다)',
        '[]',
        'intently',
        '''look intently''는 집중해서 유심히 보는 걸 의미해. closely, carefully, thoughtfully도 같은 상황에서 쓸 수 있어.',
        'r___ u_ (2단어, 달리다 + 위)',
        '["intently", "closely", "carefully", "thoughtfully", "attentively"]'
    );

-- ----- [스릴러] 끊어진 전화선 -----
INSERT IGNORE INTO
    content (
        content_id,
        title,
        category_id,
        status,
        thumbnail_url
    )
VALUES (
        3,
        '끊어진 전화선',
        3,
        'ACTIVE',
        '/images/스릴러_썸네일.png'
    );

INSERT IGNORE INTO
    script (
        script_id,
        script_content,
        sequence_num,
        has_options,
        character_name,
        content_id,
        option_id
    )
VALUES (
        119,
        '민혜는 콜센터에서 일한다.',
        1,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        120,
        '야간 근무를 맡은 지 3개월째.',
        2,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        121,
        '새벽 시간대라 전화가 거의 안 온다.',
        3,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        122,
        '대부분 술 취한 사람들의 장난 전화나 잘못 건 전화.',
        4,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        123,
        '그런데 지난주부터 이상한 전화가 오기 시작했다.',
        5,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        124,
        '새벽 2시 47분. 정확히 같은 시각.',
        6,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        125,
        '전화벨이 울린다.',
        7,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        126,
        '받으면 숨소리만 들린다.',
        8,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        127,
        '"안녕하세요, 고객센터입니다" 말해도 대답이 없다.',
        9,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        128,
        '그냥 후-후- 하는 숨소리.',
        10,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        129,
        '처음에는 장난 전화려니 했다.',
        11,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        130,
        '근데 매일 같은 시간이었다.',
        12,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        131,
        '2시 47분. 정확히.',
        13,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        132,
        '오늘도 그 시간이 다가온다.',
        14,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        133,
        '시계를 보니 2시 46분.',
        15,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        134,
        '민혜는 손에 땀이 났다.',
        16,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        135,
        '왜 이렇게 긴장하고 있는 걸까?',
        17,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        136,
        '그냥 끊어버리면 되는데.',
        18,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        137,
        '2시 47분. 벨이 울린다.',
        19,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        138,
        '손이 떨렸지만 받았다.',
        20,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        139,
        '"안녕하세요..."',
        21,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        140,
        '역시 숨소리만 들린다.',
        22,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        141,
        '그런데 오늘은 뭔가 달랐다.',
        23,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        142,
        '숨소리 뒤로 다른 소리가 섞여 있었다.',
        24,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        143,
        '뚝뚝뚝.',
        25,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        144,
        '물 떨어지는 소리? 아니다.',
        26,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        145,
        '타자 치는 소리였다.',
        27,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        146,
        '누군가 키보드를 치고 있었다.',
        28,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        147,
        '"거기 누구세요?" 민혜가 물었다.',
        29,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        148,
        '타자 소리가 멈췄다.',
        30,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        149,
        '그리고 목소리가 들렸다.',
        31,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        150,
        '"도와줘."',
        32,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        151,
        '여자 목소리였다. 떨리고 있었다.',
        33,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        152,
        '민혜는 등에 소름이 돋았다.',
        34,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        153,
        '"어디에 계세요? 무슨 일이에요?"',
        35,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        154,
        '"찾지 마. 위험해."',
        36,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        155,
        '그리고 전화가 끊어졌다.',
        37,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        156,
        '발신번호를 확인했다.',
        38,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        157,
        '표시되지 않음.',
        39,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        158,
        '민혜는 팀장에게 보고했다.',
        40,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        159,
        '"그냥 장난 전화겠지. 신경 쓰지 마."',
        41,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        160,
        '하지만 신경 쓰이지 않을 수가 없었다.',
        42,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        161,
        '그 여자의 목소리가 계속 맴돌았다.',
        43,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        162,
        '다음 날 밤.',
        44,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        163,
        '2시 47분. 또 전화가 왔다.',
        45,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        164,
        '이번에는 바로 그 목소리가 들렸다.',
        46,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        165,
        '"왜 찾으려고 해?"',
        47,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        166,
        '민혜는 당황했다.',
        48,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        167,
        '"찾는 게 아니라... 도와주려고..."',
        49,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        168,
        '"내 뒤에 있어. 지금."',
        50,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        169,
        '민혜는 뒤를 돌아봤다.',
        51,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        170,
        '아무도 없었다.',
        52,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        171,
        '텅 빈 사무실.',
        53,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        172,
        '"장난하지 마세요."',
        54,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        173,
        '"장난이 아니야. 정말로 뒤에 있어."',
        55,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        174,
        '전화기 너머로 또 다른 소리가 들렸다.',
        56,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        175,
        '의자 바퀴 굴러가는 소리.',
        57,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        176,
        '민혜는 고개를 들었다.',
        58,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        177,
        '자신의 의자가 천천히 움직이고 있었다.',
        59,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        178,
        '아무도 건드리지 않았는데.',
        60,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        179,
        '전화기에서 웃음소리가 들렸다.',
        61,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        180,
        '"이제 알겠지?"',
        62,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        181,
        '민혜는 전화기를 내려놓으려 했다.',
        63,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        182,
        '하지만 손이 말을 듣지 않았다.',
        64,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        183,
        '전화기가 귀에 붙어 떨어지지 않았다.',
        65,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        184,
        '목소리가 다시 들렸다.',
        66,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        185,
        '"3개월 전에 죽었어. 이 자리에서."',
        67,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        186,
        '민혜는 기억났다.',
        68,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        187,
        '전임자가 갑자기 그만뒀다고 들었다.',
        69,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        188,
        '아니다. 그만둔 게 아니었다.',
        70,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        189,
        '"야간 근무 중에 심장마비로..."',
        71,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        190,
        '팀장의 말이 떠올랐다.',
        72,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        191,
        '"넌 내 자리에 앉아 있어."',
        73,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        192,
        '민혜는 의자에서 일어나려 했다.',
        74,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        193,
        '몸이 움직이지 않았다.',
        75,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        194,
        '전화기 너머로 속삭임이 들렸다.',
        76,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        195,
        '"이제 네 차례야."',
        77,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        196,
        '민혜의 심장이 빠르게 뛰기 시작했다.',
        78,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        197,
        '숨이 점점 가빠졌다.',
        79,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        198,
        '가슴이 아팠다.',
        80,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        199,
        '전화기에서 마지막 목소리가 들렸다.',
        81,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        200,
        '"고마워. 이제 나는 자유야."',
        82,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        201,
        '민혜는 의자에 고개를 떨어뜨렸다.',
        83,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        202,
        '다음 날 아침, 팀장이 발견했다.',
        84,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        203,
        '심장마비였다.',
        85,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        204,
        '그리고 한 달 후.',
        86,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        205,
        '새로운 야간 근무자가 들어왔다.',
        87,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        206,
        '새벽 2시 47분.',
        88,
        b'0',
        NULL,
        3,
        NULL
    ),
    (
        207,
        '전화벨이 울린다.',
        89,
        b'0',
        NULL,
        3,
        NULL
    );

INSERT IGNORE INTO
    quiz_detail (
        quiz_id,
        quiz_type,
        content_id,
        script_id,
        difficulty
    )
VALUES (
        37,
        'multiple_choice',
        3,
        132,
        1
    ),
    (
        38,
        'fill_in_blank',
        3,
        132,
        2
    ),
    (39, 'subjective', 3, 132, 3),
    (
        40,
        'multiple_choice',
        3,
        146,
        1
    ),
    (
        41,
        'fill_in_blank',
        3,
        146,
        2
    ),
    (42, 'subjective', 3, 146, 3),
    (
        43,
        'multiple_choice',
        3,
        160,
        1
    ),
    (
        44,
        'fill_in_blank',
        3,
        160,
        2
    ),
    (45, 'contextual', 3, 160, 3),
    (
        46,
        'multiple_choice',
        3,
        174,
        1
    ),
    (
        47,
        'fill_in_blank',
        3,
        174,
        2
    ),
    (48, 'contextual', 3, 174, 3),
    (
        49,
        'multiple_choice',
        3,
        188,
        1
    ),
    (
        50,
        'fill_in_blank',
        3,
        188,
        2
    ),
    (51, 'subjective', 3, 188, 3),
    (
        52,
        'multiple_choice',
        3,
        207,
        1
    ),
    (
        53,
        'fill_in_blank',
        3,
        207,
        2
    ),
    (54, 'contextual', 3, 207, 3);

INSERT IGNORE INTO
    quiz_content (
        quiz_id,
        lang_code,
        question,
        options,
        correct_answer,
        explanation,
        hint,
        acceptable_answers
    )
VALUES (
        37,
        'EN',
        '다음 중 ''이상한, 기묘한''의 영어 표현으로 알맞은 것은?',
        '["strange", "strong", "straight", "strict"]',
        'strange',
        '''strange''는 뭔가 이상하고 기묘할 때 쓰는 표현이야. 평소와 다른 이상한 전화가 온 상황이지.',
        '''쇼크 받았다''할 때 그 shock의 과거분사 ?',
        '["strange"]'
    ),
    (
        38,
        'EN',
        'Most calls were ______ calls from drunk people or wrong numbers.',
        '["prank", "fake", "joke", "trick"]',
        'prank',
        '''prank call''은 장난 전화라는 뜻의 고정 표현이야. fake call이나 joke call은 잘 안 써.',
        'p로 시작, ''공황''이라는 단어와 친척 관계 ?',
        '["prank"]'
    ),
    (
        39,
        'EN',
        '''정확히 같은 시각에''

Every night ______ ______ the same time.
(뜻: 정확히, 꼭 맞아떨어지는)',
        '[]',
        'at exactly',
        '''at exactly''는 정확히 그 시간에라는 뜻이야. at precisely, at sharp도 같은 의미로 쓸 수 있어.',
        'f로 시작하는 7글자, flutter(파닥파닥)처럼 마음이 흔들려서 어쩔 줄 모르는 상태 ?',
        '["at exactly", "at precisely", "at sharp", "exactly at", "precisely at"]'
    ),
    (
        40,
        'EN',
        '다음 중 ''긴장한, 불안한''을 뜻하는 영어 단어는?',
        '["nervous", "normal", "natural", "narrow"]',
        'nervous',
        '''nervous''는 긴장하거나 불안할 때 쓰는 표현이야. 손에 땀이 날 정도로 긴장한 상황을 나타내지.',
        '''MT''랑 발음이 같아',
        '["nervous"]'
    ),
    (
        41,
        'EN',
        'Her hands were ______ as she picked up the phone.',
        '["trembling", "shaking", "vibrating", "moving"]',
        'trembling',
        '''trembling''은 떨림을 나타내는 가장 자연스러운 표현이야. ''shaking''도 비슷하지만 ''trembling''이 두려움이나 긴장감을 더 잘 표현해.',
        '겨울왕국 영어 제목의 과거형! ?',
        '["trembling"]'
    ),
    (
        42,
        'EN',
        '''소리가 섞여 있다''

There were other sounds ______ ______ with the breathing.
(뜻: ~와 함께 섞이다)',
        '[]',
        'mixed in',
        '''mixed in with''는 다른 소리와 함께 섞여있다는 의미야. blended in, mingled in도 비슷한 뜻으로 쓸 수 있어.',
        'w___''t o___ (거부하다, 복종하지 않다)',
        '["mixed in", "blended in", "mingled in", "intertwined", "combined"]'
    ),
    (
        43,
        'EN',
        '다음 중 ''떨리는, 진동하는''을 뜻하는 영어 표현은?',
        '["trembling", "traveling", "troubling", "training"]',
        'trembling',
        '''trembling''은 무서워서 또는 불안해서 목소리나 몸이 떨리는 걸 표현할 때 써.',
        '''반지, 고리''랑 같은 단어야',
        '["trembling"]'
    ),
    (
        44,
        'EN',
        'The mysterious voice made her ______ run down her spine.',
        '["chills", "shivers", "goosebumps", "trembles"]',
        'chills',
        '''chills run down one''s spine''은 무서워서 소름이 돋는다는 뜻의 표현이야.',
        'r으로 시작, 전화가 울릴 때 쓰는 동사',
        '["chills"]'
    ),
    (
        45,
        'EN',
        '이야기 맥락에서 ''등에 소름이 돋았다''를 영어로 가장 자연스럽게 표현한 것은?',
        '["A chill ran down her spine.", "Her back was getting cold.", "She felt a breeze on her back.", "Her spine was hurting badly."]',
        'A chill ran down her spine.',
        '''a chill ran down one''s spine''은 무서움이나 충격으로 등골이 서늘해지는 걸 표현하는 관용구야.',
        'r___ (4글자) - 종이나 벨이 소리낼 때 쓰는 동사',
        '["A chill ran down her spine."]'
    ),
    (
        46,
        'EN',
        '다음 중 ''당황한, 혼란스러운''을 뜻하는 영어 표현은?',
        '["confused", "comfortable", "confident", "curious"]',
        'confused',
        '''confused''는 상황이 이해가 안 되거나 당황스러울 때 쓰는 표현이야.',
        'l로 시작하는 동사, ''보다''의 의미',
        '["confused"]'
    ),
    (
        47,
        'EN',
        'Her voice kept ______ in my head all night.',
        '["echoing", "speaking", "calling", "talking"]',
        'echoing',
        '''echo''는 소리가 계속 맴돌고 울린다는 뜻으로, 머릿속에서 계속 들리는 상황을 자연스럽게 표현해.',
        'd로 시작, 해가 뜨기 직전의 그 시간대 ?',
        '["echoing"]'
    ),
    (
        48,
        'EN',
        '이야기 맥락에서 ''목소리가 계속 맴돌았다''를 영어로 가장 자연스럽게 표현한 것은?',
        '["Her voice kept echoing in my mind.", "Her voice kept spinning around.", "Her voice kept turning in circles.", "Her voice kept walking around."]',
        'Her voice kept echoing in my mind.',
        '''echo in my mind''는 목소리나 말이 머릿속에서 계속 맴도는 걸 표현하는 자연스러운 영어야.',
        'l___ a_____ (2단어, 돌아보면서 확인한다는 뜻)',
        '["Her voice kept echoing in my mind."]'
    ),
    (
        49,
        'EN',
        '다음 중 ''말을 듣지 않다, 통제되지 않다''의 영어 표현으로 알맞은 것은?',
        '["obey", "disobey", "display", "destroy"]',
        'disobey',
        '''disobey''는 말을 듣지 않다, 따르지 않다는 뜻이야. 손이 자기 의지대로 움직이지 않는 상황을 표현할 때 쓸 수 있어.',
        'e로 시작하는 5글자 단어',
        '["disobey"]'
    ),
    (
        50,
        'EN',
        'She tried to hang up, but her hands wouldn''t ______ her.',
        '["obey", "follow", "listen", "hear"]',
        'obey',
        '''obey''는 손이 말을 듣지 않는다는 뜻으로, 몸이 의도대로 움직이지 않을 때 쓰는 표현이야.',
        'r로 시작하는 6글자, 음악 앨범을 만들 때도 쓰는 단어 ?',
        '["obey"]'
    ),
    (
        51,
        'EN',
        '''손이 말을 듣지 않았다''

Her hands wouldn''t ______ her.
(뜻: ~의 말을 듣다, 통제하다)',
        '[]',
        'obey',
        '''obey''는 명령이나 의지에 따라 움직이다라는 뜻이야. 몸이 마음대로 안 될 때 ''wouldn''t obey me''라고 표현해.',
        'm_____ (6글자, 평균을 나타내는 수학 용어와 같은 단어)',
        '["obey", "listen to", "respond to", "cooperate with"]'
    ),
    (
        52,
        'EN',
        '다음 중 ''숨이 가빠지다, 숨이 차다''를 영어로 표현할 때 알맞은 것은?',
        '["breathe heavily", "breathe clearly", "breathe slowly", "breathe quietly"]',
        'breathe heavily',
        '''breathe heavily''는 숨이 가빠지거나 거칠게 숨을 쉬는 걸 말해. 무서운 상황에서 심장이 빨리 뛰고 숨이 가빠질 때 쓰는 표현이야.',
        'q로 시작하는 5글자 단어',
        '["breathe heavily"]'
    ),
    (
        53,
        'EN',
        'Her heart began to ______ rapidly as she started breathing heavily.',
        '["pound", "strike", "knock", "hit"]',
        'pound',
        '''heart pounds''는 심장이 세게/빠르게 뛰다는 뜻으로, 두려움이나 긴장상황에서 자주 쓰이는 표현이야.',
        'e로 시작, ''exit''의 반대말! 들어가는 곳 ?',
        '["pound"]'
    ),
    (
        54,
        'EN',
        '민혜가 공포를 느끼며 심장이 빠르게 뛰고 숨이 가빠지는 상황을 영어로 가장 적절하게 표현한 것은?',
        '["Her heart began to race and her breathing became shallow.", "Her heart was warming up and she felt relaxed.", "Her heart stopped beating and she held her breath.", "Her heart was touched and she sighed deeply."]',
        'Her heart began to race and her breathing became shallow.',
        '''heart began to race''는 심장이 빠르게 뛰기 시작했다는 뜻이고, ''breathing became shallow''는 숨이 가빠졌다는 의미야. 공포나 긴장 상황에서 자주 쓰이는 표현이야.',
        'l_____ (과거분사, 누워있다는 뜻도 있음)',
        '["Her heart began to race and her breathing became shallow."]'
    );

-- ----- [추리물] 빨간 구두 -----
INSERT IGNORE INTO
    content (
        content_id,
        title,
        category_id,
        status,
        thumbnail_url
    )
VALUES (
        4,
        '빨간 구두',
        4,
        'ACTIVE',
        '/images/추리물_썸네일.png'
    );

INSERT IGNORE INTO
    script (
        script_id,
        script_content,
        sequence_num,
        has_options,
        character_name,
        content_id,
        option_id
    )
VALUES (
        208,
        '학교 청소부 박미란은 새벽 6시에 출근했다.',
        1,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        209,
        '1층 복도를 걸어가다가 발견했다.',
        2,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        210,
        '빨간 구두 한 짝.',
        3,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        211,
        '여학생 실내화가 아니었다. 성인용 하이힐이었다.',
        4,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        212,
        '미란은 주변을 살펴봤다.',
        5,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        213,
        '복도는 어젯밤에 깨끗이 청소했던 그대로였다.',
        6,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        214,
        '아무도 없는 새벽 학교에 누가?',
        7,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        215,
        '구두는 3학년 4반 교실 문 앞에 놓여 있었다.',
        8,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        216,
        '한 짝만. 오른쪽.',
        9,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        217,
        '미란이 교실 문을 열었다.',
        10,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        218,
        '칠판에 분필로 뭔가가 적혀 있었다.',
        11,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        219,
        '"7번 책상을 봐."',
        12,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        220,
        '7번 자리는 비어 있었다. 원래 결석이 많은 학생이었다.',
        13,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        221,
        '책상 위에 종이 한 장이 접혀 있었다.',
        14,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        222,
        '"체육관으로 와. 혼자."',
        15,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        223,
        '미란은 휴대폰으로 경비실에 연락했다.',
        16,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        224,
        '"김 아저씨, 어젯밤에 학교에 누가 들어왔나요?"',
        17,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        225,
        '"아뇨. 저 밤새 깨어있었는데 아무도 안 왔어요."',
        18,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        226,
        '그런데 보안 기록을 확인해보니 이상한 점이 있었다.',
        19,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        227,
        '어젯밤 11시 47분에 정문이 열렸다가 닫혔다.',
        20,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        228,
        '하지만 다시 나간 기록은 없었다.',
        21,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        229,
        '누군가 아직 학교 안에 있다는 뜻이었다.',
        22,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        230,
        '체육관으로 가는 길에 미란은 또 발견했다.',
        23,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        231,
        '왼쪽 구두.',
        24,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        232,
        '체육관 입구 앞에 떨어져 있었다.',
        25,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        233,
        '맨발로 들어간 거였다.',
        26,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        234,
        '체육관 안은 고요했다.',
        27,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        235,
        '미란이 전등을 켰다.',
        28,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        236,
        '농구대 밑에 누군가 앉아 있었다.',
        29,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        237,
        '20대 여성이었다. 긴 머리, 교복이 아닌 정장 차림.',
        30,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        238,
        '맨발이었다.',
        31,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        239,
        '"누구세요? 여기서 뭐 하는 거예요?"',
        32,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        240,
        '여자가 고개를 들었다.',
        33,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        241,
        '"전 이 학교 졸업생이에요. 17년 전에."',
        34,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        242,
        '"밤에 왜 학교에?"',
        35,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        243,
        '"3학년 4반 7번 자리에 앉던 학생이었어요."',
        36,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        244,
        '미란이 깨달았다.',
        37,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        245,
        '구두를 벗고 들어온 이유.',
        38,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        246,
        '"선생님한테 혼나지 않으려고 맨발로 다녔던 거군요."',
        39,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        247,
        '여자가 고개를 끄덕였다.',
        40,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        248,
        '"그때처럼 조용히 들어오고 싶었어요."',
        41,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        249,
        '"왜 하필 지금?"',
        42,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        250,
        '여자가 가방에서 신문을 꺼냈다.',
        43,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        251,
        '부고란이 펼쳐져 있었다.',
        44,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        252,
        '"담임 선생님이 돌아가셨대요."',
        45,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        253,
        '미란은 그제야 이해했다.',
        46,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        254,
        '"마지막 인사를 하려고 온 거구나."',
        47,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        255,
        '여자가 일어났다.',
        48,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        256,
        '"선생님이 항상 말씀하셨어요. 신발 소리 나지 않게 조용히 다니라고."',
        49,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        257,
        '"지금도 그 습관이 남아 있어서."',
        50,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        258,
        '미란이 빨간 구두를 주워왔다.',
        51,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        259,
        '"이거 신고 나가세요."',
        52,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        260,
        '여자가 구두를 신더니 한 번 돌아봤다.',
        53,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        261,
        '"고마워요. 그리고 죄송해요."',
        54,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        262,
        '정문이 열리고 닫히는 소리가 들렸다.',
        55,
        b'0',
        NULL,
        4,
        NULL
    ),
    (
        263,
        '미란은 3학년 4반으로 돌아가서 칠판을 지웠다.',
        56,
        b'0',
        NULL,
        4,
        NULL
    );

INSERT IGNORE INTO
    quiz_detail (
        quiz_id,
        quiz_type,
        content_id,
        script_id,
        difficulty
    )
VALUES (
        55,
        'multiple_choice',
        4,
        216,
        1
    ),
    (
        56,
        'fill_in_blank',
        4,
        216,
        2
    ),
    (57, 'subjective', 4, 216, 3),
    (
        58,
        'multiple_choice',
        4,
        225,
        1
    ),
    (
        59,
        'fill_in_blank',
        4,
        225,
        2
    ),
    (60, 'contextual', 4, 225, 3),
    (
        61,
        'multiple_choice',
        4,
        234,
        1
    ),
    (
        62,
        'fill_in_blank',
        4,
        234,
        2
    ),
    (63, 'contextual', 4, 234, 3),
    (
        64,
        'multiple_choice',
        4,
        243,
        1
    ),
    (
        65,
        'fill_in_blank',
        4,
        243,
        2
    ),
    (66, 'subjective', 4, 243, 3),
    (
        67,
        'multiple_choice',
        4,
        252,
        1
    ),
    (
        68,
        'fill_in_blank',
        4,
        252,
        2
    ),
    (69, 'contextual', 4, 252, 3),
    (
        70,
        'multiple_choice',
        4,
        263,
        1
    ),
    (
        71,
        'fill_in_blank',
        4,
        263,
        2
    ),
    (72, 'subjective', 4, 263, 3);

INSERT IGNORE INTO
    quiz_content (
        quiz_id,
        lang_code,
        question,
        options,
        correct_answer,
        explanation,
        hint,
        acceptable_answers
    )
VALUES (
        55,
        'EN',
        '다음 중 ''주변을 살펴보다, 둘러보다''의 영어 표현으로 알맞은 것은?',
        '["look around", "look after", "look forward", "look through"]',
        'look around',
        '''look around''는 주변을 둘러보거나 살펴본다는 뜻이야. 미란이 구두를 발견하고 주위를 확인하는 상황이지.',
        'r로 시작하는 7글자 단어',
        '["look around"]'
    ),
    (
        56,
        'EN',
        'The hallway was ______ as she had left it after cleaning the night before.',
        '["spotless", "messy", "crowded", "noisy"]',
        'spotless',
        '''spotless''는 완전히 깨끗한 상태를 나타내는 표현이야. 청소 후 깨끗한 상태를 묘사할 때 자주 쓰여.',
        '죽음을 알리는 신문 섹션, ''ob''로 시작해',
        '["spotless"]'
    ),
    (
        57,
        'EN',
        '''그대로였다'' (변화 없이 원래 상태)

The hallway was ______ ______ ______ it had been after last night''s cleaning.
(뜻: ~했던 그대로, 정확히 같은 상태로)',
        '[]',
        'exactly as',
        '''exactly as''는 ''그대로, 정확히 같은 상태로''라는 뜻이야. ''just as''나 ''the way''도 같은 의미로 쓸 수 있어.',
        'o_______ (사망자를 기리는 글, 기억하다는 뜻의 동사와 관련)',
        '["exactly as", "just as", "precisely as", "the way", "exactly how"]'
    ),
    (
        58,
        'EN',
        '다음 중 ''결석하다, 없다''를 뜻하는 영어 표현은?',
        '["absent", "present", "active", "angry"]',
        'absent',
        '''absent''는 학교나 회사에 나오지 않아서 없다는 뜻이야. 결석이 많은 학생을 표현할 때 쓰지.',
        'e로 시작하는 5글자 단어',
        '["absent"]'
    ),
    (
        59,
        'EN',
        'The security guard said he had been ______ all night and nobody came in.',
        '["awake", "alert", "conscious", "vigilant"]',
        'awake',
        '''밤새 깨어있었다''는 표현에서 ''stay awake all night'' 또는 ''be awake all night''이 가장 자연스러운 영어 표현이야.',
        'e로 시작, ''삭제하다/완전히 없애다''라는 뜻 ?',
        '["awake"]'
    ),
    (
        60,
        'EN',
        '경비아저씨가 ''밤새 깨어있었다''고 말하는 상황에서 가장 자연스러운 영어 표현은?',
        '["I was awake all night.", "I stayed up all night.", "I was up all night keeping watch.", "I didn''t sleep at all last night."]',
        'I was up all night keeping watch.',
        '경비원이 근무 중에 밤새 깨어서 지키고 있었다는 맥락에서는 ''keeping watch''가 포함된 표현이 가장 자연스러워.',
        'w___ o__ (2단어, 운동이나 연습할 때도 쓰는 표현)',
        '["I was up all night keeping watch."]'
    ),
    (
        61,
        'EN',
        '다음 중 ''이상한, 특이한''을 뜻하는 영어 단어는?',
        '["strange", "strong", "straight", "strict"]',
        'strange',
        '''strange''는 뭔가 이상하거나 특이할 때 쓰는 단어야. 보안 기록에서 평소와 다른 이상한 점을 발견한 상황이지.',
        'mystery(미스터리)의 형용사형',
        '["strange"]'
    ),
    (
        62,
        'EN',
        'There was no record of anyone leaving. That meant someone was still ______ the school premises.',
        '["on", "in", "at", "within"]',
        'on',
        '''on the premises''는 ''건물 내에, 구내에''라는 뜻의 자연스러운 표현이야.',
        '동전을 던질 때 쓰는 그 단어와 같아 ?',
        '["on"]'
    ),
    (
        63,
        'EN',
        '이 상황에서 ''누군가 아직 학교 안에 있다는 뜻이었다''를 영어로 가장 자연스럽게 표현한 것은?',
        '["It meant someone was still inside the school.", "It meant someone was always in the school.", "It meant someone was once in the school.", "It meant someone was never in the school."]',
        'It meant someone was still inside the school.',
        '''still''은 여전히, 아직도라는 뜻으로 누군가 계속 학교에 있다는 상황을 나타내.',
        'd_____ m_____ (첫 번째 단어는 ''두 배''라는 뜻)',
        '["It meant someone was still inside the school."]'
    ),
    (
        64,
        'EN',
        '다음 중 ''누군가, 어떤 사람''을 뜻하는 영어 표현은?',
        '["someone", "something", "somewhere", "somehow"]',
        'someone',
        '''someone''은 정체를 모르는 사람을 가리킬 때 쓰는 표현이야. 농구대 밑에 정체불명의 사람이 앉아있는 상황이지.',
        'coin(동전)이 들어있는 단어 ? 동전 던지기처럼 우연히 겹치는 일 ?',
        '["someone"]'
    ),
    (
        65,
        'EN',
        'She was wearing ______ attire instead of a school uniform.',
        '["formal", "casual", "sports", "traditional"]',
        'formal',
        '''formal attire''는 정장 차림이라는 뜻이야. 교복 대신 격식 있는 옷을 입고 있다는 상황이지.',
        't으로 시작, 종양도 이 단어를 써서 표현해',
        '["formal"]'
    ),
    (
        66,
        'EN',
        '''졸업생이에요''

I''m a ______ of this school.
(뜻: 졸업한 사람)',
        '[]',
        'graduate',
        '''graduate''는 졸업생을 뜻해. 여성 졸업생은 ''alumna'', 복수나 성별 구분 없이는 ''alumni''라고도 해.',
        't_____ d___ (넘어져서 아래로 굴러간다는 뜻)',
        '["graduate", "alumna", "alumni"]'
    ),
    (
        67,
        'EN',
        '다음 중 ''깨닫다, 알아차리다''의 영어 표현으로 알맞은 것은?',
        '["realize", "receive", "remember", "recognize"]',
        'realize',
        '''realize''는 무언가를 갑자기 깨닫거나 알아차릴 때 쓰는 표현이야. 미란이 진실을 깨달은 상황이지.',
        'grade(등급)와 같은 어원 ? 한 단계씩 천천히 ?',
        '["realize"]'
    ),
    (
        68,
        'EN',
        'She pulled out a newspaper from her bag. The ______ section was open.',
        '["obituary", "editorial", "classified", "sports"]',
        'obituary',
        '''obituary''는 부고란, 즉 사망 소식을 알리는 신문 섹션을 뜻해. ''obituary section''이 부고란이야.',
        'conscious의 반대, 프로이드가 연구한 의식 아래 숨겨진 마음의 영역 ?',
        '["obituary"]'
    ),
    (
        69,
        'EN',
        '이야기 맥락에서 ''부고란이 펼쳐져 있었다''를 영어로 가장 자연스럽게 표현한 것은?',
        '["The obituary section was spread open.", "The death notice was expanding.", "The funeral paper was displayed.", "The memorial column was extended."]',
        'The obituary section was spread open.',
        '''obituary section''은 신문의 부고란을 뜻하고, ''spread open''은 펼쳐져 있는 상태를 자연스럽게 표현해.',
        'c_______ (9글자, connect의 명사형)',
        '["The obituary section was spread open."]'
    ),
    (
        70,
        'EN',
        '다음 중 ''그제야, 마침내''를 뜻하는 영어 표현은?',
        '["finally", "firstly", "formerly", "frequently"]',
        'finally',
        '''finally''는 마침내, 그제야라는 뜻이야. 미란이 상황을 이해하게 된 순간을 표현하는 거지.',
        '우리나라에서도 놀랄 때 흔히 쓰이는 단어야',
        '["finally"]'
    ),
    (
        71,
        'EN',
        'The habit is still ______ with me after all these years.',
        '["stuck", "staying", "keeping", "holding"]',
        'stuck',
        '''stuck with me''는 습관이나 기억이 여전히 남아있다는 뜻의 자연스러운 영어 표현이야.',
        'manual(수동의)과 같은 어원 mani(손) ? 손으로 교묘히 다루다 ?',
        '["stuck"]'
    ),
    (
        72,
        'EN',
        '''마지막 인사를 하다''

She came to ______ her final farewell.
(뜻: 작별 인사를 하다)',
        '[]',
        'bid',
        '''bid farewell''은 작별 인사를 한다는 격식있는 표현이야. say farewell, give farewell도 쓸 수 있어.',
        'a________ t_ (전치사구, ''~에 따르면''이란 뜻도 있어)',
        '["bid", "say", "give", "make"]'
    );

-- ----- [특이한연애썰] 남자친구가 제 꿈을 예측해요 -----
INSERT IGNORE INTO
    content (
        content_id,
        title,
        category_id,
        status,
        thumbnail_url
    )
VALUES (
        5,
        '남자친구가 제 꿈을 예측해요',
        5,
        'ACTIVE',
        '/images/특이한연애썰_썸네일.png'
    );

INSERT IGNORE INTO
    script (
        script_id,
        script_content,
        sequence_num,
        has_options,
        character_name,
        content_id,
        option_id
    )
VALUES (
        264,
        '저는 23살 여자고요, 남자친구랑 사귄 지 6개월 됐어요.',
        1,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        265,
        '남자친구가 제 꿈을 미리 알아요.',
        2,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        266,
        '처음 설명할게요. 남자친구는 예술대학에서 심리학을 복수전공하는 애예요.',
        3,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        267,
        '성격이 되게 신비로운 타입이거든요.',
        4,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        268,
        '첫 만남에서부터 뭔가 특이했어요.',
        5,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        269,
        '"오늘 밤에 파란색 나비 꿈을 꿀 것 같은데요?" 이렇게 말하더라고요.',
        6,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        270,
        '저는 그냥 "아 네...ㅋㅋㅋ" 이랬거든요.',
        7,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        271,
        '그런데 진짜로 그날 밤에 파란 나비가 나오는 꿈을 꿨어요.',
        8,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        272,
        '우연이겠지 하고 넘어갔는데.',
        9,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        273,
        '두 번째 만남에서 또 말하는 거예요.',
        10,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        274,
        '"혹시 어릴 때 살던 집 나오는 꿈 꾸지 않았어요?"',
        11,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        275,
        '맞았어요. 진짜 초등학교 때 살던 빌라가 나왔어요.',
        12,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        276,
        '세 번째 만남에서는 "계단에서 떨어지는 꿈 조심하세요" 이러더라고요.',
        13,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        277,
        '그날 밤에 진짜 꿈에서 계단을 굴러떨어졌어요.',
        14,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        278,
        '이게 우연일까요?',
        15,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        279,
        '사귀고 나서도 계속됐어요.',
        16,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        280,
        '매일 자기 전에 카톡이 와요.',
        17,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        281,
        '"오늘은 물 관련 꿈일 것 같아" "검은 고양이 나올 거야" 이런 식으로.',
        18,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        282,
        '정확도가 80% 정도 돼요.',
        19,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        283,
        '처음엔 신기했는데 점점 무서워지기 시작했어요.',
        20,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        284,
        '어떻게 이런 게 가능한 거예요?',
        21,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        285,
        '남자친구한테 직접 물어봤어요.',
        22,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        286,
        '"야 솔직히 말해. 너 뭐야? 초능력자야?"',
        23,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        287,
        '그랬더니 웃으면서 "그냥 관찰하는 거야" 이러더라고요.',
        24,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        288,
        '"관찰이 뭔데?"',
        25,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        289,
        '"네가 하루 종일 뭘 했는지, 뭘 먹었는지, 무슨 영화 봤는지 들으면 꿈이 예측돼"',
        26,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        290,
        '"심리학으로 배운 거야. 무의식이랑 꿈의 연관성 같은 거"',
        27,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        291,
        '그래도 이상했어요.',
        28,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        292,
        '그런데 한 달 전에 충격적인 걸 발견했어요.',
        29,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        293,
        '남자친구 집에 갔는데 책상에 노트가 있었어요.',
        30,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        294,
        '제목이 "○○이 꿈 패턴 분석"이었어요. 제 이름이 들어간.',
        31,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        295,
        '펼쳐보니까 날짜별로 제 하루 일과랑 그날 밤 꿈이 정리되어 있었어요.',
        32,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        296,
        '"3월 5일: 매운 음식 + 스트레스 → 쫓기는 꿈 (적중)"',
        33,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        297,
        '"3월 12일: 옛날 사진 봄 + 향수 → 고향 관련 꿈 (적중)"',
        34,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        298,
        '이런 식으로 3개월치가 빼곡히 적혀있었어요.',
        35,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        299,
        '그리고 맨 뒤 페이지에 "꿈 조작 실험"이라고 써 있었어요.',
        36,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        300,
        '뭐냐면, 제가 특정 꿈을 꿀 만한 상황을 일부러 만드는 거였어요.',
        37,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        301,
        '데이트할 때 일부러 파란색 옷을 입고 나비 장식품을 보여준다든지.',
        38,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        302,
        '옛날 이야기를 꺼낸다든지.',
        39,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        303,
        '제 꿈을 예측하는 게 아니라 조종하고 있었던 거예요.',
        40,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        304,
        '남자친구한테 따졌더니 "미안해, 근데 네 반응이 너무 귀여워서" 이러더라고요.',
        41,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        305,
        '"그리고 이거 덕분에 네 심리 상태도 더 잘 알게 되고"',
        42,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        306,
        '"나쁜 의도는 아니었어"',
        43,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        307,
        '이 사람 저를 실험용 쥐로 본 건가요?',
        44,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        308,
        '근데 또 나쁜 사람은 아닌 것 같아요.',
        45,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        309,
        '제가 스트레스받을 때 미리 알고 챙겨주거든요.',
        46,
        b'0',
        NULL,
        5,
        NULL
    ),
    (
        310,
        '어떻게 해야 할까요...?',
        47,
        b'0',
        NULL,
        5,
        NULL
    );

INSERT IGNORE INTO
    quiz_detail (
        quiz_id,
        quiz_type,
        content_id,
        script_id,
        difficulty
    )
VALUES (
        73,
        'multiple_choice',
        5,
        270,
        1
    ),
    (
        74,
        'fill_in_blank',
        5,
        270,
        2
    ),
    (75, 'contextual', 5, 270, 3),
    (
        76,
        'multiple_choice',
        5,
        277,
        1
    ),
    (
        77,
        'fill_in_blank',
        5,
        277,
        2
    ),
    (78, 'contextual', 5, 277, 3),
    (
        79,
        'multiple_choice',
        5,
        284,
        1
    ),
    (
        80,
        'fill_in_blank',
        5,
        284,
        2
    ),
    (81, 'subjective', 5, 284, 3),
    (
        82,
        'multiple_choice',
        5,
        291,
        1
    ),
    (
        83,
        'fill_in_blank',
        5,
        291,
        2
    ),
    (84, 'contextual', 5, 291, 3),
    (
        85,
        'multiple_choice',
        5,
        298,
        1
    ),
    (
        86,
        'fill_in_blank',
        5,
        298,
        2
    ),
    (87, 'subjective', 5, 298, 3),
    (
        88,
        'multiple_choice',
        5,
        310,
        1
    ),
    (
        89,
        'fill_in_blank',
        5,
        310,
        2
    ),
    (90, 'subjective', 5, 310, 3);

INSERT IGNORE INTO
    quiz_content (
        quiz_id,
        lang_code,
        question,
        options,
        correct_answer,
        explanation,
        hint,
        acceptable_answers
    )
VALUES (
        73,
        'EN',
        '다음 중 ''신비로운, 불가사의한''의 영어 표현으로 알맞은 것은?',
        '["mysterious", "marvelous", "magnificent", "mischievous"]',
        'mysterious',
        '''mysterious''는 신비롭고 이해하기 어려운 성격이나 상황을 나타낼 때 쓰는 말이야.',
        '''I wonder why~(왜일까~)'' 할 때 쓰는 동사, wonderful의 원형 ?',
        '["mysterious"]'
    ),
    (
        74,
        'EN',
        'He''s an art student who is ______ in psychology as well.',
        '["minoring", "majoring", "specializing", "concentrating"]',
        'minoring',
        '''minor in''은 복수전공하다라는 뜻이야. 주전공(major)과 함께 부전공으로 공부하는 걸 말해.',
        '선택의 순간에 쓰는 단어, ''shall''과 비슷해',
        '["minoring"]'
    ),
    (
        75,
        'EN',
        '이야기 맥락에서 ''신비로운 성격''을 영어로 가장 자연스럽게 표현한 것은?',
        '["He has a mysterious personality.", "He has a suspicious personality.", "He has a curious personality.", "He has a serious personality."]',
        'He has a mysterious personality.',
        '''mysterious''는 신비롭고 이해하기 어려운 성격을 표현할 때 쓰는 가장 자연스러운 표현이야.',
        's_____ d_ (조언을 구할 때 가장 기본적인 표현)',
        '["He has a mysterious personality."]'
    ),
    (
        76,
        'EN',
        '다음 중 ''우연한, 우연의''를 뜻하는 영어 단어는?',
        '["accidental", "artificial", "additional", "automatic"]',
        'accidental',
        '''accidental''은 우연히 일어나는 일을 뜻해. 꿈 예측이 맞아도 그냥 우연이라고 생각한 상황이지.',
        '전구가 ''밝아질'' 때도 써요',
        '["accidental"]'
    ),
    (
        77,
        'EN',
        'I thought it was just a ______ and let it slide.',
        '["coincidence", "accident", "mistake", "surprise"]',
        'coincidence',
        '''coincidence''는 우연의 일치를 의미하고, ''let it slide''는 넘어가다라는 뜻의 표현이야.',
        't로 시작, ''teach''와 같은 어근을 가진 단어',
        '["coincidence"]'
    ),
    (
        78,
        'EN',
        '이야기 맥락에서 ''우연이겠지 하고 넘어갔다''를 영어로 가장 자연스럽게 표현한 것은?',
        '["I brushed it off as a coincidence.", "I passed by it as a coincidence.", "I moved over it as a coincidence.", "I crossed it as a coincidence."]',
        'I brushed it off as a coincidence.',
        '''brush off''는 대수롭지 않게 여기고 넘어간다는 뜻이야. 우연이라고 생각하며 별로 신경 쓰지 않았다는 의미를 잘 표현해.',
        'h___ (4글자, 고용하다라는 뜻도 있음)',
        '["I brushed it off as a coincidence."]'
    ),
    (
        79,
        'EN',
        '다음 중 ''정확함의 정도, 정확도''를 뜻하는 영어 단어는?',
        '["accuracy", "activity", "ability", "anxiety"]',
        'accuracy',
        '''accuracy''는 정확도나 정확함의 정도를 나타내는 단어야. 꿈 예측이 80% 맞는다는 상황에 딱 맞지.',
        '4개 중 발음이 어려울 거 같은 단어! ?',
        '["accuracy"]'
    ),
    (
        80,
        'EN',
        'His predictions have an ______ rate of about 80%.',
        '["accuracy", "efficiency", "frequency", "intensity"]',
        'accuracy',
        '''accuracy rate''는 정확도를 나타내는 표현이야. 예측이나 측정의 정확한 정도를 말할 때 써.',
        '바꾸다 c + 주제/제목을 뜻하는 s로 시작하는 단어',
        '["accuracy"]'
    ),
    (
        81,
        'EN',
        '''정확도가 80% 정도다''

His predictions have an ______ rate of about 80%.
(뜻: 정확성, 맞는 비율)',
        '[]',
        'accuracy',
        '''accuracy rate''는 정확도를 나타내는 표현이야. success rate도 비슷한 의미로 쓸 수 있어.',
        'c_____ (전환하다, 바꾸다라는 뜻의 동사)',
        '["accuracy", "accuracy rate", "success", "success rate"]'
    ),
    (
        82,
        'EN',
        '다음 중 ''예측하다, 예상하다''의 영어 표현으로 알맞은 것은?',
        '["predict", "prevent", "pretend", "prepare"]',
        'predict',
        '''predict''는 미래에 일어날 일을 미리 예측한다는 뜻이야. 남자친구가 꿈을 예측한다고 한 상황이지.',
        '게임에서 ''딜레이 걸렸다''할 때 그 delay의 과거형 ??',
        '["predict"]'
    ),
    (
        83,
        'EN',
        'He studied the ______ between the unconscious mind and dreams in psychology class.',
        '["connection", "correlation", "relationship", "association"]',
        'correlation',
        '''correlation''은 두 현상 간의 상관관계를 나타내는 학술적인 표현이야. 심리학에서 무의식과 꿈의 연관성을 설명할 때 자주 쓰여.',
        '''Excuse me'' 할 때 그 단어의 복수형, make와 짝꿍으로 쓰여',
        '["correlation"]'
    ),
    (
        84,
        'EN',
        '이야기에서 남자친구가 설명하는 ''무의식이랑 꿈의 연관성''을 영어로 가장 적절하게 표현한 것은?',
        '["The connection between the unconscious and dreams", "The conflict between consciousness and nightmares", "The difference between awareness and sleep", "The relationship between memory and imagination"]',
        'The connection between the unconscious and dreams',
        '''무의식이랑 꿈의 연관성''은 ''the connection between the unconscious and dreams''로 표현해. 심리학에서 자주 쓰이는 표현이야.',
        'm___ u_ (2단어, 만들다ing + 위로)',
        '["The connection between the unconscious and dreams"]'
    ),
    (
        85,
        'EN',
        '다음 중 ''충격적인, 놀라운''을 뜻하는 영어 표현은?',
        '["shocking", "sharing", "shining", "shouting"]',
        'shocking',
        '''shocking''은 충격적이고 놀라운 일을 발견했을 때 쓰는 표현이야.',
        'e로 시작하는 7글자 단어, ''흥분한'' 느낌',
        '["shocking"]'
    ),
    (
        86,
        'EN',
        'The notebook was ______ packed with three months'' worth of data.',
        '["densely", "loosely", "barely", "randomly"]',
        'densely',
        '''densely packed''는 빽빽하게 채워져 있다는 뜻으로, ''빼곡히''의 영어 표현이야.',
        '건강과 행복을 뜻하는 단어, w로 시작해',
        '["densely"]'
    ),
    (
        87,
        'EN',
        '''빼곡히 적혀있다''는 표현을 영어로 하면?

Three months'' worth of data was ______ ______ written in the notebook.
(뜻: 빽빽하게, 조밀하게)',
        '[]',
        'densely packed',
        '''densely packed''은 빼곡히, 빽빽하게 기록되어 있다는 뜻이야. tightly packed, closely packed도 같은 의미로 쓸 수 있어.',
        'c___ u_ (2단어, 전화로 상황 확인할 때 쓰는 표현)',
        '["densely packed", "tightly packed", "closely packed", "meticulously recorded", "thoroughly documented", "extensively written"]'
    ),
    (
        88,
        'EN',
        '다음 중 ''조종하다, 통제하다''의 영어 표현으로 알맞은 것은?',
        '["control", "contact", "contain", "compare"]',
        'control',
        '''control''은 누군가나 무언가를 조종하고 통제한다는 뜻이야. 남자친구가 꿈을 조종했던 상황이지.',
        'cert(증명서) + ain, certificate(자격증)과 같은 어원',
        '["control"]'
    ),
    (
        89,
        'EN',
        'When I ______ him about it, he just said "Sorry, but your reaction was so cute."',
        '["confronted", "approached", "questioned", "contacted"]',
        'confronted',
        '''confront''는 누군가에게 따지거나 문제를 제기할 때 쓰는 표현이야. 여기서는 남자친구의 행동에 대해 따져 물을 때 가장 적절한 동사야.',
        'd로 시작, ''명백히/분명히''라는 부사로도 쓰여',
        '["confronted"]'
    ),
    (
        90,
        'EN',
        '''남자친구한테 따졌다''

I ________ him about it.
(뜻: 문제를 제기하며 따지다, 맞서다)',
        '[]',
        'confronted',
        '''confront''는 문제에 대해 직접적으로 맞서서 따질 때 쓰는 표현이야. challenge나 call out도 비슷한 의미로 사용할 수 있어.',
        's_____ f____ (2단어, 생선 비린내를 떠올려봐 ?)',
        '["confronted", "challenged", "called out"]'
    );

-- ----- [연애갈등] 수학 과외 선생님과 내 여자친구 -----
INSERT IGNORE INTO
    content (
        content_id,
        title,
        category_id,
        status,
        thumbnail_url
    )
VALUES (
        6,
        '수학 과외 선생님과 내 여자친구',
        6,
        'ACTIVE',
        '/images/연애갈등_썸네일.png'
    );

INSERT IGNORE INTO
    script (
        script_id,
        script_content,
        sequence_num,
        has_options,
        character_name,
        content_id,
        option_id
    )
VALUES (
        311,
        '고3 여자친구랑 8개월 사귀고 있음.',
        1,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        312,
        '내가 대학교 1학년이고 걔가 고등학교 3학년인데 동네 선후배 사이였음.',
        2,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        313,
        '원래 공부 잘하는 애가 아니라서 부모님이 과외 선생님을 붙여줬다더라.',
        3,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        314,
        '수학 과외인데 대학생 형이 온다고 함.',
        4,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        315,
        '처음에는 그냥 좋았음. 성적이 올라가면 나도 뿌듯하니까.',
        5,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        316,
        '근데 몇 주 지나더니 과외 얘기를 자주 하기 시작함.',
        6,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        317,
        '"오빠가 문제 이렇게 설명해줘서 이해했어" 이런 식으로.',
        7,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        318,
        '뭔가 밝아진 느낌? 원래는 공부 얘기만 하면 짜증내던 애가.',
        8,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        319,
        '그래도 처음에는 공부에 흥미가 생긴 건가 싶었음.',
        9,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        320,
        '근데 하루는 데이트하는데 문자가 와서 "잠깐만" 하면서 답장하는 거임.',
        10,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        321,
        '누구냐고 물어봤더니 "과외 선생님이 숙제 관련해서 물어봤어" 이러더라.',
        11,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        322,
        '주말에도 과외와 관련된 문자를 주고받나?',
        12,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        323,
        '이상했지만 그냥 넘어갔음. 공부 열심히 하는 건 좋은 일이니까.',
        13,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        324,
        '그런데 이번 주에 걔 친구를 만났는데.',
        14,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        325,
        '"요즘 과외 어때?" 물어봤더니 친구가 뭔가 어색하게 웃더라.',
        15,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        326,
        '"그냥... 열심히 하는 것 같아" 이러는데 말투가 이상함.',
        16,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        327,
        '그래서 "과외 선생님은 어떤 분이야?" 물어봤더니.',
        17,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        328,
        '친구가 잠깐 멈추더니 "잘 모르겠어, 직접 물어봐" 이러고 화제를 돌리더라고.',
        18,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        329,
        '그날부터 신경이 쓰이기 시작함.',
        19,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        330,
        '과외 하는 날을 유심히 보니까 평소보다 화장을 진하게 함.',
        20,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        331,
        '원래는 민낯으로 다니는 애였는데.',
        21,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        332,
        '그리고 과외 끝나고 연락하면 답장이 늦어짐.',
        22,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        333,
        '"피곤해서 잠깐 잤어" 이런 핑계를 대는데.',
        23,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        334,
        '어느 날 과외 하는 날에 집 앞에 가봤음.',
        24,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        335,
        '걔 집 근처 카페에서 기다리면서 언제 나오나 봤거든.',
        25,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        336,
        '과외는 보통 2시간 한다고 했는데 3시간이 넘어도 안 나와.',
        26,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        337,
        '그래서 전화했더니 "아 지금 좀 어려운 문제 풀고 있어서 조금 늦어질 것 같아" 이러더라.',
        27,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        338,
        '전화 끊고 30분 더 기다리니까 드디어 나옴.',
        28,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        339,
        '그런데 과외 선생님도 같이 나오는 거임.',
        29,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        340,
        '멀어서 정확히는 안 보이는데 둘이 뭔가 얘기하면서 웃고 있더라.',
        30,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        341,
        '그리고 헤어지기 전에 잠깐 손을 마주쳤음. 악수인지 뭔지는 모르겠지만.',
        31,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        342,
        '집에 와서 "과외 어땠어?" 물어봤더니 "그냥 평소대로" 이러는 거임.',
        32,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        343,
        '"오늘 좀 늦게 끝났네?" 했더니 "응, 모르는 게 많아서" 이러더라.',
        33,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        344,
        '그런데 목소리가 뭔가 들떠 있었음.',
        34,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        345,
        '며칠 뒤에 걔 엄마한테 안부 인사차 전화했음.',
        35,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        346,
        '"과외 효과 있나요?" 물어봤더니 뭔가 이상한 말씀을 하시더라.',
        36,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        347,
        '"요즘 공부보다는 다른 걸 배우는 것 같아요" 이러시는 거임.',
        37,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        348,
        '무슨 뜻이냐고 했더니 "글쎄요, 성적은 그대로인데 기분이 좋아 보여요" 이러시더라.',
        38,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        349,
        '그래서 어제 과외 선생님 번호를 알아냈음.',
        39,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        350,
        '걔한테 "과외 선생님 연락처 좀 달라, 나도 과외받고 싶어" 이렇게 말해서.',
        40,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        351,
        '의심 안 하고 바로 줬는데.',
        41,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        352,
        '전화해서 "안녕하세요, 과외 받고 있는 학생 남자친구입니다" 이렇게 시작했음.',
        42,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        353,
        '그랬더니 목소리가 확 달라지더라. 뭔가 당황하는 느낌?',
        43,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        354,
        '"아... 네, 안녕하세요" 이러는데 어색함.',
        44,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        355,
        '"혹시 시간 되실 때 한 번 뵙고 과외 방식에 대해 상담받을 수 있을까요?" 했더니.',
        45,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        356,
        '"그게... 요즘 좀 바빠서... 나중에 연락드릴게요" 이러고 전화를 끊어버림.',
        46,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        357,
        '보통 과외 선생님이면 학부모나 지인이 연락하면 반가워해야 하는 거 아님?',
        47,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        358,
        '지금 여자친구한테 말해야 할지 그냥 넘어가야 할지 모르겠음.',
        48,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        359,
        '확실한 건 아무것도 없는데 괜히 의심하는 건가 싶기도 하고.',
        49,
        b'0',
        NULL,
        6,
        NULL
    ),
    (
        360,
        '근데 뭔가 이상한 건 분명함.',
        50,
        b'0',
        NULL,
        6,
        NULL
    );

INSERT IGNORE INTO
    quiz_detail (
        quiz_id,
        quiz_type,
        content_id,
        script_id,
        difficulty
    )
VALUES (
        91,
        'multiple_choice',
        6,
        318,
        1
    ),
    (
        92,
        'fill_in_blank',
        6,
        318,
        2
    ),
    (93, 'subjective', 6, 318, 3),
    (
        94,
        'multiple_choice',
        6,
        326,
        1
    ),
    (
        95,
        'fill_in_blank',
        6,
        326,
        2
    ),
    (96, 'subjective', 6, 326, 3),
    (
        97,
        'multiple_choice',
        6,
        334,
        1
    ),
    (
        98,
        'fill_in_blank',
        6,
        334,
        2
    ),
    (99, 'subjective', 6, 334, 3),
    (
        100,
        'multiple_choice',
        6,
        342,
        1
    ),
    (
        101,
        'fill_in_blank',
        6,
        342,
        2
    ),
    (102, 'subjective', 6, 342, 3),
    (
        103,
        'multiple_choice',
        6,
        350,
        1
    ),
    (
        104,
        'fill_in_blank',
        6,
        350,
        2
    ),
    (105, 'subjective', 6, 350, 3),
    (
        106,
        'multiple_choice',
        6,
        360,
        1
    ),
    (
        107,
        'fill_in_blank',
        6,
        360,
        2
    ),
    (108, 'contextual', 6, 360, 3);

INSERT IGNORE INTO
    quiz_content (
        quiz_id,
        lang_code,
        question,
        options,
        correct_answer,
        explanation,
        hint,
        acceptable_answers
    )
VALUES (
        91,
        'EN',
        '다음 중 ''짜증내다, 성가해하다''를 뜻하는 영어 표현은?',
        '["annoyed", "amused", "amazed", "ashamed"]',
        'annoyed',
        '''annoyed''는 짜증나고 성가할 때 느끼는 감정을 표현해. 공부 얘기에 짜증내던 상황이지.',
        NULL,
        '["annoyed"]'
    ),
    (
        92,
        'EN',
        'She wasn''t good at studying, so her parents ______ her with a tutor.',
        '["provided", "equipped", "supplied", "connected"]',
        'provided',
        '''provide someone with something''은 누군가에게 무언가를 제공해주다라는 뜻이야. 과외 선생님을 붙여준다는 상황에 가장 자연스러운 표현이지.',
        NULL,
        '["provided"]'
    ),
    (
        93,
        'EN',
        '''밝아진 느낌''을 영어로 표현하기

She seemed to ______ ______ lately.
(뜻: 기분이 좋아지다, 밝아지다)',
        '[]',
        'light up',
        '''light up''은 사람이 기분 좋아져서 밝아지는 모습을 표현할 때 쓰는 자연스러운 표현이야. brighten up이나 cheer up도 비슷한 의미로 쓸 수 있어.',
        NULL,
        '["light up", "brighten up", "cheer up", "perk up"]'
    ),
    (
        94,
        'EN',
        '다음 중 ''어색한, 불편한''의 영어 표현으로 알맞은 것은?',
        '["awkward", "awesome", "ancient", "accurate"]',
        'awkward',
        '''awkward''는 상황이나 분위기가 어색하고 불편할 때 쓰는 표현이야. 친구가 뭔가 어색하게 반응한 상황이지.',
        NULL,
        '["awkward"]'
    ),
    (
        95,
        'EN',
        'It seemed strange, but I decided to ______ it slide since studying hard is a good thing.',
        '["let", "make", "put", "get"]',
        'let',
        '''let it slide''는 뭔가 이상하지만 그냥 넘어가다, 문제 삼지 않고 지나치다라는 뜻의 표현이야.',
        NULL,
        '["let"]'
    ),
    (
        96,
        'EN',
        '''그냥 넘어갔다'' (의심스럽지만 문제 삼지 않고 지나쳤다)

It was weird, but I just ______ it ______.
(뜻: ~을 그냥 넘어가다, 문제 삼지 않다)',
        '[]',
        'let slide',
        '''let it slide''는 뭔가 이상하거나 문제가 있어도 그냥 넘어가버린다는 뜻이야. let it go, brush it off도 비슷한 의미로 쓸 수 있어.',
        NULL,
        '["let slide", "let go", "brushed off", "shrugged off", "overlooked"]'
    ),
    (
        97,
        'EN',
        '다음 중 ''신경쓰이다, 걱정되다''를 뜻하는 영어 표현은?',
        '["bother", "brother", "butter", "better"]',
        'bother',
        '''bother''는 뭔가가 신경쓰이고 걱정될 때 쓰는 표현이야. 과외 선생님 때문에 마음이 불편해진 상황이지.',
        NULL,
        '["bother"]'
    ),
    (
        98,
        'EN',
        'From that day on, it started to ______ at me.',
        '["bother", "annoy", "nag", "worry"]',
        'nag',
        '''nag at someone''은 계속 신경 쓰이고 마음에 걸린다는 뜻의 표현이야.',
        NULL,
        '["nag"]'
    ),
    (
        99,
        'EN',
        '''신경이 쓰이기 시작했다''

From that day on, it started to ______ me.
(뜻: 신경 쓰이게 하다, 괴롭히다)',
        '[]',
        'bother',
        '''bother''는 뭔가 계속 신경 쓰이고 마음에 걸릴 때 쓰는 표현이야. bug나 nag at도 비슷한 의미로 쓸 수 있어.',
        NULL,
        '["bother", "bug", "nag at", "eat at", "trouble", "worry"]'
    ),
    (
        100,
        'EN',
        '다음 중 ''늦어지다, 지연되다''의 영어 표현으로 알맞은 것은?',
        '["delayed", "detailed", "decided", "deleted"]',
        'delayed',
        '''delayed''는 예정된 시간보다 늦어지거나 지연되는 걸 말해. 과외가 늦어진다고 할 때 쓰는 표현이야.',
        NULL,
        '["delayed"]'
    ),
    (
        101,
        'EN',
        'The tutoring session was supposed to last 2 hours, but it ______ over 3 hours.',
        '["ran", "went", "took", "lasted"]',
        'ran',
        '''run over''는 예정된 시간을 초과하다는 뜻의 숙어야. ''The meeting ran over''처럼 자주 쓰이지.',
        NULL,
        '["ran"]'
    ),
    (
        102,
        'EN',
        '''정확히는 안 보인다''를 영어로 표현할 때

I couldn''t ______ ______ clearly from that distance.
(뜻: 명확하게/정확하게 보다)',
        '[]',
        'make out',
        '''make out''은 멀리서 뭔가를 정확히 구별해서 보다라는 뜻이야. 거리 때문에 선명하게 보기 어려운 상황에서 자주 써.',
        NULL,
        '["make out", "see them", "tell exactly", "observe them"]'
    ),
    (
        103,
        'EN',
        '다음 중 ''들뜬, 흥분한''의 영어 표현으로 알맞은 것은?',
        '["excited", "exhausted", "expected", "expired"]',
        'excited',
        '''excited''는 기분이 좋아서 들떠 있는 상태를 말해. 여자친구 목소리가 평소와 달리 들떠 있었던 거지.',
        NULL,
        '["excited"]'
    ),
    (
        104,
        'EN',
        'A few days later, I called her mom to ______ how she was doing.',
        '["check in on", "look after", "keep up with", "follow up on"]',
        'check in on',
        '''check in on someone''은 누군가의 안부를 묻거나 상태를 확인한다는 뜻의 구동사야.',
        NULL,
        '["check in on"]'
    ),
    (
        105,
        'EN',
        '''연락처를 달라''는 표현

Could you give me his ______ ______? I want to get tutoring too.
(뜻: 연락 정보)',
        '[]',
        'contact information',
        '''contact information''은 연락처 정보를 뜻해. contact info, contact details도 같은 의미로 쓸 수 있어.',
        NULL,
        '["contact information", "contact info", "contact details", "phone number", "contact number"]'
    ),
    (
        106,
        'EN',
        '다음 중 ''당황한, 혼란스러운''의 영어 표현으로 알맞은 것은?',
        '["confused", "confident", "careful", "comfortable"]',
        'confused',
        '''confused''는 당황하거나 혼란스러운 상태를 나타내는 표현이야. 과외 선생님이 갑자기 남자친구라고 하니까 당황한 거지.',
        NULL,
        '["confused"]'
    ),
    (
        107,
        'EN',
        'His voice suddenly changed when I mentioned who I was. He sounded ______ and flustered.',
        '["taken aback", "excited", "confident", "pleased"]',
        'taken aback',
        '''taken aback''은 당황하거나 놀란 상태를 나타내는 표현이야. 과외 선생님이 남자친구라고 하니까 당황한 상황에 딱 맞아.',
        NULL,
        '["taken aback"]'
    ),
    (
        108,
        'EN',
        '이야기 맥락에서 ''목소리가 확 달라지고 당황하는 느낌''을 영어로 가장 자연스럽게 표현한 것은?',
        '["His voice suddenly changed and he sounded flustered.", "His voice became louder and more confident.", "His voice got softer and more romantic.", "His voice remained calm and professional."]',
        'His voice suddenly changed and he sounded flustered.',
        '''flustered''는 당황하고 어쩔 줄 몰라 하는 상태를 표현할 때 쓰는 자연스러운 영어 표현이야.',
        NULL,
        '["His voice suddenly changed and he sounded flustered."]'
    );

-- =============================================
-- 4. achievement_comment (진도율 구간별 성취 멘트)
-- =============================================
-- achievement_comment: PK가 AUTO_INCREMENT 라 INSERT IGNORE 가 못 막음.
-- 테이블이 비어있을 때만 시드 행을 일괄 삽입.
INSERT INTO
    achievement_comment (
        min_rate,
        max_rate,
        comment_text
    )
SELECT *
FROM (
        SELECT
            0 AS min_rate, 20 AS max_rate, '시작이 반이에요! 차근차근 가봐요' AS comment_text
        UNION ALL
        SELECT 21, 40, '꾸준히 진행 중이네요'
        UNION ALL
        SELECT 41, 60, '절반을 향해 잘 가고 있어요'
        UNION ALL
        SELECT 61, 80, '꽤 많이 익히셨네요!'
        UNION ALL
        SELECT 81, 99, '거의 다 왔어요!'
        UNION ALL
        SELECT 100, 100, '완벽해요! 다음 스토리도 도전해보세요'
    ) AS seed
WHERE
    NOT EXISTS (
        SELECT 1
        FROM achievement_comment
        LIMIT 1
    );

-- thumbnail_url 강제 갱신 (INSERT IGNORE로 인해 기존 row가 NULL로 남아있는 경우 대비)
UPDATE content
SET
    thumbnail_url = '/images/짝사랑_썸네일.png'
WHERE
    content_id = 1;

UPDATE content
SET
    thumbnail_url = '/images/드라마_썸네일.png'
WHERE
    content_id = 2;

UPDATE content
SET
    thumbnail_url = '/images/스릴러_썸네일.png'
WHERE
    content_id = 3;

UPDATE content
SET
    thumbnail_url = '/images/추리물_썸네일.png'
WHERE
    content_id = 4;

UPDATE content
SET
    thumbnail_url = '/images/특이한연애썰_썸네일.png'
WHERE
    content_id = 5;

UPDATE content
SET
    thumbnail_url = '/images/연애갈등_썸네일.png'
WHERE
    content_id = 6;

-- =============================================
-- 5. score_table
-- =============================================
INSERT IGNORE INTO
    score_table (
        difficulty,
        attempt,
        hint_used,
        score
    )
VALUES (1, 1, 0, 20),
    (1, 1, 1, 16),
    (1, 2, 0, 14),
    (1, 2, 1, 10),
    (1, 3, 0, 8),
    (1, 3, 1, 4),
    (1, 4, 0, 3),
    (2, 1, 0, 20),
    (2, 1, 1, 17),
    (2, 2, 0, 15),
    (2, 2, 1, 11),
    (2, 3, 0, 9),
    (2, 3, 1, 5),
    (2, 4, 0, 4),
    (3, 1, 0, 20),
    (3, 1, 1, 18),
    (3, 2, 0, 16),
    (3, 2, 1, 12),
    (3, 3, 0, 10),
    (3, 3, 1, 6),
    (3, 4, 0, 5);

SET FOREIGN_KEY_CHECKS = 1;

-- 완료!