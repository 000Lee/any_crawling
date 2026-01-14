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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AnyFiveBoardPlusCrawler_img {

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

    // 에러 로그 파일
    private static String ERROR_LOG_FILE;

    // DB 조회 SQL - 프리픽스 없는 이미지가 있는 것만
    private static final String SELECT_UNPROCESSED_SQL =
            "SELECT sourceId, content FROM board_posts_plus " +
                    "WHERE type = ? " +
                    "AND content LIKE '%<img%' " +
                    "AND content NOT LIKE '%/PMS_SITE-%'";

    // content 업데이트 SQL
    private static final String UPDATE_CONTENT_SQL =
            "UPDATE board_posts_plus SET content = ? WHERE sourceId = ?";

    public static void main(String[] args) {
        // 다운로드 경로 설정
        DOWNLOAD_BASE_PATH = DOWNLOAD_BASE_DIR + "/board_" + BOARD_TYPE + "_plus_img";
        ERROR_LOG_FILE = DOWNLOAD_BASE_PATH + "/download_errors.log";

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        // options.addArguments("--headless");

        WebDriver driver = null;
        Connection conn = null;

        try {
            // 0. 기본 폴더 생성
            File baseDir = new File(DOWNLOAD_BASE_PATH);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
                System.out.println("다운로드 기본 폴더 생성: " + DOWNLOAD_BASE_PATH);
            }

            // 1. DB에서 처리 대상 문서 조회
            System.out.println("0단계: DB에서 처리 대상 문서 조회 (type=" + BOARD_TYPE + ")");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            List<DocumentInfo> documentList = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_UNPROCESSED_SQL)) {
                pstmt.setString(1, BOARD_TYPE);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        DocumentInfo doc = new DocumentInfo();
                        doc.sourceId = rs.getString("sourceId");
                        doc.content = rs.getString("content");
                        documentList.add(doc);
                    }
                }
            }

            if (documentList.isEmpty()) {
                System.out.println("  > 모든 문서의 이미지가 이미 처리되었습니다. 크롤링을 종료합니다.");
                return;
            }
            System.out.println("  > 처리할 문서 수: " + documentList.size());

            conn.close();
            conn = null;

            // 2. WebDriver 설정
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 3. 로그인 및 메뉴 진입
            System.out.println("1단계: 로그인 및 메뉴 진입");
            driver.get(TARGET_URL);

            driver.findElement(By.id("uid")).sendKeys(USER_ID);
            driver.findElement(By.id("pwd")).sendKeys(USER_PW);
            driver.findElement(By.className("btn_login")).click();
            System.out.println("  > 로그인 버튼 클릭 완료");

            // '다음에 변경하기' 팝업 닫기
            String xpathPassButton = "//button[@class='home' and @onclick='pwChangePass.pass()']";
            WebElement passButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathPassButton)));
            passButton.click();
            System.out.println("  > '다음에 변경하기' 팝업 닫기 완료");

            // Iframe 전환
            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
            System.out.println("  > Iframe 전환 성공");

            // '게시판' 메뉴 클릭
            By brdLinkSelector = By.xpath("//a[contains(@href, '/brd/') and contains(@class, 'left_menu')]");
            WebElement brdLink = wait.until(ExpectedConditions.elementToBeClickable(brdLinkSelector));
            js.executeScript("arguments[0].click();", brdLink);
            Thread.sleep(1500);
            System.out.println("  > '게시판' 메뉴 클릭 완료");

            // '(주)애니파이브' 드롭다운 펼치기
            By corpSelector = By.xpath("//span[contains(@class,'k-link') and contains(.,'(주)애니파이브')]");
            WebElement corpItem = wait.until(ExpectedConditions.elementToBeClickable(corpSelector));
            js.executeScript("arguments[0].click();", corpItem);
            Thread.sleep(1000);
            System.out.println("  > '(주)애니파이브' 드롭다운 펼치기 완료");

            // 특정 게시판 클릭 (BOARD_NAME)
            By boardSelector = By.xpath("//span[contains(@class,'k-link') and contains(text(),'" + BOARD_NAME + "')]");
            WebElement boardItem = wait.until(ExpectedConditions.elementToBeClickable(boardSelector));
            js.executeScript("arguments[0].click();", boardItem);
            Thread.sleep(1500);
            System.out.println("  > '" + BOARD_NAME + "' 게시판 클릭 완료");

            // 4. 쿠키 획득 (파일 다운로드용)
            Set<Cookie> seleniumCookies = driver.manage().getCookies();
            String cookieString = seleniumCookies.stream()
                    .map(c -> c.getName() + "=" + c.getValue())
                    .collect(Collectors.joining("; "));
            System.out.println("  > 쿠키 획득 완료");

            // 5. DB 재연결 및 크롤링 시작
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("2단계: 이미지 크롤링 시작");

            PreparedStatement updatePstmt = conn.prepareStatement(UPDATE_CONTENT_SQL);
            int totalProcessed = 0;
            int totalDownloaded = 0;
            int noImageCount = 0;
            int errorCount = 0;

            for (int idx = 0; idx < documentList.size(); idx++) {
                DocumentInfo docInfo = documentList.get(idx);
                String sourceId = docInfo.sourceId;
                String originalContent = docInfo.content;

                // sourceId에서 앞의 _ 제거 (예: _4111840 → 4111840)
                String docIdNum = sourceId.startsWith("_") ? sourceId.substring(1) : sourceId;

                System.out.println("\n  > [" + (idx + 1) + "/" + documentList.size() + "] sourceId: " + sourceId + " 처리 중...");

                try {
                    // Iframe 전환 확인
                    driver.switchTo().defaultContent();
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    Thread.sleep(1000);

                    // 상세 페이지 로드
                    String clickFunctionCall = String.format("brdAtclList.showGo(this, %s, true);", docIdNum);
                    js.executeScript(clickFunctionCall);

                    // 페이지 로드 대기
                    Thread.sleep(2000);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#atcl_cont")));
                    Thread.sleep(500);

                    // 이미지 태그 추출 (본문 내 모든 이미지)
                    List<WebElement> imgElements = driver.findElements(By.cssSelector("div#atcl_cont img"));
                    System.out.println("    > 이미지 수: " + imgElements.size());

                    if (imgElements.isEmpty()) {
                        // 이미지 없음
                        noImageCount++;
                        totalProcessed++;
                        System.out.println("    > 이미지 없음, 스킵");
                    } else {
                        // 폴더 생성
                        String docFolderPath = DOWNLOAD_BASE_PATH + "/brd" + docIdNum;
                        File docFolder = new File(docFolderPath);
                        if (!docFolder.exists()) {
                            docFolder.mkdirs();
                        }

                        // 이미지 다운로드 및 content 수정
                        String updatedContent = originalContent;
                        int downloadedCount = 0;
                        boolean hasError = false;

                        for (int i = 0; i < imgElements.size(); i++) {
                            WebElement img = imgElements.get(i);
                            String src = img.getAttribute("src");

                            if (src == null || src.isEmpty()) {
                                continue;
                            }

                            // 이미 프리픽스가 있으면 스킵 (혹시 모를 경우 대비)
                            if (src.contains("/PMS_SITE-")) {
                                continue;
                            }

                            String savePath = docFolderPath + "/" + i + ".jpg";
                            String dbPath = DB_PATH_PREFIX + "/board_" + BOARD_TYPE + "_plus_img/brd" + docIdNum + "/" + i + ".jpg";

                            // 다운로드 URL 생성
                            String imgUrl;
                            if (src.startsWith("/")) {
                                imgUrl = TARGET_URL + src;
                            } else if (src.startsWith("http")) {
                                imgUrl = src;
                            } else {
                                imgUrl = TARGET_URL + "/" + src;
                            }

                            // 이미지 다운로드
                            boolean downloaded = downloadFile(imgUrl, savePath, cookieString);

                            if (downloaded) {
                                downloadedCount++;
                                totalDownloaded++;

                                // content에서 src 치환 (절대경로 → 상대경로로 변환 후 치환)
                                String relativeSrc = src.replace(TARGET_URL, "");
                                updatedContent = updatedContent.replace(relativeSrc, dbPath);
                                System.out.println("    > 다운로드 완료: " + i + ".jpg");
                            } else {
                                hasError = true;
                                logError(sourceId, i, src.substring(0, Math.min(100, src.length())), "다운로드 실패");
                            }
                        }

                        System.out.println("    > 다운로드 완료: " + downloadedCount + "/" + imgElements.size());

                        if (!hasError) {
                            // DB UPDATE
                            updatePstmt.setString(1, updatedContent);
                            updatePstmt.setString(2, sourceId);
                            updatePstmt.executeUpdate();
                            conn.commit();
                            totalProcessed++;
                            System.out.println("    > DB 업데이트 완료");
                        } else {
                            errorCount++;
                            conn.rollback();
                            // 부분 실패 시 폴더 삭제 (재처리 시 깔끔하게)
                            deleteFolder(docFolder);
                            System.out.println("    > 부분 실패로 폴더 삭제, 재처리 대상");
                        }
                    }

                    // 목록으로 복귀
                    driver.navigate().back();
                    Thread.sleep(1500);

                } catch (Exception e) {
                    errorCount++;
                    System.err.println("    > 오류 발생: " + e.getMessage());
                    logError(sourceId, -1, "", "페이지 처리 오류: " + e.getMessage());

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
                        System.err.println("    > 복구 실패: " + recoveryEx.getMessage());
                    }
                }

                // 10건마다 진행 상황 출력
                if ((idx + 1) % 10 == 0) {
                    System.out.println("\n=== 진행 상황: " + (idx + 1) + "/" + documentList.size() +
                            " (성공: " + totalProcessed + ", 이미지없음: " + noImageCount +
                            ", 실패: " + errorCount + ", 다운로드: " + totalDownloaded + ") ===\n");
                }
            }

            System.out.println("\n3단계: 크롤링 완료");
            System.out.println("  > 총 처리 대상: " + documentList.size());
            System.out.println("  > 처리 성공: " + totalProcessed);
            System.out.println("  > 이미지 없는 문서: " + noImageCount);
            System.out.println("  > 이미지 다운로드 수: " + totalDownloaded);
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
                System.out.println("WebDriver 종료");
            }
        }
    }

    /**
     * 에러 로그 기록
     */
    private static void logError(String docId, int imageIndex, String url, String errorMessage) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(ERROR_LOG_FILE, true), StandardCharsets.UTF_8))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(String.format("[%s] docId=%s, index=%d, url=%s, error=%s",
                    timestamp, docId, imageIndex, url, errorMessage));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("에러 로그 쓰기 실패: " + e.getMessage());
        }
    }

    /**
     * 폴더 삭제 (부분 실패 시)
     */
    private static void deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            folder.delete();
        }
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
                System.err.println("    > HTTP 오류: " + responseCode + " - " + fileUrl);
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
     * 문서 정보 클래스
     */
    static class DocumentInfo {
        String sourceId = "";
        String content = "";
    }
}
