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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnyFiveActiviesCrawler_plus {

    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe"; // ★★★ 이 부분을 수정해야 함 ★★★

    private static final String USER_ID = "master";
    private static final String USER_PW = "five4190any#";
    private static final String TARGET_URL = "http://office.anyfive.com";
    private static final String IFRAME_NAME = "content_frame"; // 게시판 콘텐츠 Iframe 이름

    // ★★★ MariaDB 연결 정보 ★★★
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval"; // DB 주소 및 스키마
    private static final String DB_USER = "root";       // 사용자 ID
    private static final String DB_PASSWORD = "1234";   // 사용자 PW
    // ----------------------------

    // ★★★ 크롤링 대상 문서 ID 목록 - 여기에 직접 입력 ★★★
    private static final String[] TARGET_DOCUMENT_IDS = {
            "26836938","26824807","26824251","26821433","26820164","26818774","26818633","26818267","26817660","26817609","26817554","26817496","26816243","26815330","26815168","26815042","26814927","26814758","26814565","26809478","26809035","26808907","26808821","26807761","26807226","26805235","26805075","26802169","26801635","26800470","26800280","26800258","26799355","26798493","26798210","26797019","26796001","26795899","26795839","26795800","26795296","26795241","26794352","26792102","26792100","26792055","26791816","26789265","26787864","26787141","26786825","26785267","26785073","26785018","26784666","26783724","26781781","26780782","26780612","26780600","26779772","26778508","26777774","26777042","26776661","26774479","26773358","26770220","26768747","26768498","26768245","26766231","26766153","26766065","26765596","26765454","26764887","26764508","26764468","26764418","26763973","26763948","26763929","26763436","26761549","26760973","26760714","26760548","26760379","26760147","26759599","26759567","26759551","26759307","26759156","26759132","26758948","26758527","26758422","26758372","26756762","26756422","26756002","26755342","26754644","26753645","26753105","26752748","26751448","26751436","26750908","26750442","26749859","26749599","26749587","26749350","26748593","26745687","26745498","26743953","26743633","26742771","26740713","26740175","26739335","26739273","26739272","26738955","26738484","26738365","26738004","26732015","26727566","26724627","26720126","26719536","26718593","26718533","26715831","26713508","26713245","26711925","26711721","26710226","26709917","26708724","26708043","26707676","26707524","26705302","26704520","26704509","26703839","26702372","26701003","26700982","26700529","26699918","26699532","26699467","26698482","26698458","26697743","26694554","26694444","26691545","26690108","26689616","26689581","26689451","26689347","26682321","26681912","26677231","26669949","26669907","26667881","26666252","26665950","26661283","26661223","26661115","26660906","26660502","26657300","26657127","26657039","26656531","26655716","26655630","26654413","26653339","26652058","26651584","26650318","26649682","26649566","26647990","26647009","26646992","26646866","26644951","26642460","26640399","26638620","26636916","26636116","26635832","26635816","26635805","26632024","26627857","26627847","26627327","26625102","26622798","26620466","26619986","26618477","26617883","26616212","26612719","26612124","26611883","26611657","26608424","26608131","26608084","26605719","26605616","26603183","26603017","26602948","26602671","26600867","26600643","26600479","26599018","26598011","26595545","26594752","26594507","26594482","26594449","26593266","26589939","26588792","26587503","26585769","26585017","26585012","26584574","26583437","26580299","26579731","26578872","26578282","26573755","26573742","26573728","26570479","26569817","26568016","26568003","26564377","26564358","26564310","26563463","26562000","26560354","26559215","26558182","26557172","26553983","26553471","26553291","26553220","26552948","26552655","26552426","26552416","26551994","26550930","26547963","26544777","26544571","26544474","26544384","26544309","26544256","26543565","26543116","26542658","26540918","26540415","26539857","26539381","26538575","26535853","26533707","26533431","26532309","26531147","26531100","26530547","26529099","26529002","26528264","26526255","26522306","26521774","26521143","26521109","26520879","26520702","26518979","26517136","26516094","26514382","26513605","26512560","26512010","26510522","26507744","26505161","26503805","26503447","26502782","26502408","26502276","26502196","26499647","26499294","26496738","26495428","26494377","26493512","26492864","26492381","26489394","26488935","26487518","26487399","26486355","26486219","26486146","26486094","26486041","26485147","26483140","26481772","26472776","26472453","26471963","26467960","26467105","26466728","26465340","26465318","26465290","26464961","26464694","26463432","26463204","26461275","26458172","26457225","26457195","26455589","26454855","26453009","26452833","26452832","26450147","26448745","26447214","26447156","26446241","26446016","26443420","26442663","26437154","26435539","26434646","26433276","26430518","26430442","26430305"
    };

    // 데이터 삽입 SQL 쿼리
    private static final String INSERT_SQL =
            "INSERT INTO approval_data_plus (document_id, post_title, sequence, status, approval_date, department, approver) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    // 이미 처리된 문서 ID를 조회하는 SQL
    private static final String SELECT_PROCESSED_IDS_SQL =
            "SELECT DISTINCT document_id FROM approval_data_plus";

    // 상세 페이지에서 문서 제목을 추출할 XPath
    private static final String DOC_TITLE_XPATH = "//span[@class='apr_title']";



    public static void main(String[] args) {
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage"); // Linux 환경에서 유용
        options.addArguments("--disable-gpu");          // GPU 사용 비활성화
        options.addArguments("--blink-settings=imagesEnabled=false"); // 이미지 로드 비활성화 (리소스 절약)
        // options.addArguments("--headless"); // UI 없이 실행하려면 이 옵션을 활성화하세요.

        WebDriver driver = null;
        Connection conn = null;
        List<String> documentIds = new ArrayList<>();

        try {
            // --- 0. 이미 처리된 문서 ID 조회 및 필터링 ---
            System.out.println("0단계: 이미 처리된 문서 ID 조회 시작.");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // 이미 처리된 문서 ID를 Set으로 조회
            Set<String> processedIds = new HashSet<>();
            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_PROCESSED_IDS_SQL);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    processedIds.add(rs.getString("document_id"));
                }
            }
            System.out.println("  > 이미 처리된 문서 수: " + processedIds.size());

            // TARGET_DOCUMENT_IDS에서 아직 처리되지 않은 문서만 필터링
            for (String id : TARGET_DOCUMENT_IDS) {
                if (!processedIds.contains(id)) {
                    documentIds.add(id);
                }
            }

            if (documentIds.isEmpty()) {
                System.out.println("  > 모든 문서가 이미 처리되었습니다. 크롤링을 종료합니다.");
                return;
            }
            System.out.println("  > 처리할 문서 수: " + documentIds.size() + " (전체 " + TARGET_DOCUMENT_IDS.length + "개 중)");

            // DB 연결 해제 (재연결은 배치 작업 직전에 수행)
            if (conn != null) {
                conn.close();
                conn = null;
            }
            // ---------------------------------------------


            // 1. WebDriver 및 크롤링 환경 설정
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // ---------------------------------------------
            // 2단계: 로그인 및 메뉴 클릭을 통한 게시판 진입
            // ---------------------------------------------

            System.out.println("2단계: 로그인 및 메뉴 클릭을 통한 게시판 진입 시도.");

            // 2-1. 로그인 페이지 접속
            driver.get(TARGET_URL);

            // 2-2. 로그인 처리
            driver.findElement(By.id("uid")).sendKeys(USER_ID);
            driver.findElement(By.id("pwd")).sendKeys(USER_PW);
            driver.findElement(By.className("btn_login")).click();
            System.out.println("  > 로그인 버튼 클릭 완료.");

            // 2-3. '다음에 변경하기' 팝업 닫기
            String xpathPassButton = "//button[@class='home' and @onclick='pwChangePass.pass()']";
            By passButtonSelector = By.xpath(xpathPassButton);
            WebElement passButton = wait.until(ExpectedConditions.elementToBeClickable(passButtonSelector));
            passButton.click();
            System.out.println("  > '다음에 변경하기' 팝업 닫기 완료.");

            // Iframe 전환: 메뉴가 Iframe 내부에 있으므로 클릭 전에 전환합니다.
            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
            System.out.println("  > 메뉴 클릭을 위해 Iframe [" + IFRAME_NAME + "]으로 전환 성공.");

            // 2-4. '전자결재' 아이콘 클릭 (메뉴 로드)
            System.out.println("  > '전자결재' 아이콘 클릭 시도...");
            By aprLinkSelector = By.xpath("//a[contains(@href, '/apr/') and contains(@class, 'left_menu')]");
            WebElement aprLink = wait.until(ExpectedConditions.elementToBeClickable(aprLinkSelector));
            js.executeScript("arguments[0].click();", aprLink); // JS 강제 클릭
            Thread.sleep(1000); // 메뉴 로드 대기
            System.out.println("  > '전자결재' 아이콘 클릭 완료.");

            // 2-5. '결재문서관리' 메뉴 클릭 (managementDoc 로드)
            System.out.println("  > '결재문서관리' 메뉴 클릭 시도...");
            By docLiSelector = By.xpath("//li[contains(@onclick, 'managementDoc')]");
            WebElement managementDocLi = wait.until(ExpectedConditions.elementToBeClickable(docLiSelector));
            js.executeScript("arguments[0].click();", managementDocLi); // JS 강제 클릭

            // managementDocList 객체가 정의될 때까지 명시적으로 기다림
            System.out.println("  > JavaScript 객체 로딩 대기 중...");
//            wait.until(driver -> (Boolean) js.executeScript("return typeof managementDocList !== 'undefined'"));
            System.out.println("  > 'managementDocList' 객체 로드 확인됨.");

            System.out.println("  > '결재문서관리' 메뉴 클릭 완료.");

            // ---------------------------------------------
            // 3단계: 건별 데이터 추출 루프
            // ---------------------------------------------

            // DB 연결 설정 (배치 작업 직전에 재연결)
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("3단계: DB 재연결 성공 (건별 삽입 준비).");

            PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL);
            int totalInsertedRows = 0;

            // 추출한 ID 목록을 순회하며 크롤링 시작
            for (String postId : documentIds) {
                String postTitle = "[제목 추출 실패]";
                int currentPostRows = 0; // 현재 문서에서 추출된 행 수

                try {
                    System.out.println("\n  > 문서 ID: " + postId + " 처리 중...");

                    // 3-1. JS 함수 호출로 상세 페이지 로드 (Iframe 내에서 실행)
                    driver.switchTo().defaultContent();
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    Thread.sleep(2000);

                    String clickFunctionCall = String.format("managementDocList.clickGridRow(%s);", postId);
                    js.executeScript(clickFunctionCall);
                    Thread.sleep(2000);
                    System.out.println("  > JS 함수 호출 성공: " + clickFunctionCall);

                    // 3-2. 상세 페이지 로드 대기 (Iframe 내용이 갱신되기를 기다림)
                    By tableSelector = By.id("apprLineTable");
                    WebElement apprLineTable = wait.until(ExpectedConditions.presenceOfElementLocated(tableSelector));
                    Thread.sleep(2000);

                    // 3-3. 문서 제목 추출
                    try {
                        WebElement titleElement = driver.findElement(By.xpath(DOC_TITLE_XPATH));
                        postTitle = titleElement.getText().trim();
                    } catch (Exception titleEx) {
                        System.err.println("  > 경고: 제목 추출 실패, 기본값 사용. 오류: " + titleEx.getMessage());
                    }

                    System.out.println("  > 결재 라인 테이블 로드 확인. (제목: " + postTitle + ")");

                    // 3-4. 결재 라인 테이블 찾기 및 데이터 추출
                    List<WebElement> rows = apprLineTable.findElements(By.tagName("tr"));

                    for (int i = 1; i < rows.size(); i++) {
                        WebElement row = rows.get(i);
                        List<WebElement> cells = row.findElements(By.tagName("td"));

                        // 데이터 추출
                        String sequence = cells.get(0).getText().trim();
                        String status = cells.get(2).getText().trim().replace("\n", " ");
                        String approvalDate = cells.get(3).getText().trim();
                        String department = cells.get(4).getText().trim();
                        String approver = cells.get(5).findElement(By.tagName("a")).getText().trim();

                        // 3-5. DB 배치에 추가
                        pstmt.setString(1, postId);
                        pstmt.setString(2, postTitle);
                        pstmt.setString(3, sequence);
                        pstmt.setString(4, status);
                        pstmt.setString(5, approvalDate);
                        pstmt.setString(6, department);
                        pstmt.setString(7, approver);

                        pstmt.addBatch();
                        totalInsertedRows++;
                        currentPostRows++;
                    }

                    // ★★★ 수정: 문서 처리 완료 후 즉시 배치 실행 및 커밋 (메모리 최적화) ★★★
                    if (currentPostRows > 0) {
                        pstmt.executeBatch(); // 배치 실행
                        conn.commit();        // 커밋
                        System.out.println("  > 배치 실행 및 커밋 완료. (" + currentPostRows + " rows inserted for " + postId + ")");
                    }
                    // ★★★ 수정 끝 ★★★

                    // 3-6. 목록 페이지로 복귀 (Iframe 내에서 작동)
                    driver.navigate().back();
                    System.out.println("  > 목록 페이지로 복귀.");

                    // 복귀 후, Iframe 콘텐츠가 다시 목록 뷰로 갱신되기를 기다립니다.
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("apprLineTable")));
                    System.out.println("  > Iframe 콘텐츠 갱신 확인 완료.");

                } catch (Exception processEx) {
                    System.err.println("  > 오류: 문서 처리 실패 (" + postId + "): " + processEx.getMessage());

                    // 오류 발생 시, 안전하게 목록 페이지로 복귀 시도
                    driver.navigate().back();
                    try {
                        // Iframe이 깨졌을 경우를 대비해 다시 전환 시도
                        driver.switchTo().defaultContent();
                        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    } catch (Exception recoveryEx) {
                        System.err.println("  > 경고: 목록 복귀 중 치명적인 오류 발생. 루프 종료.");
                        break;
                    }
                }
            } // for loop 종료

            // ---------------------------------------------
            // 4단계: 최종 처리 확인 (이전의 대규모 배치 실행 코드는 제거되었습니다.)
            // ---------------------------------------------
            System.out.println("\n4단계: 모든 DB 작업이 완료되었습니다.");
            System.out.println("  > 총 " + totalInsertedRows + "개 행 DB에 삽입 완료.");

        } catch (SQLException e) {
            System.err.println("  > [DB 오류] 데이터베이스 작업 중 오류 발생: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("  > 롤백 실패: " + rollbackEx.getMessage());
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. 자원 해제
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("DB 연결 해제 실패: " + e.getMessage());
            }
            if (driver != null) {
                // driver.quit();
                System.out.println("WebDriver 종료.");
            }
        }
    }
}