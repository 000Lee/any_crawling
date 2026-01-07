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

public class AnyFivePjtJnlCrawler {

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
            "3635698","3635506","3634208","3629511","3629369","3628766","3628252","3623484","3623341","3622421","3617957","3617443","3617281","3616939","3616787","3612902","3611882","3611180","3610993","3610904","3606891","3606308","3605921","3604863","3599993","3599698","3599530","3598930","3598809","3594964","3593027","3593013","3592977","3592631","3588164","3587748","3587198","3586732","3586673","3582393","3582179","3581000","3576264","3576242","3575419","3575219","3570759","3570595","3570379","3569192","3569109","3564680","3563751","3563628","3563602","3559520","3559281","3558482","3558198","3554376","3554292","3554136","3549249","3548314","3544793","3544093","3543731","3538950","3538471","3538445","3538352","3537189","3534017","3533282","3533174","3528565","3528438","3528059","3528041","3527606","3522488","3522477","3522234","3522105","3518061","3516990","3516717","3516690","3516567","3511471","3511166","3511106","3510949","3505523","3505370","3505275","3505220","3499650","3499446","3499299","3499294","3495010","3494415","3493704","3493589","3493563","3493371","3493369","3489594","3488699","3488669","3488661","3488475","3484782","3483738","3483734","3483731","3483562","3483374","3478951","3477933","3477855","3477744","3477699","3476717","3473899","3472958","3472422","3471839","3471741","3471481","3466447","3466411","3465793","3465674","3465663","3462308","3462288","3462216","3462049","3462042","3461008","3459226","3458336","3458039","3457410","3457326","3456982","3452530","3452227","3452215","3451306","3451120","3450962","3446620","3445657","3445534","3445522","3444462","3441680","3440551","3440482","3439824","3439798","3439747","3435906","3434540","3433843","3433787","3432762","3429949","3429163","3428832","3427782","3423327","3422509","3422369","3422325","3417836","3417474","3417472","3416462","3416375","3413826","3412796","3411506","3407129","3406232","3406144","3406026","3402417","3402160","3400517","3400197","3400148","3395761","3395330","3394693","3394464","3388958","3388092","3387525","3387347","3386494","3382768","3382593","3382089","3381481","3381409","3380858","3377308","3376306","3376158","3376112","3376102","3376088","3370417","3370128","3366361","3366269","3365976","3362636","3361857","3361446","3361409","3356197","3355979","3355869","3354914","3351317","3350496","3350406","3349043","3345679","3344857","3344633","3344616","3343565","3339764","3339069","3339052","3338702","3337759","3334280","3334120","3333459","3333333","3333162","3332210","3328775","3328400","3327883","3327842","3327787","3327698","3326405","3322754","3322675","3322103","3322072","3321893","3320811","3317276","3316362","3316336","3316055","3316041","3315028","3311433","3311390","3311251","3311236","3310264","3309099","3306163","3305486","3304523","3304518","3304369","3304214","3301530","3300965","3300938","3300922","3300876","3299890","3297049","3296921","3295738","3295652","3294680","3292027","3291254","3289966","3288623","3288428","3287256","3287102","3286593","3286539","3282720","3282596","3282390","3281560","3281188","3280548","3276899","3276840","3275995","3275919","3275051","3275017","3272050","3270948","3270891","3270857","3270757","3270046","3267012","3266007","3265954","3264999","3262336","3261413","3261247","3260997","3260920","3256798","3256743","3255764","3255400","3254904","3251448","3251075","3250979","3250110","3249762","3249264","3245130","3245104","3244929","3244782","3244086","3243179","3239438","3239091","3238314","3238125","3237818","3237099","3233414","3232204","3232181","3231756","3231656","3231612","3226912","3226610","3225909","3225528","3225443","3225371","3220772","3220511","3219579","3219216","3219202","3217369","3214383","3214353","3213604","3213325","3212403","3212289","3208496","3208402","3207475","3207315","3206332","3203487","3202996","3202155","3201913","3201537","3201308","3197373","3197306","3196487","3195937","3195935","3194149","3191515","3191437","3190502","3189987","3189269","3189240","3186366","3186312","3185366","3185251","3185206","3184771","3181311","3181071","3181033","3180450","3179868","3179853","3179746","3176135","3176104","3176072","3175235","3174933","3174896","3173797","3170160","3170141","3170127","3170025","3169945","3168813","3168575","3165729","3164034","3163934","3163910","3163743","3162784","3162522","3159243","3159242","3159075","3158858","3157908","3157814","3156787","3152785","3152727","3151598","3151436","3150728","3147672","3146736","3146545","3146506","3145557","3145469","3145262","3141665","3140752","3140544","3140388","3139162","3138985","3138478","3135613","3134762","3134747","3133297","3133166","3132504","3132456","3128678","3128526","3128467","3128372","3128024","3128002","3123632","3123571","3122502","3122496","3122164","3121436","3117629","3117404","3117362","3117156","3117129","3116150","3115368","3113674","3113622","3113464","3113021","3112912","3112780","3112057","3109390","3108519","3108514","3108331","3107953","3107075","3106966","3106069","3103470","3102157","3101802","3100984","3100924","3100768","3100733","3099902","3097001","3096290","3096042","3095959","3094779","3094664","3094505","3093618","3091122","3089603","3089479","3089435","3089405","3089246","3089214","3088123","3084323","3083570","3083472","3083416","3083392","3083025","3082106","3082098","3082067","3079456","3078858","3077632","3077298","3076173","3073723","3072564","3072509","3072364","3068865","3068795","3067962","3067678","3066797","3066637","3062911","3062804","3060720","3060698","3060648","3060247","3056826","3055723","3054529","3054186","3053656","3050838","3050085","3049216","3048541","3048450","3047643","3044746","3043919","3042457","3042391","3042217","3038347","3037678","3037651","3037645","3036241","3035757","3035372","3032395","3032227","3032217","3031562","3031483","3030263","3030192","3029790","3029247","3026392","3025685","3024180","3024005","3023297","3023196","3018241","3018070","3018054","3017856","3017819","3017692","3016965","3014097","3013103","3013036","3012753","3011908","3011893","3011709","3010950","3009428","3008667","3008542","3008514","3007318","3007286","3007183","3007043","3007013","3005852","3005279","3005043","3004640","3001167","3001152","3000216","3000214","2999820","2999799","2999216","2995555","2995357","2995227","2995215","2994241","2994136","2993919","2993722","2993068","2989749","2989384","2989082","2989052","2988566","2987902","2987881","2987878","2987587","2987508","2986791","2982792","2982755","2982482","2981792","2981790","2981734","2981677","2981663","2981656","2977547","2977529","2977371","2977288","2977222","2977202","2976376","2976317","2976313","2972148","2972033","2971862","2971649","2971096","2971010","2971003","2970947","2968403","2968273","2967725","2966731","2966722","2966708","2966673","2966466","2961935","2960876","2960767","2960687","2960468","2956770","2956414","2954936","2954898","2954890","2954737","2954646","2949917","2949878","2949677","2949460","2948497","2948446","2948442","2944735","2943522","2943520","2942496","2942467","2937261","2936183","2936181","2929361","2929360","2923054","2923051","2916380","2916278","2910368","2909833","2907478","2905262","2905072","2899699","2899386","2893308","2892896","2886897","2886895","2881659","2881364","2876573","2876304","2870537","2870389","2864487","2864480","2859531","2859514","2852828","2852750","2845888","2841765","2835588","2834596","2833884","2829333","2828169","2822853","2821998","2817799","2815825","2809707","2805668","2803691","2797558","2796060","2795127","2790390","2790322","2788299","2786695","2784866","2784406","2782239","2780029","2778360","2775611","2773059","2772320","2771030","2767910","2766154","2765292","2762142","2761533","2761448","2760462","2759562","2756059","2755743","2755706","2755617","2753616","2749913","2749850","2749802","2749798","2747748","2747691","2744524","2744142","2744062","2743341","2743202","2738521","2738443","2738384","2738378","2736459","2736357","2732844","2732629","2732609","2731407","2726165","2725774","2725719","2725404","2724743","2720080","2719977","2718999","2715923","2715353","2714585","2713868","2713298","2713111","2710563","2710226","2709221","2708552","2708410","2708376","2708288","2705700","2705582","2705549","2704523","2704512","2704260","2703363","2699647","2699593","2699446","2698558","2698466","2697430","2696643","2694357","2693676","2693276","2693027","2692499","2692451","2692337","2689785","2689187","2689099","2688294","2688293","2685721","2685715","2684748","2684274","2683581","2682464","2682462","2679799","2679756","2679741","2678607","2677708","2677678","2677675","2673903","2673881","2672931","2672667","2672139","2671059","2671057","2669792","2669758","2668782","2668062","2668038","2667887","2665007","2664348","2663450","2663240","2663234","2662661","2662524","2662517","2660880","2660545","2660013","2658643","2658626","2658321","2657639","2657630","2652991","2652972","2652475","2651681","2651679","2648977","2648898","2648371","2647856","2647850","2646802","2646798","2646791","2645880","2642934","2642908","2641834","2640761","2640309","2637287","2636836","2635299","2635240","2634117","2630847","2630789","2630680","2630678","2627575","2627545","2627534","2624552","2623545","2622562","2622062","2621371","2621369","2618367","2618363","2618294","2617840","2616245","2616061","2615170","2615165","2613280","2613249","2613187","2613134","2612898","2611107","2609699","2609675","2608229","2608190","2607561","2606162","2606111","2606079","2606075","2601901","2601846","2600544","2599732","2599500","2595199","2594921","2594704","2593798","2590298","2589902","2589214","2588678","2587907","2585204","2585152","2584203","2584196","2582801","2579121","2579066","2578298","2577916","2577017","2573479","2573086","2572119","2572109","2570945","2567144","2567105","2566500","2566170","2564944","2560064","2559898","2558779","2554209","2554208","2553013","2549154","2548304","2547682","2547161","2543129","2542725","2541017","2535888","2535845","2531917","2530994","2499413","2493125","2474161","2468346","2468210","2462138","2460742","2455553","2454292","2448678","2448087","2442361","2441854","2435423","2430586","2428895","2428664","2425008","2423513","2423380","2421616","2418676","2417045","2416681","2416489","2412693","2412008","2410991","2410869","2407818","2406255","2405511","2405221","2405144","2402882","2399079","2396125","2395947","2395205","2392667","2390328","2386171","2386085","2385152","2383954","2383785","2383746","2378032","2377079","2375609","2375494","2374830","2369495","2368678","2368316","2367315","2366863","2366525","2361388","2359846","2358854","2358629","2358276","2358176","2354428","2352782","2352358","2352300","2351958","2351017","2350501","2347653","2346755","2346676","2346430","2345631","2345235","2345203","2345199","2341449","2339978","2339958","2339339","2339098","2339062","2338925","2338789","2338759","2334956","2334078","2333631","2333614","2332282","2332271","2332176","2332099","2331651","2331515","2326742","2325904","2325263","2325258","2324505","2324126","2324050","2323398","2323322","2323061","2318172","2318012","2317377","2316728","2316559","2316386","2316027","2315739","2314840","2314760","2309806","2309551","2308603","2307990","2307856","2307629","2307197","2306573","2306563","2306373","2302129","2301862","2301861","2301860","2301256","2301255","2300481","2298973","2298834","2298670","2298630","2293056","2293038","2292799","2291596","2291591","2291543","2291447","2284142"
            // 여기에 문서 ID를 붙여넣으세요
    };
    // ------------------------------------ 여기까지 수정하세요

    // 콘텐츠 로딩 대기 설정
    private static final int STABLE_CHECK_INTERVAL_MS = 500;
    private static final int STABLE_COUNT_REQUIRED = 3;
    private static final int MAX_WAIT_TIME_MS = 30000;

    // SQL - 테이블명 변경: pjt_jnl_documents, pjt_jnl_comments
    private static final String SELECT_PROCESSED_IDS_SQL =
            "SELECT source_id FROM pjt_jnl_documents WHERE end_year = ?";

    private static final String INSERT_DOC_SQL =
            "INSERT INTO pjt_jnl_documents (source_id, end_year, title, work_box, work_type, writer_name, " +
                    "report_date, work_date, attaches, doc_body) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_COMMENT_SQL =
            "INSERT INTO pjt_jnl_comments (post_source_id, end_year, writer_name, created_at, content) " +
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

            // ★★★ 변경: PJT진척보고 선택 ★★★
            By lblSelector = By.xpath("//li[@data-lblnm='PJT진척보고']");
            WebElement lblItem = wait.until(ExpectedConditions.elementToBeClickable(lblSelector));
            js.executeScript("arguments[0].click();", lblItem);
            Thread.sleep(1500);
            System.out.println("  > 'PJT진척보고' 클릭 완료.");

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

            // 이미지 경로 변환 - pjt_jnl_img_연도 폴더 사용
            Pattern imgPattern = Pattern.compile("<img([^>]*)src=\"[^\"]+\"([^>]*)>");
            Matcher matcher = imgPattern.matcher(html);

            StringBuffer sb = new StringBuffer();
            int imgIndex = 0;
            while (matcher.find()) {
                String before = matcher.group(1);
                String after = matcher.group(2);
                String newPath = DB_PATH_PREFIX + "/pjt_jnl_img_" + endYear + "/jnl" + docId + "/" + imgIndex + ".jpg";
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
