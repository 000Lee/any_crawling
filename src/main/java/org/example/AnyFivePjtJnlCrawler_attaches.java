package org.example;

import org.openqa.selenium.*;
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

public class AnyFivePjtJnlCrawler_attaches {

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

    // 사용자 입력값
    private static final int END_YEAR = 2025;

    // 파일 저장 기본 경로 (연도는 자동으로 붙음)
    private static final String DOWNLOAD_BASE_DIR = "C:/Users/LEEJUHWAN/Downloads";

    // DB path 프리픽스
    private static final String DB_PATH_PREFIX = "";
    //    /PMS_SITE-U7OI43JLDSMO/jnl

    // ------------------------------------ 여기까지 수정하세요

    // 연도가 포함된 실제 다운로드 경로 (런타임에 설정) - pjt_ 접두어 추가
    private static String DOWNLOAD_BASE_PATH;

    // attaches가 비어있는 문서 ID 조회 SQL - 테이블명 변경
    private static final String SELECT_EMPTY_ATTACHES_SQL =
            "SELECT source_id FROM pjt_jnl_documents " +
                    "WHERE end_year = ? AND (attaches IS NULL OR attaches = '')";

    // attaches 업데이트 SQL - 테이블명 변경
    private static final String UPDATE_ATTACHES_SQL =
            "UPDATE pjt_jnl_documents SET attaches = ? WHERE source_id = ?";

    public static void main(String[] args) {
        // 연도가 포함된 다운로드 경로 설정 - pjt_ 접두어 추가
        DOWNLOAD_BASE_PATH = DOWNLOAD_BASE_DIR + "/pjt_jnl_attachments_" + END_YEAR;

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        WebDriver driver = null;
        Connection conn = null;

        try {
            // 0. DB에서 attaches가 비어있는 문서 ID 조회 (해당 연도만)
            System.out.println("0단계: attaches가 비어있는 문서 ID 조회 시작. (end_year=" + END_YEAR + ")");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            List<String> documentIds = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_EMPTY_ATTACHES_SQL)) {
                pstmt.setInt(1, END_YEAR);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        documentIds.add(rs.getString("source_id"));
                    }
                }
            }

            if (documentIds.isEmpty()) {
                System.out.println("  > 모든 문서의 attaches가 이미 처리되었습니다. 크롤링을 종료합니다.");
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
            System.out.println("2단계: 로그인 및 메뉴 진입.");
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

            // '업무관리' 아이콘 클릭
            By jnlLinkSelector = By.xpath("//a[contains(@href, '/jnl/') and contains(@class, 'left_menu')]");
            WebElement jnlLink = wait.until(ExpectedConditions.elementToBeClickable(jnlLinkSelector));
            js.executeScript("arguments[0].click();", jnlLink);
            Thread.sleep(1500);
            System.out.println("  > '업무관리' 아이콘 클릭 완료.");

            // '회사 업무함' 클릭
            By corpBoxSelector = By.xpath("//span[contains(@class, 'corp-span')]");
            WebElement corpBox = wait.until(ExpectedConditions.elementToBeClickable(corpBoxSelector));
            js.executeScript("arguments[0].click();", corpBox);
            Thread.sleep(1000);
            System.out.println("  > '회사 업무함' 클릭 완료.");

            // ★★★ 변경: PJT진척보고 선택 ★★★
            By lblSelector = By.xpath("//li[@data-lblnm='PJT진척보고']");
            WebElement lblItem = wait.until(ExpectedConditions.elementToBeClickable(lblSelector));
            js.executeScript("arguments[0].click();", lblItem);
            Thread.sleep(1500);
            System.out.println("  > 'PJT진척보고' 클릭 완료.");

            // 4. 쿠키 획득 (파일 다운로드용)
            Set<Cookie> seleniumCookies = driver.manage().getCookies();
            String cookieString = seleniumCookies.stream()
                    .map(c -> c.getName() + "=" + c.getValue())
                    .collect(Collectors.joining("; "));
            System.out.println("  > 쿠키 획득 완료.");

            // 5. DB 재연결 및 크롤링 시작
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("3단계: 첨부파일 크롤링 시작.");

            PreparedStatement updatePstmt = conn.prepareStatement(UPDATE_ATTACHES_SQL);
            int totalUpdated = 0;
            int totalDownloaded = 0;
            int errorCount = 0;

            for (int idx = 0; idx < documentIds.size(); idx++) {
                String docId = documentIds.get(idx);
                System.out.println("\n  > [" + (idx + 1) + "/" + documentIds.size() + "] 문서 ID: " + docId + " 처리 중...");

                try {
                    // Iframe 전환 확인
                    driver.switchTo().defaultContent();
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    Thread.sleep(1000);

                    // 상세 페이지 로드
                    String clickFunctionCall = String.format("jnlInfoList.showGo(null, %s, 1, true, '');", docId);
                    js.executeScript(clickFunctionCall);

                    // 페이지 로드 대기 - 요청한 문서 ID가 로드될 때까지 대기
                    final String expectedDocId = docId;
                    wait.until(driver1 -> {
                        try {
                            WebElement seqInput = driver1.findElement(
                                    By.cssSelector("form#jnlInfoShowParamAttr input[name='work_jrnl_seq']"));
                            String loadedId = seqInput.getAttribute("value");
                            return expectedDocId.equals(loadedId);
                        } catch (Exception e) {
                            return false;
                        }
                    });
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
                        String docFolderPath = DOWNLOAD_BASE_PATH + "/jnl" + docId;
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
                            // 연도가 포함된 DB 경로 - pjt_ 접두어 추가
                            String dbPath = DB_PATH_PREFIX + "/pjt_jnl_attachments_" + END_YEAR + "/jnl" + docId + "/" + att.fileName;

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
                    updatePstmt.setString(2, docId);
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

            System.out.println("\n4단계: 크롤링 완료.");
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
            List<WebElement> fileItems = driver.findElements(By.cssSelector("#jnlInfoShowFileMap li"));

            for (WebElement li : fileItems) {
                try {
                    AttachmentInfo info = new AttachmentInfo();

                    // onclick에서 파일 ID 추출
                    WebElement clickSpan = li.findElement(By.cssSelector("span[onclick*='fileDown']"));
                    String onclick = clickSpan.getAttribute("onclick");
                    Pattern pattern = Pattern.compile("fileDown\\((\\d+)\\)");
                    Matcher matcher = pattern.matcher(onclick);

                    if (matcher.find()) {
                        String fileSeq = matcher.group(1);
                        info.downloadUrl = TARGET_URL + "/cmn/atchfile/download?atch_file_seq=" + fileSeq;
                    }

                    // 파일명 추출 (첫 번째 verti_Top만)
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
