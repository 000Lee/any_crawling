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

public class AnyFiveIpJnlListCrawler {

    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

    private static final String USER_ID = "master";
    private static final String USER_PW = "five4190any#";
    private static final String TARGET_URL = "http://office.anyfive.com";
    private static final String IFRAME_NAME = "content_frame";

    // ================================== 여기부터 수정하세요 ==================================
    // MariaDB 연결 정보
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_jnl";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    // 날짜 검색 범위 (형식: "yyyy-MM-dd")
    private static final String SEARCH_DATE_MIN = "2016-01-01";  // 시작일
    private static final String SEARCH_DATE_MAX = "2025-12-31";  // 종료일

    // DB 저장용 연도 (위 날짜 범위의 연도와 맞춰주세요)
    private static final int END_YEAR = 2025;

    // 페이지 설정 (START_PAGE부터 END_PAGE까지, END_PAGE가 -1이면 마지막 페이지까지)
    private static final int START_PAGE = 1;
    private static final int END_PAGE = -1;  // -1: 전체 페이지, 양수: 해당 페이지까지

    // 최대 재시도 횟수
    private static final int MAX_RETRY = 3;

    // 현재는 end_year가 같은 것만 중복 체크합니다
    // 만약 연도 상관없이 전체에서 중복 체크하려면 // SQL 부분의 "SELECT source_id FROM ip_jnl_list WHERE end_year = ?";에서 WHERE end_year = ?를 삭제하세요
    // ================================== 여기까지 수정하세요 ==================================

    // SQL
    private static final String SELECT_PROCESSED_IDS_SQL =
            "SELECT source_id FROM ip_jnl_list WHERE end_year = ?";

    private static final String INSERT_LIST_SQL =
            "INSERT INTO ip_jnl_list (source_id, end_year, has_attach, work_div, work_type, title, " +
                    "comment_count, work_box, start_date, end_date, writer_name, receiver_name, " +
                    "status, progress, priority) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            By lblSelector = By.xpath("//li[@data-lblnm='IP솔루션팀']");
            WebElement lblItem = wait.until(ExpectedConditions.elementToBeClickable(lblSelector));
            js.executeScript("arguments[0].click();", lblItem);
            Thread.sleep(1500);
            System.out.println("  > 'IP솔루션팀' 클릭 완료.");

            // 2-1. 날짜 범위 설정 및 조회
            System.out.println("  > 날짜 범위 설정: " + SEARCH_DATE_MIN + " ~ " + SEARCH_DATE_MAX);

            // 시작일 입력 (readonly라서 JavaScript로 값 설정)
            WebElement dateMinInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jnlInfoDateMin")));
            js.executeScript("arguments[0].value = arguments[1];", dateMinInput, SEARCH_DATE_MIN);

            // 종료일 입력
            WebElement dateMaxInput = driver.findElement(By.id("jnlInfoDateMax"));
            js.executeScript("arguments[0].value = arguments[1];", dateMaxInput, SEARCH_DATE_MAX);

            // 조회 버튼 클릭
            WebElement searchBtn = driver.findElement(By.id("jnlInfoListFindDateBtn"));
            js.executeScript("arguments[0].click();", searchBtn);
            Thread.sleep(2000);
            System.out.println("  > 조회 버튼 클릭 완료.");

            // 테이블 로드 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#jnlInfoGrid tbody tr")));
            Thread.sleep(500);

            // 3. DB 재연결 및 크롤링 시작
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("3단계: 목록 크롤링 시작.");

            PreparedStatement listPstmt = conn.prepareStatement(INSERT_LIST_SQL);
            int totalInserted = 0;
            int totalSkipped = 0;
            int errorCount = 0;
            int currentPage = START_PAGE;
            List<Integer> failedPages = new ArrayList<>();

            // 시작 페이지로 이동 (1페이지가 아닌 경우)
            if (START_PAGE > 1) {
                System.out.println("  > " + START_PAGE + " 페이지로 이동 중...");
                for (int p = 1; p < START_PAGE; p++) {
                    if (!goToNextPage(driver, wait, js)) {
                        System.err.println("  > " + START_PAGE + " 페이지 도달 실패");
                        return;
                    }
                    Thread.sleep(1000);
                }
            }

            // 페이지 순회
            boolean hasMorePages = true;
            while (hasMorePages) {
                System.out.println("\n  > [" + currentPage + " 페이지] 처리 중...");

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

                        // 테이블 로드 대기
                        wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("#jnlInfoGrid tbody tr")));
                        Thread.sleep(500);

                        // 현재 페이지의 행들 추출
                        List<WebElement> rows = driver.findElements(
                                By.cssSelector("#jnlInfoGrid tbody tr"));

                        int pageInserted = 0;
                        int pageSkipped = 0;

                        for (WebElement row : rows) {
                            try {
                                List<WebElement> tds = row.findElements(By.tagName("td"));
                                if (tds.size() < 12) continue;

                                // 문서 ID 추출
                                String sourceId = "";
                                try {
                                    WebElement checkbox = tds.get(0).findElement(
                                            By.cssSelector("input.jnlInfoListCheckBox"));
                                    sourceId = checkbox.getAttribute("value");
                                } catch (Exception e) {
                                    continue;
                                }

                                // 중복 체크
                                if (processedIds.contains(sourceId)) {
                                    pageSkipped++;
                                    continue;
                                }

                                // 첨부파일 여부
                                int hasAttach = 0;
                                try {
                                    tds.get(0).findElement(By.cssSelector("span.o-i-fileLink"));
                                    hasAttach = 1;
                                } catch (Exception e) {
                                    hasAttach = 0;
                                }

                                // 구분
                                String workDiv = tds.get(1).getText().trim();

                                // 업무유형
                                String workType = tds.get(2).getText().trim();

                                // 제목
                                String title = "";
                                try {
                                    WebElement titleLink = tds.get(3).findElement(By.cssSelector("a.grdTit"));
                                    title = titleLink.getText().trim();
                                } catch (Exception e) {
                                    title = tds.get(3).getText().trim();
                                }

                                // 댓글 수
                                int commentCount = 0;
                                try {
                                    WebElement commentSpan = tds.get(3).findElement(By.cssSelector("span.F_11_gray"));
                                    String countText = commentSpan.getText().trim();
                                    commentCount = Integer.parseInt(countText);
                                } catch (Exception e) {
                                    commentCount = 0;
                                }

                                // 업무함 (여러 개일 수 있음)
                                String workBox = "";
                                try {
                                    List<WebElement> workBoxSpans = tds.get(4).findElements(By.tagName("span"));
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < workBoxSpans.size(); i++) {
                                        String boxTitle = workBoxSpans.get(i).getAttribute("title");
                                        if (boxTitle != null && !boxTitle.isEmpty()) {
                                            if (sb.length() > 0) sb.append(", ");
                                            sb.append(boxTitle);
                                        }
                                    }
                                    workBox = sb.toString();
                                } catch (Exception e) {
                                    workBox = "";
                                }

                                // 시작일
                                String startDate = tds.get(5).getText().trim();

                                // 종료(예정)일
                                String endDate = tds.get(6).getText().trim();

                                // 작성자
                                String writerName = "";
                                try {
                                    WebElement writerSpan = tds.get(7).findElement(By.cssSelector("span.abcUsr"));
                                    writerName = writerSpan.getText().trim();
                                } catch (Exception e) {
                                    writerName = tds.get(7).getText().trim();
                                }

                                // 수신자
                                String receiverName = "";
                                try {
                                    WebElement receiverSpan = tds.get(8).findElement(By.cssSelector("span.abcUsr"));
                                    receiverName = receiverSpan.getText().trim();
                                } catch (Exception e) {
                                    receiverName = tds.get(8).getText().trim();
                                }

                                // 상태
                                String status = "";
                                try {
                                    WebElement statusSpan = tds.get(9).findElement(By.tagName("span"));
                                    status = statusSpan.getText().trim();
                                } catch (Exception e) {
                                    status = tds.get(9).getText().trim();
                                }

                                // 진행률
                                String progress = tds.get(10).getText().trim();

                                // 우선순위
                                String priority = tds.get(11).getText().trim();

                                // DB 저장
                                listPstmt.setString(1, sourceId);
                                listPstmt.setInt(2, END_YEAR);
                                listPstmt.setInt(3, hasAttach);
                                listPstmt.setString(4, workDiv);
                                listPstmt.setString(5, workType);
                                listPstmt.setString(6, title);
                                listPstmt.setInt(7, commentCount);
                                listPstmt.setString(8, workBox);
                                listPstmt.setString(9, startDate);
                                listPstmt.setString(10, endDate);
                                listPstmt.setString(11, writerName);
                                listPstmt.setString(12, receiverName);
                                listPstmt.setString(13, status);
                                listPstmt.setString(14, progress);
                                listPstmt.setString(15, priority);

                                listPstmt.executeUpdate();
                                processedIds.add(sourceId);  // 중복 방지를 위해 추가
                                pageInserted++;

                            } catch (Exception rowEx) {
                                System.err.println("    > 행 처리 오류: " + rowEx.getMessage());
                            }
                        }

                        conn.commit();
                        totalInserted += pageInserted;
                        totalSkipped += pageSkipped;
                        System.out.println("    > " + pageInserted + "건 저장, " + pageSkipped + "건 스킵 (중복)");
                        success = true;

                    } catch (Exception e) {
                        retryCount++;
                        System.err.println("    > 페이지 처리 오류 (시도 " + retryCount + "): " + e.getMessage());

                        try {
                            conn.rollback();
                        } catch (SQLException rollbackEx) {
                            System.err.println("    > 롤백 실패: " + rollbackEx.getMessage());
                        }

                        // 복구 시도
                        try {
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
                    failedPages.add(currentPage);
                    System.err.println("    > 최종 실패: " + currentPage + " 페이지");
                }

                // 진행 상황 출력 (5페이지마다)
                if (currentPage % 5 == 0) {
                    System.out.println("\n=== 진행 상황: " + currentPage + " 페이지 완료 " +
                            "(총 저장: " + totalInserted + ", 스킵: " + totalSkipped + ", 실패: " + errorCount + ") ===\n");
                }

                // 종료 조건 체크
                if (END_PAGE > 0 && currentPage >= END_PAGE) {
                    System.out.println("  > 설정된 종료 페이지(" + END_PAGE + ")에 도달. 크롤링 종료.");
                    break;
                }

                // 다음 페이지로 이동
                hasMorePages = goToNextPage(driver, wait, js);
                if (hasMorePages) {
                    currentPage++;
                    Thread.sleep(1000);
                }
            }

            System.out.println("\n4단계: 크롤링 완료.");
            System.out.println("  > 총 페이지: " + currentPage);
            System.out.println("  > 총 저장: " + totalInserted);
            System.out.println("  > 총 스킵 (중복): " + totalSkipped);
            System.out.println("  > 실패 페이지: " + errorCount);

            if (!failedPages.isEmpty()) {
                System.out.println("\n  > 실패한 페이지 목록:");
                System.out.println("    " + failedPages);
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

    /**
     * 다음 페이지로 이동
     * @return 다음 페이지가 있으면 true, 마지막 페이지면 false
     */
    private static boolean goToNextPage(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        try {
            // 다음 페이지 버튼 찾기 (k-i-arrow-e 아이콘의 부모 a 태그)
            WebElement nextButton = driver.findElement(
                    By.cssSelector("a.k-pager-nav[aria-label='Go to the next page']"));

            // 비활성화 상태인지 확인
            String classAttr = nextButton.getAttribute("class");
            if (classAttr != null && classAttr.contains("k-state-disabled")) {
                System.out.println("    > 마지막 페이지 도달.");
                return false;
            }

            // 다음 페이지 클릭
            js.executeScript("arguments[0].click();", nextButton);
            Thread.sleep(1500);

            // 테이블 로드 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#jnlInfoGrid tbody tr")));

            return true;

        } catch (Exception e) {
            System.err.println("    > 페이지 이동 오류: " + e.getMessage());
            return false;
        }
    }
}