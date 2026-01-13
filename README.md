# Core Domain API

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ureca-mini2-div4_template-backend&metric=alert_status&token=35bd9a636ce0f7bc836903d9cd4487365306b29c)](https://sonarcloud.io/summary/new_code?id=ureca-mini2-div4_template-backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ureca-mini2-div4_template-backend&metric=coverage&token=35bd9a636ce0f7bc836903d9cd4487365306b29c)](https://sonarcloud.io/summary/new_code?id=ureca-mini2-div4_template-backend)

## 1. 프로젝트 개요
**유저 정보 관리**, **통신 서비스 가입**, **요금제 변경**, **부가서비스 관리**, **할인서비스 관리**, **소액결제 내역 처리**를 담당하는 Core 백엔드 모듈입니다. 대용량 트래픽과 데이터 환경을 고려하여 **이력 관리**와 **데이터 무결성**에 중점을 두고 설계되었습니다.

## 2. 기술 스택 (Tech Stack)

- Language: Java 17
- Framework: Spring Boot 3.x
- Database: PostgreSQL
- ORM: JPA (Hibernate)
- Security: AES-256 Encryption (개인정보 암호화)
- Build Tool: Gradle

## 3. 핵심 도메인 구조

고객(Customer)은 여러 회선(Subscription)을 가질 수 있으며 각 회선은 요금제(PLAN), 부가서비스(VAS), 소액결제(MICRO), 할인(DISCOUNT)의 이력을 관리합니다.

<img width="1318" height="569" alt="mermaid-diagram-2026-01-14-004755" src="https://github.com/user-attachments/assets/6fb58842-c9c6-41eb-932f-8b9c9772d955" />

## 4. 핵심 로직

### 신규 개통 프로세스

고객이 회선을 새로 개통할 때 **기존 사용 번호 복구**와 **신규 번호 채번**을 자동으로 판단하는 로직입니다.

1. 유효성 검사:
   - 요청받은 고객 정보가 실제 DB에 존재하는지 확인합니다.

2. 번호 채번 정책:
   - 메인 번호 복구: 만약 고객이 현재 사용 중인 회선이 0개라면, 고객 정보에 등록된 연락처를 우선적으로 회선 번호로 할당하여 **쓰던 번호 그대로** 사용할 수 있게 합니다. (단, 타인이 사용 중이지 않을 경우)
   - 신규 번호 생성: 이미 회선이 있거나(투폰/서브폰), 기존 번호를 사용할 수 없는 경우, 시스템은 중복되지 않는 랜덤 전화번호를 새로 생성하여 할당합니다.

3. 회선 생성:
   - 확정된 전화번호는 AES-256 알고리즘으로 암호화되어 DB에 저장됩니다.

4. 초기 요금제 연결:
   - 회선 생성과 동시에, 선택한 요금제에 대한 첫 번째 이력 데이터를 관련 테이블에 생성합니다.

<img width="1318" height="228" alt="mermaid-diagram-2026-01-14-005843" src="https://github.com/user-attachments/assets/43a7bf14-3f10-4ad5-8b1c-c3c9b6393a7b" />

### 요금제 변경 프로세스

단순 업데이트가 아닌 **기존 이력을 만료시키고 새 이력을 쌓는** 방식으로 데이터의 시간적 변화를 추적합니다.

1. 상태 및 유효성 체크:
   - 변경하려는 회선이 존재하고, 현재 해지된 상태가 아닌지 확인합니다.

2. 기존 이력 만료:
   - SubscriptionPlan 테이블에서 현재 적용 중인 요금제 이력을 조회합니다.
   - 해당 레코드의 **종료일을 현재 시점으로 업데이트**하여, 해당 요금제의 효력을 만료시킵니다.

3. 신규 이력 생성:
   - 변경할 새로운 요금제 정보를 담은 **새로운 레코드를 INSERT**합니다.
   - 이 레코드의 시작일(created_date)은 현재 시점으로 설정되어 현재 유효한 요금제가 됩니다.

<img width="1318" height="250" alt="mermaid-diagram-2026-01-14-010543" src="https://github.com/user-attachments/assets/d56af37c-7e08-4dff-8c7d-a1bf6bf4e46d" />
