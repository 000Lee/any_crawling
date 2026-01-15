# 📋 애니파이브 전자결재 크롤링 가이드

> 이 문서는 office.anyfive.com 전자결재 시스템에서 데이터를 추출하여 새로운 시스템으로 마이그레이션하기 위한 cmds를 만드는 전체 과정입니다.
> 파일 이름에 AnyFiveOOJnlOOCrawler_OO 형식으로 되어있는것은 업무관리 데이터 크롤러로 이에 관련된 설명은 모두 [any_jnl_crawling](https://github.com/000Lee/any_jnl_crawling.git)에 있습니다.
--
## 결재선 크롤링
- 설명과 자바코드는 현재 📋 애니파이브 전자결재 크롤링 가이드

> 이 문서는 office.anyfive.com 전자결재 시스템에서 데이터를 추출하여 새로운 시스템으로 마이그레이션하기 위한 cmds를 만드는 전체 과정입니다.
> 파일 이름에 AnyFiveOOJnlOOCrawler_OO 형식으로 되어있는것은 업무관리 데이터 크롤러로 이에 관련된 설명은 모두 [any_jnl_crawling](https://github.com/000Lee/any_jnl_crawling.git)에 있습니다.
> AnyFiveBoardPlusCrawler_attaches.java, AnyFiveBoardPlusCrawler_img.java는 게시판 크롤러로 이에 관련된 설명은 모두 [any_board](https://github.com/000Lee/any_board.git)에 있습니다.
--
## 결재선 크롤링
- 설명과 자바코드는 현재 페이지을 참고
- 파이썬코드는 [any_approval_plus](https://github.com/000Lee/any_approval_plus.git)을 참고
--
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
14. [⭐전자결재 최신 버전⭐](#14-전자결재-최신-버전)

---

## 1. 개요

### 1.1 이 가이드의 목적

- 애니파이브 전자결재 시스템(office.anyfive.com)에 저장된 결재 문서들을 추출하여 새로운 시스템으로 이전하는 cmds를 생성하는 작업을 단계별로 안내합니다.
- any_crawling_earlyVersion은 첨부파일,이미지,정보수집을 한번에 하며 상세를 하나하나 눌러서 들어갔다가 나오기 때문에 타임아웃 오류에 취약하고 복구 로직 또한 안정적이지 않습니다. (사용자가 재시작점을 수동으로 매번 설정해야합니다)
- any_crawling의 경우 문서 ID만 따로 크롤링해서 사전수집을 한것을 바탕으로 호출해서 정보를 수집합니다. 이미지, 첨부파일 모두 따로 다운받고 DB에 저장되어 있지 않은 데이터만 자동으로 수집하므로 보다 안정적으로 재시작이 가능합니다.

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
- 깃허브 any_crawling은 증분치를 위해 나중에 만들어졌습니다.
- 깃허브 any_htmlver이 초안이고 테이블명은 documents입니다.
- 깃허브 any_crawling은 new_documents 테이블에 정보를 저장합니다.
- new_documents 테이블에 정보를 다 저장 하고 cmds를 생성한 뒤에 documents로 추후에 옮겨서 저장하였습니다.
- 이와 관련해서 깃허브 any_htmlver에서        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
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
- 깃허브 any_crawling은 증분치를 위해 나중에 만들어졌습니다.
- 깃허브 any_htmlver이 초안이고 테이블명은 documents입니다.
- 깃허브 any_crawling은 new_documents 테이블에 정보를 저장합니다.
- new_documents 테이블에 정보를 다 저장 하고 cmds를 생성한 뒤에 documents로 추후에 옮겨서 저장하였습니다.
~~- 이와 관련해서 깃허브 [any_htmlver](https://github.com/000Lee/any_htmlver.git)에서 ⭐⭐⭐누락된 문서 확인 & 대처⭐⭐⭐를 확인해주세요~~
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
```

---

## 5. 1단계: 전자결재 문서 ID 추출
- 파일 위치 : any_htmlVer_all/새로운크롤링/해당 기간 내에 있는 문서 ID만 txt파일로 가져오는 파이썬코드.ipynb
  
### 5.1 목적
특정 기간의 전자결재 문서 ID들을 텍스트 파일로 추출합니다. 이 ID 목록은 이후 상세 크롤링의 입력 데이터로 사용됩니다.

### 5.2 코드 수정 (⚠️ 반드시 수정)

파일: `해당 기간 내에 있는 문서 ID만 txt파일로 가져오는 파이썬코드.ipynb`

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
혹은 해당 스크립트에서 ctrl + enter
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

- 프로그램 시작 시 new_documents 테이블에서 이미 저장된 source_id를 조회해서 처리 여부를 판단한 후 이미 처리된 문서는 자동으로 스킵됩니다

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
2. `main` 메서드 옆의 ▶ 버튼 클릭 또는 Shift+F10

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
- 프로그램 시작 시 new_documents 테이블에서 attaches가 비어있는 문서만 조회해서 처리 여부를 판단한 후 이미 처리된 문서는 자동으로 스킵됩니다
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
2. `main` 메서드 옆의 ▶ 버튼 클릭 또는 Shift+F10

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

**CSS 파일 필요**: 애니파이브 홈페이지에서 다운로드한 CSS 파일이 필요합니다.
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
2. `main` 메서드 옆의 ▶ 버튼 클릭 또는 Shift+F10

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
| 기안자 | CSV 정보로 업데이트 | email='master', 이름만 남기고 공란 |
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
- 여기서 숫자 = 이관 시도 횟수 "deptName": "",
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
- 여기서 숫자 = 이관 시도 횟수
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

---

## 📌 추가 크롤러 가이드

### 추가 크롤러 목록

| 크롤러 | 파일명 | 목적 |
|--------|--------|------|
| 결재 라인 크롤러 | `AnyFiveActiviesCrawler.java` | 결재 순서/상태/결재자 정보 수집 |
| 결재 라인 크롤러 (ID목록 직접 입력) | `AnyFiveActiviesCrawler_plus.java` | 특정 문서만 선택적 크롤링 |
| 결재 댓글 크롤러 | `AnyFiveCommentCrawler.java` | 결재 문서의 댓글 수집 |
| 누락 첨부파일 크롤러 | `AnyFivePlusCrawler_attaches.java` | 누락된 첨부파일 보완 다운로드 |
| 참조문서 크롤러 | `AnyFiveReferenceDocCrawler.java` | 다운로드파일에 참조문서 정보 없어서 따로 크롤링 |
| 메타데이터 크롤러 수정판 | `AnyFiveNewCrawler9670.java` | 상세접속 후 호출한 문서와 호출된 문서가 같은지 확인|
---

## A. 결재 라인 데이터 크롤링 (AnyFiveActiviesCrawler)

### A.1 목적

전자결재 문서의 **결재 라인 정보**를 추출하여 별도 테이블에 저장합니다.

| 추출 항목 | 설명 |
|-----------|------|
| sequence | 결재 순서 |
| status | 결재 상태 (승인, 반려 등) |
| approval_date | 결재 일시 |
| department | 결재자 부서 |
| approver | 결재자 이름 |

### A.2 사전 준비

**테이블 생성:**
```sql
CREATE TABLE approval_data_2025 (
    record_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    document_id VARCHAR(255) NOT NULL COMMENT '문서 ID',
    post_title VARCHAR(512) COMMENT '문서 제목',
    sequence INT(11) COMMENT '결재 순서',
    status VARCHAR(50) COMMENT '결재 상태',
    approval_date VARCHAR(50) COMMENT '결재 일시',
    department VARCHAR(100) COMMENT '결재자 부서',
    approver VARCHAR(100) COMMENT '결재자 이름',
    
    INDEX idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**document_sources 테이블 필요:**
- 크롤링 대상 문서 ID는 `document_sources` 테이블의 `source_id` 컬럼에서 로드됩니다.
- `end_year = 2025` 조건으로 필터링됩니다.

### A.3 코드 수정 (⚠️ 반드시 수정)

파일: `AnyFiveActiviesCrawler.java`

```java
// ========== 크롬 드라이버 경로 ==========
private static final String WEB_DRIVER_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";
    // ↑ 본인의 ChromeDriver 경로로 수정

// ========== DB 연결 정보 ==========
private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234";  // ← DB 비밀번호 수정
```

### A.4 실행 방법

1. IntelliJ에서 `AnyFiveActiviesCrawler.java` 열기
2. `main` 메서드 옆의 ▶ 버튼 클릭 또는 Shift+F10

### A.5 실행 과정

```
0단계: DB에서 크롤링할 문서 ID 목록 로드 시작.
  > DB 로드 완료. 총 500개의 문서 ID 확인.

2단계: 로그인 및 메뉴 진입.
  > 로그인 버튼 클릭 완료.
  > '전자결재' 아이콘 클릭 완료.
  > '결재문서관리' 메뉴 클릭 완료.

3단계: DB 재연결 성공 (건별 삽입 준비).
  > 문서 ID: 27444 처리 중...
    > 결재 라인 테이블 로드 확인. (제목: 2025년 1월 출장비 정산)
    > 배치 실행 및 커밋 완료. (5 rows inserted for 27444)
    > 목록 페이지로 복귀.

4단계: 모든 DB 작업이 완료되었습니다.
  > 총 2500개 행 DB에 삽입 완료.
```

### A.6 재실행 시

- 이미 `approval_data_2025` 테이블에 존재하는 `document_id`는 **자동으로 스킵**됩니다.
- 중단 후 재실행해도 중복 삽입 없이 이어서 진행됩니다.

### A.7 결과물

**DB 테이블 예시:**
| document_id | post_title | sequence | status | approval_date | department | approver |
|-------------|------------|----------|--------|---------------|------------|----------|
| 27444 | 출장비 정산 | 1 | 기안 | 2025-01-15 09:30 | 개발팀 | 홍길동 |
| 27444 | 출장비 정산 | 2 | 승인 | 2025-01-15 10:15 | 개발팀 | 김철수 |
| 27444 | 출장비 정산 | 3 | 승인 | 2025-01-15 14:00 | 경영지원팀 | 이영희 |

---

## B. 결재 라인 데이터 크롤링 - 문서ID 직접 입력 버전 (AnyFiveActiviesCrawler_plus)

### B.1 목적

**특정 문서 ID만 선택적으로** 결재 라인 데이터를 크롤링합니다. DB에서 자동 로드하지 않고 코드에 직접 ID를 지정합니다.

### B.2 사용 시점

- 특정 기간의 문서만 추가로 크롤링할 때
- 누락된 문서를 보완할 때
- 테스트 목적으로 소수의 문서만 처리할 때

### B.3 사전 준비

**테이블 생성:**
```sql
CREATE TABLE approval_data_plus (
    record_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    document_id VARCHAR(255) NOT NULL COMMENT '문서 ID',
    post_title VARCHAR(512) COMMENT '문서 제목',
    sequence INT(11) COMMENT '결재 순서',
    status VARCHAR(50) COMMENT '결재 상태',
    approval_date VARCHAR(50) COMMENT '결재 일시',
    department VARCHAR(100) COMMENT '결재자 부서',
    approver VARCHAR(100) COMMENT '결재자 이름',
    
    INDEX idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### B.4 코드 수정 (⚠️ 반드시 수정)

파일: `AnyFiveActiviesCrawler_plus.java`

```java
// ========== 크롬 드라이버 경로 ==========
private static final String WEB_DRIVER_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

// ========== DB 연결 정보 ==========
private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234";

// ========== 크롤링 대상 문서 ID 목록 - 여기에 직접 입력 ==========
private static final String[] TARGET_DOCUMENT_IDS = {
    "26836938","26824807","26824251","26821433","26820164"
    // ↑ 크롤링할 문서 ID를 쉼표로 구분하여 입력
};
```

### B.5 실행 방법

1. `TARGET_DOCUMENT_IDS` 배열에 크롤링할 문서 ID 입력
2. IntelliJ에서 `AnyFiveActiviesCrawler_plus.java` 실행

### B.6 재실행 시

- `approval_data_plus` 테이블에서 이미 처리된 `document_id`를 조회하여 자동 스킵
- 중단 후 재실행 시 처리되지 않은 문서만 진행

---

## C. 결재 댓글 크롤링 (AnyFiveCommentCrawler)

### C.1 목적

전자결재 문서에 작성된 **결재 댓글**을 추출합니다.

| 추출 항목 | 설명 |
|-----------|------|
| sourceId | 댓글 고유 ID (순번_문서ID_01 형식) |
| sourceDocumentId | 원본 문서 ID |
| writer | 댓글 작성자 |
| createdAt | 작성 일시 (Unix timestamp) |
| message | 댓글 내용 |

### C.2 코드 수정 (⚠️ 반드시 수정)

파일: `AnyFiveCommentCrawler.java`

```java
// ========== 크롬 드라이버 경로 ==========
private static final String WEB_DRIVER_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

// ========== DB 연결 정보 ==========
private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234";

// ========== 파일 저장 경로 ==========
private static final String PROGRESS_DIR = "C:/Users/LEEJUHWAN/Downloads/comment_crawl";
// ↑ 진행 상황 및 에러 로그가 저장될 폴더

// ========== 크롤링 대상 문서 ID 목록 ==========
private static final String[] TARGET_DOCUMENT_IDS = {
    "2006627","2006626","2006625"
    // ↑ 크롤링할 문서 ID를 입력
};
```

### C.3 실행 방법

1. `TARGET_DOCUMENT_IDS` 배열에 크롤링할 문서 ID 입력
2. IntelliJ에서 `AnyFiveCommentCrawler.java` 실행

### C.4 재시작 지원

크롤링 중단 시 재시작을 지원합니다:

| 파일 | 용도 |
|------|------|
| `processed_ids.txt` | 처리 완료된 문서 ID 목록 |
| `error_log.txt` | 오류 발생 문서 및 에러 메시지 |

- 재실행 시 `processed_ids.txt`에 있는 문서는 자동 스킵
- 완전히 처음부터 시작하려면 `processed_ids.txt` 파일 삭제

### C.5 결과물

**폴더 구조:**
```
C:/Users/LEEJUHWAN/Downloads/comment_crawl/
├── processed_ids.txt     # 처리 완료 목록
└── error_log.txt         # 에러 로그
```

**추출되는 댓글 정보 예시:**
```
sourceId: 01_27444_01
sourceDocumentId: 27444
writer: 홍길동
createdAt: 1705290600000
message: 검토 완료했습니다. 승인합니다.
```

---

## D. 누락 첨부파일 보완 크롤링 (AnyFivePlusCrawler_attaches)

### D.1 목적

기존 크롤링에서 **누락되거나 실패한 첨부파일**을 보완 다운로드합니다.

### D.2 특징

| 특징 | 설명 |
|------|------|
| **All or Nothing** | 모든 첨부파일 다운로드 성공 시에만 DB 업데이트 |
| **CSV 입력 지원** | 누락 목록을 CSV로 입력 가능 |
| **다중 로그 관리** | 완료/실패/이름불일치/개수경고 별도 로그 |
| **재시작 지원** | 완료된 항목 자동 스킵 |

### D.3 코드 수정 (⚠️ 반드시 수정)

파일: `AnyFivePlusCrawler_attaches.java`

```java
// ========== 크롬 드라이버 경로 ==========
private static final String WEB_DRIVER_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

// ========== DB 연결 정보 ==========
private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234";

// ========== 소스 ID 로딩 방식 ==========
// true: CSV 파일에서 읽기, false: MANUAL_SOURCE_IDS 리스트 사용
private static final boolean USE_CSV = true;
private static final String CSV_PATH = "C:/Users/LEEJUHWAN/empty_path_documents.csv";

// 수동 지정 시 사용할 source_id 리스트 (USE_CSV = false일 때 사용)
private static final List<String> MANUAL_SOURCE_IDS = Arrays.asList(
    "2002144",
    "2002200",
    "2002203"
);

// ========== 파일 저장 경로 ==========
// 실제 파일 저장 기본 경로
private static final String DOWNLOAD_BASE_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/approval_plus_attachments";

// DB에 저장될 경로 프리픽스
private static final String DB_PATH_PREFIX = 
    "/PMS_SITE-U7OI43JLDSMO/approval/approval_plus_attachments";
```

### D.4 입력 방식

**방식 1: CSV 파일 사용 (`USE_CSV = true`)**

CSV 파일 형식:
```csv
source_id,doc_num,title,created_at,attaches
2002144,문서번호-001,제목1,1705290600000,"[{""name"":""파일1.xlsx"",""path"":""/old/path""}]"
2002200,문서번호-002,제목2,1705290700000,"[]"
```

**방식 2: 수동 리스트 사용 (`USE_CSV = false`)**

```java
private static final List<String> MANUAL_SOURCE_IDS = Arrays.asList(
    "2002144",
    "2002200",
    "2002203"
);
```

### D.5 실행 방법

1. `USE_CSV` 설정에 따라 CSV 경로 또는 수동 리스트 설정
2. 파일 저장 경로 및 DB 경로 프리픽스 설정
3. IntelliJ에서 `AnyFivePlusCrawler_attaches.java` 실행

### D.6 로그 파일

| 로그 파일 | 내용 |
|-----------|------|
| `crawler_completed.txt` | 처리 완료된 source_id 목록 |
| `crawler_failed.txt` | 실패한 source_id 및 사유 |
| `crawler_name_mismatch.txt` | CSV와 실제 파일명이 다른 경우 |
| `crawler_count_warning.txt` | CSV와 실제 첨부파일 개수가 다른 경우 |

### D.7 실행 과정

```
========================================
1단계: source_id 목록 로드
========================================
  > CSV 파일에서 로드: C:/Users/.../empty_path_documents.csv
  > 총 로드된 source_id: 150건

========================================
2단계: 완료된 항목 확인 (재시작 지원)
========================================
  > 이미 완료된 source_id: 50건
  > 처리 대상 source_id: 100건

========================================
5단계: 첨부파일 크롤링 시작
========================================

------------------------------------------
[1/100] source_id: 2002144 처리 중...
------------------------------------------
  > 크롤링된 첨부파일 수: 3
    > 다운로드 성공: 계약서.pdf
    > 다운로드 성공: 견적서.xlsx
    > 다운로드 성공: 사진.jpg
  > 다운로드 완료: 3/3
  > DB 업데이트 완료

========================================
6단계: 크롤링 완료
========================================
  > 총 처리 대상: 100
  > 성공: 98
  > 실패: 2
  > 다운로드된 파일 수: 287
```

### D.8 결과물

**폴더 구조:**
```
C:/Users/LEEJUHWAN/Downloads/approval_plus_attachments/
├── apr2002144/
│   ├── 계약서.pdf
│   ├── 견적서.xlsx
│   └── 사진.jpg
├── apr2002200/
│   └── 보고서.docx
└── ...
```

**DB attaches 컬럼 업데이트:**
```json
[
  {"name":"계약서.pdf","path":"/PMS_SITE-U7OI43JLDSMO/approval/approval_plus_attachments/apr2002144/계약서.pdf"},
  {"name":"견적서.xlsx","path":"/PMS_SITE-U7OI43JLDSMO/approval/approval_plus_attachments/apr2002144/견적서.xlsx"},
  {"name":"사진.jpg","path":"/PMS_SITE-U7OI43JLDSMO/approval/approval_plus_attachments/apr2002144/사진.jpg"}
]
```

---
---

## E. 참조문서 크롤링 (AnyFiveReferenceDocCrawler)

### E.1 목적

전자결재 문서에 연결된 **참조문서 ID**를 추출하여 별도 테이블에 저장합니다.

| 추출 항목 | 설명 |
|-----------|------|
| source_document_id | 원본 문서 ID |
| reference_document_id | 참조문서 ID |

### E.2 사전 준비

**테이블 생성:**
```sql
CREATE TABLE reference_documents (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    source_document_id VARCHAR(255) NOT NULL COMMENT '원본 문서 ID',
    reference_document_id VARCHAR(255) NOT NULL COMMENT '참조문서 ID',
    
    INDEX idx_source_document_id (source_document_id),
    INDEX idx_reference_document_id (reference_document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### E.3 코드 수정 (⚠️ 반드시 수정)

파일: `AnyFiveReferenceDocCrawler.java`
```java
// ========== 크롬 드라이버 경로 ==========
private static final String WEB_DRIVER_PATH = 
    "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";
    // ↑ 본인의 ChromeDriver 경로로 수정

// ========== DB 연결 정보 ==========
private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234";  // ← DB 비밀번호 수정

// ========== 크롤링 대상 문서 ID 목록 - 여기에 직접 입력 ==========
List<String> documentIds = new ArrayList<>();
documentIds.addAll(Arrays.asList(
    "15377405", "15391835", "15484981"
    // ↑ 참조문서를 추출할 문서 ID를 쉼표로 구분하여 입력
));
```

### E.4 실행 방법

1. `documentIds` 리스트에 크롤링할 문서 ID 입력
2. IntelliJ에서 `AnyFiveReferenceDocCrawler.java` 실행

### E.5 실행 과정
```
총 250개의 문서 ID 로드 완료.
2단계: 로그인 및 메뉴 클릭을 통한 게시판 진입 시도.
  > 로그인 버튼 클릭 완료.
  > '전자결재' 아이콘 클릭 완료.
  > '결재문서관리' 메뉴 클릭 완료.

3단계: DB 연결 성공 (참조문서 삽입 준비).

  > [1/250] 문서 ID: 15377405 처리 중...
  > JS 함수 호출 성공: managementDocList.clickGridRow(15377405);
    - 참조문서 ID 발견: 15370001
    - 참조문서 ID 발견: 15370025
  > 배치 실행 및 커밋 완료. (2 참조문서 저장)
  > 목록 페이지로 복귀.

4단계: 모든 DB 작업이 완료되었습니다.
  > 총 250개 문서 처리 완료
  > 참조문서가 있는 문서: 180개
  > 참조문서가 없는 문서: 70개
  > 총 320개 참조문서 관계 DB에 저장 완료.
```

### E.6 결과물

**DB 테이블 예시:**
| source_document_id | reference_document_id |
|--------------------|----------------------|
| 15377405 | 15370001 |
| 15377405 | 15370025 |
| 17030834 | 17025001 |
---
## F. 문서 기본정보 크롤링 - 별도 테이블 버전 (AnyFiveNewCrawler9670)
기존 크롤러와 유사하나, 상세 접속 이후 문서 ID 검증로직이 추가되었습니다.

---
---

## 14. ⭐전자결재 최신 버전⭐

> 이 섹션은 전자결재 최신 버전 가이드입니다. (2026-01-14)
> 
> - **자바 코드 설명**: 현재 페이지 (any_crawling)
> - **파이썬 코드 설명**: [any_approval_plus](https://github.com/000Lee/any_approval_plus.git) - `전자결재 추가 수정/전자결재 증증분치 (2025이전)/` 경로

---

### 14.1 설정 변경 사항

#### URL 변경
```java
// 기존
private static final String BASE_URL = "http://office.anyfive.com";

// 변경
private static final String BASE_URL = "https://auth.onnet21.com/?re=anyfive.onnet21.com/sso/login";
```

#### DB 테이블명
- 테이블명은 용도에 맞게 자유롭게 지정 (예: `new_documents_2024`, `new_documents_2023` 등)
- 모든 Java 크롤러와 Python 스크립트에서 동일한 테이블명 사용 필수

---

### 14.2 전체 실행 순서 (16단계)
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    전자결재 증분 크롤링 워크플로우 (2025년 이전)                    │
└─────────────────────────────────────────────────────────────────────────────────┘

[1단계] 🐍 문서 ID 추출 ─────────────────────────> doc_ids.txt
         └── 파이썬코드_증증분치.ipynb
                                                      │
[2단계] 🐍 누락 문서 찾기 ───────────────────────> 누락 ID 목록
         └── find_missing_docs_multi_table.ipynb
                                                      │
                                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              Java 크롤링 단계                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│ [3단계] ☕ AnyFiveNewCrawler9670.java ────────> 문서 기본정보 크롤링              │
│         └── 2번 결과를 TARGET_DOCUMENT_IDS에 입력                                │
│         └── 참조자 괄호 제거 SQL 실행 (후처리)                                    │
│                                                                                 │
│ [4단계] ☕ AnyFiveNewCrawler_attaches.java ───> 첨부파일 크롤링                   │
│         └── 다운로드 검증 필수                                                   │
│                                                                                 │
│ [5단계] ☕ AnyFiveNewCrawler_img.java ────────> 본문 이미지 크롤링                │
│         └── 다운로드 검증 필수                                                   │
│                                                                                 │
│ [6단계] ☕ AnyFiveNewCrawler_docBody.java ────> 문서 본문 HTML 크롤링             │
│                                                                                 │
│ [7단계] ☕ AnyFiveCommentCrawler.java ────────> 결재 댓글 크롤링                  │
│         └── 2번 결과를 TARGET_DOCUMENT_IDS에 입력                                │
│                                                                                 │
│ [8단계] ☕ AnyFiveActiviesCrawler_plus.java ──> 결재이력 크롤링                   │
│         └── 2번 결과를 TARGET_DOCUMENT_IDS에 입력                                │
│         └── ⚠️ 반려 처리 주의 (수동 작업 필요)                                   │
└─────────────────────────────────────────────────────────────────────────────────┘
                                                      │
                                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           Python 후처리 단계                                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│ [9단계]  🐍 add_year_column.ipynb ───────────> 연도 컬럼 추가                    │
│ [10단계] 🐍 DB에_조직도_다시_반영하기_수정.ipynb > 조직도 반영                    │
│ [11단계] 🐍 update_db_referrers.ipynb ───────> 참조자 업데이트                   │
│ [12단계] 🐍 DB_조직도_반영_검증_v2.ipynb ────> 조직도 검증                        │
│ [13단계] 🐍 fix_activities_order_db3.ipynb ──> 결재순서 정렬 (0건이면 스킵)       │
└─────────────────────────────────────────────────────────────────────────────────┘
                                                      │
                                                      ▼
[14단계] 🗄️ 스타일태그 검증 SQL ─────────────────> 문서당 1개인지 확인

[15단계] 🐍 export_documents_v3.ipynb ───────────> 최종 내보내기 

[16단계] 🐍 comments_to_cmds 증분치.ipynb ───────> 최종 내보내기 (댓글)
```

---

### 14.3 단계별 상세 설명

#### 📌 [1~2단계] 문서 ID 추출 및 누락 문서 찾기 (Python)

> 파이썬 코드 상세 설명은 [any_approval_plus](https://github.com/000Lee/any_approval_plus.git) 참조
> 재실행이 아닌 최초수집이라면 2단계는 건너뛰세요

| 단계 | 파일명 | 설명 |
|:---:|--------|------|
| 1 | `파이썬코드_증증분치.ipynb` | 해당 기간 내 문서 ID를 txt 파일로 추출 |
| 2 | `find_missing_docs_multi_table.ipynb` | 기존 테이블과 비교하여 누락된 문서 ID 추출 |

---

#### 📌 [3단계] 문서 기본정보 크롤링 - AnyFiveNewCrawler9670.java

**기능:**
- 문서 상세 페이지 접근 후 기본 정보 크롤링
- **문서 ID 검증 로직 포함**: 호출한 문서 ID와 실제 로드된 문서 ID 일치 여부 확인
- DB에 문서 메타데이터 저장

**사용 방법:**
```sql
CREATE TABLE 테이블이름 (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    source_id VARCHAR(50) NOT NULL COMMENT '원본 시스템 문서 ID',
    doc_num VARCHAR(100) COMMENT '문서번호',
    doc_type VARCHAR(50) DEFAULT 'DRAFT' COMMENT '문서유형',
    title VARCHAR(500) COMMENT '제목',
    doc_status VARCHAR(50) DEFAULT 'COMPLETE' COMMENT '문서상태',
    created_at BIGINT(20) COMMENT '기안일 (Unix timestamp)',
    drafter_name VARCHAR(100) COMMENT '기안자 이름',
    drafter_position VARCHAR(100) COMMENT '기안자 직위',
    drafter_dept VARCHAR(100) COMMENT '기안자 부서',
    drafter_email VARCHAR(100) COMMENT '기안자 이메일(ID)',
    drafter_dept_code VARCHAR(50) COMMENT '기안자 부서코드',
    form_name VARCHAR(200) COMMENT '양식명',
    is_public TINYINT(1) DEFAULT 0 COMMENT '문서공개 여부 (0:비공개, 1:공개)',
    end_year INT(11) COMMENT '대상연도',
    `references` TEXT COMMENT '참조문서 JSON',
    attaches TEXT COMMENT '첨부파일 JSON',
    referrers TEXT COMMENT '참조자 JSON',
    activities TEXT COMMENT '결재활동 JSON',
    doc_body MEDIUMTEXT COMMENT '문서본문 HTML',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    year INT(11) COMMENT '연도',
    
    UNIQUE KEY uk_source_id (source_id),
    INDEX idx_end_year (end_year),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```
```java
// 1. 2단계 결과(누락 문서 ID)를 TARGET_DOCUMENT_IDS에 입력
private static final String[] TARGET_DOCUMENT_IDS = {
    "27444","28456","29777","30123"
    // ↑ find_missing_docs_multi_table.ipynb 결과를 붙여넣기
};

// 2. 테이블명 수정
private static final String TABLE_NAME = "new_documents_2024";  // ← 원하는 테이블명

// 3. 로그인 URL 수정
private static final String BASE_URL = "https://auth.onnet21.com/?re=anyfive.onnet21.com/sso/login";
```

**후처리 - 참조자 괄호 제거:**

크롤링 완료 후 참조자 컬럼에 `이름(아이디)` 형식이 있는 경우 아래 SQL 실행:
```sql
-- 참조자 컬럼에서 괄호와 내용 제거 (이름만 남김)
UPDATE 테이블이름
SET referrers = REGEXP_REPLACE(referrers, '\\([^)]+\\)', '')
WHERE referrers LIKE '%(%';
```

---

#### 📌 [4단계] 첨부파일 크롤링 - AnyFiveNewCrawler_attaches.java

**기능:**
- DB에 저장된 문서들의 첨부파일 다운로드
- attaches 컬럼 업데이트

**검증 방법:**

크롤링 완료 후 반드시 검증:
```sql
-- DB에 첨부파일이 있는 문서 개수 및 총 첨부파일 개수 확인
SELECT 
    COUNT(DISTINCT source_id) as doc_count,
    SUM(JSON_LENGTH(attaches)) as total_attaches
FROM 테이블이름
WHERE attaches IS NOT NULL AND attaches != '[]';
```

실제 다운로드 폴더에서 확인:
- 폴더 개수 = `doc_count`와 일치해야 함
- 총 파일 개수 = `total_attaches`와 일치해야 함

---

#### 📌 [5단계] 본문 이미지 크롤링 - AnyFiveNewCrawler_img.java

**기능:**
- 문서 본문에 포함된 이미지(`<img>` 태그) 다운로드
- `processed_ids.txt`로 재시작 지원

**검증 방법:**
```sql
-- [6단계] 문서 본문 크롤링 - AnyFiveNewCrawler_docBody.java 실행 후 검증하기
-- DB에 이미지가 있는 문서 개수 및 총 이미지 태그 개수 확인
SELECT 
    COUNT(*) as img_doc_count,
    SUM(
        (LENGTH(doc_body) - LENGTH(REPLACE(LOWER(doc_body), '<img', ''))) / LENGTH('<img')
    ) as total_img_count
FROM 테이블이름
WHERE doc_body LIKE '%<img%';
```

실제 다운로드 폴더에서:
- 폴더 개수 = `img_doc_count`와 일치해야 함
- 총 파일 개수 = `total_img_count`와 일치해야 함

---

#### 📌 [6단계] 문서 본문 크롤링 - AnyFiveNewCrawler_docBody.java

**기능:**
- 인쇄 페이지 HTML 추출
- 불필요 섹션 제거 (첨부파일 영역, 결재의견, 결재댓글, 조회자)
- 이미지 경로 변환
- CSS 인라인 삽입 및 HTML 압축
- DB `doc_body` 컬럼에 저장

---

#### 📌 [7단계] 결재 댓글 크롤링 - AnyFiveCommentCrawler.java

**기능:**
- 전자결재 문서의 결재 댓글 추출
- 재시작 지원 (`processed_ids.txt`)

**사용 방법:**
```sql
CREATE TABLE 테이블이름 (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    source_id VARCHAR(50) COMMENT '댓글 고유 ID',
    source_document_id VARCHAR(20) NOT NULL COMMENT '원본 문서 ID',
    created_at BIGINT(20) COMMENT '작성일시 (Unix timestamp)',
    updated_at BIGINT(20) COMMENT '수정일시 (Unix timestamp)',
    writer VARCHAR(50) COMMENT '작성자',
    message TEXT COMMENT '댓글 내용',
    
    INDEX idx_source_document_id (source_document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```
```java
// 2단계 결과를 TARGET_DOCUMENT_IDS에 입력
private static final String[] TARGET_DOCUMENT_IDS = {
    "27444","28456","29777"
    // ↑ find_missing_docs_multi_table.ipynb 결과
};
```

---

#### 📌 [8단계] 결재테이블 크롤링 - AnyFiveActiviesCrawler_plus.java

**기능:**
- 결재 라인 정보 (순서, 상태, 결재일시, 결재자) 크롤링
- 별도의 테이블에 참고용으로 저장 

**사용 방법:**
```sql
CREATE TABLE 테이블이름 (
    record_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    document_id VARCHAR(255) NOT NULL COMMENT '문서 ID',
    post_title VARCHAR(512) COMMENT '문서 제목',
    sequence INT(11) COMMENT '결재 순서',
    status VARCHAR(50) COMMENT '결재 상태',
    approval_date VARCHAR(50) COMMENT '결재 일시',
    department VARCHAR(100) COMMENT '결재자 부서',
    approver VARCHAR(100) COMMENT '결재자',
    
    INDEX idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```
```java
// 2단계 결과를 TARGET_DOCUMENT_IDS에 입력
private static final String[] TARGET_DOCUMENT_IDS = {
    "27444","28456","29777"
};
```

**⚠️ 반려 처리 주의사항:**

`AnyFiveNewCrawler9670.java`에는 **반려 상태 매핑이 없습니다.**

반려된 문서의 경우:
- `activities` 컬럼의 `type`, `actionLogType`에 한글 **"반려"**로 저장됨
- 건수가 적으므로 **수동 처리** 실행함
- !참고! 초기에는 반려를 RETURN 타입으로 변환했으나, 이후 APPROVAL 타입 + [반려] 코멘트 prefix 방식으로 정책이 변경되었습니다. 관련 변환 코드는 [any_approval_plus](https://github.com/000Lee/any_approval_plus.git)의 DB에 수정 반영하기/ 폴더에 있습니다. (verify_action_type.ipynb->RETURN 변환.ipynb)

**반려 수동 처리 방법:**
```sql
-- (결재테이블에서) 반려가 있는지 확인
SELECT *
FROM 테이블이름
WHERE status = '반려';

-- (결재테이블에서) 반려 건수만 확인
SELECT COUNT(*) as reject_count
FROM 테이블이름
WHERE status = '반려';

-- (결재테이블에서) 반려가 있는 문서 ID 목록 확인
SELECT DISTINCT document_id, post_title
FROM 테이블이름
WHERE status = '반려';

-- 1. (AnyFiveNewCrawler9670.java 결과 테이블에서) 반려 문서 확인 
SELECT source_id, activities
FROM 테이블이름
WHERE activities LIKE '%"actionLogType": "반려"%'
   OR activities LIKE '%"type": "반려"%';

-- 2. (AnyFiveNewCrawler9670.java 결과 테이블에서) 수동으로 수정
-- type: "반려" → "APPROVAL"
-- actionLogType: "반려" → "APPROVAL"  
-- actionComment 맨 앞에 "[반려] " 추가
```

---

#### 📌 [9~13단계] Python 후처리

> 파이썬 코드 상세 설명은 [any_approval_plus](https://github.com/000Lee/any_approval_plus.git) 참조

| 단계 | 파일명 | 설명 | 비고 |
|:---:|--------|------|------|
| 9 | `add_year_column.ipynb` | 연도 컬럼 추가 | |
| 10 | `DB에_조직도_다시_반영하기_수정.ipynb` | 조직도(인사정보) DB 반영 | |
| 11 | `update_db_referrers.ipynb` | 참조자 정보 업데이트 | |
| 12 | `DB_조직도_반영_검증_v2.ipynb` | 조직도 반영 검증 | |
| 13 | `fix_activities_order_db3.ipynb` | 결재순서 정렬 | **0건이면 스킵** |

---

#### 📌 [14단계] 스타일태그 검증 (SQL)

문서 하나당 `<style>` 태그가 1개인지 확인:
```sql
SELECT
    (LENGTH(doc_body) - LENGTH(REPLACE(LOWER(doc_body), '<style', ''))) / LENGTH('<style') AS style_tag_count,
    COUNT(*) AS doc_count
FROM 테이블이름
WHERE doc_body IS NOT NULL
GROUP BY style_tag_count
ORDER BY style_tag_count DESC;
```

**기대 결과:**
| style_tag_count | doc_count |
|-----------------|-----------|
| 1 | (전체 문서 수) |

- `style_tag_count`가 1이 아닌 문서가 있으면 확인 필요

---

#### 📌 [15~16단계] 최종 내보내기 (Python)

> 파이썬 코드 상세 설명은 [any_approval_plus](https://github.com/000Lee/any_approval_plus.git) 참조

| 단계 | 파일명 | 설명 |
|:---:|--------|------|
| 15 | `export_documents_v3.ipynb` | cmds 형식으로 변환 |
| 16 | `comments_to_cmds 증분치.ipynb` | cmds 형식으로 변환 (댓글) |

---

### 14.4 Java 크롤러 설정 요약

| 크롤러 | TARGET_DOCUMENT_IDS | 테이블명 | URL 변경 |
|--------|:-------------------:|:--------:|:--------:|
| AnyFiveNewCrawler9670 | ✅ 2단계 결과 | ✅ | ✅ |
| AnyFiveNewCrawler_attaches | ❌ (DB 기준) | ✅ | ✅ |
| AnyFiveNewCrawler_img | ❌ (DB 기준) | ✅ | ✅ |
| AnyFiveNewCrawler_docBody | ❌ (DB 기준) | ✅ | ✅ |
| AnyFiveCommentCrawler | ✅ 2단계 결과 | ✅ | ✅ |
| AnyFiveActiviesCrawler_plus | ✅ 2단계 결과 | ✅ | ✅ |

---

### 14.5 체크리스트

#### 크롤링 전
- [ ] 로그인 URL 변경 완료
- [ ] 테이블명 통일 확인
- [ ] ChromeDriver 버전 확인
- [ ] DB 연결 정보 확인

#### 크롤링 후
- [ ] 첨부파일 개수 검증 (DB vs 폴더)
- [ ] 이미지 개수 검증 (DB vs 폴더)
- [ ] 반려 문서 확인 및 수동 처리
- [ ] 스타일태그 검증 완료
- [ ] 결재순서 정렬 확인

---
---

