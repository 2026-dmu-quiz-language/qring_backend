#!/usr/bin/env node
/**
 * 봇 컴피티션 문제 json → SQL 변환기.
 *
 *   node scripts/competition-quiz-to-sql.js <출력.sql> <json 파일...>
 *   예) node scripts/competition-quiz-to-sql.js sql/competition_quiz_import.sql C:/Users/kanta/Downloads/competition_quiz_*_0*.json
 *
 * - 세트 키(set_key)는 파일명 접미사에서 뽑는다: competition_quiz_ja_02.json → "02".
 *   같은 세트의 언어 파일은 같은 (set_key, level, id) 의 detail 한 줄에 매달린다.
 * - detail.quiz_type 은 그 세트에서 먼저 나온 언어(보통 en) 기준. 출제는 본문 모양으로 유형을 정하므로 참고값이다.
 * - CompetitionQuizImportService 와 같은 규칙: 빈 배열은 NULL, JSON 은 Jackson 과 같은 compact 형식.
 * - 저장 전 검사(서비스와 동일): 유형 ↔ options/tiles 모양, 그리고 성조·CJK 옆의 '?'(인코딩 깨짐 흔적). 하나라도 걸리면 파일을 만들지 않는다.
 * - 실행은 반드시 utf8mb4 로. mysql 클라이언트를 --default-character-set=utf8mb4 로 띄운 뒤 `source 출력.sql`,
 *   또는 cmd.exe 에서 `mysql --default-character-set=utf8mb4 ... < 출력.sql`.
 *   PowerShell 의 `Get-Content | mysql` 파이프는 금지 — 5.1 은 파이프를 ASCII/cp949 로 다시 인코딩해 CJK 가 ? 로 깨진다 (지난번 원인과 동일).
 */
const fs = require("fs");
const path = require("path");

const [outPath, ...inputs] = process.argv.slice(2);
if (!outPath || inputs.length === 0) {
  console.error("사용법: node scripts/competition-quiz-to-sql.js <출력.sql> <json 파일...>");
  process.exit(2);
}

const files = inputs.map((p) => {
  const m = /_([0-9A-Za-z]+)\.json$/i.exec(path.basename(p));
  if (!m) throw new Error(`파일명에서 세트 키를 찾지 못함 (예: competition_quiz_ja_02.json): ${p}`);
  const buf = fs.readFileSync(p);
  const json = JSON.parse(buf.toString("utf8"));
  if (!json.language || !Array.isArray(json.levels)) throw new Error(`json 구조가 다름 (language/levels): ${p}`);
  return { path: p, setKey: m[1], lang: json.language, levels: json.levels };
});

/* ---------- 검증 (서비스의 rejectIfInconsistent 와 동일 + 인코딩 흔적) ---------- */
const problems = [];
const suspicious = /[A-Za-z\u3040-\u30ff\u3400-\u9fff\u00c0-\u024f]\?|\?[A-Za-z\u3040-\u30ff\u3400-\u9fff\u00c0-\u024f]/;
for (const f of files) {
  for (const L of f.levels) {
    for (const q of L.questions) {
      const where = `${path.basename(f.path)} level=${L.level} id=${q.id}`;
      const hasOptions = Array.isArray(q.options) && q.options.length > 0;
      const hasTiles = Array.isArray(q.tiles) && q.tiles.length > 0;
      const shape =
        q.type === "multiple_choice" ? (hasOptions ? null : "multiple_choice 인데 options 없음")
        : q.type === "word_arrange" ? (hasTiles ? null : "word_arrange 인데 tiles 없음")
        : q.type === "subjective" ? ((hasOptions || hasTiles) ? "subjective 인데 options/tiles 있음" : null)
        : `알 수 없는 type: ${q.type}`;
      if (shape) problems.push(`${where}: ${shape}`);
      if (!q.question || !q.answer) problems.push(`${where}: question/answer 비어 있음`);
      const text = JSON.stringify(q);
      if (suspicious.test(text)) problems.push(`${where}: 글자 옆에 '?' — 인코딩 깨짐 의심: ${text.match(suspicious)[0]}`);
    }
  }
}
if (problems.length) {
  console.error(`검증 실패 ${problems.length}건 — SQL 을 만들지 않습니다.\n` + problems.join("\n"));
  process.exit(1);
}

/* ---------- SQL 생성 ---------- */
const esc = (s) => "'" + String(s).replace(/\\/g, "\\\\").replace(/'/g, "\\'") + "'";
const str = (s) => (s === undefined || s === null ? "NULL" : esc(s));
const jsonOrNull = (list) => (Array.isArray(list) && list.length > 0 ? esc(JSON.stringify(list)) : "NULL");

// detail: (set, level, id) 당 한 줄, 유형은 먼저 나온 파일 기준. 같은 세트 안에서 언어별 유형이 달라도 경고만 (서비스와 동일).
const details = new Map();       // key -> {setKey, level, id, type, from}
let typeDiff = 0;
for (const f of files) {
  for (const L of f.levels) {
    for (const q of L.questions) {
      const key = `${f.setKey}|${L.level}|${q.id}`;
      const d = details.get(key);
      if (!d) details.set(key, { setKey: f.setKey, level: L.level, id: q.id, type: q.type, from: f.lang });
      else if (d.type !== q.type) typeDiff++;
    }
  }
}

const lines = [];
lines.push(`-- 봇 컴피티션 문제 import (scripts/competition-quiz-to-sql.js 가 생성, ${new Date().toISOString().slice(0, 10)})`);
lines.push(`-- 원본: ${files.map((f) => `${path.basename(f.path)} [set ${f.setKey}, ${f.lang}]`).join(", ")}`);
lines.push(`-- 실행: mysql --default-character-set=utf8mb4 -h <host> -u admin -p qring_db 로 접속한 뒤  source <이 파일 경로>  (PowerShell 파이프 금지: CJK 가 ? 로 깨짐)`);
lines.push(`-- 전제: competition_quiz_detail 에 set_key 컬럼과 UNIQUE(set_key, level, origin_id) 가 있어야 함 (sql/competition_quiz_prepare.sql)`);
lines.push(`SET NAMES utf8mb4;`);
lines.push(`START TRANSACTION;`);
lines.push(``);

// 1) detail
for (const setKey of [...new Set(files.map((f) => f.setKey))].sort()) {
  const rows = [...details.values()].filter((d) => d.setKey === setKey).sort((a, b) => a.level - b.level || a.id - b.id);
  lines.push(`-- detail: set ${setKey} (${rows.length}건, quiz_type 은 ${rows[0].from} 파일 기준)`);
  lines.push(`INSERT INTO competition_quiz_detail (set_key, level, origin_id, quiz_type) VALUES`);
  lines.push(rows.map((d) => `  (${esc(d.setKey)}, ${d.level}, ${d.id}, ${esc(d.type)})`).join(",\n") + ";");
  lines.push(``);
}

// 2) content — quiz_id 는 서브쿼리로 연결 (id 를 미리 알 필요 없음)
const cols = "quiz_id, lang_code, question, korean, tiles, answer_tiles, distractor_tiles, options, answer, acceptable_answers";
let contentRows = 0;
for (const f of files) {
  for (const L of f.levels) {
    lines.push(`-- content: ${path.basename(f.path)} level ${L.level} (${L.questions.length}건)`);
    lines.push(`INSERT INTO competition_quiz_content (${cols}) VALUES`);
    const rows = L.questions.map((q) => {
      contentRows++;
      const quizId = `(SELECT quiz_id FROM competition_quiz_detail WHERE set_key = ${esc(f.setKey)} AND level = ${L.level} AND origin_id = ${q.id})`;
      return `  (${quizId}, ${esc(f.lang)}, ${esc(q.question)}, ${str(q.korean)}, ${jsonOrNull(q.tiles)}, ${jsonOrNull(q.answerTiles)}, ${jsonOrNull(q.distractorTiles)}, ${jsonOrNull(q.options)}, ${esc(q.answer)}, ${jsonOrNull(q.acceptableAnswers)})`;
    });
    lines.push(rows.join(",\n") + ";");
    lines.push(``);
  }
}

lines.push(`COMMIT;`);
lines.push(``);
lines.push(`-- 확인: select d.set_key, c.lang_code, count(*) from competition_quiz_detail d join competition_quiz_content c on c.quiz_id = d.quiz_id group by d.set_key, c.lang_code;`);
lines.push(`--       기대: ${[...new Set(files.map((f) => f.setKey))].length}세트 × 언어별 ${files[0].levels.reduce((n, L) => n + L.questions.length, 0)}건`);

fs.writeFileSync(outPath, lines.join("\n") + "\n", { encoding: "utf8" });
console.log(`생성 완료: ${outPath}`);
console.log(`  detail ${details.size}건, content ${contentRows}건, 파일 ${files.length}개`);
if (typeDiff) console.log(`  참고: 같은 세트·id 에서 언어별 유형이 다른 문항 ${typeDiff}건 (본문 기준 출제라 문제 없음)`);
