# Postman API 테스트 가이드

서버 실행: `./gradlew bootRun` (기본 포트: 8080)

## 1. User API

### 1.1 모든 사용자 조회
- **Method**: GET
- **URL**: `http://localhost:8080/api/users`
- **Headers**: 없음
- **Body**: 없음
- **예상 응답**: 200 OK, UserDto 배열

### 1.2 User 등록
- **Method**: POST
- **URL**: `http://localhost:8080/api/users`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
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
- **예상 응답**: 200 OK, UserResponse

### 1.3 User 정보 수정
- **Method**: PATCH
- **URL**: `http://localhost:8080/api/users/{userId}`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "name": "updatedname",
  "password": "newpassword",
  "profileImage": null
}
```
- **예상 응답**: 200 OK, UserResponse

### 1.4 User 삭제
- **Method**: DELETE
- **URL**: `http://localhost:8080/api/users/{userId}`
- **Headers**: 없음
- **Body**: 없음
- **예상 응답**: 204 No Content

### 1.5 User 온라인 상태 업데이트
- **Method**: PATCH
- **URL**: `http://localhost:8080/api/users/{userId}/userStatus`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "userId": "user-uuid-here"
}
```
- **참고**: 경로 파라미터의 userId가 사용되므로 body의 userId는 무시됩니다. 빈 객체 `{}`도 가능합니다.
- **예상 응답**: 200 OK, UserStatusResponse

## 2. ReadStatus API

### 2.1 User의 Message 읽음 상태 목록 조회
- **Method**: GET
- **URL**: `http://localhost:8080/api/readStatuses?userId={userId}`
- **Headers**: 없음
- **Body**: 없음
- **예상 응답**: 200 OK, ReadStatus 배열

### 2.2 Message 읽음 상태 생성
- **Method**: POST
- **URL**: `http://localhost:8080/api/readStatuses`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "userId": "user-uuid",
  "channelId": "channel-uuid",
  "lastReadMessageId": "message-uuid"
}
```
- **예상 응답**: 200 OK, ReadStatus

### 2.3 Message 읽음 상태 수정
- **Method**: PATCH
- **URL**: `http://localhost:8080/api/readStatuses/{readStatusId}`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "lastReadMessageId": "new-message-uuid"
}
```
- **예상 응답**: 200 OK, ReadStatus

## 3. Channel API

### 3.1 User가 참여 중인 Channel 목록 조회
- **Method**: GET
- **URL**: `http://localhost:8080/api/channels?userId={userId}`
- **Headers**: 없음
- **Body**: 없음
- **예상 응답**: 200 OK, ChannelResponse 배열

### 3.2 Public Channel 생성
- **Method**: POST
- **URL**: `http://localhost:8080/api/channels/public`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "name": "Public Channel",
  "description": "This is a public channel",
  "ownerId": null
}
```
- **예상 응답**: 200 OK, ChannelResponse

### 3.3 Private Channel 생성
- **Method**: POST
- **URL**: `http://localhost:8080/api/channels/private`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "name": "Private Channel",
  "description": "This is a private channel",
  "ownerId": "user-uuid"
}
```
- **예상 응답**: 200 OK, ChannelResponse

### 3.4 Channel 정보 수정
- **Method**: PATCH
- **URL**: `http://localhost:8080/api/channels/{channelId}`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "name": "Updated Channel Name",
  "description": "Updated description"
}
```
- **예상 응답**: 200 OK, ChannelResponse

### 3.5 Channel 삭제
- **Method**: DELETE
- **URL**: `http://localhost:8080/api/channels/{channelId}`
- **Headers**: 없음
- **Body**: 없음
- **예상 응답**: 204 No Content

## 4. Message API

### 4.1 Channel의 Message 목록 조회
- **Method**: GET
- **URL**: `http://localhost:8080/api/messages?channelId={channelId}`
- **Headers**: 없음
- **Body**: 없음
- **예상 응답**: 200 OK, MessageResponse 배열

### 4.2 Message 생성
- **Method**: POST
- **URL**: `http://localhost:8080/api/messages`
- **Headers**: `Content-Type: application/x-www-form-urlencoded`
- **Body** (form-data):
  - content: "Hello, world!"
  - authorId: {author-uuid}
  - channelId: {channel-uuid}
- **예상 응답**: 200 OK, MessageResponse

### 4.3 Message 내용 수정
- **Method**: PATCH
- **URL**: `http://localhost:8080/api/messages/{messageId}`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "content": "Updated message content",
  "attachmentIds": []
}
```
- **예상 응답**: 200 OK, Message

### 4.4 Message 삭제
- **Method**: DELETE
- **URL**: `http://localhost:8080/api/messages/{messageId}`
- **Headers**: 없음
- **Body**: 없음
- **예상 응답**: 204 No Content

## 5. BinaryContent API

### 5.1 여러 첨부 파일 조회
- **Method**: GET
- **URL**: `http://localhost:8080/api/binaryContents?binaryContentIds={id1}&binaryContentIds={id2}`
- **Headers**: 없음
- **Body**: 없음
- **예상 응답**: 200 OK, BinaryContent 배열

### 5.2 첨부 파일 조회
- **Method**: GET
- **URL**: `http://localhost:8080/api/binaryContents/{binaryContentId}`
- **Headers**: 없음
- **Body**: 없음
- **예상 응답**: 200 OK, BinaryContent

## 6. Auth API

### 6.1 로그인
- **Method**: POST
- **URL**: `http://localhost:8080/api/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```
- **참고**: 현재 구현은 Map을 사용하므로 위 형식이 가능하지만, LoginRequest DTO로 변경 권장
- **예상 응답**: 200 OK, UserResponse

## 테스트 순서 추천

1. **User 생성** (POST /api/users) - 테스트용 사용자 생성
2. **모든 사용자 조회** (GET /api/users) - 생성된 사용자 확인
3. **로그인** (POST /api/auth/login) - 인증 테스트
4. **Public Channel 생성** (POST /api/channels/public)
5. **Channel 목록 조회** (GET /api/channels?userId={userId})
6. **Message 생성** (POST /api/messages)
7. **Message 목록 조회** (GET /api/messages?channelId={channelId})
8. **ReadStatus 생성** (POST /api/readStatuses)
9. **ReadStatus 목록 조회** (GET /api/readStatuses?userId={userId})
