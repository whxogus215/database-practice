# 🏗️ 대규모 트래픽 처리를 위한 데이터베이스 아키텍처 설계

이 디렉토리는 서비스의 성장 단계별로 발생하는 데이터베이스 성능 이슈와 이를 해결하기 위한 아키텍처 설계 과정을 담고 있습니다.  
단일 데이터베이스의 한계를 극복하기 위해 **Replication, Partitioning, Sharding**을 단계적으로 도입하고, 대용량 실시간 데이터 처리를 위한 **Streaming Architecture**까지 확장하는 과정을 다뤘습니다.

---

## 📂 목차

1. [Mission 1: Replication](./1-replication.md)
2. [Mission 2: Partitioning](./2-partioning.md)
3. [Mission 3: Sharding](./3-sharding.md)
4. [Mission 4: Streaming Processing](./4-streaming-processing.md)

---

## 🚀 상세 설계 내용 및 핵심 포인트

### 1. Replication: 읽기/쓰기 부하 분산
**핵심 문제**: 단일 DB에 집중된 트래픽으로 인한 성능 저하 및 SPOF(단일 장애점) 위험  
**해결 전략**:
- **Master-Slave 구조**: 쓰기(Write)는 Source DB, 읽기(Read)는 Replica DB로 분리하여 부하 분산
- **고가용성 확보**: Source DB 장애 시 Replica를 승격시켜 서비스 지속성 보장
- **Trade-off 고려**: Replication Lag로 인한 데이터 불일치 문제와 해결 방안 모색

### 2. Partitioning: 거대 테이블 성능 최적화
**핵심 문제**: 수십억 건의 데이터가 쌓인 테이블의 조회 성능 저하 및 관리(삭제)의 어려움  
**해결 전략**:
- **Range Partitioning**: `created_at`을 기준으로 월별 파티셔닝 적용
- **Partition Pruning**: 쿼리 조건에 맞는 파티션만 스캔하여 조회 성능 극대화
- **효율적인 데이터 관리**: `DROP PARTITION`을 통해 락(Lock) 없이 오래된 데이터 즉시 삭제

### 3. Sharding: 물리적 한계 극복과 수평 확장
**핵심 문제**: 파티셔닝으로도 해결되지 않는 쓰기 병목 현상과 단일 서버의 스토리지 한계  
**해결 전략**:
- **Horizontal Scaling**: 데이터를 여러 물리적 서버(Shard)에 분산 저장
- **Shard Key 선정**: `user_id`를 기준으로 데이터를 균등하게 분배하여 특정 샤드 쏠림(Hot Spot) 방지
- **Consistent Hashing**: 해시 링 알고리즘을 도입하여 노드 추가/삭제 시 데이터 리밸런싱 최소화

### 4. Streaming Processing: 실시간 대용량 데이터 처리
**핵심 문제**: 초당 5만 건 이상의 GPS 위치 데이터를 RDBMS로 직접 처리 시 발생하는 지연 및 장애  
**해결 전략**:
- **Event Driven Architecture**: Kafka를 활용하여 트래픽 버퍼링 및 비동기 처리
- **Stream Processing**: Flink를 이용한 실시간 데이터 가공 및 이상 징후 탐지
- **Data Serving**: 실시간 조회는 Redis(Hot), 이력 저장은 S3(Cold)로 분리

---
## 📖 참고 자료
- [인프런 - 5천억건이 넘는 금융 데이터를 처리하는 토스 개발자에게 배우는 MySQL](https://www.inflearn.com/course/5%EC%B2%9C%EC%96%B5%EA%B1%B4%EC%9D%B4-%EB%84%98%EB%8A%94-%EA%B8%88%EC%9C%B5-%EB%8D%B0%EC%9D%B4%ED%84%B0%EB%A5%BC-%EC%B2%98%EB%A6%AC%ED%95%98/dashboard)
- Google Gemini
