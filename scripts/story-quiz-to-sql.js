#!/usr/bin/env node
/**
 * 스토리 대본·퀴즈 json → SQL 변환기 (이미 등록된 콘텐츠의 대본 문장 갱신 + 퀴즈 교체).
 *
 *   node scripts/story-quiz-to-sql.js <출력.sql> <json 파일...>
 *   예) node scripts/story-quiz-to-sql.js sql/story_quiz_import.sql C:/Users/kanta/Downloads/드라마_02.json C:/Users/kanta/Downloads/quiz_*_드라마_02.json ...
 *
 * 입력은 스토리 파일({title, sentences[]})과 퀴즈 파일({storyTitle, language, sourceFile, quizBreaks[]})을 섞어서 준다.
 * 퀴즈 파일은 sourceFile 로 스토리 파일과 묶인다.
 *
 * DB 대응 (2026-09-20 운영 스키마 기준):
 *   - content   : title 로 찾는다 (새로 만들지 않음). 없으면 NOT NULL 위반으로 실패 → 아무것도 안 들어감.
 *   - script    : 문장 수가 DB 와 같아야 한다 (다르면 실패). sequence_num 기준으로 script_content 만 UPDATE (script_id 유지 →
 *                 quiz_result.script_id 등 사용자 기록이 그대로 유효).
 *   - quiz_detail / quiz_content : 그 콘텐츠의 기존 퀴즈를 지우고 다시 넣는다. 지우기 전에 user_study_log.quiz_id 를 NULL 로
 *                 풀어 학습일 기록(연속 학습일)은 보존하고, wrong_answer(오답 노트)는 옛 문제를 가리키므로 삭제한다.
 *   - 퀴즈 위치  : afterSentenceIndex 는 0 부터 세므로 sequence_num = index + 1 인 script 뒤에 붙는다.
 *   - quiz_type : en 파일(첫 언어) 기준. 언어별로 다른 슬롯은 경고만 — 앱은 본문 모양(options 유무)으로 보정한다.
 *   - lang_code : 스토리 테이블은 대문자(EN/JA/ZH).
 *   - options 는 비면 NULL, acceptable_answers 는 비면 '[]' (기존 시드와 같은 규칙).
 *
 * 실행: mysql --default-character-set=utf8mb4 로 접속해 source. 파일에 COMMIT 이 없으므로 오류가 없었을 때만 COMMIT; 을 직접 입력.
 *       PowerShell 파이프(Get-Content | mysql) 금지 — CJK 가 ? 로 깨진다.
 */
const fs = require("fs");
const path = require("path");

const [outPath, ...inputs] = process.argv.slice(2);
if (!outPath || inputs.length === 0) {
  console.error("사용법: node scripts/story-quiz-to-sql.js <출력.sql> <json 파일...>");
  process.exit(2);
}

/* ---------- 읽기 & 묶기 ---------- */
const stories = new Map();   // sourceFile 이름 -> {file, title, sentences, quizzes: {EN: json, ...}}
const quizFiles = [];
for (const p of inputs) {
  const json = JSON.parse(fs.readFileSync(p, "utf8"));
  const name = path.basename(p);
  if (Array.isArray(json.sentences)) {
    stories.set(name, { file: name, title: json.title, sentences: json.sentences, quizzes: {} });
  } else if (Array.isArray(json.quizBreaks)) {
    quizFiles.push({ file: name, json });
  } else {
    throw new Error(`스토리(sentences)도 퀴즈(quizBreaks)도 아닌 파일: ${p}`);
  }
}
for (const { file, json } of quizFiles) {
  const story = stories.get(json.sourceFile);
  if (!story) throw new Error(`${file}: sourceFile "${json.sourceFile}" 에 해당하는 스토리 파일이 입력에 없음`);
  if (story.title !== json.storyTitle) throw new Error(`${file}: storyTitle "${json.storyTitle}" ≠ 스토리 파일 title "${story.title}"`);
  const lang = String(json.language).toUpperCase();
  if (story.quizzes[lang]) throw new Error(`${file}: ${story.file} 의 ${lang} 퀴즈가 중복`);
  story.quizzes[lang] = json;
}

/* ---------- 검증 ---------- */
const problems = [];
const suspicious = /[\u3040-\u30ff\u3400-\u9fff\u00c0-\u024f]\?|\?[\u3040-\u30ff\u3400-\u9fff\u00c0-\u024f]/;   // CJK·성조 옆 '?'
let typeDiffs = [];
for (const s of stories.values()) {
  const langs = Object.keys(s.quizzes).sort();
  if (langs.length === 0) problems.push(`${s.file}: 퀴즈 파일 없음`);
  if (s.sentences.some((t) => !t || !t.trim())) problems.push(`${s.file}: 빈 문장 있음`);
  const first = langs[0] && s.quizzes[langs[0]];
  for (const lang of langs) {
    const q = s.quizzes[lang];
    for (const b of q.quizBreaks) {
      const idx = b.afterSentenceIndex;
      if (!(idx >= 0 && idx < s.sentences.length)) problems.push(`${q.sourceFile} ${lang} break ${b.quizBreakIndex}: afterSentenceIndex ${idx} 범위 밖 (문장 ${s.sentences.length}개)`);
      else if (b.lastSentence && s.sentences[idx] !== b.lastSentence) problems.push(`${q.sourceFile} ${lang} break ${b.quizBreakIndex}: lastSentence 가 sentences[${idx}] 와 다름`);
      for (const [d, arr] of Object.entries(b.difficulties)) {
        if (!Array.isArray(arr) || arr.length !== 1) { problems.push(`${q.sourceFile} ${lang} break ${b.quizBreakIndex} 난이도 ${d}: 문제 수 ${arr && arr.length} (1 이어야 함)`); continue; }
        const x = arr[0];
        const hasOpt = Array.isArray(x.options) && x.options.length > 0;
        if (!x.question || !x.answer) problems.push(`${q.sourceFile} ${lang} b${b.quizBreakIndex} d${d}: question/answer 비어 있음`);
        if (x.type === "subjective" && hasOpt) problems.push(`${q.sourceFile} ${lang} b${b.quizBreakIndex} d${d}: subjective 인데 options 있음`);
        if (x.type !== "subjective" && !hasOpt) problems.push(`${q.sourceFile} ${lang} b${b.quizBreakIndex} d${d}: ${x.type} 인데 options 없음`);
        if (suspicious.test(JSON.stringify(x))) problems.push(`${q.sourceFile} ${lang} b${b.quizBreakIndex} d${d}: 글자 옆 '?' — 인코딩 깨짐 의심`);
        if (first && q !== first) {
          const e = first.quizBreaks.find((fb) => fb.quizBreakIndex === b.quizBreakIndex)?.difficulties?.[d]?.[0];
          if (!e) problems.push(`${q.sourceFile} ${lang} b${b.quizBreakIndex} d${d}: ${langs[0]} 에 같은 슬롯 없음`);
          else if (e.type !== x.type) typeDiffs.push(`${s.title} b${b.quizBreakIndex} d${d}: ${langs[0]}=${e.type} ${lang}=${x.type}`);
        }
      }
    }
  }
}
if (problems.length) {
  console.error(`검증 실패 ${problems.length}건 — SQL 을 만들지 않습니다.\n` + problems.join("\n"));
  process.exit(1);
}

/* ---------- SQL ---------- */
const esc = (s) => "'" + String(s).replace(/\\/g, "\\\\").replace(/'/g, "\\'") + "'";
const str = (s) => (s === undefined || s === null || s === "" ? "NULL" : esc(s));
const optionsSql = (list) => (Array.isArray(list) && list.length > 0 ? esc(JSON.stringify(list)) : "NULL");
const acceptableSql = (list) => esc(JSON.stringify(Array.isArray(list) ? list : []));

const L = [];
L.push(`-- 스토리 퀴즈 교체 (scripts/story-quiz-to-sql.js 가 생성, ${new Date().toISOString().slice(0, 10)})`);
L.push(`-- 대상: ${[...stories.values()].map((s) => `"${s.title}" (${s.file}, ${Object.keys(s.quizzes).sort().join("/")})`).join(", ")}`);
L.push(`-- 실행: mysql --default-character-set=utf8mb4 ... 로 접속 → source <이 파일>. 끝에 COMMIT 이 없다. 오류가 하나도 없었을 때만 COMMIT; 을 입력하고, 오류가 보이면 ROLLBACK;`);
L.push(`SET NAMES utf8mb4;`);
L.push(`START TRANSACTION;`);
L.push(``);

for (const s of stories.values()) {
  const langs = Object.keys(s.quizzes).sort();
  const first = s.quizzes[langs[0]];
  const n = s.sentences.length;
  L.push(`-- ============================================================`);
  L.push(`-- "${s.title}"  (${s.file}: 문장 ${n}개, 퀴즈 지점 ${first.quizBreaks.length}개 × 난이도 3, 언어 ${langs.join("/")})`);
  L.push(`-- ============================================================`);
  L.push(`SET @cid = (SELECT content_id FROM content WHERE title = ${esc(s.title)});`);
  L.push(`-- 안전장치: 콘텐츠가 없거나 대본 문장 수가 다르면 NOT NULL 위반으로 여기서 실패 (아무 행도 안 들어감)`);
  L.push(`INSERT INTO quiz_detail (quiz_type, content_id, script_id, difficulty)`);
  L.push(`  SELECT 'ASSERT_CONTENT_EXISTS', @cid, NULL, NULL FROM DUAL WHERE @cid IS NULL;`);
  L.push(`INSERT INTO quiz_detail (quiz_type, content_id, script_id, difficulty)`);
  L.push(`  SELECT 'ASSERT_SENTENCE_COUNT', @cid, NULL, NULL FROM DUAL WHERE (SELECT COUNT(*) FROM script WHERE content_id = @cid) <> ${n};`);
  L.push(``);
  L.push(`-- 1) 대본 문장 갱신 (script_id 유지)`);
  s.sentences.forEach((t, i) => {
    L.push(`UPDATE script SET script_content = ${esc(t)} WHERE content_id = @cid AND sequence_num = ${i + 1};`);
  });
  L.push(``);
  L.push(`-- 2) 기존 퀴즈 제거 (학습일 기록은 quiz_id 만 풀어서 보존, 오답 노트는 옛 문제라 삭제)`);
  L.push(`UPDATE user_study_log SET quiz_id = NULL WHERE quiz_id IN (SELECT quiz_id FROM quiz_detail WHERE content_id = @cid);`);
  L.push(`DELETE FROM wrong_answer WHERE content_id = @cid;`);
  L.push(`DELETE FROM quiz_content WHERE quiz_id IN (SELECT quiz_id FROM quiz_detail WHERE content_id = @cid);`);
  L.push(`DELETE FROM quiz_detail WHERE content_id = @cid;`);
  L.push(``);
  L.push(`-- 3) quiz_detail: (퀴즈 지점, 난이도) 당 한 줄. quiz_type 은 ${langs[0]} 기준`);
  L.push(`INSERT INTO quiz_detail (quiz_type, content_id, script_id, difficulty) VALUES`);
  const detailRows = [];
  for (const b of first.quizBreaks) {
    for (const d of Object.keys(b.difficulties).sort()) {
      const x = b.difficulties[d][0];
      detailRows.push(`  (${esc(x.type)}, @cid, (SELECT script_id FROM script WHERE content_id = @cid AND sequence_num = ${b.afterSentenceIndex + 1}), ${Number(d)})`);
    }
  }
  L.push(detailRows.join(",\n") + ";");
  L.push(``);
  for (const lang of langs) {
    const q = s.quizzes[lang];
    L.push(`-- 4) quiz_content ${lang}`);
    L.push(`INSERT INTO quiz_content (quiz_id, lang_code, question, options, correct_answer, explanation, hint, acceptable_answers) VALUES`);
    const rows = [];
    for (const b of q.quizBreaks) {
      for (const d of Object.keys(b.difficulties).sort()) {
        const x = b.difficulties[d][0];
        const quizId = `(SELECT quiz_id FROM quiz_detail WHERE content_id = @cid AND difficulty = ${Number(d)} AND script_id = (SELECT script_id FROM script WHERE content_id = @cid AND sequence_num = ${b.afterSentenceIndex + 1}))`;
        rows.push(`  (${quizId}, ${esc(lang)}, ${esc(x.question)}, ${optionsSql(x.options)}, ${esc(x.answer)}, ${str(x.explanation)}, ${str(x.hint)}, ${acceptableSql(x.acceptableAnswers)})`);
      }
    }
    L.push(rows.join(",\n") + ";");
    L.push(``);
  }
}

L.push(`-- 확인 후 직접 입력: COMMIT;   (오류가 있었으면 ROLLBACK;)`);
L.push(`SELECT c.title, q.difficulty, qc.lang_code, COUNT(*) AS n`);
L.push(`  FROM quiz_detail q JOIN quiz_content qc ON qc.quiz_id = q.quiz_id JOIN content c ON c.content_id = q.content_id`);
L.push(` WHERE c.title IN (${[...stories.values()].map((s) => esc(s.title)).join(", ")})`);
L.push(` GROUP BY c.title, q.difficulty, qc.lang_code ORDER BY c.title, q.difficulty, qc.lang_code;`);

fs.writeFileSync(outPath, L.join("\n") + "\n", { encoding: "utf8" });
const totalQuiz = [...stories.values()].reduce((n, s) => n + Object.values(s.quizzes).reduce((m, q) => m + q.totalQuestions, 0), 0);
console.log(`생성 완료: ${outPath}  (스토리 ${stories.size}개, 대본 문장 ${[...stories.values()].reduce((n, s) => n + s.sentences.length, 0)}줄, 퀴즈 ${totalQuiz}문항)`);
if (typeDiffs.length) console.log(`  참고: 언어별 유형이 다른 슬롯 ${typeDiffs.length}건 (앱이 options 유무로 보정)\n    ` + typeDiffs.join("\n    "));
