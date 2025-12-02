# 📋 애니파이브 전자결재 크롤링 가이드

> 이 문서는 office.anyfive.com 전자결재 시스템에서 데이터를 추출하여 새로운 시스템으로 마이그레이션하기 위한 cmds를 만드는 전체 과정입니다.
---

## 📑 목차

1. [개요](#1-개요)
2. [전체 작업 흐름](#2-전체-작업-흐름)
3. [사전 준비 - 필수 소프트웨어 설치](#3-사전-준비---필수-소프트웨어-설치)
4. [데이터베이스 설정](#4-데이터베이스-설정)
5. [1단계: 문서 ID 목록 추출](#5-1단계-문서-id-목록-추출-python)
6. [2단계: 문서 기본 정보 크롤링](#6-2단계-문서-기본-정보-크롤링-java)
7. [3단계: 첨부파일 크롤링](#7-3단계-첨부파일-크롤링-java)
8. [4단계: 본문 이미지 크롤링](#8-4단계-본문-이미지-크롤링-java)
9. [5단계: 문서 본문(HTML) 크롤링](#9-5단계-문서-본문html-크롤링-java)
10. [6단계: 조직도(인사정보) DB 반영](#10-6단계-조직도인사정보-db-반영-python)
11. [7단계: CMDS 형식 변환](#11-7단계-cmds-형식-변환-python)
12. [문제 해결 (Troubleshooting)](#12-문제-해결-troubleshooting)
13. [참고 사항](#13-참고-사항)

---

## 1. 개요

### 1.1 이 가이드의 목적

애니파이브 전자결재 시스템(office.anyfive.com)에 저장된 결재 문서들을 추출하여 새로운 시스템으로 이전하는 cmds를 생성하는 작업을 단계별로 안내합니다.

### 1.2 추출 대상 데이터

| 항목 | 설명 |
|------|------|
| 문서 기본 정보 | 문서번호, 제목, 기안일, 기안자, 양식명 등 |
| 결재 활동 | 결재라인, 결재일시, 결재의견 |
| 참조자/참조문서 | 참조자 목록, 연결된 참조문서 |
| 첨부파일 | 문서에 첨부된 모든 파일 |
| 본문 이미지 | 문서 본문에 포함된 이미지 |
| 문서 본문(HTML) | 인쇄용 HTML 형태의 문서 본문 |

---

## 2. 전체 작업 흐름

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        전체 마이그레이션 워크플로우                        │
└─────────────────────────────────────────────────────────────────────────┘

[1단계] 문서 ID 추출 (Python)
    │   └── doc_ids.txt 생성
    ▼
[2단계] 기본 정보 크롤링 (Java)
    │   └── new_documents 테이블에 기본 정보 저장
    ▼
[3단계] 첨부파일 크롤링 (Java)
    │   ├── 파일 다운로드: approval_{연도}_new_attachments/
    │   └── DB attaches 컬럼 업데이트
    ▼
[4단계] 본문 이미지 크롤링 (Java)
    │   └── 이미지 다운로드: approval_{연도}_new_img/
    ▼
[5단계] 문서 본문 크롤링 (Java)
    │   └── DB doc_body 컬럼 업데이트
    ▼
[6단계] 조직도 반영 (Python)
    │   └── 인사정보 CSV로 DB 업데이트
    ▼
[7단계] CMDS 형식 변환 (Python)
    └── 최종 마이그레이션 데이터 생성
```

---

## 3. 사전 준비 - 필수 소프트웨어 설치

### 3.1 Java 개발 환경

#### JDK 설치 (Java 11 이상)

1. [Oracle JDK 다운로드 페이지](https://www.oracle.com/java/technologies/downloads/) 접속
2. Windows x64 Installer 다운로드 및 설치
3. 환경변수 설정:
    - `JAVA_HOME`: JDK 설치 경로 (예: `C:\Program Files\Java\jdk-17`)
    - `Path`에 `%JAVA_HOME%\bin` 추가

**설치 확인:**
```cmd
java -version
```

#### IntelliJ IDEA 설치 (권장)

1. [IntelliJ IDEA 다운로드](https://www.jetbrains.com/idea/download/) 접속
2. Community Edition (무료) 다운로드 및 설치

### 3.2 Python 환경
📋 설치 방법 (2가지 중 선택)

- 방법 1: Anaconda 설치 

**Anaconda 하나만 설치하면 Python + Jupyter + 기본 패키지가 모두 설치됩니다.**

**다운로드**: https://www.anaconda.com/download

**설치 후 확인:**
```bash
python --version
jupyter --version
```

**추가 패키지 설치:**
```bash
pip install selenium pymysql
```

---

- 방법 2: 개별 설치

#### 1. Python 3.8 이상 설치

**다운로드**: https://www.python.org/downloads/

**설치 시 주의사항:**
- ✅ "Add Python to PATH" 반드시 체크

**설치 확인:**
```bash
python --version
```

#### 2. Jupyter Notebook 설치
```bash
pip install jupyter
```

#### 3. 필요한 패키지 설치
```bash
pip install selenium pandas pymysql openpyxl
```

### 3.3 Chrome 및 ChromeDriver

#### Chrome 브라우저

1. [Chrome 다운로드](https://www.google.com/chrome/) 에서 설치

#### ChromeDriver 설치

1. Chrome 버전 확인: Chrome 주소창에 `chrome://version` 입력
2. [ChromeDriver 다운로드](https://googlechromelabs.github.io/chrome-for-testing/) 접속
3. Chrome 버전과 일치하는 ChromeDriver 다운로드
4. 압축 해제 후 경로 기억 (예: `C:\Users\{사용자명}\Downloads\chromedriver-win64\chromedriver.exe`)

### 3.4 MariaDB 설치

#### MariaDB 서버 설치

1. [MariaDB 다운로드](https://mariadb.org/download/) 접속
2. Windows MSI 패키지 다운로드
3. 설치 시 root 비밀번호 설정 (예: `1234`)
4. 포트: 기본값 `3306` 사용

#### DBeaver 설치 (DB 관리 도구)

1. [DBeaver 다운로드](https://dbeaver.io/download/) 접속
2. Windows 64 bit installer 다운로드 및 설치

### 3.5 Maven 의존성 (Java 프로젝트용)

IntelliJ에서 새 Maven 프로젝트 생성 후 `pom.xml`에 다음 의존성 추가:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>anyfive-crawler</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Selenium WebDriver -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>4.15.0</version>
        </dependency>

        <!-- MariaDB JDBC Driver -->
        <dependency>
            <groupId>org.mariadb.jdbc</groupId>
            <artifactId>mariadb-java-client</artifactId>
            <version>3.2.0</version>
        </dependency>

        <!-- Jsoup (HTML 파싱) -->
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.16.2</version>
        </dependency>
    </dependencies>
</project>
```

---

## 4. 데이터베이스 설정

### 4.1 데이터베이스 생성

DBeaver 또는 명령줄에서 실행:

```sql
-- 데이터베이스 생성
CREATE DATABASE any_approval CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 데이터베이스 선택
USE any_approval;
```

### 4.2 테이블 생성

```sql
CREATE TABLE new_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id VARCHAR(50) NOT NULL COMMENT '원본 시스템 문서 ID',
    doc_num VARCHAR(100) COMMENT '문서번호',
    doc_type VARCHAR(20) DEFAULT 'DRAFT' COMMENT '문서유형',
    title VARCHAR(500) COMMENT '제목',
    doc_status VARCHAR(20) DEFAULT 'COMPLETE' COMMENT '문서상태',
    created_at BIGINT COMMENT '기안일 (Unix timestamp)',
    drafter_name VARCHAR(100) COMMENT '기안자 이름',
    drafter_position VARCHAR(100) COMMENT '기안자 직위',
    drafter_dept VARCHAR(200) COMMENT '기안자 부서',
    drafter_email VARCHAR(200) COMMENT '기안자 이메일(ID)',
    drafter_dept_code VARCHAR(50) COMMENT '기안자 부서코드',
    form_name VARCHAR(200) COMMENT '양식명',
    is_public TINYINT DEFAULT 0 COMMENT '문서공개 여부 (0:비공개, 1:공개)',
    end_year INT NOT NULL COMMENT '대상연도',
    `references` TEXT COMMENT '참조문서 JSON',
    attaches TEXT COMMENT '첨부파일 JSON',
    referrers TEXT COMMENT '참조자 JSON',
    activities TEXT COMMENT '결재활동 JSON',
    doc_body LONGTEXT COMMENT '문서본문 HTML',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_source_end (source_id, end_year),
    INDEX idx_end_year (end_year),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 5. 1단계: 문서 ID 목록 추출 (Python)

### 5.1 목적

특정 기간의 전자결재 문서 ID들을 텍스트 파일로 추출합니다. 이 ID 목록은 이후 상세 크롤링의 입력 데이터로 사용됩니다.

### 5.2 코드 수정 (⚠️ 반드시 수정)

파일: `문서ID추출.py`

```python
# ═══════════════════════════════════════════════════════════
# 🔧 설정 (여기만 수정하세요!)
# ═══════════════════════════════════════════════════════════

BASE_URL = "http://office.anyfive.com/home/"

# 기간 설정
START_DATE = "2025-01-01"  # ← 여기를 수정하세요 - 시작일
END_DATE = "2025-12-31"    # ← 여기를 수정하세요 - 종료일

# 출력 파일명
OUTPUT_FILE = "doc_ids_2025.txt"  # ← 여기를 수정하세요
```

### 5.3 실행 방법

1. **Python 스크립트 실행:**
```cmd
python 문서ID추출.py
```

2. **브라우저가 열리면 수동 작업 수행:**

   ① 로그인 (ID/PW 입력)

   ② 좌측 메뉴에서 **"전자결재"** 클릭

   ③ **"결재 문서관리"** 클릭

   ④ 필터 설정:
    - 상태: **완료** 선택
    - 시작일: 설정한 START_DATE 입력
    - 종료일: 설정한 END_DATE 입력

   ⑤ **"조회"** 버튼 클릭

   ⑥ 그리드(목록)가 화면에 나타나면 **Enter** 키 입력

3. **자동 추출 진행:**
    - 스크립트가 모든 페이지를 순회하며 문서 ID 수집
    - 완료 시 `doc_ids_2025.txt` 파일 생성

### 5.4 결과물

```
# doc_ids_2025.txt 예시
"27444","28456","29777","30123","31456",...
```

---

## 6. 2단계: 문서 기본 정보 크롤링 (Java)

### 6.1 목적

1단계에서 추출한 문서 ID를 이용하여 각 문서의 상세 정보를 크롤링합니다.

### 6.2 코드 수정 (⚠️ 반드시 수정)

파일: `AnyFiveNewCrawler.java`

```java
// ========== 크롬 드라이버 경로 ==========
private static final String WEB_DRIVER_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";
    // ↑ 본인의 ChromeDriver 경로로 수정

// ========== DB 연결 정보 ==========
private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234";  // ← DB 비밀번호 수정

// ========== 대상 연도 ==========
private static final int END_YEAR = 2025;  // ← 크롤링 대상 연도 수정

// ========== 크롤링 대상 문서 ID 목록 ==========
private static final String[] TARGET_DOCUMENT_IDS = {
    "27444","28456","29777","30123","31456"
    // ↑ 1단계에서 생성한 doc_ids.txt 내용을 복사하여 붙여넣기
    // 쌍따옴표는 유지하고, 쉼표로 구분
};
```

### 6.3 doc_ids.txt → Java 배열 변환 방법

1. `doc_ids.txt` 파일 열기
2. 전체 내용 복사 (Ctrl+A → Ctrl+C)
3. `TARGET_DOCUMENT_IDS` 배열 안에 붙여넣기

**변환 예시:**
```
// doc_ids.txt 내용:
"27444","28456","29777"

// Java 코드에 붙여넣기:
private static final String[] TARGET_DOCUMENT_IDS = {
    "27444","28456","29777"
};
```

### 6.4 실행 방법

1. IntelliJ에서 `AnyFiveNewCrawler.java` 열기
2. `main` 메서드 옆의 ▶ 버튼 클릭 또는 Shift+F10

### 6.5 실행 과정

```
0단계: 이미 처리된 문서 ID 조회 시작.
  > 이미 처리된 문서 수: 0
  > 처리할 문서 수: 150

2단계: 로그인 및 메뉴 진입.
  > 로그인 버튼 클릭 완료.
  > '다음에 변경하기' 팝업 닫기 완료.
  > Iframe 전환 성공.
  > '전자결재' 아이콘 클릭 완료.
  > '결재문서관리' 메뉴 클릭 완료.

3단계: 크롤링 시작.
  > [1/150] 문서 ID: 27444 처리 중...
    > 저장 완료: 2025년 1월 출장비 정산

  > [2/150] 문서 ID: 28456 처리 중...
    > 저장 완료: 연간 교육계획서

=== 진행 상황: 10/150 (성공: 10, 실패: 0) ===

4단계: 크롤링 완료.
  > 총 처리: 150
  > 성공: 148
  > 실패: 2
```

### 6.6 결과물

- **DB 테이블**: `new_documents`에 문서 기본 정보 저장
- **저장되는 컬럼**: source_id, doc_num, title, created_at, drafter_name, form_name, is_public, references, referrers, activities

### 6.7 재실행 시

- 이미 처리된 문서는 자동으로 스킵됩니다
- 실패한 문서만 다시 처리하려면 해당 ID만 배열에 넣고 재실행

---

## 7. 3단계: 첨부파일 크롤링 (Java)

### 7.1 목적

DB에 저장된 문서들의 첨부파일을 다운로드하고, 경로 정보를 DB에 업데이트합니다.

### 7.2 코드 수정 (⚠️ 반드시 수정)

파일: `AnyFiveNewCrawler_attaches.java`

```java
// ========== 크롬 드라이버 경로 ==========
private static final String WEB_DRIVER_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

//------------------------------------여기부터 수정하세요

// ========== DB 연결 정보 ==========
private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234";

// ========== 대상 연도 ==========
private static final int END_YEAR = 2025;  // ← 크롤링 대상 연도

// ========== 파일 저장 경로 ==========
private static final String DOWNLOAD_BASE_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/approval_2025_new_attachments";
    // ↑ 첨부파일이 저장될 폴더 경로

// ========== DB에 저장될 경로 프리픽스 ==========
private static final String DB_PATH_PREFIX = 
    "/PMS_SITE-U7OI43JLDSMO/approval/approval_2025_new_attachments";
    // ↑ 새 시스템에서 사용할 경로 (연도에 맞게 수정)

//------------------------------------여기까지 수정하세요
```

### 7.3 실행 방법

1. IntelliJ에서 `AnyFiveNewCrawler_attaches.java` 열기
2. `main` 메서드 실행

### 7.4 결과물

**폴더 구조:**
```
C:/Users/LEEJUHWAN/Downloads/approval_2025_new_attachments/
├── apr27444/
│   ├── 출장비정산서.xlsx
│   └── 영수증.pdf
├── apr28456/
│   └── 교육계획서.docx
└── apr29777/
    ├── 회의록.hwp
    └── 첨부이미지.png
```

**DB attaches 컬럼:**
```json
[
  {"name": "출장비정산서.xlsx", "path": "/PMS_SITE-U7OI43JLDSMO/approval/approval_2025_new_attachments/apr27444/출장비정산서.xlsx"},
  {"name": "영수증.pdf", "path": "/PMS_SITE-U7OI43JLDSMO/approval/approval_2025_new_attachments/apr27444/영수증.pdf"}
]
```

---

## 8. 4단계: 본문 이미지 크롤링 (Java)

### 8.1 목적

문서 본문에 포함된 이미지(`<img>` 태그)들을 다운로드합니다.

### 8.2 코드 수정 (⚠️ 반드시 수정)

파일: `AnyFiveNewCrawler_img.java`

```java
// ========== 크롬 드라이버 경로 ==========
private static final String WEB_DRIVER_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

//------------------------------------여기부터 수정하세요

// ========== DB 연결 정보 ==========
private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234";

// ========== 대상 연도 ==========
private static final int END_YEAR = 2025;

// ========== 이미지 저장 경로 ==========
private static final String DOWNLOAD_BASE_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/approval_2025_new_img";

// ========== 처리 완료 목록 파일 (재시작 시 활용) ==========
private static final String PROCESSED_IDS_FILE = 
    DOWNLOAD_BASE_PATH + "/processed_ids.txt";

// ========== 에러 로그 파일 ==========
private static final String ERROR_LOG_FILE = 
    DOWNLOAD_BASE_PATH + "/download_errors.log";

//------------------------------------여기까지 수정하세요
```

### 8.3 실행 방법

1. IntelliJ에서 `AnyFiveNewCrawler_img.java` 열기
2. `main` 메서드 실행

### 8.4 특징

- **자동 재시작 지원**: `processed_ids.txt` 파일로 진행 상황 저장
- **중단 후 재실행**: 이미 처리된 문서는 자동 스킵
- **에러 로깅**: 실패한 이미지는 `download_errors.log`에 기록

### 8.5 결과물

**폴더 구조:**
```
C:/Users/LEEJUHWAN/Downloads/approval_2025_new_img/
├── processed_ids.txt          # 처리 완료 목록
├── download_errors.log        # 에러 로그
├── apr27444/
│   ├── 0.jpg                  # 첫 번째 이미지
│   ├── 1.jpg                  # 두 번째 이미지
│   └── 2.jpg                  # 세 번째 이미지
├── apr28456/
│   └── 0.jpg
└── ...
```

---

## 9. 5단계: 문서 본문(HTML) 크롤링 (Java)

### 9.1 목적

인쇄 페이지를 HTML로 추출하여 1줄로 압축한 후 DB에 저장합니다.

### 9.2 사전 준비

**CSS 파일 필요**: 기존에 크롤링한 CSS 파일이 필요합니다.
- src/main/resources/css/apr.doc.print.jstl.css
### 9.3 코드 수정 (⚠️ 반드시 수정)

파일: `AnyFiveNewCrawler_docBody.java`

```java
//------------------------------------여기부터 수정하세요
// ========== 크롬 드라이버 경로 ==========
private static final String WEB_DRIVER_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";


// ========== DB 연결 정보 ==========
private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234";

// ========== 대상 연도 ==========
private static final int END_YEAR = 2025;

// ========== CSS 파일 경로 ==========
private static final String CSS_FILE_PATH = 
    "src/main/resources/css/apr.doc.print.jstl.css";


// ========== 이미지 경로 프리픽스 ==========
private static final String IMG_PATH_PREFIX = 
    "/PMS_SITE-U7OI43JLDSMO/approval/approval_2025_new_img";
    // ↑ 4단계에서 다운로드한 이미지 경로와 매칭

//------------------------------------여기까지 수정하세요
```

### 9.4 실행 방법

1. IntelliJ에서 `AnyFiveNewCrawler_docBody.java` 열기
2. `main` 메서드 실행

### 9.5 처리 내용

이 크롤러는 다음 작업을 수행합니다:

1. 각 문서의 인쇄 페이지 열기
2. HTML 추출
3. 불필요한 섹션 제거 (첨부파일 영역, 결재의견, 결재댓글, 조회자)
4. 이미지 src 경로 변환 (`/image/namoimage/...` → `/PMS_SITE.../0.jpg`)
5. CSS 인라인 삽입
6. HTML 압축 (minify)
7. DB `doc_body` 컬럼에 저장

### 9.6 결과물

**DB doc_body 예시:**
```html
<div id="middle" style="margin-top:45px;"><div class="content">...문서내용...</div></div><style>.content{width:80% !important;}...</style>
```

---

## 10. 6단계: 조직도(인사정보) DB 반영 (Python)

### 10.1 목적

- 인사정보 CSV 파일을 읽어서 DB의 결재자/참조자 정보를 업데이트합니다.

### 10.2 사전 준비

- 깃허브에서 any_htmlVer_all/새로운크롤링/6(NewUser_insert).ipynb 다운로드
- 6(NewUser_insert).ipynb와 같은 위치에 any_htmlVer_all/새로운크롤링/인사정보_부서코드추가.csv를 다운

**CSV 형식:**
```csv
사원명,ID,부서,사원번호,직위,부서코드
홍길동,hong,경영지원팀,200101010001,책임,DEPT001
김철수,kim,개발팀,200201020002,선임,DEPT002
...
```

### 10.3 코드 수정 (⚠️ 반드시 수정)

파일: `6(NewUser_insert).py`

```python
# ═══════════════════════════════════════════════════════════
# 설정 #여기를 수정하세요
# ═══════════════════════════════════════════════════════════

# DB 설정
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '1234',          # ← DB 비밀번호
    'database': 'any_approval'
}

CSV_FILE = '인사정보_부서코드추가.csv'  # ← CSV 파일 경로
```

### 10.4 실행 방법

```cmd
python 6(NewUser_insert).py
```
혹은 ctrl + enter

### 10.5 처리 내용

| 구분 | 현직자 (CSV에 있음) | 퇴사자 (CSV에 없음) |
|------|---------------------|---------------------|
| 기안자 | CSV 정보로 업데이트 | email='master', 나머지 공란 |
| activities | CSV 정보로 업데이트 | 이름만 남기고 공란 |
| referrers | empNo, deptCode 추가 | 이름만 남기고 공란 |

### 10.6 결과물

**업데이트 전:**
```json
{
  "name": "홍길동",
  "emailId": "",
  "deptName": "",
  "positionName": "",
  "deptCode": ""
}
```

**업데이트 후:**
```json
{
  "name": "홍길동",
  "emailId": "hong",
  "deptName": "경영지원팀",
  "positionName": "책임",
  "deptCode": "DEPT001"
}
```

---

## 11. 7단계: CMDS 형식 변환 (Python)

### 11.1 목적

DB에 저장된 데이터를 최종 마이그레이션 형식(CMDS)으로 변환합니다.

### 11.2 사전 준비

- 깃허브에서 any_htmlver/새로운크롤링/12_8(DB에서 cmds로 변환).ipynb 다운로드

### 11.2 실행

```cmd
python 12_8(DB에서 cmds로 변환).py
```
혹은 ctrl + enter

## 🛠️ 수동으로 수정해야 할 것들
### Notepad++ 정규식 작업
**doc_sourceId_숫자 붙이기**
- 패턴: `("sourceId":\s*")(\d+)(")`
- 치환: `\1doc_\2_숫자\3`

---

## 12. 문제 해결 (Troubleshooting)

### 12.1 ChromeDriver 관련 오류

**오류 메시지:**
```
SessionNotCreatedException: session not created: This version of ChromeDriver only supports Chrome version XX
```

**해결:**
1. Chrome 버전 확인: `chrome://version`
2. 해당 버전의 ChromeDriver 다운로드
3. `WEB_DRIVER_PATH` 경로 수정

### 12.2 DB 연결 오류

**오류 메시지:**
```
Communications link failure
```

**해결:**
1. MariaDB 서비스 실행 확인: `services.msc` → MariaDB 서비스 시작
2. 포트 번호 확인 (기본: 3306)
3. 방화벽 설정 확인

### 12.3 로그인 실패

**증상**: 브라우저가 열리지만 로그인이 안 됨

**해결:**
1. 수동으로 브라우저에서 로그인 테스트
2. ID/PW 확인
3. 네트워크 연결 확인

### 12.4 메모리 부족

**증상**: 대량 크롤링 시 Out of Memory

**해결:**
1. 문서 ID를 배치(batch)로 나누어 실행 (예: 500개씩)
2. Java 힙 메모리 증가: `-Xmx2g` 옵션 추가

### 12.5 크롤링 중단 후 재시작

**상황**: 크롤링 도중 오류로 중단됨

**해결:**
- **2단계 (기본정보)**: 자동으로 처리된 문서 스킵 (DB 기준)
- **3단계 (첨부파일)**: attaches 컬럼이 비어있는 문서만 처리
- **4단계 (이미지)**: `processed_ids.txt` 기준으로 자동 스킵
- **5단계 (본문)**: doc_body 컬럼이 비어있는 문서만 처리

### 12.6 한글 인코딩 오류

**증상**: 파일명이나 내용이 깨짐

**해결:**
1. DB charset 확인: `utf8mb4`
2. 파일 저장 시 UTF-8 인코딩 사용
3. CSV 파일: UTF-8 with BOM 또는 `utf-8-sig` 인코딩


