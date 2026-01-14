package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnyFiveBoardPlusCrawler_attaches {

    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

    private static final String USER_ID = "master";
    private static final String USER_PW = "five4190any#";
    private static final String TARGET_URL = "http://office.anyfive.com";
    private static final String IFRAME_NAME = "content_frame";

    // ------------------------------------ 여기부터 수정하세요
    // MariaDB 연결 정보
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/crawling";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    // 게시판 설정
    private static final String BOARD_NAME = "전사게시판";  // 클릭할 게시판명
    private static final String BOARD_TYPE = "biz";           // DB type 컬럼 값, 폴더명에도 사용

    // 파일 저장 기본 경로
    private static final String DOWNLOAD_BASE_DIR = "C:/Users/LEEJUHWAN/Downloads";

    // DB path 프리픽스
    private static final String DB_PATH_PREFIX = "/PMS_SITE-U7OI43JLDSMO/board/attach";
    // ------------------------------------ 여기까지 수정하세요

    // 런타임에 설정되는 경로
    private static String DOWNLOAD_BASE_PATH;

    // DB 조회 SQL - 아직 처리되지 않은 것만
    private static final String SELECT_UNPROCESSED_SQL =
            "SELECT sourceId FROM board_posts_plus " +
                    "WHERE type = ? " +
                    "AND attaches_json IS NOT NULL " +
                    "AND attaches_json != '' " +
                    "AND attaches_json != '[]' " +
                    "AND attaches_json NOT LIKE '%/PMS_SITE-%'";

    // attaches_json 업데이트 SQL
    private static final String UPDATE_ATTACHES_SQL =
            "UPDATE board_posts_plus SET attaches_json = ? WHERE sourceId = ?";

    public static void main(String[] args) {
        // 다운로드 경로 설정
        DOWNLOAD_BASE_PATH = DOWNLOAD_BASE_DIR + "/board_" + BOARD_TYPE + "_plus_attachments";

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        WebDriver driver = null;
        Connection conn = null;

        try {
            // 0. DB에서 처리 대상 문서 ID 조회
            System.out.println("0단계: 처리 대상 문서 ID 조회 시작. (type=" + BOARD_TYPE + ")");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            List<String> documentIds = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_UNPROCESSED_SQL)) {
                pstmt.setString(1, BOARD_TYPE);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        documentIds.add(rs.getString("sourceId"));
                    }
                }
            }

            if (documentIds.isEmpty()) {
                System.out.println("  > 모든 문서의 첨부파일이 이미 처리되었습니다. 크롤링을 종료합니다.");
                return;
            }
            System.out.println("  > 처리할 문서 수: " + documentIds.size());

            conn.close();
            conn = null;

            // 1. 다운로드 기본 폴더 생성
            File baseDir = new File(DOWNLOAD_BASE_PATH);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
                System.out.println("  > 다운로드 기본 폴더 생성: " + DOWNLOAD_BASE_PATH);
            }

            // 2. WebDriver 설정
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 3. 로그인 및 메뉴 진입
            System.out.println("1단계: 로그인 및 메뉴 진입.");
            driver.get(TARGET_URL);

            driver.findElement(By.id("uid")).sendKeys(USER_ID);
            driver.findElement(By.id("pwd")).sendKeys(USER_PW);
            driver.findElement(By.className("btn_login")).click();
            System.out.println("  > 로그인 버튼 클릭 완료.");

            // '다음에 변경하기' 팝업 닫기
            String xpathPassButton = "//button[@class='home' and @onclick='pwChangePass.pass()']";
            WebElement passButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathPassButton)));
            passButton.click();
            System.out.println("  > '다음에 변경하기' 팝업 닫기 완료.");

            // Iframe 전환
            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
            System.out.println("  > Iframe 전환 성공.");

            // '게시판' 메뉴 클릭
            By brdLinkSelector = By.xpath("//a[contains(@href, '/brd/') and contains(@class, 'left_menu')]");
            WebElement brdLink = wait.until(ExpectedConditions.elementToBeClickable(brdLinkSelector));
            js.executeScript("arguments[0].click();", brdLink);
            Thread.sleep(1500);
            System.out.println("  > '게시판' 메뉴 클릭 완료.");

            // '(주)애니파이브' 드롭다운 펼치기
            By corpSelector = By.xpath("//span[contains(@class,'k-link') and contains(.,'(주)애니파이브')]");
            WebElement corpItem = wait.until(ExpectedConditions.elementToBeClickable(corpSelector));
            js.executeScript("arguments[0].click();", corpItem);
            Thread.sleep(1000);
            System.out.println("  > '(주)애니파이브' 드롭다운 펼치기 완료.");

            // 특정 게시판 클릭 (BOARD_NAME)
            By boardSelector = By.xpath("//span[contains(@class,'k-link') and contains(text(),'" + BOARD_NAME + "')]");
            WebElement boardItem = wait.until(ExpectedConditions.elementToBeClickable(boardSelector));
            js.executeScript("arguments[0].click();", boardItem);
            Thread.sleep(1500);
            System.out.println("  > '" + BOARD_NAME + "' 게시판 클릭 완료.");

            // 4. 쿠키 획득 (파일 다운로드용)
            Set<Cookie> seleniumCookies = driver.manage().getCookies();
            String cookieString = seleniumCookies.stream()
                    .map(c -> c.getName() + "=" + c.getValue())
                    .collect(Collectors.joining("; "));
            System.out.println("  > 쿠키 획득 완료.");

            // 5. DB 재연결 및 크롤링 시작
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("2단계: 첨부파일 크롤링 시작.");

            PreparedStatement updatePstmt = conn.prepareStatement(UPDATE_ATTACHES_SQL);
            int totalUpdated = 0;
            int totalDownloaded = 0;
            int errorCount = 0;

            for (int idx = 0; idx < documentIds.size(); idx++) {
                String sourceId = documentIds.get(idx);
                // sourceId에서 앞의 _ 제거 (예: _4111840 → 4111840)
                String docIdNum = sourceId.startsWith("_") ? sourceId.substring(1) : sourceId;

                System.out.println("\n  > [" + (idx + 1) + "/" + documentIds.size() + "] sourceId: " + sourceId + " 처리 중...");

                try {
                    // Iframe 전환 확인
                    driver.switchTo().defaultContent();
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    Thread.sleep(1000);

                    // 상세 페이지 로드
                    String clickFunctionCall = String.format("brdAtclList.showGo(this, %s, true);", docIdNum);
                    js.executeScript(clickFunctionCall);

                    // 페이지 로드 대기 - 첨부파일 영역이 로드될 때까지
                    Thread.sleep(2000);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul#brdAtclShowFileMap")));
                    Thread.sleep(500);

                    // 첨부파일 정보 추출
                    List<AttachmentInfo> attachments = extractAttachments(driver);
                    System.out.println("    > 첨부파일 수: " + attachments.size());

                    String attachesJson;

                    if (attachments.isEmpty()) {
                        // 첨부파일 없음
                        attachesJson = "[]";
                        System.out.println("    > 첨부파일 없음, 빈 배열로 저장.");
                    } else {
                        // 폴더 생성
                        String docFolderPath = DOWNLOAD_BASE_PATH + "/brd" + docIdNum;
                        File docFolder = new File(docFolderPath);
                        if (!docFolder.exists()) {
                            docFolder.mkdirs();
                        }

                        // 파일 다운로드 및 JSON 생성
                        StringBuilder jsonBuilder = new StringBuilder("[");
                        int downloadedCount = 0;

                        for (int i = 0; i < attachments.size(); i++) {
                            AttachmentInfo att = attachments.get(i);
                            String savePath = docFolderPath + "/" + att.fileName;
                            // DB 경로
                            String dbPath = DB_PATH_PREFIX + "/board_" + BOARD_TYPE + "_plus_attachments/brd" + docIdNum + "/" + att.fileName;

                            // 파일 다운로드
                            boolean downloaded = downloadFile(att.downloadUrl, savePath, cookieString);
                            if (downloaded) {
                                downloadedCount++;
                                totalDownloaded++;
                                System.out.println("    > 다운로드 완료: " + att.fileName);
                            } else {
                                System.err.println("    > 다운로드 실패: " + att.fileName);
                            }

                            // JSON 항목 추가 (다운로드 성공 여부와 관계없이)
                            if (i > 0) jsonBuilder.append(",");
                            jsonBuilder.append("{\"name\":\"").append(escapeJson(att.fileName))
                                    .append("\",\"path\":\"").append(escapeJson(dbPath)).append("\"}");
                        }
                        jsonBuilder.append("]");
                        attachesJson = jsonBuilder.toString();

                        System.out.println("    > 다운로드 완료: " + downloadedCount + "/" + attachments.size());
                    }

                    // DB UPDATE
                    updatePstmt.setString(1, attachesJson);
                    updatePstmt.setString(2, sourceId);
                    updatePstmt.executeUpdate();
                    conn.commit();
                    totalUpdated++;
                    System.out.println("    > DB 업데이트 완료.");

                    // 목록으로 복귀
                    driver.navigate().back();
                    Thread.sleep(1500);

                } catch (Exception e) {
                    errorCount++;
                    System.err.println("    > 오류 발생: " + e.getMessage());
                    e.printStackTrace();

                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        System.err.println("    > 롤백 실패: " + rollbackEx.getMessage());
                    }

                    // 복구 시도
                    try {
                        driver.navigate().back();
                        Thread.sleep(2000);
                        driver.switchTo().defaultContent();
                        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    } catch (Exception recoveryEx) {
                        System.err.println("    > 복구 실패, 다음 문서로 진행: " + recoveryEx.getMessage());
                    }
                }

                // 10건마다 진행 상황 출력
                if ((idx + 1) % 10 == 0) {
                    System.out.println("\n=== 진행 상황: " + (idx + 1) + "/" + documentIds.size() +
                            " (성공: " + totalUpdated + ", 실패: " + errorCount + ", 다운로드: " + totalDownloaded + ") ===\n");
                }
            }

            System.out.println("\n3단계: 크롤링 완료.");
            System.out.println("  > 총 처리 대상: " + documentIds.size());
            System.out.println("  > DB 업데이트 성공: " + totalUpdated);
            System.out.println("  > 파일 다운로드 수: " + totalDownloaded);
            System.out.println("  > 실패: " + errorCount);

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
                System.out.println("WebDriver 종료.");
            }
        }
    }

    /**
     * 첨부파일 정보 추출
     */
    private static List<AttachmentInfo> extractAttachments(WebDriver driver) {
        List<AttachmentInfo> attachments = new ArrayList<>();

        try {
            List<WebElement> fileItems = driver.findElements(By.cssSelector("#brdAtclShowFileMap li"));

            for (WebElement li : fileItems) {
                try {
                    AttachmentInfo info = new AttachmentInfo();

                    // onclick에서 파일 경로 추출
                    WebElement clickSpan = li.findElement(By.cssSelector("span[onclick*='fileDownLoad']"));
                    String onclick = clickSpan.getAttribute("onclick");
                    // brdAtclShow.fileDownLoad('brd/20251202/uuid') 형식에서 경로 추출
                    Pattern pattern = Pattern.compile("fileDownLoad\\('([^']+)'\\)");
                    Matcher matcher = pattern.matcher(onclick);

                    if (matcher.find()) {
                        String svrFilePath = matcher.group(1);
                        info.downloadUrl = TARGET_URL + "/brd/atcl/download?svr_file_path=" + svrFilePath;
                    }

                    // 파일명 추출
                    List<WebElement> fileNameSpans = li.findElements(By.cssSelector("span.verti_Top"));
                    if (!fileNameSpans.isEmpty()) {
                        info.fileName = fileNameSpans.get(0).getText().trim();
                    }

                    if (info.fileName != null && !info.fileName.isEmpty() &&
                            info.downloadUrl != null && !info.downloadUrl.isEmpty()) {
                        attachments.add(info);
                    }
                } catch (Exception e) {
                    System.err.println("    > 첨부파일 파싱 오류: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("    > 첨부파일 목록 추출 실패: " + e.getMessage());
        }

        return attachments;
    }

    /**
     * HTTP를 이용한 파일 다운로드
     */
    private static boolean downloadFile(String fileUrl, String savePath, String cookieString) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(fileUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Cookie", cookieString);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(savePath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                return true;
            } else {
                System.err.println("    > HTTP 오류: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            System.err.println("    > 다운로드 오류: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * JSON 문자열 이스케이프
     */
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 첨부파일 정보 클래스
     */
    static class AttachmentInfo {
        String fileName = "";
        String downloadUrl = "";
    }
}