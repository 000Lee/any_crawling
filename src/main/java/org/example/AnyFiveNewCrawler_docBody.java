package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnyFiveNewCrawler_docBody {

    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/Users/LEEJUHWAN/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe"; //여기를 수정하세요

    private static final String USER_ID = "master";
    private static final String USER_PW = "five4190any#";
    private static final String TARGET_URL = "http://office.anyfive.com";
    private static final String IFRAME_NAME = "content_frame";

    //------------------------------------여기부터 수정하세요
    // MariaDB 연결 정보
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/any_approval";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    // 사용자 입력값
    private static final int END_YEAR = 2015;

    // CSS 파일 경로
    private static final String CSS_FILE_PATH = "C:/Users/LEEJUHWAN/Downloads/2021-01-01~2025-10-31/html/resource/css/apr.doc.print.jstl.css";

    // 이미지 경로 프리픽스
    private static final String IMG_PATH_PREFIX = "/PMS_SITE-U7OI43JLDSMO/approval/approval_2015_new_img";

    //------------------------------------여기까지 수정하세요

    // doc_body가 비어있는 문서 ID 조회 SQL
    private static final String SELECT_EMPTY_DOCBODY_SQL =
            "SELECT source_id FROM new_documents WHERE end_year = ? " +
                    "AND (doc_body IS NULL OR doc_body = '')";

    // doc_body 업데이트 SQL
    private static final String UPDATE_DOCBODY_SQL =
            "UPDATE new_documents SET doc_body = ? WHERE source_id = ? AND end_year = ?";

    // CSS 내용 (프로그램 시작 시 로드)
    private static String CSS_CONTENT = "";

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
            // 0. CSS 파일 로드 및 가공
            System.out.println("0단계: CSS 파일 로드 및 가공.");
            CSS_CONTENT = loadAndProcessCss(CSS_FILE_PATH);
            if (CSS_CONTENT.isEmpty()) {
                System.err.println("CSS 파일 로드 실패. 프로그램을 종료합니다.");
                return;
            }
            System.out.println("  > CSS 파일 로드 완료. 길이: " + CSS_CONTENT.length());

            // 1. DB에서 doc_body가 비어있는 문서 ID 조회
            System.out.println("1단계: doc_body가 비어있는 문서 ID 조회 시작.");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            List<String> documentIds = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_EMPTY_DOCBODY_SQL)) {
                pstmt.setInt(1, END_YEAR);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        documentIds.add(rs.getString("source_id"));
                    }
                }
            }

            if (documentIds.isEmpty()) {
                System.out.println("  > 모든 문서의 doc_body가 이미 처리되었습니다. 크롤링을 종료합니다.");
                return;
            }
            System.out.println("  > 처리할 문서 수: " + documentIds.size());

            // DB 연결 해제 (재연결은 크롤링 시작 전에)
            conn.close();
            conn = null;

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

            // 아무 문서나 하나 클릭해서 상세페이지 진입 (approvalAddition 객체 로드를 위해)
            String firstDocId = documentIds.get(0);
            String clickFunctionCall = String.format("managementDocList.clickGridRow(%s);", firstDocId);
            js.executeScript(clickFunctionCall);
            Thread.sleep(2500);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("apprLineTable")));
            System.out.println("  > 상세페이지 진입 완료 (approvalAddition 객체 로드).");

            // 원래 창 핸들 저장
            String mainWindowHandle = driver.getWindowHandle();

            // 4. DB 재연결 및 크롤링 시작
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            System.out.println("3단계: 인쇄창 크롤링 시작.");

            PreparedStatement updatePstmt = conn.prepareStatement(UPDATE_DOCBODY_SQL);
            int totalUpdated = 0;
            int errorCount = 0;

            for (int idx = 0; idx < documentIds.size(); idx++) {
                String docId = documentIds.get(idx);
                System.out.println("\n  > [" + (idx + 1) + "/" + documentIds.size() + "] 문서 ID: " + docId + " 처리 중...");

                try {
                    // Iframe 전환 확인
                    driver.switchTo().defaultContent();
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(IFRAME_NAME));
                    Thread.sleep(500);

                    // 인쇄창 호출
                    String printFunctionCall = String.format(
                            "approvalAddition.print.showPrint('%s', 'MANAGEMENT');", docId);
                    js.executeScript(printFunctionCall);
                    Thread.sleep(2000);

                    // 새 창으로 전환
                    Set<String> windowHandles = driver.getWindowHandles();
                    String printWindowHandle = null;
                    for (String handle : windowHandles) {
                        if (!handle.equals(mainWindowHandle)) {
                            printWindowHandle = handle;
                            break;
                        }
                    }

                    if (printWindowHandle == null) {
                        System.err.println("    > 인쇄창을 찾을 수 없습니다.");
                        errorCount++;
                        continue;
                    }

                    driver.switchTo().window(printWindowHandle);
                    Thread.sleep(1500);

                    // HTML 추출 및 가공
                    String rawHtml = (String) js.executeScript("return document.documentElement.outerHTML;");
                    String processedHtml = processHtml(rawHtml, docId);

                    // DB UPDATE
                    updatePstmt.setString(1, processedHtml);
                    updatePstmt.setString(2, docId);
                    updatePstmt.setInt(3, END_YEAR);
                    updatePstmt.executeUpdate();
                    conn.commit();
                    totalUpdated++;
                    System.out.println("    > 저장 완료. HTML 길이: " + processedHtml.length());

                    // 인쇄창 닫기
                    driver.close();

                    // 원래 창으로 복귀
                    driver.switchTo().window(mainWindowHandle);

                } catch (Exception e) {
                    errorCount++;
                    System.err.println("    > 오류 발생: " + e.getMessage());
                    e.printStackTrace();

                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        System.err.println("    > 롤백 실패: " + rollbackEx.getMessage());
                    }

                    // 복구 시도: 모든 창 닫고 메인 창으로
                    try {
                        Set<String> handles = driver.getWindowHandles();
                        for (String handle : handles) {
                            if (!handle.equals(mainWindowHandle)) {
                                driver.switchTo().window(handle);
                                driver.close();
                            }
                        }
                        driver.switchTo().window(mainWindowHandle);
                    } catch (Exception recoveryEx) {
                        System.err.println("    > 복구 실패: " + recoveryEx.getMessage());
                    }
                }

                // 10건마다 진행 상황 출력
                if ((idx + 1) % 10 == 0) {
                    System.out.println("\n=== 진행 상황: " + (idx + 1) + "/" + documentIds.size() +
                            " (성공: " + totalUpdated + ", 실패: " + errorCount + ") ===\n");
                }
            }

            System.out.println("\n4단계: 크롤링 완료.");
            System.out.println("  > 총 처리 대상: " + documentIds.size());
            System.out.println("  > DB 업데이트 성공: " + totalUpdated);
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
     * CSS 파일 로드 및 가공
     */
    private static String loadAndProcessCss(String cssFilePath) {
        try {
            String css = new String(Files.readAllBytes(Paths.get(cssFilePath)), Charset.forName("EUC-KR"));

            // 1. 커스텀 CSS 맨 앞에 추가
            String customCss = ".content{width:80% !important;}.content table{width:100% !important;}";

            // 2. 선택자에서 html, body 제거
            // html {overflow-y:scroll;} 전체 규칙 삭제
            css = css.replaceAll("html\\s*\\{[^}]*\\}", "");

            // html, body, span, ... { } 에서 html, body 제거
            css = css.replaceAll("html\\s*,\\s*body\\s*,\\s*", "");
            css = css.replaceAll("html\\s*,\\s*", "");
            css = css.replaceAll("body\\s*,\\s*", "");

            // 3. .content_box 규칙 수정: min-width:850px → 819px, width:99.9% 삭제
            css = css.replaceAll(
                    "\\.content_box\\s*\\{[^}]*\\}",
                    ".content_box{border:1px #000000 solid;margin:5px 0 0 0;word-break:break-all;overflow:hidden;min-width:819px;}"
            );

            // 4. ul, ol 규칙 수정
            css = css.replaceAll(
                    "ul\\s*,\\s*ol\\s*\\{[^}]*\\}",
                    "ul,ol{list-style:none !important;padding-left:0 !important;margin-left:0 !important;}"
            );

            // 5. CSS 압축 (minify)
            css = minifyCss(css);

            // 6. 커스텀 CSS + 원본 CSS
            return customCss + css;

        } catch (IOException e) {
            System.err.println("CSS 파일 읽기 실패: " + e.getMessage());
            return "";
        }
    }

    /**
     * CSS 압축 (minify)
     */
    private static String minifyCss(String css) {
        // 주석 제거
        css = css.replaceAll("/\\*.*?\\*/", "");
        // 줄바꿈 제거
        css = css.replace("\n", "").replace("\r", "");
        // 연속 공백을 하나로
        css = css.replaceAll("\\s+", " ");
        // { } : ; , 앞뒤 공백 제거
        css = css.replaceAll("\\s*\\{\\s*", "{");
        css = css.replaceAll("\\s*\\}\\s*", "}");
        css = css.replaceAll("\\s*:\\s*", ":");
        css = css.replaceAll("\\s*;\\s*", ";");
        css = css.replaceAll("\\s*,\\s*", ",");
        return css.trim();
    }

    /**
     * HTML 가공 (Jsoup 사용)
     */
    private static String processHtml(String html, String docId) {
        // Jsoup으로 파싱
        Document doc = Jsoup.parse(html);

        // 1. div.content 영역 추출
        Element contentDiv = doc.selectFirst("div.content");
        if (contentDiv == null) {
            System.err.println("    > div.content를 찾을 수 없습니다.");
            return "";
        }

        // 2. 불필요 섹션 삭제

        // 2-1. 첨부파일/참조문서 영역 삭제 (id="attachFileArea")
        Element attachFileArea = contentDiv.selectFirst("#attachFileArea");
        if (attachFileArea != null) {
            attachFileArea.remove();
        }

        // 2-2. 결재의견 영역 삭제 (table04_box 중 caption이 "결재의견")
        Elements table04Boxes = contentDiv.select("div.table04_box");
        for (Element box : table04Boxes) {
            Element caption = box.selectFirst("caption");
            if (caption != null && caption.text().contains("결재의견")) {
                box.remove();
            }
        }

        // 2-3. 결재댓글 영역 삭제 (id="cmtArea")
        Element cmtArea = contentDiv.selectFirst("#cmtArea");
        if (cmtArea != null) {
            cmtArea.remove();
        }

        // 2-4. 조회자 행 삭제 (id="hitPwrListTr")
        Element hitPwrListTr = contentDiv.selectFirst("#hitPwrListTr");
        if (hitPwrListTr != null) {
            hitPwrListTr.remove();
        }

        // 3. 이미지 src 변환
        Elements imgTags = contentDiv.select("img");
        int imageIndex = 0;
        for (Element img : imgTags) {
            String newSrc = IMG_PATH_PREFIX + "/apr" + docId + "/" + imageIndex + ".jpg";
            img.attr("src", newSrc);
            imageIndex++;
        }

        // 4. div#middle로 감싸기 + style 태그 추가
        String contentHtml = "<div id=\"middle\" style=\"margin-top:45px;\">" +
                contentDiv.outerHtml() +
                "</div>" +
                "<style>" + CSS_CONTENT + "</style>";

        // 5. HTML 압축 (minify)
        String result = minifyHtml(contentHtml);

        return result;
    }

    /**
     * HTML 압축 (minify)
     */
    private static String minifyHtml(String html) {
        // 1. HTML 주석 제거
        html = html.replaceAll("<!--.*?-->", "");
        // 2. 태그 사이 공백 제거
        html = html.replaceAll(">\\s+<", "><");
        // 3. 연속 공백을 하나로
        html = html.replaceAll("\\s+", " ");
        // 4. 줄바꿈 제거
        html = html.replace("\n", "").replace("\r", "");

        return html.trim();
    }
}