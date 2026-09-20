# 봇 컴피티션 문제 json 을 서버 API 로 올린다 (관리자 계정 필요, ADMIN_USER_IDS 에 그 userId 가 있어야 함).
# SQL 로 변환해서 mysql 클라이언트로 넣지 말 것 — Windows 클라이언트 기본 문자집합(cp949) 때문에
# 간체자·성조 병음이 '?' 로 깨진다 (2026-09-20 실제 발생). API 경로는 UTF-8 그대로 저장한다.
#
# 사용 예:
#   .\scripts\import-competition-quiz.ps1 -BaseUrl https://q-ring.app -Email admin@example.com `
#       -Files @{ "01" = @("C:\Users\kanta\Downloads\competition_quiz_en_01.json",
#                         "C:\Users\kanta\Downloads\competition_quiz_ja_01.json",
#                         "C:\Users\kanta\Downloads\competition_quiz_zh_01.json");
#                 "02" = @("C:\Users\kanta\Downloads\competition_quiz_en_02.json",
#                         "C:\Users\kanta\Downloads\competition_quiz_ja_02.json",
#                         "C:\Users\kanta\Downloads\competition_quiz_zh_02.json") }
#   비밀번호는 프롬프트로 입력받는다.
param(
    [Parameter(Mandatory = $true)] [string] $BaseUrl,
    [Parameter(Mandatory = $true)] [string] $Email,
    [Parameter(Mandatory = $true)] [hashtable] $Files   # quizSet -> 파일 경로 배열
)

$ErrorActionPreference = "Stop"
$curl = Get-Command curl.exe -ErrorAction Stop   # PowerShell 의 curl 별칭이 아니라 실제 curl.exe

$secure = Read-Host -Prompt "비밀번호 ($Email)" -AsSecureString
$password = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))

# 1) 로그인 → accessToken
$loginBody = @{ email = $Email; password = $password } | ConvertTo-Json -Compress
$login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/login" `
    -ContentType "application/json; charset=utf-8" -Body $loginBody
$token = $login.accessToken
if (-not $token) { throw "로그인 응답에 accessToken 이 없습니다: $($login | ConvertTo-Json -Compress)" }
Write-Host "로그인 성공"

# 2) 세트별 업로드 (같은 세트의 언어 파일은 같은 quizSet 으로)
foreach ($setKey in ($Files.Keys | Sort-Object)) {
    foreach ($path in $Files[$setKey]) {
        if (-not (Test-Path $path)) { throw "파일 없음: $path" }
        Write-Host "업로드 quizSet=$setKey  $path"
        # curl.exe 는 파일 바이트를 그대로 보낸다 (인코딩 변환 없음)
        $out = & $curl.Source -s -S -X POST "$BaseUrl/admin/competition/quiz/import?quizSet=$setKey" `
            -H "Authorization: Bearer $token" `
            -F "file=@`"$path`";type=application/json"
        Write-Host "  -> $out"
        if ($out -match '"success"\s*:\s*false') { throw "업로드 실패: $path" }
    }
}
Write-Host "완료. DB 확인: select set_key, lang_code, count(*) from competition_quiz_detail d join competition_quiz_content c on c.quiz_id=d.quiz_id group by set_key, lang_code;"
