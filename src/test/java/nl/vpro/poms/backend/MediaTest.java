package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import nl.vpro.domain.media.*;
import nl.vpro.domain.media.exceptions.ModificationException;
import nl.vpro.domain.media.search.DateRange;
import nl.vpro.domain.media.search.MediaForm;
import nl.vpro.domain.media.search.MediaPager;
import nl.vpro.domain.media.search.TitleForm;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.SegmentUpdate;
import nl.vpro.rules.AllowUnavailable;
import nl.vpro.rules.TestMDC;

import static com.jayway.restassured.RestAssured.given;
import static nl.vpro.poms.AbstractApiTest.CONFIG;
import static nl.vpro.api.client.utils.Config.Prefix.backend_api;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeNotNull;


/**
 * Basic tests which do not even use our client.
 *
 * @author Daan Debie
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MediaTest {

    @Rule
    public AllowUnavailable allowUnavailable = new AllowUnavailable();

    @Rule
    public TestMDC testMDC = new TestMDC();

    private static final String MEDIA_URL = CONFIG.url(backend_api, "media/media");
    private static final String FIND_URL = CONFIG.url(backend_api, "media/find");
    private static final String USERNAME = CONFIG.configOption(backend_api, "username").orElse("vpro-mediatools");
    private static final String PASSWORD = CONFIG.requiredOption(backend_api, "password");
    private static final String ERRORS_EMAIL = CONFIG.configOption(backend_api, "errors_email").orElse("digitaal-techniek@vpro.nl");
    private static final String BASE_CRID = "crid://apitests";
    private static final String TITLE_PREFIX = "API_FUNCTIONAL_TEST_";
    private static String dynamicSuffix;
    private static String cridIdFromSuffix;
    private static String clipMid;

    @BeforeClass
    public static void setUpShared() {
        dynamicSuffix = LocalDateTime.now().toString();
        cridIdFromSuffix = dynamicSuffix.replaceAll("\\D", "");
    }

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
    }

    @Test
    public void test01PostClip() throws ModificationException {
        List<Segment> segments = Collections.singletonList(createSegment(null, dynamicSuffix, null));
        ProgramUpdate clip =
            ProgramUpdate.create(
                createClip(null, dynamicSuffix, segments));

        clipMid = given()
            .auth().basic(USERNAME, PASSWORD)
            .contentType(ContentType.XML)
            .body(clip)
            .queryParam("errors", ERRORS_EMAIL)
            .log().all()
            .when()
            .  post(MEDIA_URL)
            .then()
            .  log().all()
            .  statusCode(202)
            .  body(startsWith("POMS_VPRO"))
            .  extract().asString();
    }

    @Test
    public void test02PostClipWithCrid() throws ModificationException {
        String clipCrid = clipCrid(cridIdFromSuffix);
        List<Segment> segments = Collections.singletonList(createSegment(null, dynamicSuffix, null));
        ProgramUpdate clip = ProgramUpdate.create(createClip(clipCrid, dynamicSuffix, segments));

        given()
            .auth().basic(USERNAME, PASSWORD)
            .contentType(ContentType.XML)
            .body(clip)
            .queryParam("errors", ERRORS_EMAIL)
            .log().all()
            .when()
            .  post(MEDIA_URL)
            .then()
            .  log().all()
            .  statusCode(202)
            .  body(equalTo(clipCrid));
    }

    @Test
    public void test03PostSegment() throws ModificationException {
        SegmentUpdate segment = SegmentUpdate.create(createSegment(null, dynamicSuffix, clipMid));

        given()
            .auth().basic(USERNAME, PASSWORD)
            .contentType(ContentType.XML)
            .body(segment)
            .queryParam("errors", ERRORS_EMAIL)
            .log().all()
            .when()
            .  post(MEDIA_URL)
            .then()
            .  log().all()
            .  statusCode(202)
            .  body(startsWith("POMS_VPRO"));
    }

    @Test
    public void test04WaitForProcessing() throws InterruptedException {
        // Wait for posted clips to be processed
        Thread.sleep(60000);
    }

    @Test
    public void test05RetrieveClip() {
        assumeNotNull(clipMid);
        given()
            .auth().basic(USERNAME, PASSWORD)
            .contentType("application/xml")
            .queryParam("errors", ERRORS_EMAIL)
            .log().all()
            .when()
            .  get(MEDIA_URL + "/" + clipMid)
            .then()
            .  log().all()
            .  statusCode(200)
            .  body(hasXPath("/program/title[@type='MAIN']/text()", equalTo(TITLE_PREFIX + dynamicSuffix)))
            .  body(hasXPath("/program/@deleted", isEmptyOrNullString()));
    }


    @Test
    public void test06RetrieveClipWithCrid() throws UnsupportedEncodingException {

        String clipCrid = clipCrid(cridIdFromSuffix);
        String encodedClipCrid = URLEncoder.encode(clipCrid, "UTF-8");
        given()
            .auth().basic(USERNAME, PASSWORD)
            .contentType("application/xml")
            .queryParam("errors", ERRORS_EMAIL)
            .log().all()
            .when()
            .  get(MEDIA_URL + "/" + encodedClipCrid)
            .then()
            .  log().all()
            .  statusCode(200)
            .  body(hasXPath("/program/title[@type='MAIN']/text()", equalTo(TITLE_PREFIX + dynamicSuffix)))
            .  body(hasXPath("/program/@deleted", isEmptyOrNullString()));
    }

    @Test
    public void test07FindClips() {

        MediaForm search = MediaForm.builder()
            .pager(MediaPager.builder().max(50).build())
            .broadcaster("VPRO")
            .creationRange(new DateRange(LocalDate.now().atStartOfDay(), LocalDate.now().atStartOfDay().plusHours(24)))
            .build();

        TitleForm form = new TitleForm(TITLE_PREFIX + dynamicSuffix, false);
        search.addTitle(form);
        given()
            .auth().basic(USERNAME, PASSWORD)
            .contentType(ContentType.XML)
            .body(search)
            .log().all()
            .when()
            .  post(FIND_URL)
            .then()
            .  log().all()
            .  statusCode(200)
            .  body(hasXPath("/list/@totalCount", equalTo("2")))

        ;
    }

    @Test
    public void test08DeleteClip() throws InterruptedException {
        given()
            .auth().basic(USERNAME, PASSWORD)
            .queryParam("errors", ERRORS_EMAIL)
            .log().all()
            .when()
            .  delete(MEDIA_URL + "/" + clipMid)
            .then()
            .  log().all()
            .  statusCode(202);

        // Wait for posted clip to be deleted
        Thread.sleep(1000);

        given()
            .auth()
            .basic(USERNAME, PASSWORD)
            .contentType(ContentType.XML)
            .queryParam("errors", ERRORS_EMAIL)
            .log().all()
            .when()
            .  get(MEDIA_URL + "/" + clipMid)
            .then()
            .  log().all()
            .  statusCode(200)
            .  body(hasXPath("/program/@deleted", equalTo("true")));
    }


    @Test
    public void test10Retrieve404() {
        log.info(given()
            .auth().basic(USERNAME, PASSWORD)
            .log().all()
            .when()
            .  get(MEDIA_URL + "/BESTAATNIET")
            .then()
            .  log().all()
            .statusCode(404)
            .extract()
            .asString());
    }


    private Program createClip(String crid, String dynamicSuffix, List<Segment> segments) throws ModificationException {

        return MediaTestDataBuilder.program()
            .validNew()
            .crids(crid)
            .clearBroadcasters()
            .broadcasters("VPRO")
            .type(ProgramType.CLIP)
            .segments(segments)
            .ageRating(AgeRating.ALL)
            .title(TITLE_PREFIX + dynamicSuffix)
            .build();
    }



    private Segment createSegment(String crid, String dynamicSuffix, String midRef) throws ModificationException {
        return MediaTestDataBuilder.segment()
            .validNew()
            .crids(crid)
            .clearBroadcasters()
            .broadcasters("VPRO")
            .title(TITLE_PREFIX + "(1) " + dynamicSuffix)
            .midRef(midRef)
            .ageRating(AgeRating.ALL)
            .build();
    }

    private String crid(String type, String dynamicSuffix) {
        return BASE_CRID + "/" + type + "/" + dynamicSuffix;
    }

    private String clipCrid(String dynamicSuffix) {
        return crid("clip", dynamicSuffix);
    }
}
