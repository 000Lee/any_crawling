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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class AnyFiveNewCrawler9670{

    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";// 여기를 수정하세요

    private static final String USER_ID = "master";
    private static final String USER_PW = "five4190any#";
    private static final String TARGET_URL = "https://auth.onnet21.com/?re=anyfive.onnet21.com/sso/login";
    private static final String IFRAME_NAME = "content_frame";

    // MariaDB 연결 정보 // 여기를 수정하세요
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    // 사용자 입력값
    private static final int END_YEAR = 2024; // 여기를 수정하세요

    // 최대 재시도 횟수
    private static final int MAX_RETRY = 3;

    // 크롤링 대상 문서 ID 목록 // 여기를 수정하세요
    private static final String[] TARGET_DOCUMENT_IDS = {
            //17년도 2건
            "8817728","8445141",

            //24년도 374건
            "24453086","24433849","24343041","24341538","24332495","24321215","24257998","24240298","24210693","24196549","24190855","24161825","24044837","24044779","24038368","24019517","24015061","24014609","24009827","24001823","24000561","23999030","23992443","23990589","23989275","23984928","23983553","23980304","23973460","23969243","23965513","23964991","23964706","23964520","23964462","23964449","23960249","23958244","23957286","23956660","23956652","23955699","23953009","23952507","23945526","23942091","23929581","23929061","23928881","23928488","23928280","23920198","23917177","23915450","23914627","23914551","23914335","23914243","23904248","23897393","23896028","23893334","23888159","23884844","23884480","23881467","23877593","23875479","23875389","23875177","23875077","23848112","23847866","23844095","23842012","23836822","23836708","23836607","23836334","23836155","23827007","23815685","23788019","23787763","23787668","23764082","23754046","23750490","23749976","23748658","23743315","23743211","23739686","23739238","23729352","23729287","23729231","23729055","23720975","23716488","23710161","23699458","23696555","23686381","23664383","23656963","23654068","23642765","23639029","23628630","23622775","23622744","23620461","23619842","23619250","23605633","23563011","23561430","23560807","23560780","23557522","23557372","23553070","23552598","23551939","23531541","23523930","23519303","23511104","23490208","23488574","23483467","23445463","23434763","23434625","23432653","23429395","23427237","23421108","23421002","23419914","23419664","23418955","23418918","23411132","23411011","23410740","23408924","23408131","23408083","23407646","23407423","23371244","23352123","23345546","23345431","23345349","23345231","23344631","23330863","23330636","23328674","23316372","23306527","23262572","23238522","23233220","23231778","23226542","23218484","23217444","23217280","23214392","23210483","23210424","23210385","23210019","23209788","23209371","23209196","23208787","23208597","23186556","23153717","23145298","23144557","23144518","23144464","23144404","23101216","23056973","23053077","23041617","23037243","23032725","23028537","23021256","23010837","23006692","23006526","23005125","22997922","22996418","22996338","22996251","22993600","22989723","22988771","22988563","22988456","22987444","22986424","22957838","22957173","22947998","22940407","22938116","22938078","22937998","22937885","22876566","22864739","22856003","22845341","22844618","22823198","22820527","22818711","22818621","22813379","22813246","22812086","22811561","22811551","22811160","22810950","22767563","22757120","22747054","22747012","22746979","22746927","22742939","22728162","22725956","22718855","22709268","22688930","22686503","22656093","22640116","22624673","22616099","22612683","22606437","22605139","22605034","22604199","22592906","22591948","22591822","22590278","22590109","22590022","22588195","22587808","22578077","22564662","22560053","22559249","22548922","22544046","22543398","22535767","22535659","22535580","22535329","22534575","22523268","22480556","22475740","22471509","22470701","22459521","22452596","22440471","22439147","22439109","22428039","22426756","22422005","22421696","22421649","22420614","22414926","22413644","22413478","22413275","22413131","22410439","22404407","22393767","22376838","22375460","22361161","22359719","22359628","22359374","22359118","22355594","22338796","22308048","22304083","22297426","22291817","22273310","22268350","22254516","22243676","22240568","22230103","22229808","22229560","22229082","22221800","22221581","22214469","22214216","22214143","22212063","22210375","22206719","22204332","22190141","22184989","22179068","22166301","22159348","22157783","22156296","22149511","22144968","22144828","22144742","22144567","22134990","22134909","22134672","22134583","22132288","22120229","22120048","22119985","22117924","22116689","22114469","22114215","22107943","22107588","22107105","22107072","22099356","22098504","22098223","22097977","22097756","22096965","22096604","22096089","22095879","22095654","22091678","22090808","22086615"

//            "26849267","26786389","24765323"
//            "26637566","26637598","26640369","26640844","26644951","26646974","26647990","26651584","26653603","26656531","26657039","26661223","26665950","26666252","26666840","26673064","26674574","26681912","26682321","26689347","26689451","26689581","26689616","26690108","26691545","26694444","26694554","26697743","26698458","26698482","26699467","26699532","26699918","26700982","26701003","26702372","26703839","26704180","26704509","26704520","26708043","26708724","26709917","26710226","26711721","26711925","26713508","26715649","26715680","26715831","26718533","26719536","26720126","26724479","26724605","26724627","26726092","26732015","26738004","26738484","26738955","26739272","26739273","26707524","26707676"
    };

    // 이미 처리된 문서 ID를 조회하는 SQL
    private static final String SELECT_PROCESSED_IDS_SQL =
            "SELECT source_id FROM new_documents_2024 WHERE end_year = ?";

    // 데이터 삽입 SQL
    private static final String INSERT_SQL =
            "INSERT INTO new_documents_2024 (source_id, doc_num, doc_type, title, doc_status, created_at, " +
                    "drafter_name, drafter_position, drafter_dept, drafter_email, drafter_dept_code, " +
                    "form_name, is_public, end_year, `references`, attaches, referrers, activities, doc_body) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // 날짜 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--blink-settings=imagesEnabled=false");
        // options.addArguments("--headless");

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

            // DB 연결 해제 (재연결은 크롤링 시작 전에)
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

            // '다음에 변경하기' 팝업 닫기
            String xpathPassButton = "//button[@class='home' and @onclick='pwChangePass.pass()']";
            WebElement passButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathPassButton)));
            passButton.click();
            System.out.println("  > '다음에 변경하기' 팝업 닫기 완료.");

            // Iframe 전환
            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
            System.out.println("  > Iframe 전환 성공.");

            // '전자결재' 아이콘 클릭
            By aprLinkSelector = By.xpath("//a[contains(@href, '/apr/') and contains(@class, 'left_menu')]");
            WebElement aprLink = wait.until(ExpectedConditions.elementToBeClickable(aprLinkSelector));
            js.executeScript("arguments[0].click();", aprLink);
            Thread.sleep(1000);
            System.out.println("  > '전자결재' 아이콘 클릭 완료.");

            // '결재문서관리' 메뉴 클릭
            By docLiSelector = By.xpath("//li[contains(@onclick, 'managementDoc')]");
            WebElement managementDocLi = wait.until(ExpectedConditions.elementToBeClickable(docLiSelector));
            js.executeScript("arguments[0].click();", managementDocLi);
            Thread.sleep(1500);
            System.out.println("  > '결재문서관리' 메뉴 클릭 완료.");

            // 3. DB 재연결 및 크롤링 시작
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("3단계: 크롤링 시작.");

            PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL);
            int totalInserted = 0;
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
                        Thread.sleep(1000);

                        // 상세 페이지 로드
                        String clickFunctionCall = String.format("managementDocList.clickGridRow(%s);", docId);
                        js.executeScript(clickFunctionCall);
                        Thread.sleep(2500);

                        // ★★★ 문서 ID 검증 로직 추가 ★★★
                        final String expectedDocId = docId;
                        boolean idMatched = wait.until(driver1 -> {
                            try {
                                WebElement seqInput = driver1.findElement(
                                        By.cssSelector("input[name='appr_doc_seq']"));
                                String loadedId = seqInput.getAttribute("value");
                                return expectedDocId.equals(loadedId);
                            } catch (Exception e) {
                                return false;
                            }
                        });

                        if (!idMatched) {
                            System.err.println("    > 문서 ID 불일치, 재시도...");
                            retryCount++;
                            driver.navigate().back();
                            Thread.sleep(2000);
                            continue;
                        }

                        System.out.println("    > 문서 ID 검증 성공: " + docId);

                        // 결재 라인 테이블 로드 대기
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("apprLineTable")));
                        Thread.sleep(1000);

                        // 데이터 추출
                        String docNum = extractDocNum(driver);
                        if (docNum.isEmpty()) {
                            System.err.println("    > 문서번호가 비어있음, 재시도...");
                            retryCount++;
                            driver.navigate().back();
                            Thread.sleep(2000);
                            continue;
                        }

                        String title = extractTitle(driver);
                        long createdAt = extractCreatedAt(driver);
                        String drafterName = extractDrafterName(driver);
                        String formName = extractFormName(driver);
                        int isPublic = extractIsPublic(driver);
                        String references = extractReferences(driver);
                        String referrers = extractReferrers(driver);
                        String activities = extractActivities(driver);

                        // PreparedStatement 설정
                        pstmt.setString(1, docId);                    // source_id
                        pstmt.setString(2, docNum);                   // doc_num
                        pstmt.setString(3, "DRAFT");                  // doc_type (고정)
                        pstmt.setString(4, title);                    // title
                        pstmt.setString(5, "COMPLETE");               // doc_status (고정)
                        pstmt.setLong(6, createdAt);                  // created_at
                        pstmt.setString(7, drafterName);              // drafter_name
                        pstmt.setString(8, "");                       // drafter_position (공란)
                        pstmt.setString(9, "");                       // drafter_dept (공란)
                        pstmt.setString(10, "");                      // drafter_email (공란)
                        pstmt.setString(11, "");                      // drafter_dept_code (공란)
                        pstmt.setString(12, formName);                // form_name
                        pstmt.setInt(13, isPublic);                   // is_public
                        pstmt.setInt(14, END_YEAR);                   // end_year
                        pstmt.setString(15, references);              // references
                        pstmt.setString(16, "");                      // attaches (공란)
                        pstmt.setString(17, referrers);               // referrers
                        pstmt.setString(18, activities);              // activities
                        pstmt.setString(19, "");                      // doc_body (공란)

                        pstmt.executeUpdate();
                        conn.commit();
                        totalInserted++;
                        success = true;
                        System.out.println("    > 저장 완료: " + title);

                        // 목록으로 복귀
                        driver.navigate().back();
                        Thread.sleep(1500);

                    } catch (Exception e) {
                        retryCount++;
                        System.err.println("    > 오류 발생 (시도 " + retryCount + "): " + e.getMessage());
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
                }

                if (!success) {
                    errorCount++;
                    failedIds.add(docId);
                    System.err.println("    > 최종 실패: 문서 ID " + docId);
                }

                // 10건마다 진행 상황 출력
                if ((idx + 1) % 10 == 0) {
                    System.out.println("\n=== 진행 상황: " + (idx + 1) + "/" + documentIds.size() +
                            " (성공: " + totalInserted + ", 실패: " + errorCount + ") ===\n");
                }
            }

            System.out.println("\n4단계: 크롤링 완료.");
            System.out.println("  > 총 처리: " + documentIds.size());
            System.out.println("  > 성공: " + totalInserted);
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

    /**
     * 문서번호 추출
     */
    private static String extractDocNum(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    if (ths.get(i).getText().contains("문서번호")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > i) {
                            return tds.get(i).getText().trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("    > 문서번호 추출 실패: " + e.getMessage());
        }
        return "";
    }

    /**
     * 제목 추출
     */
    private static String extractTitle(WebDriver driver) {
        try {
            WebElement titleElement = driver.findElement(By.cssSelector("span.apr_title"));
            return titleElement.getText().trim().replace("\u00A0", " ");
        } catch (Exception e) {
            System.err.println("    > 제목 추출 실패: " + e.getMessage());
        }
        return "";
    }

    /**
     * 기안일 추출 → Unix timestamp (밀리초) 변환
     */
    private static long extractCreatedAt(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    String thText = ths.get(i).getText();
                    if (thText.contains("기안일")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > i) {
                            String dateText = tds.get(i).getText().trim();
                            // 첫 번째 줄만 추출 (완료일이 같이 있을 수 있음)
                            if (dateText.contains("\n")) {
                                dateText = dateText.split("\n")[0].trim();
                            }
                            LocalDateTime dateTime = LocalDateTime.parse(dateText, DATE_FORMATTER);
                            return dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("    > 기안일 추출 실패: " + e.getMessage());
        }
        return 0;
    }

    /**
     * 기안자 이름 추출 (이름만)
     */
    private static String extractDrafterName(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    if (ths.get(i).getText().contains("기안자")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > i) {
                            WebElement td = tds.get(i);
                            try {
                                WebElement anchor = td.findElement(By.cssSelector("a.abcUsr"));
                                String fullText = anchor.getText().trim();
                                // "김별님/경영지원팀/책임" → "김별님"
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
            System.err.println("    > 기안자 추출 실패: " + e.getMessage());
        }
        return "";
    }

    /**
     * 양식명 추출
     */
    private static String extractFormName(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    if (ths.get(i).getText().contains("양식명")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > i) {
                            return tds.get(i).getText().trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("    > 양식명 추출 실패: " + e.getMessage());
        }
        return "";
    }

    /**
     * 문서공개 여부 추출 (비공개=0, 공개=1)
     */
    private static int extractIsPublic(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    if (ths.get(i).getText().contains("문서공개")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > i) {
                            String value = tds.get(i).getText().trim();
                            return value.contains("공개") && !value.contains("비공개") ? 1 : 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("    > 문서공개 추출 실패: " + e.getMessage());
        }
        return 0;
    }

    /**
     * 참조문서 추출 → JSON 배열
     * [{"sourceId":"26322481"},{"sourceId":"19596713"}]
     */
    private static String extractReferences(WebDriver driver) {
        try {
            List<WebElement> refLinks = driver.findElements(By.cssSelector("#aprDocShowAddDocList a[onclick*='openRefDoc']"));
            if (refLinks.isEmpty()) {
                return "[]";
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < refLinks.size(); i++) {
                String onclick = refLinks.get(i).getAttribute("onclick");
                // aprDocShow.openRefDoc('26322481') → 26322481 추출
                Pattern pattern = Pattern.compile("openRefDoc\\('(\\d+)'\\)");
                Matcher matcher = pattern.matcher(onclick);
                if (matcher.find()) {
                    if (i > 0) sb.append(",");
                    sb.append("{\"sourceId\":\"").append(matcher.group(1)).append("\"}");
                }
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            System.err.println("    > 참조문서 추출 실패: " + e.getMessage());
        }
        return "[]";
    }

    /**
     * 참조자 추출 → JSON 배열
     * [{"name":"최기원"},{"name":"김철수"}]
     */
    private static String extractReferrers(WebDriver driver) {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.tbl_none-titInfo tr"));
            for (WebElement row : rows) {
                List<WebElement> ths = row.findElements(By.tagName("th"));
                for (int i = 0; i < ths.size(); i++) {
                    if (ths.get(i).getText().contains("참조") && !ths.get(i).getText().contains("참조문서")) {
                        List<WebElement> tds = row.findElements(By.tagName("td"));
                        if (tds.size() > i) {
                            List<WebElement> anchors = tds.get(i).findElements(By.cssSelector("a.abcUsr"));
                            if (anchors.isEmpty()) {
                                return "[]";
                            }

                            StringBuilder sb = new StringBuilder("[");
                            for (int j = 0; j < anchors.size(); j++) {
                                String fullText = anchors.get(j).getText().trim();
                                String name = fullText.contains("/") ? fullText.split("/")[0].trim() : fullText;
                                if (j > 0) sb.append(",");
                                sb.append("{\"name\":\"").append(escapeJson(name)).append("\"}");
                            }
                            sb.append("]");
                            return sb.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("    > 참조자 추출 실패: " + e.getMessage());
        }
        return "[]";
    }

    /**
     * 결재 활동 추출 → JSON 배열
     * 결재라인 테이블 + 결재의견 매칭
     */
    private static String extractActivities(WebDriver driver) {
        try {
            // 1. 결재 라인 테이블에서 결재자 정보 수집
            List<ActivityInfo> activityList = new ArrayList<>();
            WebElement apprLineTable = driver.findElement(By.id("apprLineTable"));
            List<WebElement> rows = apprLineTable.findElements(By.cssSelector("tbody tr"));

            for (WebElement row : rows) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() >= 6) {
                    ActivityInfo info = new ActivityInfo();

                    // 상태 (기안, 승인, 합의)
                    String statusText = cells.get(2).getText().trim().replace("\n", " ");
                    info.actionLogType = mapActionType(statusText);
                    info.type = info.actionLogType;

                    // 결재일시
                    info.actionDateStr = cells.get(3).getText().trim();
                    info.actionDate = parseDateTime(info.actionDateStr);

                    // 부서
                    info.deptName = cells.get(4).getText().trim();

                    // 결재자 (이름 직위)
                    try {
                        WebElement anchor = cells.get(5).findElement(By.tagName("a"));
                        String approverText = anchor.getText().trim();
                        // "김별님 책임" → 이름: 김별님, 직위: 책임
                        String[] parts = approverText.split(" ");
                        if (parts.length >= 2) {
                            info.name = parts[0];
                            info.positionName = parts[parts.length - 1];
                        } else {
                            info.name = approverText;
                            info.positionName = "";
                        }
                    } catch (Exception e) {
                        info.name = cells.get(5).getText().trim();
                        info.positionName = "";
                    }

                    info.emailId = "";
                    info.deptCode = "";
                    info.actionComment = "";

                    activityList.add(info);
                }
            }

            // 2. 결재의견에서 의견 수집
            List<OpinionInfo> opinionList = new ArrayList<>();
            List<WebElement> opinionUls = driver.findElements(By.cssSelector("table.tbl_none-titInfo ul._mp"));
            for (WebElement ul : opinionUls) {
                try {
                    OpinionInfo opinion = new OpinionInfo();

                    // 이름 추출
                    WebElement nameAnchor = ul.findElement(By.cssSelector("a.abcUsr"));
                    String fullName = nameAnchor.getText().trim();
                    opinion.name = fullName.contains("/") ? fullName.split("/")[0].trim() : fullName;

                    // 결재일시 추출
                    WebElement dateSpan = ul.findElement(By.cssSelector("li > span[style*='margin-left']"));
                    opinion.dateStr = dateSpan.getText().trim();

                    // 의견 추출 (두 번째 li)
                    List<WebElement> lis = ul.findElements(By.tagName("li"));
                    if (lis.size() >= 2) {
                        opinion.comment = lis.get(1).getText().trim();
                    }

                    opinionList.add(opinion);
                } catch (Exception e) {
                    // 의견 파싱 실패 시 무시
                }
            }

            // 3. 매칭: 이름 + 결재일시가 일치하면 의견 연결
            for (ActivityInfo activity : activityList) {
                for (OpinionInfo opinion : opinionList) {
                    if (activity.name.equals(opinion.name) && activity.actionDateStr.equals(opinion.dateStr)) {
                        activity.actionComment = opinion.comment;
                        break;
                    }
                }
            }

            // 4. JSON 생성
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < activityList.size(); i++) {
                ActivityInfo info = activityList.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"positionName\":\"").append(escapeJson(info.positionName)).append("\",");
                sb.append("\"deptName\":\"").append(escapeJson(info.deptName)).append("\",");
                sb.append("\"actionLogType\":\"").append(info.actionLogType).append("\",");
                sb.append("\"name\":\"").append(escapeJson(info.name)).append("\",");
                sb.append("\"emailId\":\"").append(info.emailId).append("\",");
                sb.append("\"type\":\"").append(info.type).append("\",");
                sb.append("\"actionDate\":").append(info.actionDate).append(",");
                sb.append("\"deptCode\":\"").append(info.deptCode).append("\",");
                sb.append("\"actionComment\":\"").append(escapeJson(info.actionComment)).append("\"");
                sb.append("}");
            }
            sb.append("]");
            return sb.toString();

        } catch (Exception e) {
            System.err.println("    > 결재활동 추출 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return "[]";
    }

    /**
     * 상태값 매핑: 기안→DRAFT, 승인→APPROVAL, 합의→AGREEMENT
     */
    private static String mapActionType(String status) {
        if (status.contains("기안")) return "DRAFT";
        if (status.contains("승인")) return "APPROVAL";
        if (status.contains("합의")) return "AGREEMENT";
        return status;
    }

    /**
     * 날짜 문자열 → Unix timestamp (밀리초) 변환
     */
    private static long parseDateTime(String dateStr) {
        try {
            if (dateStr == null || dateStr.isEmpty()) return 0;
            LocalDateTime dateTime = LocalDateTime.parse(dateStr.trim(), DATE_FORMATTER);
            return dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
        } catch (Exception e) {
            return 0;
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

    // 결재 활동 정보 클래스
    static class ActivityInfo {
        String positionName = "";
        String deptName = "";
        String actionLogType = "";
        String name = "";
        String emailId = "";
        String type = "";
        long actionDate = 0;
        String actionDateStr = "";
        String deptCode = "";
        String actionComment = "";
    }

    // 결재 의견 정보 클래스
    static class OpinionInfo {
        String name = "";
        String dateStr = "";
        String comment = "";
    }
}