# API 스펙 비교 및 수정 필요 사항

## 주요 차이점 분석

### 1. Message GET API - Pageable 지원
**API 스펙**:
- `GET /api/messages?channelId=...&pageable=...`
- `pageable` 파라미터 필수
- 반환: `PageResponse<MessageDto>`

**현재 구현**:
- `GET /api/messages?channelId=...&userId=...`
- `pageable` 파라미터 없음
- 반환: `List<MessageResponse>`

**필요 작업**:
- `Pageable` 파라미터 추가
- `PageResponse` DTO 생성
- 페이징 로직 구현

---

### 2. UserDto 구조 차이
**API 스펙**:
```json
{
  "id": "uuid",
  "username": "string",
  "email": "string",
  "profile": {
    "id": "uuid",
    "fileName": "string",
    "size": "int64",
    "contentType": "string"
  },
  "online": "boolean"
}
```

**현재 구현**:
- `profileId` (UUID)만 있음
- `profile` 객체 없음

**필요 작업**:
- `UserDto`에 `profile: BinaryContentDto` 필드 추가
- 프로필 이미지 정보를 포함한 응답 생성

---

### 3. MessageDto 구조 차이
**API 스펙**:
```json
{
  "id": "uuid",
  "createdAt": "date-time",
  "updatedAt": "date-time",
  "content": "string",
  "channelId": "uuid",
  "author": {
    // UserDto 구조
  },
  "attachments": [
    // BinaryContentDto 배열
  ]
}
```

**현재 구현**:
- `authorId` (UUID)만 있음
- `author` 객체 없음
- `attachments`가 `List<UUID>` (attachmentIds)

**필요 작업**:
- `MessageDto` 생성 (현재는 `MessageResponse` 사용)
- `author: UserDto` 필드 추가
- `attachments: List<BinaryContentDto>` 필드 추가

---

### 4. ChannelDto 구조 차이
**API 스펙**:
```json
{
  "id": "uuid",
  "type": "PUBLIC | PRIVATE",
  "name": "string",
  "description": "string",
  "participants": [
    // UserDto 배열
  ],
  "lastMessageAt": "date-time"
}
```

**현재 구현**:
- `participantIds` (Set<UUID>)만 있음
- `participants` 배열 없음
- `lastMessageAt` 필드 없음
- `ownerId` 필드 있음 (API 스펙에는 없음)

**필요 작업**:
- `ChannelDto` 생성 또는 `ChannelResponse` 수정
- `participants: List<UserDto>` 필드 추가
- `lastMessageAt` 필드 추가
- `ownerId` 필드 제거 또는 선택적 처리

---

### 5. UserUpdateRequest 필드명 차이
**API 스펙**:
```json
{
  "newUsername": "string",
  "newEmail": "string",
  "newPassword": "string"
}
```

**현재 구현**:
- `name`, `email`, `password` 또는 다른 구조 사용 가능

**필요 작업**:
- `UserUpdateRequest` 필드명 변경
- Service 레이어에서 필드명 매핑 수정

---

### 6. BinaryContent Download 엔드포인트
**API 스펙**:
- `GET /api/binaryContents/{binaryContentId}/download`
- 반환: `binary` (파일 바이너리)

**현재 구현**:
- `GET /api/binaryContents/{binaryContentId}/image` (프로필 이미지용)

**필요 작업**:
- `/download` 엔드포인트 추가
- 일반 파일 다운로드 지원

---

### 7. Response 상태 코드
**API 스펙**:
- POST: `201 Created`
- PATCH: `200 OK`
- DELETE: `204 No Content`
- GET: `200 OK`

**현재 구현**:
- 대부분 맞지만 일부 확인 필요

---

### 8. ReadStatusDto 구조
**API 스펙**:
```json
{
  "id": "uuid",
  "userId": "uuid",
  "channelId": "uuid",
  "lastReadAt": "date-time"
}
```

**현재 구현**:
- `ReadStatus` 엔티티 직접 반환 가능
- DTO 확인 필요

---

### 9. 기타 확인 사항

#### UserCreateRequest
- API 스펙: `username`, `email`, `password`
- 현재 구현 확인 필요

#### PublicChannelCreateRequest
- API 스펙: `name`, `description`
- 현재 구현 확인 필요

#### PrivateChannelCreateRequest
- API 스펙: `participantIds: UUID[]`
- 현재 구현 확인 필요

---

## 수정 우선순위

1. **높음**:
   - Message GET API 페이징 구현
   - UserDto, MessageDto, ChannelDto 구조 수정
   - Response DTO에 중첩 객체 추가

2. **중간**:
   - UserUpdateRequest 필드명 변경
   - BinaryContent download 엔드포인트 추가
   - Response 상태 코드 확인

3. **낮음**:
   - 기타 DTO 구조 세부 확인
