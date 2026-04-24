## Spring Event - 파일 업로드 로직 분리하기

- 디스코드잇은 BinaryContent의 메타 데이터(DB)와 바이너리 데이터(FileSystem/S3)를 분리해 저장합니다.
- 만약 지금처럼 두 로직이 하나의 트랜잭션으로 묶인 경우 트랜잭션을 과도하게 오래 점유할 수 있는 문제가 있습니다.
    - 바이너리 데이터 저장 연산은 오래 걸릴 수 있는 연산이며, 해당 연산이 끝날 때까지 트랜잭션이 대기해야합니다.
- 따라서 Spring Event를 활용해 메타 데이터 저장 트랜잭션으로부터 바이너리 데이터 저장 로직을 분리하여, 메타데이터 저장 트랜잭션이 종료되면 바이너리 데이터를 저장하도록
  변경합니다.
- [ ] BinaryContentStorage.put을 직접 호출하는 대신 BinaryContentCreatedEvent를 발행하세요.
    - BinaryContentCreatedEvent를 정의하세요.
        - BinaryContent 메타 정보가 DB에 잘 저장되었다는 사실을 의미하는 이벤트입니다.
    - 다음의 메소드에서 BinaryContentStorage를 호출하는 대신 BinaryContentCreatedEvent를 발행하세요.
        - `UserService.create/update`
        - `MessageService.create`
        - `BinaryContentService.create`
    - `ApplicationEventPublisher`를 활용하세요.
- [ ]  이벤트를 받아 실제 바이너리 데이터를 저장하는 리스너를 구현하세요.
- 이벤트를 발행한 메인 서비스의 트랜잭션이 커밋되었을 때 리스너가 실행되도록 설정하세요.
- BinaryContentStorage를 통해 바이너리 데이터를 저장하세요.
- [ ] 바이너리 데이터 저장 성공 여부를 알 수 있도록 메타 데이터를 리팩토링하세요.
    - `BinaryContent`에 바이너리 데이터 업로드 상태 속성(status)을 추가하세요.
    - PROCESSING: 업로드 중
        - 기본값입니다.
    - SUCCESS: 업로드 완료
    - FAIL: 업로드 실패
    -
  ```sql
        -- schema.sql
  CREATE TABLE binary_contents
  (
  id uuid PRIMARY KEY,
  created_at timestamp with time zone NOT NULL,
  updated_at timestamp with time zone,
  file_name varchar(255)             NOT NULL,
  size bigint NOT NULL,
  content_type varchar(100)             NOT NULL,
  status varchar(20)              NOT NULL
  );

  -- ALTER TABLE binary_contents
  -- ADD COLUMN updated_at timestamp with time zone;
  -- ALTER TABLE binary_contents
  -- ADD COLUMN status varchar(20) NOT NULL;

    ```
    - `BinaryContent`의 상태를 업데이트하는 메소드를 정의하세요.
        - 트랜잭션 전파 범위에 유의하세요.
    - [ ]  바이너리 데이터 저장 성공 여부를 메타 데이터에 반영하세요.
      - 성공 시 `BinaryContent`의 `status`를 SUCCESS로 업데이트하세요.
      - 실패 시 `BinaryContent`의 `status`를 FAIL로 업데이트하세요.