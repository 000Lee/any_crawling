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

public class AnyFiveNewCrawler{

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
    private static final int END_YEAR = 2025; // 여기를 수정하세요

    // 크롤링 대상 문서 ID 목록 // 여기를 수정하세요
    private static final String[] TARGET_DOCUMENT_IDS = {
            "26700529", "26699918", "26699532", "26699467", "26698482", "26698458", "26697743", "26694554", "26694444", "26691545", "26690108", "26689616", "26689581", "26689451", "26689347", "26682321", "26681912", "26674574", "26673064", "26666840", "26666252", "26665950", "26661223", "26657039", "26656531", "26653603", "26651584", "26647990", "26646974", "26644951", "26640844", "26640369", "26637598", "26637566"

            //            "26418630","26413634","26406820","26388102","26377486","26373173","26370081","26350403","26348827","26343524","26343338","26326932","26316206","26313025","26307857","26295978","26294007","26291373","26289134","26287914","26279835","26273888","26272795","26271930","26267982","26267110","26265952","26265687","26265633","26265579","26263329","26263089","26262962","26261807","26260938","26260842","26208239","26120340","26090880","26072008","26071998","26071976","26069128","26066059","26059164","26055098","26054990","26054623","26054437","26050371","26050221","26049716","26049471","26046243","26022701","26017583","26009025","26002413","25989936","25972546","25965563","25961859","25961149","25960605","25960542","25952045","25938442","25921085","25919698","25919459","25919280","25918625","25916768","25916209","25915971","25915757","25915191","25905722","25904068","25880999","25845051","25654969","25643529","25642796","25621047","25597901","25597724","25411813","25407613","25351750","25333029","25332900","25332767","25332632","25332427","25236864","25023083","25020648","25013476","25006126","24821093","24811384","24811349","24800652","24790535","24774640","24730813","24716548","24708653","24670108","24658329","24620951","24612985","24551208","24545859","24484282","24477677",

//            "26895054","26890842","26889806","26888670","26888504","26886530","26886133","26885982","26885960","26885829","26885673","26884406","26883793","26880766","26880087","26879573","26879467","26876058","26876005","26875900","26875283","26875000","26870965","26870759","26869836","26869348","26867203","26867095","26866627","26862873","26861751","26860284","26859581","26857684","26857028","26856981","26856949","26856781","26856636","26855792","26855644","26855216","26854954","26854879","26853151","26852485","26851919","26851491","26851358","26851347","26851180","26846561","26846469","26846381","26846246","26845946","26845364","26843891","26843407","26842628","26842550","26841992","26838170","26837263","26836938","26836694","26836181","26835519","26834957","26834781","26832150","26831813","26831074","26830725","26828462","26828023","26826717","26825723","26824807","26824525","26824251","26822344","26821433","26820164","26818774","26818633","26818267","26817660","26817609","26817554","26817496","26816243","26815330","26815168","26815042","26814927","26814758","26814565","26809478","26809412","26809035","26808907","26808821","26807761","26807226","26805235","26805075","26803775","26802169","26801635","26800470","26800280","26800258","26800112","26799355","26798493","26798210","26797019","26796001","26795899","26795839","26795800","26795296","26795241","26794352","26792102","26792100","26792055","26791816","26789265","26787864","26787141","26786825","26785458","26785267","26785073","26785018","26784666","26783724","26781781","26780782","26780612","26780600","26779772","26778508","26777774","26777042","26776661","26774479","26773358","26773210","26770220","26768747","26768498","26768245","26766231","26766153","26766065","26765596","26765454","26765439","26764887","26764508","26764468","26764418","26763973","26763948","26763929","26763436","26762538","26762265","26762064","26761800","26761549","26761419","26760973","26760714","26760548","26760379","26760147","26759599","26759567","26759551","26759307","26759156","26759132","26758948","26758527","26758422","26758372","26757979","26757883","26757817","26757753","26757614","26756762","26756422","26756002","26755342","26754644","26753645","26753105","26752748","26751448","26751436","26750908","26750442","26749859","26749599","26749587","26749350","26748593","26745687","26745498","26743953","26743633","26742771","26740713","26740175","26739335","26739273","26739272","26738955","26738484","26738365","26738004","26732015","26727566","26726092","26724627","26724605","26724479","26720126","26719536","26718593","26718533","26715831","26715687","26715680","26715649","26713508","26713245","26711925","26711721","26710226","26709917","26708724","26708043","26707753","26707676","26707524","26705302","26704520","26704509","26704180","26703839","26702372","26701003","26700982","26700529","26699918","26699532","26699467","26698482","26698458","26697743","26694554","26694444","26691545","26690108","26689616","26689581","26689451","26689347","26682321","26681912","26674574","26673064","26666840","26666252","26665950","26661223","26657039","26656531","26653603","26651584","26647990","26646974","26644951","26640844","26640369","26637598","26637566","26636281","26635608","26635363","26635198","26632186","26632045","26631773","26631582","26631581","26631512","26631086","26631024","26630792","26620466","26604475","26586611","26552693","26524426","26480609","26463103","26462674","26458790","26457821","26453276","26452497","26450636","26447102","26444746","26444392","26442615","26442471","26440161","26439211","26438694","26438689","26438624","26438458","26434730","26433834"


//            "26415181"
//            "26677231","26669949","26669907","26667881","26661283","26661115","26660906","26660502","26657300","26657127","26655716","26655630","26654413","26653339","26652058","26650318","26649682","26649566","26647009","26646992","26646866","26642460","26640399","26638620","26636916","26636116","26635832","26635816","26635805","26632024","26627857","26627847","26627327","26625102","26622798","26619986","26618477","26617883","26616212","26612719","26612124","26611883","26611657","26608424","26608131","26608084","26605719","26605616","26603183","26603017","26602948","26602671","26600867","26600643","26600479","26599018","26598011","26595545","26594752","26594507","26594482","26594449","26593266","26589939","26588792","26587503","26585769","26585017","26585012","26584574","26583437","26580299","26579731","26578872","26578282","26573755","26573742","26573728","26570479","26569817","26568016","26568003","26564377","26564358","26564310","26563463","26562000","26560354","26559215","26558182","26557172","26553983","26553471","26553291","26553220","26552948","26552655","26552426","26552416","26551994","26550930","26547963","26544777","26544571","26544474","26544384","26544309","26544256","26543565","26543116","26542658","26540918","26540415","26539857","26539381","26538575","26535853","26533707","26533431","26532309","26531147","26531100","26530547","26529099","26529002","26528264","26526255","26522306","26521774","26521143","26521109","26520879","26520702","26518979","26517136","26516094","26514382","26513605","26512560","26512010","26510522","26507744","26505161","26503805","26503447","26502782","26502408","26502276","26502196","26499647","26499294","26496738","26495428","26494377","26493512","26492864","26492381","26489394","26488935","26487518","26487399","26486355","26486219","26486146","26486094","26486041","26485147","26483140","26481772","26472776","26472453","26471963","26467960","26467105","26466728","26465340","26465318","26465290","26464961","26464694","26463432","26463204","26461275","26458172","26457225","26457195","26455589","26454855","26453009","26452833","26452832","26450147","26448745","26447214","26447156","26446241","26446016","26443420","26442663","26437154","26435539","26434646","26433276","26430518","26430442","26430305"

            //            "2008214","2008497"

//              "2002390"

//            "26396776","26401764","26405607","26406214","26412966","26418217"

//            "2009491"
//            "26573755","26573742","26573728","26570479","26569817","26564377","26564358","26564310",
//            "26563463","26562000","26560354","26559215","26558182","26557172","26553983","26553471",
//            "26553291","26553220","26552948","26552655","26552426","26552416","26551994","26550930",
//            "26547963","26544777","26544571","26544474","26544384","26544309","26544256","26543565",
//            "26543116","26542658","26540918","26540415","26539857","26539381","26538575","26535853",
//            "26533707","26533431","26532309","26531147","26531100","26530547","26529099","26529002",
//            "26528264","26526255","26522306","26521774","26521143","26521109","26520879","26520702",
//            "26518979","26517136","26516094","26514382","26513605","26512560","26512010","26510522",
//            "26507744","26505161","26503805","26503447","26502782","26502408","26502276","26502196",
//            "26499647","26499294","26496738","26495428","26494377","26493512","26492864","26492381",
//            "26489394","26488935","26487518","26487399","26486355","26486219","26486146","26486094",
//            "26486041","26485147","26483140","26481772","26472776","26472453","26471963","26467960",
//            "26467105","26466728","26465340","26465318","26465290","26464961","26464694","26463432",
//            "26463204","26461275","26458172","26457225","26457195","26455589","26454855","26453009",
//            "26452833","26452832","26450147","26448745","26447214","26447156","26446241","26446016",
//            "26443420","26442663","26437154","26435539","26434646","26433276","26430518","26430442",
//            "26430305","26426366","26420200","26420102","26418951","26418857","26418855","26418663"
    };

    // 이미 처리된 문서 ID를 조회하는 SQL
    private static final String SELECT_PROCESSED_IDS_SQL =
            "SELECT source_id FROM new_documents_9670 WHERE end_year = ?";

    // 데이터 삽입 SQL
    private static final String INSERT_SQL =
            "INSERT INTO new_documents_9670 (source_id, doc_num, doc_type, title, doc_status, created_at, " +
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

            for (int idx = 0; idx < documentIds.size(); idx++) {
                String docId = documentIds.get(idx);
                System.out.println("\n  > [" + (idx + 1) + "/" + documentIds.size() + "] 문서 ID: " + docId + " 처리 중...");

                try {
                    // Iframe 전환 확인
                    driver.switchTo().defaultContent();
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    Thread.sleep(1000);

                    // 상세 페이지 로드
                    String clickFunctionCall = String.format("managementDocList.clickGridRow(%s);", docId);
                    js.executeScript(clickFunctionCall);
                    Thread.sleep(2500);

                    // 결재 라인 테이블 로드 대기
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("apprLineTable")));
                    Thread.sleep(1000);

                    // 데이터 추출
                    String docNum = extractDocNum(driver);
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
                    System.out.println("    > 저장 완료: " + title);

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
                            " (성공: " + totalInserted + ", 실패: " + errorCount + ") ===\n");
                }
            }

            System.out.println("\n4단계: 크롤링 완료.");
            System.out.println("  > 총 처리: " + documentIds.size());
            System.out.println("  > 성공: " + totalInserted);
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