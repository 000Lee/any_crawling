package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnyFiveBizJnlCrawler {

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
    //    /PMS_SITE-U7OI43JLDSMO/jnl

    // 사용자 입력값
    private static final int END_YEAR = 2025;

    // 최대 재시도 횟수
    private static final int MAX_RETRY = 3;

    // 크롤링 대상 문서 ID 목록
    private static final String[] TARGET_DOCUMENT_IDS = {
            "3140623","3117451","3108390","3102310","3095752","3089784","3083377","3077571","3073999","3068163","3062432","3061854","3061767","3056981","3056017","3051284","3050017","3049684","3044094","3043953","3042691","3037683","3037619","3037445","3031690","3031540","3031347","3025589","3025587","3025394","3019087","3018052","3017873","3013025","3012913","3012272","3008436","3008408","3000225","3000171","3000108","2995374","2995335","2989233","2988972","2982859","2982805","2977409","2977326","2977284","2972031","2972021","2971977","2967960","2967929","2967788","2961970","2961736","2956067","2955937","2955905","2943524","2938494","2937491","2937272","2930689","2930638","2924163","2918004","2917924","2917797","2911577","2911345","2906462","2906382","2906031","2900801","2900372","2894337","2893987","2892631","2888220","2887870","2881383","2877623","2872177","2871828","2865794","2865639","2865626","2859209","2854630","2854565","2853033","2852021","2848221","2847177","2846826","2846755","2842406","2840830","2840434","2840303","2836048","2834513","2834464","2834386","2829650","2828791","2828561","2828008","2824000","2823793","2823128","2822736","2817171","2817038","2816935","2810797","2810791","2810718","2810485","2804767","2804719","2804656","2804561","2798970","2798768","2798699","2798660","2796286","2795284","2795045","2794959","2790636","2789417","2789345","2789100","2783381","2783380","2783308","2783276","2783078","2777618","2777459","2776510","2772856","2772809","2772626","2771768","2766617","2766565","2766414","2766002","2762348","2760783","2760213","2760191","2755824","2754961","2754906","2748997","2748991","2748838","2743872","2743335","2742156","2737697","2737517","2736693","2731494","2731485","2731443","2725757","2725702","2725470","2720060","2719996","2719968","2714374","2714309","2713283","2703399","2694787","2694778","2693235","2684804","2684610","2684499","2678890","2678862","2678518","2671971","2671935","2671125","2668930","2668918","2664263","2664079","2658832","2658715","2658588","2653906","2653702","2653693","2647975","2647889","2647827","2641819","2641374","2640868","2635813","2635809","2635217","2628670","2628637","2628481"
            // 여기에 문서 ID를 붙여넣으세요
    };
    // ------------------------------------ 여기까지 수정하세요

    // 콘텐츠 로딩 대기 설정
    private static final int STABLE_CHECK_INTERVAL_MS = 500;
    private static final int STABLE_COUNT_REQUIRED = 3;
    private static final int MAX_WAIT_TIME_MS = 30000;

    // SQL - 테이블명 변경: tech_jnl_documents, biz_jnl_comments
    private static final String SELECT_PROCESSED_IDS_SQL =
            "SELECT source_id FROM biz_jnl_documents WHERE end_year = ?";

    private static final String INSERT_DOC_SQL =
            "INSERT INTO biz_jnl_documents (source_id, end_year, title, work_box, work_type, writer_name, " +
                    "report_date, work_date, attaches, doc_body) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_COMMENT_SQL =
            "INSERT INTO biz_jnl_comments (post_source_id, end_year, writer_name, created_at, content) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        WebDriver driver = null;
        Connection conn = null;

        try {
            // 0. DB에서 이미 처리된 문서 ID 조회
            System.out.println("0단계: 이미 처리된 문서 ID 조회 시작.");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            Set<String> processedIds = new HashSet<>();
            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_PROCESSED_IDS_SQL)) {
                pstmt.setInt(1, END_YEAR);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        processedIds.add(rs.getString("source_id"));
                    }
                }
            }
            System.out.println("  > 이미 처리된 문서 수: " + processedIds.size());

            // 처리할 문서 ID 필터링
            List<String> documentIds = new ArrayList<>();
            for (String id : TARGET_DOCUMENT_IDS) {
                if (!processedIds.contains(id)) {
                    documentIds.add(id);
                }
            }

            if (documentIds.isEmpty()) {
                System.out.println("  > 모든 문서가 이미 처리되었습니다. 크롤링을 종료합니다.");
                return;
            }
            System.out.println("  > 처리할 문서 수: " + documentIds.size());

            conn.close();
            conn = null;

            // 1. WebDriver 설정
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 2. 로그인 및 메뉴 진입
            System.out.println("2단계: 로그인 및 메뉴 진입.");
            driver.get(TARGET_URL);

            driver.findElement(By.id("uid")).sendKeys(USER_ID);
            driver.findElement(By.id("pwd")).sendKeys(USER_PW);
            driver.findElement(By.className("btn_login")).click();
            System.out.println("  > 로그인 버튼 클릭 완료.");

            String xpathPassButton = "//button[@class='home' and @onclick='pwChangePass.pass()']";
            WebElement passButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathPassButton)));
            passButton.click();
            System.out.println("  > '다음에 변경하기' 팝업 닫기 완료.");

            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
            System.out.println("  > Iframe 전환 성공.");

            By jnlLinkSelector = By.xpath("//a[contains(@href, '/jnl/') and contains(@class, 'left_menu')]");
            WebElement jnlLink = wait.until(ExpectedConditions.elementToBeClickable(jnlLinkSelector));
            js.executeScript("arguments[0].click();", jnlLink);
            Thread.sleep(1500);
            System.out.println("  > '업무관리' 아이콘 클릭 완료.");

            By corpBoxSelector = By.xpath("//span[contains(@class, 'corp-span')]");
            WebElement corpBox = wait.until(ExpectedConditions.elementToBeClickable(corpBoxSelector));
            js.executeScript("arguments[0].click();", corpBox);
            Thread.sleep(1000);
            System.out.println("  > '회사 업무함' 클릭 완료.");

            // ★★★ 변경: 경영기획팀 선택 ★★★
            By lblSelector = By.xpath("//li[@data-lblnm='경영기획팀']");
            WebElement lblItem = wait.until(ExpectedConditions.elementToBeClickable(lblSelector));
            js.executeScript("arguments[0].click();", lblItem);
            Thread.sleep(1500);
            System.out.println("  > '경영기획팀' 클릭 완료.");

            // 3. DB 재연결 및 크롤링 시작
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("3단계: 크롤링 시작.");

            PreparedStatement docPstmt = conn.prepareStatement(INSERT_DOC_SQL);
            PreparedStatement cmtPstmt = conn.prepareStatement(INSERT_COMMENT_SQL);
            int totalInserted = 0;
            int totalComments = 0;
            int errorCount = 0;
            List<String> failedIds = new ArrayList<>();

            for (int idx = 0; idx < documentIds.size(); idx++) {
                String docId = documentIds.get(idx);
                System.out.println("\n  > [" + (idx + 1) + "/" + documentIds.size() + "] 문서 ID: " + docId + " 처리 중...");

                boolean success = false;
                int retryCount = 0;

                while (!success && retryCount < MAX_RETRY) {
                    try {
                        if (retryCount > 0) {
                            System.out.println("    > 재시도 " + retryCount + "/" + MAX_RETRY);
                        }

                        // Iframe 전환 확인
                        driver.switchTo().defaultContent();
                        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                        Thread.sleep(500);

                        // 상세 페이지 로드 (JavaScript 함수 호출)
                        String clickFunctionCall = String.format("jnlInfoList.showGo(null, %s, 1, true, '');", docId);
                        js.executeScript(clickFunctionCall);

                        // 요청한 문서 ID가 로드될 때까지 대기
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
                            System.err.println("    > 문서 ID 로드 실패, 재시도...");
                            retryCount++;
                            driver.navigate().back();
                            Thread.sleep(2000);
                            continue;
                        }

                        System.out.println("    > 문서 ID 검증 성공: " + docId);
                        Thread.sleep(500);

                        // 데이터 추출
                        String title = extractTitle(driver);
                        if (title.isEmpty()) {
                            System.err.println("    > 제목이 비어있음, 재시도...");
                            retryCount++;
                            driver.navigate().back();
                            Thread.sleep(2000);
                            continue;
                        }

                        String workBox = extractWorkBox(driver);
                        String workType = extractWorkType(driver);
                        String writerName = extractWriterName(driver);
                        long reportDate = extractReportDate(driver);
                        String workDate = extractWorkDate(driver);
                        String docBody = extractDocBody(driver, docId, END_YEAR);

                        // 문서 저장
                        docPstmt.setString(1, docId);
                        docPstmt.setInt(2, END_YEAR);
                        docPstmt.setString(3, title);
                        docPstmt.setString(4, workBox);
                        docPstmt.setString(5, workType);
                        docPstmt.setString(6, writerName);
                        docPstmt.setLong(7, reportDate);
                        docPstmt.setString(8, workDate);
                        docPstmt.setString(9, "");
                        docPstmt.setString(10, docBody);

                        docPstmt.executeUpdate();
                        totalInserted++;
                        System.out.println("    > 문서 저장 완료: " + title);

                        // 의견 추출 및 저장
                        List<CommentInfo> comments = extractComments(driver);
                        for (CommentInfo cmt : comments) {
                            cmtPstmt.setString(1, docId);
                            cmtPstmt.setInt(2, END_YEAR);
                            cmtPstmt.setString(3, cmt.writerName);
                            cmtPstmt.setLong(4, cmt.createdAt);
                            cmtPstmt.setString(5, cmt.content);
                            cmtPstmt.executeUpdate();
                            totalComments++;
                        }
                        if (!comments.isEmpty()) {
                            System.out.println("    > 의견 저장 완료: " + comments.size() + "개");
                        }

                        conn.commit();
                        success = true;

                        // 목록으로 복귀
                        driver.navigate().back();
                        Thread.sleep(1500);

                    } catch (Exception e) {
                        retryCount++;
                        System.err.println("    > 오류 발생 (시도 " + retryCount + "): " + e.getMessage());

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
                            Thread.sleep(1000);
                        } catch (Exception recoveryEx) {
                            System.err.println("    > 복구 실패: " + recoveryEx.getMessage());
                        }
                    }
                }

                if (!success) {
                    errorCount++;
                    failedIds.add(docId);
                    System.err.println("    > 최종 실패: 문서 ID " + docId);
                }

                // 10건마다 진행 상황 출력
                if ((idx + 1) % 10 == 0) {
                    System.out.println("\n=== 진행 상황: " + (idx + 1) + "/" + documentIds.size() +
                            " (문서: " + totalInserted + ", 의견: " + totalComments + ", 실패: " + errorCount + ") ===\n");
                }
            }

            System.out.println("\n4단계: 크롤링 완료.");
            System.out.println("  > 총 처리: " + documentIds.size());
            System.out.println("  > 문서 저장: " + totalInserted);
            System.out.println("  > 의견 저장: " + totalComments);
            System.out.println("  > 실패: " + errorCount);

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
                System.out.println("WebDriver 종료.");
            }
        }
    }

    // === 데이터 추출 메서드들 ===

    private static String extractTitle(WebDriver driver) {
        try {
            WebElement titleTd = driver.findElement(By.cssSelector("table.tbl_none-titpoint td"));
            String text = titleTd.getText().trim();
            return text.replace("\u00A0", " ").trim();
        } catch (Exception e) {
            System.err.println("    > 제목 추출 실패: " + e.getMessage());
        }
        return "";
    }

    private static String extractWorkBox(WebDriver driver) {
        try {
            List<WebElement> workBoxSpans = driver.findElements(By.cssSelector("span.workSelItemBx"));
            if (workBoxSpans.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < workBoxSpans.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(workBoxSpans.get(i).getText().trim());
            }
            return sb.toString();
        } catch (Exception e) {
            System.err.println("    > 업무함 추출 실패: " + e.getMessage());
        }
        return "";
    }

    private static String extractWorkType(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    if (ths.get(i).getText().contains("업무 유형")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > i) {
                            return tds.get(i).getText().trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("    > 업무 유형 추출 실패: " + e.getMessage());
        }
        return "";
    }

    private static String extractWriterName(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    if (ths.get(i).getText().contains("작성자")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > 0) {
                            WebElement td = tds.get(0);
                            try {
                                WebElement userSpan = td.findElement(By.cssSelector("span.abcUsr"));
                                String fullText = userSpan.getText().trim();
                                if (fullText.contains("/")) {
                                    return fullText.split("/")[0].trim();
                                }
                                return fullText;
                            } catch (Exception e) {
                                return td.getText().trim().split("/")[0].trim();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("    > 작성자 추출 실패: " + e.getMessage());
        }
        return "";
    }

    private static long extractReportDate(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    if (ths.get(i).getText().contains("보고 일자")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > i) {
                            String dateText = tds.get(i).getText().trim();
                            if (dateText.contains("\n")) {
                                dateText = dateText.split("\n")[0].trim();
                            }
                            LocalDateTime dateTime = LocalDateTime.parse(dateText, DATE_TIME_FORMATTER);
                            return dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("    > 보고 일자 추출 실패: " + e.getMessage());
        }
        return 0;
    }

    private static String extractWorkDate(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    if (ths.get(i).getText().contains("업무 일자")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > i) {
                            return tds.get(i).getText().trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("    > 업무 일자 추출 실패: " + e.getMessage());
        }
        return "";
    }

    /**
     * 콘텐츠 길이가 안정화될 때까지 대기 후 doc_body 추출
     * ★★★ 변경: 이미지 경로에 tech_ 접두어 추가 ★★★
     */
    private static String extractDocBody(WebDriver driver, String docId, int endYear) {
        try {
            // 콘텐츠 안정화 대기
            String html = waitForContentStable(driver);

            if (html == null || html.isEmpty()) {
                System.err.println("    > 본문 로딩 타임아웃 또는 빈 콘텐츠");
                return "";
            }

            // 이미지 경로 변환 - biz_jnl_img_연도 폴더 사용
            Pattern imgPattern = Pattern.compile("<img([^>]*)src=\"[^\"]+\"([^>]*)>");
            Matcher matcher = imgPattern.matcher(html);

            StringBuffer sb = new StringBuffer();
            int imgIndex = 0;
            while (matcher.find()) {
                String before = matcher.group(1);
                String after = matcher.group(2);
                String newPath = DB_PATH_PREFIX + "/biz_jnl_img_" + endYear + "/jnl" + docId + "/" + imgIndex + ".jpg";
                String replacement = "<img" + before + "src=\"" + newPath + "\"" + after + ">";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                imgIndex++;
            }
            matcher.appendTail(sb);
            html = sb.toString();

            // 공백 정리
            html = html.replaceAll("[\\r\\n]+", " ").replaceAll("\\s+", " ").trim();

            return html;
        } catch (Exception e) {
            System.err.println("    > 본문 추출 실패: " + e.getMessage());
        }
        return "";
    }

    /**
     * 콘텐츠 길이가 안정화될 때까지 대기
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
                        return html;
                    }
                } else {
                    stableCount = 0;
                }

                prevLength = currLength;
                Thread.sleep(STABLE_CHECK_INTERVAL_MS);

            } catch (Exception e) {
                try {
                    Thread.sleep(STABLE_CHECK_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }

        return html;
    }

    private static List<CommentInfo> extractComments(WebDriver driver) {
        List<CommentInfo> comments = new ArrayList<>();

        try {
            List<WebElement> articles = driver.findElements(By.cssSelector("#jnlInfoCmtList article.comentWrap"));

            for (WebElement article : articles) {
                try {
                    CommentInfo cmt = new CommentInfo();

                    WebElement userSpan = article.findElement(By.cssSelector("span.abcUsr"));
                    String fullName = userSpan.getText().trim();
                    cmt.writerName = fullName.contains("/") ? fullName.split("/")[0].trim() : fullName;

                    WebElement textPre = article.findElement(By.cssSelector("li.text pre"));
                    cmt.content = textPre.getText().trim();

                    WebElement dateLi = article.findElement(By.cssSelector("li.date_time"));
                    String dateStr = dateLi.getText().trim();
                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
                        cmt.createdAt = dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
                    } catch (Exception e) {
                        cmt.createdAt = 0;
                    }

                    comments.add(cmt);
                } catch (Exception e) {
                    // 개별 의견 파싱 실패 시 무시
                }
            }
        } catch (Exception e) {
            System.err.println("    > 의견 추출 실패: " + e.getMessage());
        }

        return comments;
    }

    static class CommentInfo {
        String writerName = "";
        long createdAt = 0;
        String content = "";
    }
}
