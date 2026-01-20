# Postman 사용법 가이드

## 1. Postman 설치 및 실행

1. Postman 다운로드: https://www.postman.com/downloads/
2. 설치 후 Postman 실행
3. (선택) 계정 생성 또는 "Skip signing in and take me straight to the app" 클릭

## 2. 서버 실행 (필수)

터미널에서 프로젝트 디렉토리로 이동:
```bash
cd c:\myWs\Mission\8-sprint-mission
./gradlew bootRun
```

서버가 정상적으로 시작되면 다음과 같은 메시지가 표시됩니다:
```
Started DiscodeitApplication in X.XXX seconds
```

## 3. Postman에서 API 테스트하는 방법

### 기본 사용법

1. **새 요청 만들기**
   - Postman 좌측 상단의 "New" 버튼 클릭
   - "HTTP Request" 선택
   - 또는 상단의 "+" 버튼 클릭

2. **HTTP Method 선택**
   - 요청 창 왼쪽에서 HTTP Method 선택 (GET, POST, PATCH, DELETE 등)

3. **URL 입력**
   - 주소창에 API URL 입력
   - 예: `http://localhost:8080/api/users`

4. **Headers 설정 (필요한 경우)**
   - "Headers" 탭 클릭
   - Key: `Content-Type`
   - Value: `application/json`
   - "Save" 클릭

5. **Body 작성 (POST, PATCH의 경우)**
   - "Body" 탭 클릭
   - "raw" 선택
   - 오른쪽 드롭다운에서 "JSON" 선택
   - JSON 형식으로 데이터 입력

6. **요청 보내기**
   - 오른쪽의 "Send" 버튼 클릭

7. **응답 확인**
   - 하단에 응답 결과가 표시됩니다
   - Status Code (200, 404 등) 확인
   - Body 탭에서 응답 데이터 확인

## 4. 실제 테스트 예시

### 예시 1: 모든 사용자 조회 (GET)

1. **Method**: GET 선택
2. **URL**: `http://localhost:8080/api/users`
3. **Headers**: 없음 (설정 불필요)
4. **Body**: 없음
5. **Send** 클릭
6. **예상 응답**: 
   - Status: `200 OK`
   - Body: 사용자 배열 (JSON)

### 예시 2: 사용자 생성 (POST)

1. **Method**: POST 선택
2. **URL**: `http://localhost:8080/api/users`
3. **Headers**:
   - Key: `Content-Type`
   - Value: `application/json`
4. **Body** 탭 → **raw** 선택 → **JSON** 선택
5. **Body 내용**:
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
6. **Send** 클릭
7. **예상 응답**:
   - Status: `200 OK`
   - Body: 생성된 사용자 정보 (JSON)
   - **중요**: 응답에서 받은 `id` 값을 복사해두세요 (다른 API 테스트에 사용)

### 예시 3: 사용자 정보 수정 (PATCH)

1. **Method**: PATCH 선택
2. **URL**: `http://localhost:8080/api/users/{userId}`
   - `{userId}` 부분을 실제 사용자 ID로 교체
   - 예: `http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000`
3. **Headers**:
   - Key: `Content-Type`
   - Value: `application/json`
4. **Body** 탭 → **raw** 선택 → **JSON** 선택
5. **Body 내용**:
```json
{
  "name": "updatedname",
  "password": "newpassword",
  "profileImage": null
}
```
6. **Send** 클릭

### 예시 4: 쿼리 파라미터 사용 (GET with Query)

1. **Method**: GET 선택
2. **URL**: `http://localhost:8080/api/channels?userId=550e8400-e29b-41d4-a716-446655440000`
   - `?userId=` 뒤에 실제 사용자 ID 입력
3. **Headers**: 없음
4. **Body**: 없음
5. **Send** 클릭

**또는 Postman의 Params 탭 사용:**
- **Params** 탭 클릭
- Key: `userId`
- Value: `550e8400-e29b-41d4-a716-446655440000`
- Postman이 자동으로 URL에 추가합니다

### 예시 5: 경로 파라미터 사용 (DELETE)

1. **Method**: DELETE 선택
2. **URL**: `http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000`
   - URL의 마지막 부분(`/users/` 뒤)을 실제 사용자 ID로 교체
3. **Headers**: 없음
4. **Body**: 없음
5. **Send** 클릭
6. **예상 응답**:
   - Status: `204 No Content`
   - Body: 없음

## 5. 유용한 Postman 기능

### 5.1 환경 변수 사용 (Environment Variables)

여러 번 사용하는 값(예: userId, channelId)을 변수로 저장:

1. 오른쪽 상단의 톱니바퀴 아이콘 클릭
2. "Add" 클릭
3. Environment 이름 입력 (예: "Local")
4. Variables 추가:
   - Variable: `baseUrl`, Initial Value: `http://localhost:8080`
   - Variable: `userId`, Initial Value: (실제 UUID)
5. "Save" 클릭
6. 오른쪽 상단에서 환경 선택
7. URL에서 사용: `{{baseUrl}}/api/users/{{userId}}`

### 5.2 컬렉션(Collection) 만들기

여러 요청을 그룹으로 관리:

1. 왼쪽 상단의 "New" → "Collection" 선택
2. Collection 이름 입력 (예: "Discodeit API")
3. 요청을 만들 때 "Save" 버튼 클릭
4. Collection 선택하여 저장

### 5.3 응답에서 값 추출하기

다른 요청에서 사용할 값을 자동으로 추출:

1. Tests 탭 클릭
2. JavaScript 코드 작성:
```javascript
var jsonData = pm.response.json();
pm.environment.set("userId", jsonData.id);
```

## 6. 테스트 체크리스트

각 API를 테스트할 때 확인할 사항:

- [ ] HTTP Status Code가 예상과 일치하는가? (200, 201, 204, 400, 404 등)
- [ ] 응답 Body가 올바른 형식(JSON)인가?
- [ ] 필수 필드가 모두 포함되어 있는가?
- [ ] 에러 케이스도 테스트했는가? (잘못된 ID, 중복 데이터 등)

## 7. 자주 발생하는 오류 및 해결법

### 오류: "Could not get any response"
- **원인**: 서버가 실행되지 않음
- **해결**: `./gradlew bootRun` 명령어로 서버 실행 확인

### 오류: "404 Not Found"
- **원인**: URL이 잘못되었거나 경로 파라미터가 틀림
- **해결**: URL 확인, 경로 파라미터에 올바른 UUID 입력

### 오류: "400 Bad Request"
- **원인**: 요청 Body의 JSON 형식이 잘못되었거나 필수 필드 누락
- **해결**: JSON 형식 확인, 필수 필드 확인

### 오류: "415 Unsupported Media Type"
- **원인**: Content-Type 헤더가 없거나 잘못됨
- **해결**: Headers에 `Content-Type: application/json` 추가

## 8. 추천 테스트 순서

1. **GET /api/users** - 초기 데이터 확인
2. **POST /api/users** - 사용자 생성
3. **GET /api/users** - 생성 확인
4. **POST /api/auth/login** - 로그인 테스트
5. **POST /api/channels/public** - 채널 생성
6. **GET /api/channels?userId={userId}** - 채널 목록 조회
7. **POST /api/messages** - 메시지 생성
8. **GET /api/messages?channelId={channelId}** - 메시지 목록 조회
9. **PATCH /api/users/{userId}** - 사용자 정보 수정
10. **DELETE /api/users/{userId}** - 사용자 삭제
