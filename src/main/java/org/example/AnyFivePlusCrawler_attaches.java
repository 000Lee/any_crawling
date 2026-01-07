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
import java.nio.file.*;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnyFivePlusCrawler_attaches {

    // ======================= WebDriver 설정 =======================
    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

    // ======================= 로그인 정보 =======================
    private static final String USER_ID = "master";
    private static final String USER_PW = "five4190any#";
    private static final String TARGET_URL = "http://office.anyfive.com";
    private static final String IFRAME_NAME = "content_frame";

    // ======================= DB 연결 정보 =======================
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    // ======================= 소스 ID 로딩 방식 =======================
    // true: CSV 파일에서 읽기, false: MANUAL_SOURCE_IDS 리스트 사용
    private static final boolean USE_CSV = true;
    private static final String CSV_PATH = "C:\\Users\\LEEJUHWAN\\퇴사자명단\\DB에 수정 반영하기\\empty_path_documents_20251230_212707.csv";

    // 수동 지정 시 사용할 source_id 리스트 (USE_CSV = false일 때 사용)
    private static final List<String> MANUAL_SOURCE_IDS = Arrays.asList(
            // 여기에 source_id를 추가하세요
            // "2002144",
            // "2002200",
            // "2002203"
    );

    // ======================= 파일 저장 경로 =======================
    // 실제 파일 저장 기본 경로
    private static final String DOWNLOAD_BASE_PATH = "C:/Users/LEEJUHWAN/Downloads/approval_plus_attachments";

    // DB에 저장될 경로 프리픽스
    private static final String DB_PATH_PREFIX = "/PMS_SITE-U7OI43JLDSMO/approval/approval_plus_attachments";

    // ======================= 로그 파일 경로 =======================
    private static final String LOG_BASE_PATH = "C:/Users/LEEJUHWAN/Downloads";
    private static final String COMPLETED_LOG = LOG_BASE_PATH + "/crawler_completed.txt";
    private static final String NAME_MISMATCH_LOG = LOG_BASE_PATH + "/crawler_name_mismatch.txt";
    private static final String FAILED_LOG = LOG_BASE_PATH + "/crawler_failed.txt";
    private static final String COUNT_WARNING_LOG = LOG_BASE_PATH + "/crawler_count_warning.txt";

    // ======================= SQL =======================
    private static final String UPDATE_ATTACHES_SQL =
            "UPDATE documents SET attaches = ? WHERE source_id = ?";

    // ======================= 날짜 포맷 =======================
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
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
            // ======================= 1단계: source_id 목록 로드 =======================
            System.out.println("========================================");
            System.out.println("1단계: source_id 목록 로드");
            System.out.println("========================================");

            Map<String, List<String>> csvAttachesMap = new HashMap<>(); // source_id -> 기존 파일명 목록
            List<String> sourceIds;

            if (USE_CSV) {
                System.out.println("  > CSV 파일에서 로드: " + CSV_PATH);
                sourceIds = loadSourceIdsFromCsv(CSV_PATH, csvAttachesMap);
            } else {
                System.out.println("  > 수동 지정 리스트 사용");
                sourceIds = new ArrayList<>(MANUAL_SOURCE_IDS);
            }

            if (sourceIds.isEmpty()) {
                System.out.println("  > 처리할 source_id가 없습니다. 종료합니다.");
                return;
            }
            System.out.println("  > 총 로드된 source_id: " + sourceIds.size() + "건");

            // ======================= 2단계: 완료된 항목 제외 =======================
            System.out.println("\n========================================");
            System.out.println("2단계: 완료된 항목 확인 (재시작 지원)");
            System.out.println("========================================");

            Set<String> completedIds = loadCompletedIds();
            System.out.println("  > 이미 완료된 source_id: " + completedIds.size() + "건");

            List<String> remainingIds = sourceIds.stream()
                    .filter(id -> !completedIds.contains(id))
                    .collect(Collectors.toList());

            if (remainingIds.isEmpty()) {
                System.out.println("  > 모든 source_id가 이미 처리되었습니다. 종료합니다.");
                return;
            }
            System.out.println("  > 처리 대상 source_id: " + remainingIds.size() + "건");

            // ======================= 3단계: 기본 폴더 생성 =======================
            System.out.println("\n========================================");
            System.out.println("3단계: 다운로드 폴더 생성");
            System.out.println("========================================");

            File baseDir = new File(DOWNLOAD_BASE_PATH);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
                System.out.println("  > 다운로드 기본 폴더 생성: " + DOWNLOAD_BASE_PATH);
            } else {
                System.out.println("  > 다운로드 기본 폴더 존재: " + DOWNLOAD_BASE_PATH);
            }

            // ======================= 4단계: WebDriver 설정 및 로그인 =======================
            System.out.println("\n========================================");
            System.out.println("4단계: WebDriver 설정 및 로그인");
            System.out.println("========================================");

            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            driver.get(TARGET_URL);
            System.out.println("  > 페이지 로드 완료: " + TARGET_URL);

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

            // '전자결재' 아이콘 클릭
            By aprLinkSelector = By.xpath("//a[contains(@href, '/apr/') and contains(@class, 'left_menu')]");
            WebElement aprLink = wait.until(ExpectedConditions.elementToBeClickable(aprLinkSelector));
            js.executeScript("arguments[0].click();", aprLink);
            Thread.sleep(1000);
            System.out.println("  > '전자결재' 아이콘 클릭 완료");

            // '결재문서관리' 메뉴 클릭
            By docLiSelector = By.xpath("//li[contains(@onclick, 'managementDoc')]");
            WebElement managementDocLi = wait.until(ExpectedConditions.elementToBeClickable(docLiSelector));
            js.executeScript("arguments[0].click();", managementDocLi);
            Thread.sleep(1500);
            System.out.println("  > '결재문서관리' 메뉴 클릭 완료");

            // 쿠키 획득 (파일 다운로드용)
            Set<Cookie> seleniumCookies = driver.manage().getCookies();
            String cookieString = seleniumCookies.stream()
                    .map(c -> c.getName() + "=" + c.getValue())
                    .collect(Collectors.joining("; "));
            System.out.println("  > 쿠키 획득 완료");

            // ======================= 5단계: DB 연결 및 크롤링 시작 =======================
            System.out.println("\n========================================");
            System.out.println("5단계: 첨부파일 크롤링 시작");
            System.out.println("========================================");

            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("  > DB 연결 완료");

            PreparedStatement updatePstmt = conn.prepareStatement(UPDATE_ATTACHES_SQL);

            int totalSuccess = 0;
            int totalFailed = 0;
            int totalDownloaded = 0;
            int nameMismatchCount = 0;
            int countWarningCount = 0;

            for (int idx = 0; idx < remainingIds.size(); idx++) {
                String sourceId = remainingIds.get(idx);
                System.out.println("\n------------------------------------------");
                System.out.println("[" + (idx + 1) + "/" + remainingIds.size() + "] source_id: " + sourceId + " 처리 중...");
                System.out.println("------------------------------------------");

                boolean success = false;
                String tempFolderPath = DOWNLOAD_BASE_PATH + "/apr" + sourceId + "_temp";
                String finalFolderPath = DOWNLOAD_BASE_PATH + "/apr" + sourceId;

                try {
                    // Iframe 전환 확인
                    driver.switchTo().defaultContent();
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    Thread.sleep(1000);

                    // 상세 페이지 로드
                    String clickFunctionCall = String.format("managementDocList.clickGridRow(%s);", sourceId);
                    js.executeScript(clickFunctionCall);
                    Thread.sleep(2500);

                    // 페이지 로드 대기
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("apprLineTable")));
                    Thread.sleep(1000);

                    // 첨부파일 정보 추출
                    List<AttachmentInfo> attachments = extractAttachments(driver);
                    System.out.println("  > 크롤링된 첨부파일 수: " + attachments.size());

                    // CSV와 개수 비교 (경고만)
                    if (USE_CSV && csvAttachesMap.containsKey(sourceId)) {
                        List<String> csvNames = csvAttachesMap.get(sourceId);
                        if (csvNames.size() != attachments.size()) {
                            countWarningCount++;
                            logCountWarning(sourceId, csvNames.size(), attachments.size());
                            System.out.println("  > [경고] 첨부파일 개수 불일치! CSV: " + csvNames.size() + ", 크롤링: " + attachments.size());
                        }

                        // 이름 비교
                        List<String> crawledNames = attachments.stream()
                                .map(a -> a.fileName)
                                .collect(Collectors.toList());
                        checkNameMismatch(sourceId, csvNames, crawledNames);
                    }

                    String attachesJson;

                    if (attachments.isEmpty()) {
                        // 첨부파일 없음
                        attachesJson = "[]";
                        System.out.println("  > 첨부파일 없음, 빈 배열로 저장");
                        success = true;
                    } else {
                        // 임시 폴더 생성
                        File tempFolder = new File(tempFolderPath);
                        if (tempFolder.exists()) {
                            deleteDirectory(tempFolder);
                        }
                        tempFolder.mkdirs();

                        // 모든 파일 다운로드 시도
                        boolean allDownloaded = true;
                        int downloadedCount = 0;

                        for (AttachmentInfo att : attachments) {
                            String savePath = tempFolderPath + "/" + att.fileName;
                            boolean downloaded = downloadFile(att.downloadUrl, savePath, cookieString);

                            if (downloaded) {
                                downloadedCount++;
                                System.out.println("    > 다운로드 성공: " + att.fileName);
                            } else {
                                allDownloaded = false;
                                System.err.println("    > 다운로드 실패: " + att.fileName);
                                break; // All or Nothing: 하나라도 실패하면 중단
                            }
                        }

                        if (allDownloaded) {
                            // 모든 파일 다운로드 성공 → JSON 생성
                            StringBuilder jsonBuilder = new StringBuilder("[");
                            for (int i = 0; i < attachments.size(); i++) {
                                AttachmentInfo att = attachments.get(i);
                                String dbPath = DB_PATH_PREFIX + "/apr" + sourceId + "/" + att.fileName;

                                if (i > 0) jsonBuilder.append(",");
                                // 공백 없는 Compact JSON
                                jsonBuilder.append("{\"name\":\"").append(escapeJson(att.fileName))
                                        .append("\",\"path\":\"").append(escapeJson(dbPath)).append("\"}");
                            }
                            jsonBuilder.append("]");
                            attachesJson = jsonBuilder.toString();

                            // 임시 폴더 → 정식 폴더로 이동
                            File finalFolder = new File(finalFolderPath);
                            if (finalFolder.exists()) {
                                deleteDirectory(finalFolder);
                            }
                            tempFolder.renameTo(finalFolder);

                            totalDownloaded += downloadedCount;
                            success = true;
                            System.out.println("  > 다운로드 완료: " + downloadedCount + "/" + attachments.size());
                        } else {
                            // 실패 → 임시 폴더 삭제, 로그 기록
                            deleteDirectory(new File(tempFolderPath));
                            logFailed(sourceId, "다운로드 실패 - " + downloadedCount + "/" + attachments.size() + "개만 성공");
                            attachesJson = null;
                            System.err.println("  > All or Nothing: 다운로드 실패로 롤백");
                        }
                    }

                    if (success && attachesJson != null) {
                        // DB UPDATE
                        updatePstmt.setString(1, attachesJson);
                        updatePstmt.setString(2, sourceId);
                        updatePstmt.executeUpdate();
                        conn.commit();

                        // 완료 로그 기록
                        logCompleted(sourceId);
                        totalSuccess++;
                        System.out.println("  > DB 업데이트 완료");
                    } else if (!success) {
                        totalFailed++;
                    }

                    // 목록으로 복귀
                    driver.navigate().back();
                    Thread.sleep(1500);

                } catch (Exception e) {
                    totalFailed++;
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "알 수 없는 오류";
                    System.err.println("  > 오류 발생: " + errorMsg);
                    e.printStackTrace();

                    // 임시 폴더 정리
                    try {
                        deleteDirectory(new File(tempFolderPath));
                    } catch (Exception ignored) {}

                    // 실패 로그
                    logFailed(sourceId, errorMsg);

                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        System.err.println("  > 롤백 실패: " + rollbackEx.getMessage());
                    }

                    // 복구 시도
                    try {
                        driver.navigate().back();
                        Thread.sleep(2000);
                        driver.switchTo().defaultContent();
                        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    } catch (Exception recoveryEx) {
                        System.err.println("  > 복구 실패, 다음 문서로 진행: " + recoveryEx.getMessage());
                    }
                }

                // 10건마다 진행 상황 출력
                if ((idx + 1) % 10 == 0) {
                    System.out.println("\n=== 진행 상황: " + (idx + 1) + "/" + remainingIds.size() +
                            " (성공: " + totalSuccess + ", 실패: " + totalFailed +
                            ", 다운로드: " + totalDownloaded + ") ===\n");
                }
            }

            // ======================= 6단계: 완료 =======================
            System.out.println("\n========================================");
            System.out.println("6단계: 크롤링 완료");
            System.out.println("========================================");
            System.out.println("  > 총 처리 대상: " + remainingIds.size());
            System.out.println("  > 성공: " + totalSuccess);
            System.out.println("  > 실패: " + totalFailed);
            System.out.println("  > 다운로드된 파일 수: " + totalDownloaded);
            System.out.println("  > 이름 불일치 건수: " + nameMismatchCount);
            System.out.println("  > 개수 경고 건수: " + countWarningCount);
            System.out.println("\n로그 파일 위치:");
            System.out.println("  > 완료 목록: " + COMPLETED_LOG);
            System.out.println("  > 실패 목록: " + FAILED_LOG);
            System.out.println("  > 이름 불일치: " + NAME_MISMATCH_LOG);
            System.out.println("  > 개수 경고: " + COUNT_WARNING_LOG);

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
                System.out.println("\nWebDriver 종료");
            }
        }
    }

    /**
     * CSV 파일에서 source_id 목록과 기존 첨부파일 정보 로드
     */
    private static List<String> loadSourceIdsFromCsv(String csvPath, Map<String, List<String>> attachesMap) throws IOException {
        List<String> sourceIds = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // 헤더 스킵
                }

                // CSV 파싱 (attaches에 쉼표가 있을 수 있으므로 주의)
                String[] parts = parseCsvLine(line);
                if (parts.length >= 5) {
                    String sourceId = parts[0].trim();
                    String attaches = parts[4].trim();

                    sourceIds.add(sourceId);

                    // attaches에서 파일명 추출
                    List<String> fileNames = extractFileNamesFromJson(attaches);
                    attachesMap.put(sourceId, fileNames);
                }
            }
        }

        return sourceIds;
    }

    /**
     * CSV 라인 파싱 (따옴표 내 쉼표 처리)
     */
    private static String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 이스케이프된 따옴표
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    /**
     * JSON에서 파일명 목록 추출
     */
    private static List<String> extractFileNamesFromJson(String json) {
        List<String> names = new ArrayList<>();

        // "name": "파일명" 또는 "name":"파일명" 패턴 추출
        Pattern pattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            names.add(matcher.group(1));
        }

        return names;
    }

    /**
     * 완료된 source_id 목록 로드
     */
    private static Set<String> loadCompletedIds() {
        Set<String> completed = new HashSet<>();
        File file = new File(COMPLETED_LOG);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        completed.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("완료 로그 읽기 실패: " + e.getMessage());
            }
        }

        return completed;
    }

    /**
     * 완료된 source_id 기록
     */
    private static void logCompleted(String sourceId) {
        try (FileWriter fw = new FileWriter(COMPLETED_LOG, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(sourceId);
        } catch (IOException e) {
            System.err.println("완료 로그 기록 실패: " + e.getMessage());
        }
    }

    /**
     * 실패 로그 기록
     */
    private static void logFailed(String sourceId, String reason) {
        try (FileWriter fw = new FileWriter(FAILED_LOG, true);
             PrintWriter pw = new PrintWriter(fw)) {
            String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
            pw.println("─────────────────────────────────────────");
            pw.println("[" + timestamp + "] source_id: " + sourceId);
            pw.println("실패 사유: " + reason);
            pw.println();
        } catch (IOException e) {
            System.err.println("실패 로그 기록 실패: " + e.getMessage());
        }
    }

    /**
     * 이름 불일치 확인 및 로그
     */
    private static void checkNameMismatch(String sourceId, List<String> csvNames, List<String> crawledNames) {
        List<String> mismatches = new ArrayList<>();

        // CSV에 있지만 크롤링에 없는 것
        for (String csvName : csvNames) {
            if (!crawledNames.contains(csvName)) {
                mismatches.add("CSV에만 존재: " + csvName);
            }
        }

        // 크롤링에 있지만 CSV에 없는 것
        for (String crawledName : crawledNames) {
            if (!csvNames.contains(crawledName)) {
                mismatches.add("크롤링에만 존재: " + crawledName);
            }
        }

        if (!mismatches.isEmpty()) {
            try (FileWriter fw = new FileWriter(NAME_MISMATCH_LOG, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
                pw.println("─────────────────────────────────────────");
                pw.println("[" + timestamp + "] source_id: " + sourceId);
                pw.println("CSV 파일명 목록: " + csvNames);
                pw.println("크롤링 파일명 목록: " + crawledNames);
                pw.println("차이점:");
                for (String mismatch : mismatches) {
                    pw.println("  - " + mismatch);
                }
                pw.println();
            } catch (IOException e) {
                System.err.println("이름 불일치 로그 기록 실패: " + e.getMessage());
            }

            System.out.println("  > [정보] 파일명 불일치 발견, 로그 기록됨");
        }
    }

    /**
     * 개수 경고 로그
     */
    private static void logCountWarning(String sourceId, int csvCount, int crawledCount) {
        try (FileWriter fw = new FileWriter(COUNT_WARNING_LOG, true);
             PrintWriter pw = new PrintWriter(fw)) {
            String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
            pw.println("─────────────────────────────────────────");
            pw.println("[" + timestamp + "] source_id: " + sourceId);
            pw.println("CSV 첨부파일 개수: " + csvCount);
            pw.println("크롤링 첨부파일 개수: " + crawledCount);
            pw.println("차이: " + Math.abs(csvCount - crawledCount) + "개");
            pw.println();
        } catch (IOException e) {
            System.err.println("개수 경고 로그 기록 실패: " + e.getMessage());
        }
    }

    /**
     * 첨부파일 정보 추출
     */
    private static List<AttachmentInfo> extractAttachments(WebDriver driver) {
        List<AttachmentInfo> attachments = new ArrayList<>();

        try {
            List<WebElement> fileLinks = driver.findElements(By.cssSelector("#aprShowFileMap a.F_11_gray"));

            for (WebElement link : fileLinks) {
                try {
                    AttachmentInfo info = new AttachmentInfo();

                    // 다운로드 URL
                    String href = link.getAttribute("href");
                    if (href != null && !href.isEmpty()) {
                        if (href.startsWith("/")) {
                            info.downloadUrl = TARGET_URL + href;
                        } else {
                            info.downloadUrl = href;
                        }
                    }

                    // 파일명 추출: "파일명.xlsx (134.29KB)" → "파일명.xlsx"
                    String linkText = link.getText().trim();
                    Pattern pattern = Pattern.compile("\\s*\\([\\d.,]+\\s*(KB|MB|GB|B)\\)\\s*$", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(linkText);
                    info.fileName = matcher.replaceAll("").trim();

                    // 유효한 정보인 경우에만 추가
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
     * 디렉토리 삭제 (재귀)
     */
    private static void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
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
