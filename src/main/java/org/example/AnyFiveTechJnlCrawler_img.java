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

public class AnyFiveTechJnlCrawler_img {

    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe"; // 여기를 수정하세요

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
    private static final int END_YEAR = 2025; // 여기를 수정하세요

    // 파일 저장 기본 경로 (연도는 자동으로 붙음)
    private static final String DOWNLOAD_BASE_DIR = "C:/Users/LEEJUHWAN/Downloads";
    // ------------------------------------ 여기까지 수정하세요

    // 연도가 포함된 실제 다운로드 경로 (런타임에 설정)
    private static String DOWNLOAD_BASE_PATH;

    // 처리 완료 목록 파일 (런타임에 설정)
    private static String PROCESSED_IDS_FILE;

    // 에러 로그 파일 (런타임에 설정)
    private static String ERROR_LOG_FILE;

    // 해당 연도의 source_id 조회 SQL
    private static final String SELECT_SOURCE_IDS_BY_YEAR_SQL =
            "SELECT source_id FROM tech_jnl_documents WHERE end_year = ? ORDER BY source_id";

    public static void main(String[] args) {
        // 연도가 포함된 경로들 설정
        DOWNLOAD_BASE_PATH = DOWNLOAD_BASE_DIR + "/tech_jnl_img_" + END_YEAR;
        PROCESSED_IDS_FILE = DOWNLOAD_BASE_PATH + "/processed_ids.txt";
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

            // 1. 처리 완료된 ID 로드
            Set<String> processedIds = loadProcessedIds();
            System.out.println("1단계: 처리 완료된 문서 수: " + processedIds.size());

            // 2. DB에서 해당 연도의 source_id 조회
            System.out.println("2단계: DB에서 end_year=" + END_YEAR + "인 source_id 조회");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            List<String> allDocumentIds = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_SOURCE_IDS_BY_YEAR_SQL)) {
                pstmt.setInt(1, END_YEAR);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        allDocumentIds.add(rs.getString("source_id"));
                    }
                }
            }
            System.out.println("  > 해당 연도 문서 수: " + allDocumentIds.size());

            // 처리 대상 필터링
            List<String> targetIds = allDocumentIds.stream()
                    .filter(id -> !processedIds.contains(id))
                    .collect(Collectors.toList());

            if (targetIds.isEmpty()) {
                System.out.println("  > 모든 문서가 이미 처리되었습니다. 크롤링을 종료합니다.");
                return;
            }
            System.out.println("  > 처리 대상 문서 수: " + targetIds.size());

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

            // '다음에 변경하기' 팝업 닫기
            String xpathPassButton = "//button[@class='home' and @onclick='pwChangePass.pass()']";
            WebElement passButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathPassButton)));
            passButton.click();
            System.out.println("  > '다음에 변경하기' 팝업 닫기 완료");

            // Iframe 전환
            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
            System.out.println("  > Iframe 전환 성공");

            // '업무관리' 아이콘 클릭
            By jnlLinkSelector = By.xpath("//a[contains(@href, '/jnl/') and contains(@class, 'left_menu')]");
            WebElement jnlLink = wait.until(ExpectedConditions.elementToBeClickable(jnlLinkSelector));
            js.executeScript("arguments[0].click();", jnlLink);
            Thread.sleep(1500);
            System.out.println("  > '업무관리' 아이콘 클릭 완료");

            // '회사 업무함' 클릭
            By corpBoxSelector = By.xpath("//span[contains(@class, 'corp-span')]");
            WebElement corpBox = wait.until(ExpectedConditions.elementToBeClickable(corpBoxSelector));
            js.executeScript("arguments[0].click();", corpBox);
            Thread.sleep(1000);
            System.out.println("  > '회사 업무함' 클릭 완료");

            // '기술지원팀' 클릭
            By lblSelector = By.xpath("//li[@data-lblnm='기술지원팀']");
            WebElement lblItem = wait.until(ExpectedConditions.elementToBeClickable(lblSelector));
            js.executeScript("arguments[0].click();", lblItem);
            Thread.sleep(1500);
            System.out.println("  > '기술지원팀' 클릭 완료");

            // 5. 쿠키 획득 (파일 다운로드용)
            Set<Cookie> seleniumCookies = driver.manage().getCookies();
            String cookieString = seleniumCookies.stream()
                    .map(c -> c.getName() + "=" + c.getValue())
                    .collect(Collectors.joining("; "));
            System.out.println("  > 쿠키 획득 완료");

            // 6. 크롤링 시작
            System.out.println("4단계: 이미지 크롤링 시작");

            int totalProcessed = 0;
            int totalDownloaded = 0;
            int noImageCount = 0;
            int errorCount = 0;

            for (int idx = 0; idx < targetIds.size(); idx++) {
                String docId = targetIds.get(idx);
                System.out.println("\n  > [" + (idx + 1) + "/" + targetIds.size() + "] 문서 ID: " + docId + " 처리 중...");

                boolean success = false;

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

                    // 이미지 태그 추출 (본문 내 모든 이미지)
                    List<WebElement> imgElements = driver.findElements(
                            By.cssSelector("div#jrnl_cont img"));
                    System.out.println("    > 이미지 수: " + imgElements.size());

                    if (imgElements.isEmpty()) {
                        // 이미지 없음 - 폴더 생성 안 함, processed_ids에만 추가
                        noImageCount++;
                        success = true;
                        System.out.println("    > 이미지 없음, 스킵");
                    } else {
                        // 폴더 생성
                        String docFolderPath = DOWNLOAD_BASE_PATH + "/jnl" + docId;
                        File docFolder = new File(docFolderPath);
                        if (!docFolder.exists()) {
                            docFolder.mkdirs();
                        }

                        // 이미지 다운로드
                        int downloadedCount = 0;
                        boolean hasError = false;

                        for (int i = 0; i < imgElements.size(); i++) {
                            WebElement img = imgElements.get(i);
                            String src = img.getAttribute("src");

                            if (src == null || src.isEmpty()) {
                                continue;
                            }

                            String savePath = docFolderPath + "/" + i + ".jpg";
                            boolean downloaded;

                            if (src.startsWith("data:image")) {
                                // base64 이미지
                                downloaded = saveBase64Image(src, savePath);
                            } else {
                                // URL 이미지
                                String imgUrl;
                                if (src.startsWith("/")) {
                                    imgUrl = TARGET_URL + src;
                                } else if (src.startsWith("http")) {
                                    imgUrl = src;
                                } else {
                                    imgUrl = TARGET_URL + "/" + src;
                                }
                                downloaded = downloadFile(imgUrl, savePath, cookieString);
                            }

                            if (downloaded) {
                                downloadedCount++;
                                totalDownloaded++;
                            } else {
                                hasError = true;
                                logError(docId, i, src.substring(0, Math.min(100, src.length())), "다운로드 실패");
                            }
                        }

                        System.out.println("    > 다운로드 완료: " + downloadedCount + "/" + imgElements.size());

                        // 모든 이미지가 성공해야 처리 완료로 표시
                        if (!hasError) {
                            success = true;
                        } else {
                            errorCount++;
                            // 부분 실패 시 폴더 삭제 (재처리 시 깔끔하게)
                            deleteFolder(docFolder);
                            System.out.println("    > 부분 실패로 폴더 삭제, 재처리 대상");
                        }
                    }

                    // 처리 완료 시 processed_ids에 추가
                    if (success) {
                        appendProcessedId(docId);
                        totalProcessed++;
                    }

                    // 목록으로 복귀
                    driver.navigate().back();
                    Thread.sleep(1500);

                } catch (Exception e) {
                    errorCount++;
                    System.err.println("    > 오류 발생: " + e.getMessage());
                    logError(docId, -1, "", "페이지 처리 오류: " + e.getMessage());

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
                    System.out.println("\n=== 진행 상황: " + (idx + 1) + "/" + targetIds.size() +
                            " (성공: " + totalProcessed + ", 이미지없음: " + noImageCount +
                            ", 실패: " + errorCount + ", 다운로드: " + totalDownloaded + ") ===\n");
                }
            }

            System.out.println("\n5단계: 크롤링 완료");
            System.out.println("  > 총 처리 대상: " + targetIds.size());
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
     * 처리 완료된 ID 추가
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
     * Base64 이미지 저장
     */
    private static boolean saveBase64Image(String dataUri, String savePath) {
        try {
            String base64Data = dataUri.substring(dataUri.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            try (FileOutputStream fos = new FileOutputStream(savePath)) {
                fos.write(imageBytes);
            }
            return true;
        } catch (Exception e) {
            System.err.println("    > Base64 저장 오류: " + e.getMessage());
            return false;
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
}