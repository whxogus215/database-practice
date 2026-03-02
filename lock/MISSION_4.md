# 🚀 Mission 4: 네임드 락 커넥션 풀(Connection Pool) 완전 분리

## 📝 개요

Mission 3에서 확인했듯, 네임드 락은 부모 트랜잭션(락 관리)과 자식 트랜잭션(비즈니스 로직)이 동시에 각각의 커넥션을 요구합니다.
이로 인해 트래픽이 몰리면 HikariCP의 커넥션이 순식간에 고갈되어 **데드락(Connection is not available)**이 발생합니다.
이를 해결하기 위해 락 전용 데이터소스(DataSource)와 일반 비즈니스용 데이터소스를 물리적으로 분리합니다.

## 🎯 주요 학습 목표

1. Spring Boot의 다중 데이터소스(Multi-DataSource) 설정 방법을 체득합니다.
2. `@Configuration`과 `@Qualifier`, `@Primary`를 활용해 빈(Bean) 충돌을 해결하고 원하는 DB 커넥션을 주입합니다.
3. 데이터소스 분리 전/후의 부하 테스트를 통해 커넥션 고갈 문제가 해결됨을 직접 증명합니다.

---

## 🛠️ 요구사항

### 1. 설정 파일(`application.yml`) 분리

* 하나의 DB를 가리키더라도, 커넥션 풀을 2개 만들기 위해 논리적으로 설정을 나눕니다.
* 예시: `spring.datasource.hikari` (기본 비즈니스용) / `spring.datasource.lock.hikari` (락 전용)
* 테스트를 위해 두 풀의 `maximum-pool-size`를 10정도로 작게 설정하세요.

### 2. DataSource Configuration 클래스 작성

* 2개의 DataSource 객체를 생성하는 설정 클래스를 만드세요.
* 기본 비즈니스 레포지토리가 사용할 DataSource에는 `@Primary`를 붙여주세요.
* 락을 관리하는 레포지토리(Mission 3에서 만든 것)가 특정 DataSource(락 전용)를 사용하도록 설정하세요. (필요하다면 JPA EntityManagerFactory를 분리해야 할 수도 있습니다. 힌트: *JdbcTemplate*을 쓰면 분리가 훨씬 쉽습니다!)

### 3. Connection 고갈 테스트 (Before & After)

* **Before:** 데이터소스를 분리하기 전, 동시 요청 20개를 쏘면 `Connection is not available` 예외가 터지는 것을 눈으로 확인하세요.
* **After:** 데이터소스 분리 코드를 적용한 뒤, 똑같이 동시 요청 20개(혹은 100개)를 쏘았을 때 타임아웃 없이 모두 안전하게 락을 획득하고 이벤트를 완료하는지 검증하세요.
