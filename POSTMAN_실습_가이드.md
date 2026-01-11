# Postman 실습 가이드 - 직접 따라하기

## 📋 준비사항

1. **서버 실행 (먼저!)**
   ```bash
   cd c:\myWs\Mission\8-sprint-mission
   ./gradlew bootRun
   ```
   서버가 시작될 때까지 기다리세요. "Started DiscodeitApplication" 메시지가 보이면 준비 완료!

2. **Postman 실행**
   - Postman 앱 열기
   - 계정 로그인 없이도 사용 가능 ("Skip signing in" 선택)

---

## 🚀 첫 번째 테스트: 모든 사용자 조회 (GET)

### 단계별 따라하기

1. **새 요청 만들기**
   - Postman 상단 왼쪽의 **"+"** 버튼 클릭
   - 또는 상단 메뉴에서 **"File" → "New" → "HTTP Request"**

2. **Method 선택**
   - 요청 창 왼쪽의 드롭다운에서 **"GET"** 선택 (기본값이 GET일 수 있음)

3. **URL 입력**
   - 주소창에 입력: `http://localhost:8080/api/users`
   - 정확히 입력하세요! 대소문자 구분됩니다.

4. **Send 버튼 클릭**
   - 오른쪽 상단의 파란색 **"Send"** 버튼 클릭

5. **결과 확인**
   - 하단에 응답이 표시됩니다
   - Status: `200 OK` 가 보이면 성공!
   - Body 탭에서 사용자 목록(JSON) 확인

**예상 결과:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "jessie",
    "email": "jessie@codeit.com",
    ...
  }
]
```

---

## ✏️ 두 번째 테스트: 사용자 생성 (POST)

### 단계별 따라하기

1. **새 요청 만들기**
   - **"+"** 버튼으로 새 탭 열기

2. **Method 선택**
   - 드롭다운에서 **"POST"** 선택

3. **URL 입력**
   - 주소창에 입력: `http://localhost:8080/api/users`

4. **Headers 설정**
   - **"Headers"** 탭 클릭
   - Key 입력란에: `Content-Type`
   - Value 입력란에: `application/json`
   - (자동완성으로 선택해도 됩니다)

5. **Body 작성**
   - **"Body"** 탭 클릭
   - **"raw"** 라디오 버튼 선택
   - 오른쪽 드롭다운에서 **"JSON"** 선택 (Text가 기본값일 수 있음)
   - 큰 텍스트 박스에 아래 JSON 입력:

```json
{
  "name": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "fileName": null,
  "fileType": null,
  "fileSize": null,
  "profileImage": null
}
```

6. **Send 버튼 클릭**

7. **결과 확인**
   - Status: `200 OK`
   - Body에서 생성된 사용자 정보 확인
   - **중요**: `id` 값을 복사해두세요! (마우스로 드래그하여 선택, Ctrl+C)

**예상 결과:**
```json
{
  "id": "새로 생성된 UUID",
  "name": "testuser",
  "email": "test@example.com",
  ...
}
```

---

## 🔍 세 번째 테스트: 쿼리 파라미터 사용 (GET)

### Channel 목록 조회

1. **새 요청 만들기**
   - **"+"** 버튼 클릭

2. **Method**: GET 선택

3. **URL 입력**
   - 방법 1: 직접 입력
     ```
     http://localhost:8080/api/channels?userId=여기에사용자ID입력
     ```
   - 방법 2: Params 탭 사용 (더 쉬움!)
     - URL 입력란에: `http://localhost:8080/api/channels`
     - **"Params"** 탭 클릭
     - Key: `userId`
     - Value: 위에서 복사한 사용자 ID 붙여넣기
     - Postman이 자동으로 URL을 완성합니다!

4. **Send 클릭**

5. **결과 확인**
   - Status: `200 OK`
   - Body에서 채널 목록 확인

---

## ✏️ 네 번째 테스트: 경로 파라미터 사용 (PATCH)

### 사용자 정보 수정

1. **새 요청 만들기**

2. **Method**: PATCH 선택

3. **URL 입력**
   ```
   http://localhost:8080/api/users/여기에사용자ID입력
   ```
   예: `http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000`
   - 마지막 `/` 뒤에 사용자 ID를 입력하세요

4. **Headers 설정**
   - Headers 탭
   - Key: `Content-Type`
   - Value: `application/json`

5. **Body 작성**
   - Body 탭 → raw → JSON 선택
   - 입력:
   ```json
   {
     "name": "updatedname",
     "password": "newpassword",
     "profileImage": null
   }
   ```

6. **Send 클릭**

7. **결과 확인**
   - Status: `200 OK`
   - Body에서 수정된 사용자 정보 확인

---

## 🗑️ 다섯 번째 테스트: 삭제 (DELETE)

### 사용자 삭제

1. **새 요청 만들기**

2. **Method**: DELETE 선택

3. **URL 입력**
   ```
   http://localhost:8080/api/users/여기에사용자ID입력
   ```

4. **Headers**: 없음 (설정 불필요)

5. **Body**: 없음

6. **Send 클릭**

7. **결과 확인**
   - Status: `204 No Content` (성공!)
   - Body는 비어있음 (정상)

---

## 📝 실전 테스트 시나리오

### 전체 플로우 테스트하기

1. **GET /api/users** → 초기 사용자 확인
2. **POST /api/users** → 새 사용자 생성 (ID 복사!)
3. **GET /api/users** → 새 사용자가 목록에 추가되었는지 확인
4. **POST /api/channels/public** → Public 채널 생성
   ```json
   {
     "name": "테스트 채널",
     "description": "테스트용 채널입니다",
     "ownerId": null
   }
   ```
   - 생성된 채널 ID 복사!
5. **GET /api/channels?userId=사용자ID** → 채널 목록 확인
6. **POST /api/messages** → 메시지 생성
   - Body 탭 → form-data 또는 x-www-form-urlencoded 선택
   - Key-Value 입력:
     - content: "안녕하세요!"
     - authorId: 사용자ID
     - channelId: 채널ID
7. **GET /api/messages?channelId=채널ID** → 메시지 목록 확인
8. **PATCH /api/users/사용자ID** → 사용자 정보 수정
9. **DELETE /api/users/사용자ID** → 사용자 삭제
10. **GET /api/users** → 삭제 확인

---

## 💡 유용한 팁

### 1. 요청 저장하기
- 요청 후 **"Save"** 버튼 클릭
- 이름 입력 (예: "Get All Users")
- Collection 선택 또는 새로 만들기
- 나중에 다시 사용 가능!

### 2. 환경 변수 사용 (고급)
- 오른쪽 상단 톱니바퀴 → "Add"
- Environment 이름: "Local"
- Variables:
  - `baseUrl`: `http://localhost:8080`
  - `userId`: (실제 UUID)
- 환경 선택 후 URL: `{{baseUrl}}/api/users/{{userId}}`

### 3. 응답 포맷 확인
- Body 탭 오른쪽에 "Pretty", "Raw", "Preview" 옵션
- "Pretty" 선택 시 JSON이 보기 좋게 정렬됨

### 4. 상태 코드 의미
- **200 OK**: 성공
- **201 Created**: 생성 성공 (POST)
- **204 No Content**: 성공했지만 응답 본문 없음 (DELETE)
- **400 Bad Request**: 요청 형식 오류
- **404 Not Found**: 리소스를 찾을 수 없음
- **500 Internal Server Error**: 서버 오류

---

## ❌ 문제 해결

### 서버 연결 안 됨
```
Error: Could not get any response
```
→ 서버가 실행 중인지 확인 (`./gradlew bootRun`)

### 404 에러
```
404 Not Found
```
→ URL이 정확한지 확인, 경로 파라미터(ID) 확인

### 400 에러
```
400 Bad Request
```
→ JSON 형식 확인, 필수 필드 누락 확인, 쉼표(,) 확인

### JSON 파싱 에러
→ Body의 JSON 형식이 올바른지 확인 (따옴표, 중괄호, 쉼표)

---

## ✅ 체크리스트

테스트할 때 확인할 것:
- [ ] 서버가 실행 중인가?
- [ ] HTTP Method가 올바른가? (GET/POST/PATCH/DELETE)
- [ ] URL이 정확한가? (대소문자, 경로)
- [ ] Headers에 Content-Type이 있는가? (POST/PATCH인 경우)
- [ ] Body의 JSON 형식이 올바른가?
- [ ] 경로 파라미터(ID)가 올바른가?
- [ ] 쿼리 파라미터가 올바른가?

---

**이제 직접 테스트해보세요! 🚀**
