# Swagger UI 사용법

## 설치 완료

springdoc-openapi를 활용한 Swagger UI가 설정되었습니다.

## 사용 방법

### 1. 서버 실행

```bash
./gradlew bootRun
```

### 2. Swagger UI 접근

서버가 실행되면 브라우저에서 다음 URL로 접근하세요:

**Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

또는

```
http://localhost:8080/swagger-ui/index.html
```

**OpenAPI JSON 문서:**
```
http://localhost:8080/api-docs
```

### 3. Swagger UI에서 할 수 있는 것

1. **모든 API 엔드포인트 확인**
   - 좌측에 컨트롤러별로 그룹화된 API 목록 표시
   - 각 API의 HTTP Method, 경로, 설명 확인

2. **API 상세 정보 확인**
   - 각 API를 클릭하면 상세 정보 확인 가능
   - 요청 파라미터, 요청 Body 스키마, 응답 스키마 확인

3. **실제 API 테스트**
   - "Try it out" 버튼 클릭
   - 파라미터 입력
   - "Execute" 버튼으로 실제 요청 전송
   - 응답 결과 확인

### 4. 주요 기능

- **필터링**: 상단 검색창에서 API 검색
- **그룹화**: 컨트롤러별로 API 그룹화
- **스키마 확인**: Request/Response 스키마 확인
- **실시간 테스트**: 브라우저에서 직접 API 테스트 가능

## 설정 정보

### OpenAPI 설정 (OpenApiConfig.java)
- 제목: "Discodeit API 문서"
- 버전: "1.0"
- 설명: "Discodeit 프로젝트의 Swagger API 문서입니다."

### application.yml 설정
- API 문서 경로: `/api-docs`
- Swagger UI 경로: `/swagger-ui.html`
- Swagger UI 활성화: true

## 참고사항

- Swagger UI는 개발 환경에서만 사용하는 것을 권장합니다
- 프로덕션 환경에서는 Swagger UI를 비활성화하는 것을 고려하세요
- 모든 API는 자동으로 문서화되며, 추가 어노테이션 없이 기본 정보가 표시됩니다
