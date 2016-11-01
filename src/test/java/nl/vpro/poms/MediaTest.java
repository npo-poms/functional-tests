package nl.vpro.poms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
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
import nl.vpro.domain.media.search.Pager;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.SegmentUpdate;

import static com.jayway.restassured.RestAssured.given;
import static nl.vpro.poms.Config.configOption;
import static nl.vpro.poms.Config.requiredOption;
import static nl.vpro.poms.Config.url;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeNotNull;


/**
 * Basic test which do not even use our client.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MediaTest {

    private static final String MEDIA_URL = url("backendapi.url", "media/media");
    private static final String FIND_URL = url("backendapi.url", "media/find");
    private static final String USERNAME = configOption("backendapi.username").orElse("vpro-mediatools");
    private static final String PASSWORD = requiredOption("backendapi.password");
    private static final String ERRORS_EMAIL = configOption("errors.email").orElse("digitaal-techniek@vpro.nl");
    private static final String BASE_CRID = "crid://apitests";
    private static final String TITLE_PREFIX = "API FUNCTIONAL TEST ";
    private static String dynamicSuffix;
    private static String cridIdFromSuffix;
    private static String clipMid;

    @BeforeClass
    public static void setUpShared() {
        dynamicSuffix = LocalDate.now().toString();
        cridIdFromSuffix = dynamicSuffix.replaceAll("\\D", "");
    }

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
    }

    @Test
    public void test01PostClip() throws UnsupportedEncodingException, InterruptedException, ModificationException {
        List<Segment> segments = Collections.singletonList(createSegment(null, dynamicSuffix, null));
        ProgramUpdate clip = ProgramUpdate.create(createClipWithAgeRating(null, dynamicSuffix, segments));

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
    public void test02PostClipWithCrid() throws UnsupportedEncodingException, InterruptedException, ModificationException {
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
        Thread.sleep(30000);
    }

    @Test
    public void test05RetrieveClip() throws UnsupportedEncodingException, InterruptedException {
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
    public void test06RetrieveClipWithCrid() throws UnsupportedEncodingException, InterruptedException {

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
    public void test07FindClips() throws UnsupportedEncodingException, InterruptedException {

        MediaForm search = new MediaForm(new Pager(50), TITLE_PREFIX + dynamicSuffix);
        search.setBroadcasters(Collections.singletonList("VPRO"));
        search.setCreationRange(new DateRange(LocalDate.now().atStartOfDay(), LocalDate.now().atStartOfDay().plusHours(24)));
        given()
            .auth().basic(USERNAME, PASSWORD)
            .contentType(ContentType.XML)
            .body(search)
            .log().all()
            .when()
            .  post(FIND_URL)
            .then()
            .  log().all()
            .  statusCode(200);
    }

    @Test
    public void test08DeleteClip() throws UnsupportedEncodingException, InterruptedException {
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

    private Program createClip(String crid, String dynamicSuffix, List<Segment> segments) throws ModificationException {

        return MediaTestDataBuilder.program()
            .validNew()
            .crids(crid)
            .clearBroadcasters()
            .broadcasters("VPRO")
            .type(ProgramType.CLIP)
            .segments(segments)
            .title(TITLE_PREFIX + dynamicSuffix)
            .build();
    }

    private Program createClipWithAgeRating(String crid, String dynamicSuffix, List<Segment> segments) throws ModificationException {

        return MediaTestDataBuilder.program()
                .validNew()
                .crids(crid)
                .clearBroadcasters()
                .broadcasters("VPRO")
                .type(ProgramType.CLIP)
                .segments(segments)
                .title(TITLE_PREFIX + dynamicSuffix)
                .ageRating(AgeRating._12)
                .contentRatings(ContentRating.ANGST, ContentRating.DRUGS_EN_ALCOHOL)
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
            .build();
    }

    private String crid(String type, String dynamicSuffix) {
        return BASE_CRID + "/" + type + "/" + dynamicSuffix;
    }

    private String clipCrid(String dynamicSuffix) {
        return crid("clip", dynamicSuffix);
    }
}
