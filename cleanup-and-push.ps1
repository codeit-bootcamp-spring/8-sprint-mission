# CI만 남기고 불필요한 파일 추적 제거 스크립트
# 실행 전: Git GUI, IDE의 Git 작업, 다른 터미널의 git 명령을 모두 종료하세요.

Write-Host "=== 1. index.lock 제거 ===" -ForegroundColor Yellow
if (Test-Path ".git/index.lock") {
    Remove-Item ".git/index.lock" -Force
    Write-Host "index.lock 삭제됨" -ForegroundColor Green
} else {
    Write-Host "index.lock 없음" -ForegroundColor Gray
}

Write-Host "`n=== 2. 추적에서 제거 (.env, .logs, node_modules, .discodeit, storage) ===" -ForegroundColor Yellow
git rm --cached .env 2>$null
git rm -r --cached .logs 2>$null
git rm -r --cached node_modules 2>$null
git rm -r --cached .discodeit 2>$null
git rm -r --cached storage 2>$null
Write-Host "완료" -ForegroundColor Green

Write-Host "`n=== 3. 변경 사항 확인 ===" -ForegroundColor Yellow
git status

Write-Host "`n=== 4. 커밋 (수동으로 실행) ===" -ForegroundColor Yellow
Write-Host 'git add .gitignore'
Write-Host 'git commit -m "chore: remove .env, .logs, node_modules, .discodeit, storage from tracking"'
Write-Host ""
Write-Host "=== 5. origin에 force push (수동으로 실행) ===" -ForegroundColor Yellow
Write-Host "현재 브랜치가 main이면: git push origin main --force"
Write-Host "현재 브랜치가 task/sprint8-submission이면: git push origin task/sprint8-submission --force"
Write-Host ""
Write-Host "주의: --force push는 원격 히스토리를 덮어씁니다. 팀 작업 시 확인 후 실행하세요." -ForegroundColor Red
