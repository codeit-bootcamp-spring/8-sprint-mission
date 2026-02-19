# 한글 인코딩 문제 해결 가이드

## 문제
PowerShell에서 Git 명령어 실행 시 한글 브랜치 이름이 깨져 보이는 문제

## 해결 방법

### 1. Git 설정 확인 (이미 완료됨)
```bash
git config --global core.quotepath false
git config --global i18n.commitencoding utf-8
git config --global i18n.logoutputencoding utf-8
```

### 2. PowerShell에서 한글 사용 시

#### 방법 A: 스크립트 실행 전 인코딩 설정
```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001
```

#### 방법 B: Git Bash 사용 (권장)
Git Bash에서는 한글이 정상적으로 표시됩니다:
```bash
cd /c/myWs/Mission/8-sprint-mission
git checkout 현승원-sprint5
git push origin 현승원-sprint5
```

#### 방법 C: IntelliJ Git 기능 사용
IntelliJ의 Git 기능을 사용하면 한글 문제 없이 작업할 수 있습니다:
- VCS → Git → Branches → 브랜치 선택
- VCS → Git → Push

### 3. 브랜치 이름을 변수로 저장
```powershell
$branchName = git branch --show-current
git push origin $branchName
```

## 현재 상태
- Git 설정: ✅ 완료
- `.gitattributes`: ✅ UTF-8 인코딩 설정됨
- PowerShell 인코딩: 세션별로 설정 필요

## 추천 방법
**Git Bash** 또는 **IntelliJ의 Git 기능**을 사용하는 것을 권장합니다.
