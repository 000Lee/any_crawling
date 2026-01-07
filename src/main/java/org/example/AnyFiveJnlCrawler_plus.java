package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * AnyFiveJnlCrawler_plus.java
 *
 * 모든 문서의 doc_body를 재수집하는 크롤러
 * - 콘텐츠 길이 안정화 대기 방식으로 페이지 로딩 완료 확인
 * - doc_body 컬럼만 UPDATE
 */
public class AnyFiveJnlCrawler_plus {

    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

    private static final String USER_ID = "master";
    private static final String USER_PW = "five4190any#";
    private static final String TARGET_URL = "http://office.anyfive.com";
    private static final String IFRAME_NAME = "content_frame";

    // ------------------------------------ 여기부터 수정하세요
    // MariaDB 연결 정보
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_jnl";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    // DB path 프리픽스
    private static final String DB_PATH_PREFIX = "";

    // 사용자 입력값
    private static final int END_YEAR = 2016;

    // 로그 파일 저장 기본 경로
    private static final String LOG_BASE_DIR = "C:/Users/LEEJUHWAN/Downloads";
    // ------------------------------------ 여기까지 수정하세요

    // 콘텐츠 로딩 대기 설정
    private static final int STABLE_CHECK_INTERVAL_MS = 500;  // 500ms마다 체크
    private static final int STABLE_COUNT_REQUIRED = 3;       // 3번 연속 같으면 완료
    private static final int MAX_WAIT_TIME_MS = 30000;        // 최대 30초 대기

    // 로그 폴더 및 파일 경로 (런타임에 설정)
    private static String LOG_DIR;
    private static String PROCESSED_IDS_FILE;
    private static String RECRAWLED_LOG_FILE;
    private static String ERROR_LOG_FILE;

    // SQL
    private static final String SELECT_SOURCE_IDS_SQL =
            "SELECT source_id FROM jnl_documents WHERE end_year = ? ORDER BY source_id";

    private static final String UPDATE_DOC_BODY_SQL =
            "UPDATE jnl_documents SET doc_body = ? WHERE source_id = ? AND end_year = ?";

    public static void main(String[] args) {
        // 로그 폴더 및 파일 경로 설정
        LOG_DIR = LOG_BASE_DIR + "/jnl_plus_" + END_YEAR;
        PROCESSED_IDS_FILE = LOG_DIR + "/processed_ids.txt";
        RECRAWLED_LOG_FILE = LOG_DIR + "/recrawled_ids.txt";
        ERROR_LOG_FILE = LOG_DIR + "/errors.log";

        // 로그 폴더 생성
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
            System.out.println("로그 폴더 생성: " + LOG_DIR);
        }

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        WebDriver driver = null;
        Connection conn = null;

        // 재수집한 문서 ID 목록
        List<String> recrawledIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();

        try {
            // 1. 처리 완료된 ID 로드
            Set<String> processedIds = loadProcessedIds();
            System.out.println("1단계: 처리 완료된 문서 수: " + processedIds.size());

            // 2. DB에서 해당 연도의 모든 source_id 조회
            System.out.println("2단계: DB에서 end_year=" + END_YEAR + "인 source_id 조회");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            List<String> allDocumentIds = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_SOURCE_IDS_SQL)) {
                pstmt.setInt(1, END_YEAR);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        allDocumentIds.add(rs.getString("source_id"));
                    }
                }
            }
            System.out.println("  > 총 문서 수: " + allDocumentIds.size());

            // 처리 대상 필터링 (이미 처리한 문서 제외)
            List<String> documentIds = new ArrayList<>();
            for (String id : allDocumentIds) {
                if (!processedIds.contains(id)) {
                    documentIds.add(id);
                }
            }

            if (documentIds.isEmpty()) {
                System.out.println("  > 모든 문서가 이미 처리되었습니다. 종료합니다.");
                return;
            }
            System.out.println("  > 처리 대상 문서 수: " + documentIds.size());

            conn.close();
            conn = null;

            // 3. WebDriver 설정
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 4. 로그인 및 메뉴 진입
            System.out.println("3단계: 로그인 및 메뉴 진입");
            driver.get(TARGET_URL);

            driver.findElement(By.id("uid")).sendKeys(USER_ID);
            driver.findElement(By.id("pwd")).sendKeys(USER_PW);
            driver.findElement(By.className("btn_login")).click();
            System.out.println("  > 로그인 버튼 클릭 완료");

            String xpathPassButton = "//button[@class='home' and @onclick='pwChangePass.pass()']";
            WebElement passButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathPassButton)));
            passButton.click();
            System.out.println("  > '다음에 변경하기' 팝업 닫기 완료");

            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
            System.out.println("  > Iframe 전환 성공");

            By jnlLinkSelector = By.xpath("//a[contains(@href, '/jnl/') and contains(@class, 'left_menu')]");
            WebElement jnlLink = wait.until(ExpectedConditions.elementToBeClickable(jnlLinkSelector));
            js.executeScript("arguments[0].click();", jnlLink);
            Thread.sleep(1500);
            System.out.println("  > '업무관리' 아이콘 클릭 완료");

            By corpBoxSelector = By.xpath("//span[contains(@class, 'corp-span')]");
            WebElement corpBox = wait.until(ExpectedConditions.elementToBeClickable(corpBoxSelector));
            js.executeScript("arguments[0].click();", corpBox);
            Thread.sleep(1000);
            System.out.println("  > '회사 업무함' 클릭 완료");

            By lblSelector = By.xpath("//li[@data-lblnm='전사업무보고']");
            WebElement lblItem = wait.until(ExpectedConditions.elementToBeClickable(lblSelector));
            js.executeScript("arguments[0].click();", lblItem);
            Thread.sleep(1500);
            System.out.println("  > '전사업무보고' 클릭 완료");

            // 5. DB 재연결 및 크롤링 시작
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("4단계: doc_body 재수집 시작");

            PreparedStatement updatePstmt = conn.prepareStatement(UPDATE_DOC_BODY_SQL);
            int totalProcessed = 0;
            int errorCount = 0;

            for (int idx = 0; idx < documentIds.size(); idx++) {
                String docId = documentIds.get(idx);
                System.out.println("\n  > [" + (idx + 1) + "/" + documentIds.size() + "] 문서 ID: " + docId + " 처리 중...");

                try {
                    // Iframe 전환 확인
                    driver.switchTo().defaultContent();
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    Thread.sleep(500);

                    // 상세 페이지 로드
                    String clickFunctionCall = String.format("jnlInfoList.showGo(null, %s, 1, true, '');", docId);
                    js.executeScript(clickFunctionCall);

                    // 문서 ID가 로드될 때까지 대기
                    final String expectedDocId = docId;
                    boolean idMatched = wait.until(driver1 -> {
                        try {
                            WebElement seqInput = driver1.findElement(
                                    By.cssSelector("form#jnlInfoShowParamAttr input[name='work_jrnl_seq']"));
                            String loadedId = seqInput.getAttribute("value");
                            return expectedDocId.equals(loadedId);
                        } catch (Exception e) {
                            return false;
                        }
                    });

                    if (!idMatched) {
                        throw new Exception("문서 ID 로드 실패");
                    }

                    // 콘텐츠 길이 안정화 대기
                    String docBodyHtml = waitForContentStable(driver);

                    if (docBodyHtml == null || docBodyHtml.isEmpty()) {
                        throw new Exception("콘텐츠 로딩 타임아웃 또는 빈 콘텐츠");
                    }

                    // 이미지 경로 변환 및 정리
                    String processedHtml = processDocBody(docBodyHtml, docId, END_YEAR);

                    // DB 업데이트
                    updatePstmt.setString(1, processedHtml);
                    updatePstmt.setString(2, docId);
                    updatePstmt.setInt(3, END_YEAR);
                    updatePstmt.executeUpdate();
                    conn.commit();

                    totalProcessed++;
                    recrawledIds.add(docId);
                    appendProcessedId(docId);
                    appendRecrawledLog(docId);
                    System.out.println("    > doc_body 업데이트 완료 (길이: " + processedHtml.length() + ")");

                    // 목록으로 복귀
                    driver.navigate().back();
                    Thread.sleep(1500);

                } catch (Exception e) {
                    errorCount++;
                    failedIds.add(docId);
                    String errorMsg = e.getMessage();
                    System.err.println("    > 오류 발생: " + errorMsg);
                    appendErrorLog(docId, errorMsg);

                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        // 롤백 실패 무시
                    }

                    // 복구 시도
                    try {
                        driver.navigate().back();
                        Thread.sleep(2000);
                        driver.switchTo().defaultContent();
                        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                        Thread.sleep(1000);
                    } catch (Exception recoveryEx) {
                        // 복구 실패 무시
                    }
                }

                // 50건마다 진행 상황 출력
                if ((idx + 1) % 50 == 0) {
                    System.out.println("\n=== 진행 상황: " + (idx + 1) + "/" + documentIds.size() +
                            " (성공: " + totalProcessed + ", 실패: " + errorCount + ") ===\n");
                }
            }

            // 6. 결과 출력
            System.out.println("\n========================================");
            System.out.println("5단계: 크롤링 완료");
            System.out.println("========================================");
            System.out.println("  > 총 처리 대상: " + documentIds.size() + "건");
            System.out.println("  > 재수집 성공: " + totalProcessed + "건");
            System.out.println("  > 실패: " + errorCount + "건");

            if (!failedIds.isEmpty()) {
                System.out.println("\n  > 실패한 문서 ID 목록:");
                System.out.println("    " + String.join(", ", failedIds));
            }

        } catch (Exception e) {
            System.err.println("크롤링 중 치명적 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("DB 연결 해제 실패: " + e.getMessage());
            }
            if (driver != null) {
                driver.quit();
                System.out.println("WebDriver 종료");
            }
        }
    }

    /**
     * 콘텐츠 길이가 안정화될 때까지 대기
     * - 500ms마다 체크
     * - 3번 연속 같으면 완료
     * - 최대 30초 대기
     */
    private static String waitForContentStable(WebDriver driver) {
        long startTime = System.currentTimeMillis();
        int prevLength = -1;
        int stableCount = 0;
        String html = "";

        while (System.currentTimeMillis() - startTime < MAX_WAIT_TIME_MS) {
            try {
                WebElement bodyDiv = driver.findElement(By.cssSelector("div#jrnl_cont"));
                html = bodyDiv.getAttribute("innerHTML");
                int currLength = html.length();

                if (currLength == prevLength && currLength > 0) {
                    stableCount++;
                    if (stableCount >= STABLE_COUNT_REQUIRED) {
                        return html;  // 안정화 완료
                    }
                } else {
                    stableCount = 0;  // 리셋
                }

                prevLength = currLength;
                Thread.sleep(STABLE_CHECK_INTERVAL_MS);

            } catch (Exception e) {
                // 요소를 못 찾으면 잠시 대기 후 재시도
                try {
                    Thread.sleep(STABLE_CHECK_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }

        // 타임아웃 - 마지막으로 가져온 값 반환 (빈 값일 수 있음)
        return html;
    }

    /**
     * 처리 완료된 ID 목록 로드
     */
    private static Set<String> loadProcessedIds() {
        Set<String> ids = new HashSet<>();
        File file = new File(PROCESSED_IDS_FILE);

        if (!file.exists()) {
            return ids;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    ids.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("processed_ids.txt 읽기 실패: " + e.getMessage());
        }

        return ids;
    }

    /**
     * 처리 완료된 ID 추가 (실시간)
     */
    private static void appendProcessedId(String docId) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(PROCESSED_IDS_FILE, true), StandardCharsets.UTF_8))) {
            writer.write(docId);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("processed_ids.txt 쓰기 실패: " + e.getMessage());
        }
    }

    /**
     * 재수집 완료 로그 기록 (실시간)
     */
    private static void appendRecrawledLog(String docId) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(RECRAWLED_LOG_FILE, true), StandardCharsets.UTF_8))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write("[" + timestamp + "] " + docId);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("재수집 로그 쓰기 실패: " + e.getMessage());
        }
    }

    /**
     * 에러 로그 기록 (실시간)
     */
    private static void appendErrorLog(String docId, String errorMessage) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(ERROR_LOG_FILE, true), StandardCharsets.UTF_8))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write("[" + timestamp + "] docId=" + docId + ", error=" + errorMessage);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("에러 로그 쓰기 실패: " + e.getMessage());
        }
    }

    /**
     * doc_body 처리: 이미지 경로 변환 및 공백 정리
     */
    private static String processDocBody(String html, String docId, int endYear) {
        // 이미지 경로 변환
        Pattern imgPattern = Pattern.compile("<img([^>]*)src=\"[^\"]+\"([^>]*)>");
        Matcher matcher = imgPattern.matcher(html);

        StringBuffer sb = new StringBuffer();
        int imgIndex = 0;
        while (matcher.find()) {
            String before = matcher.group(1);
            String after = matcher.group(2);
            String newPath = DB_PATH_PREFIX + "/jnl_img_" + endYear + "/jnl" + docId + "/" + imgIndex + ".jpg";
            String replacement = "<img" + before + "src=\"" + newPath + "\"" + after + ">";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            imgIndex++;
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // 공백 정리
        html = html.replaceAll("[\\r\\n]+", " ").replaceAll("\\s+", " ").trim();

        return html;
    }
}
