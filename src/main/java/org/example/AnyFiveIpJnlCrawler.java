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

public class AnyFiveIpJnlCrawler {

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
            "3630664","3624232","3618817","3613001","3606331","3589015","3582993","3577449","3571434","3565635","3560192","3555254","3550364","3544879","3534791","3523793","3518812","3512574","3507431","3501307","3495723","3489655","3485813","3479223","3473834","3467827","3462258","3458533","3453098","3447546","3435568","3430089","3424411","3418142","3413785","3408062","3402333","3396601","3388691","3383872","3378111","3371834","3367641","3357462","3352406","3345841","3340274","3334680","3329516","3323424","3318022","3306490","3301538","3283470","3278329","3272844","3267860","3263327","3257567","3240326","3233643","3221812","3204886","3198518","3197905","3186810","3171141","3164478","3160003","3153833","3141648","3135617","3128714","3117958","3109324","3102836","3062159","2872821","2853322","2847987","2835158","2829337","2824137","2817937","2798806","2795330","2789270","2767164","2761509","2755701","2749842","2744125","2738421","2732626","2726729","2721001","2714502","2710304","2705450","2699128","2694306","2689295","2685708","2679769","2669607","2664989","2660804","2654211","2648950","2642841","2636029","2629933","2624114","2612396","2608203","2602002","2595121","2589887","2573080","2566573","2560964","2554170","2549085","2543084","2537208","2532916","2505065","2492261","2486014","2468745","2443046","2437576","2430468","2425015","2417853","2412357","2399131","2395639","2386265","2377979","2369516","2354296","2347011","2340604","2334794","2325709","2315521","2309581","2301198","2283779","2276878","2268159","2253392","2244384","2238689","2230044","2222385","2206210","2197844","2190053","2181171","2173434","2166838","2159083","2150468","2146118","2138178","2129071","2121039","2113948","2106135","2099741","2093822","2085915","2078587","2070744","2062483","2055016","2048437","2038084","2031163","2023276","2013439","2007569","2003636","1996375","1987066","1985182","1978412","1971163","1968639","1968335","1960090","1957361","1953203","1948613","1940977","1932048","1931556","1924618","1917341","1916027","1910150","1907739","1899173","1898885","1891701","1890796","1882896","1882685","1874158","1873604","1865202","1859522","1849284","1840500","1833997","1829092","1828646","1819957","1812664","1804240","1796215","1795210","1786700","1778456","1768881","1760062","1754485","1742625","1735180","1726615","1712851","1706189","1370247","1361485","1343821","1334947","1326247","1312625","1288430","1280141","1260772","1252847","1245041","1236903","1229128","1220467","1213561","1205395","1196538","1189545","1182759","1175509","1171210","1165119","1156141","1147768","1139717","1104110","1095387","1080210","1070728","1063577","1050046","1041710","1039846","1026612","1019730","1004013","996575","979101","971071","962422","946052","932658","911797","903036","860785","860585","851319","841549","841468","834801","832038","824009","823100","813792","813503","803735","803599","784664","775353","775273","758240","748781","748342","730394","721013","720859","712557","711450","705455","704050","697334","695400","687841","678652","678517","678498","668667","660293","659913","652079","650799","642096","642061","641974","632128","623605","623399","623385","618741","616231","616052","615990","608049","607853","607729","599156","598878","597950","590975","590056","569401","568333","560370","559137","551795","548240","542171","533333","533139","523880","504846","504428","497151","496666","488429","477501","467810","344950"
            // 여기에 문서 ID를 붙여넣으세요
    };
    // ------------------------------------ 여기까지 수정하세요

    // 콘텐츠 로딩 대기 설정
    private static final int STABLE_CHECK_INTERVAL_MS = 500;
    private static final int STABLE_COUNT_REQUIRED = 3;
    private static final int MAX_WAIT_TIME_MS = 30000;

    // SQL - 테이블명 변경: ip_jnl_documents, ip_jnl_comments
    private static final String SELECT_PROCESSED_IDS_SQL =
            "SELECT source_id FROM ip_jnl_documents WHERE end_year = ?";

    private static final String INSERT_DOC_SQL =
            "INSERT INTO ip_jnl_documents (source_id, end_year, title, work_box, work_type, writer_name, " +
                    "report_date, work_date, attaches, doc_body) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_COMMENT_SQL =
            "INSERT INTO ip_jnl_comments (post_source_id, end_year, writer_name, created_at, content) " +
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

            // ★★★ 변경: IP솔루션팀 선택 ★★★
            By lblSelector = By.xpath("//li[@data-lblnm='IP솔루션팀']");
            WebElement lblItem = wait.until(ExpectedConditions.elementToBeClickable(lblSelector));
            js.executeScript("arguments[0].click();", lblItem);
            Thread.sleep(1500);
            System.out.println("  > 'IP솔루션팀' 클릭 완료.");

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

            // 이미지 경로 변환 - ip_jnl_img_연도 폴더 사용
            Pattern imgPattern = Pattern.compile("<img([^>]*)src=\"[^\"]+\"([^>]*)>");
            Matcher matcher = imgPattern.matcher(html);

            StringBuffer sb = new StringBuffer();
            int imgIndex = 0;
            while (matcher.find()) {
                String before = matcher.group(1);
                String after = matcher.group(2);
                String newPath = DB_PATH_PREFIX + "/ip_jnl_img_" + endYear + "/jnl" + docId + "/" + imgIndex + ".jpg";
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
