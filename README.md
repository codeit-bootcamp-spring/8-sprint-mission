# Discodeit

> Spring Boot 기반의 **채팅 서비스 백엔드 학습 프로젝트**  
> 파일 기반 저장소(JCF / File Repository 전환), 멀티파트 파일 업로드, 도메인 중심 설계를 학습하기 위한 미션 프로젝트

---

## 1. 프로젝트 개요

**Discodeit**은 Discord와 유사한 개념의 채팅 서비스를 모델링한 **백엔드 중심 학습 프로젝트**입니다.

이 프로젝트의 핵심 목적은 다음과 같습니다.

- Spring Boot 기반 REST API 설계 이해
- 도메인(Entity) 중심 설계 연습
- Repository 구현 방식(JCF ↔ File) 전환 경험
- Multipart 파일 업로드 처리 흐름 이해
- DTO / Service / Repository 계층 분리
- 실제 요청 → 저장 → 조회까지의 **전체 흐름 추적 가능**

---

## 2. 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.8 |
| Build Tool | Gradle (Groovy DSL) |
| Web | Spring Web (REST API) |
| Config | application.yml |
| File Upload | MultipartFile |
| Persistence | In-memory(JCF) / File 기반 저장소 |
| Time API | `java.time.Instant` |

---

## 3. 프로젝트 구조

```text
com.sprint.mission.discodeit
├─ controller        # HTTP 요청/응답 처리
├─ service           # 비즈니스 로직
├─ repository
│  ├─ jcf             # 메모리 기반 저장소
│  └─ file            # 파일 기반 저장소
├─ entity             # 도메인 엔티티
├─ dto
│  ├─ user
│  ├─ channel
│  ├─ message
│  └─ binarycontent
├─ config             # 설정 클래스
└─ DiscodeitApplication
