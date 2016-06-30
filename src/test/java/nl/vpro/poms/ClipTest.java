package nl.vpro.poms;

import com.jayway.restassured.RestAssured;
import nl.vpro.domain.media.*;
import nl.vpro.domain.media.exceptions.ModificationException;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.SegmentUpdate;
import nl.vpro.domain.user.Broadcaster;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClipTest {

    // TODO: Credentials should not be checked in.
    private static final String BASE_URL = "https://api-dev.poms.omroep.nl";
    private static final String MEDIA_URL = BASE_URL + "/media/media";
    private static final String USERNAME = "vpro-mediatools";
    private static final String PASSWORD = "***REMOVED***";
    private static final String BASE_CRID = "crid://apitests";
    private static final String ERRORS_EMAIL = "d.debie@vpro.nl";
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
        ProgramUpdate clip = ProgramUpdate.create(createClip(null, dynamicSuffix, segments));

        clipMid = given()
                .auth()
                .basic(USERNAME, PASSWORD)
                .contentType("application/xml")
                .body(clip)
                .queryParam("errors", ERRORS_EMAIL)
                .log().all()
        .when()
                .post(MEDIA_URL)
        .then()
                .log().all()
                .statusCode(202)
                .body(startsWith("POMS_VPRO"))
                .extract().asString();
    }

    @Test
    public void test02PostClipWithCrid() throws UnsupportedEncodingException, InterruptedException, ModificationException {
        String clipCrid = clipCrid(cridIdFromSuffix);
        List<Segment> segments = Collections.singletonList(createSegment(null, dynamicSuffix, null));
        ProgramUpdate clip = ProgramUpdate.create(createClip(clipCrid, dynamicSuffix, segments));

        given()
                .auth()
                .basic(USERNAME, PASSWORD)
                .contentType("application/xml")
                .body(clip)
                .queryParam("errors", ERRORS_EMAIL)
                .log().all()
        .when()
                .post(MEDIA_URL)
        .then()
                .log().all()
                .statusCode(202)
                .body(equalTo(clipCrid));
    }

    @Test
    public void test03PostSegment() throws ModificationException {
        SegmentUpdate segment = SegmentUpdate.create(createSegment(null, dynamicSuffix, clipMid));

        given()
                .auth()
                .basic(USERNAME, PASSWORD)
                .contentType("application/xml")
                .body(segment)
                .queryParam("errors", ERRORS_EMAIL)
                .log().all()
        .when()
                .post(MEDIA_URL)
        .then()
                .log().all()
                .statusCode(202)
                .body(startsWith("POMS_VPRO"));
    }

    @Test
    public void test04WaitForProcessing() throws InterruptedException {
        // Wait for posted clips to be processed
        Thread.sleep(30000);
    }

    @Test
    public void test05RetrieveClip() throws UnsupportedEncodingException, InterruptedException {

        given()
                .auth()
                .basic(USERNAME, PASSWORD)
                .contentType("application/xml")
                .log().all()
        .when()
                .get(MEDIA_URL + "/" + clipMid)
        .then()
                .log().all()
                .statusCode(200)
                .body(hasXPath("/program/title[@type='MAIN']/text()", equalTo("hoi " + dynamicSuffix)))
                .body(hasXPath("/program/@deleted", isEmptyOrNullString()));
    }

    @Test
    public void test06RetrieveClipWithCrid() throws UnsupportedEncodingException, InterruptedException {

        String clipCrid = clipCrid(cridIdFromSuffix);
        String encodedClipCrid = URLEncoder.encode(clipCrid, "UTF-8");
        given()
                .auth()
                .basic(USERNAME, PASSWORD)
                .contentType("application/xml")
                .log().all()
        .when()
                .get(MEDIA_URL + "/" + encodedClipCrid)
        .then()
                .log().all()
                .statusCode(200)
                .body(hasXPath("/program/title[@type='MAIN']/text()", equalTo("hoi " + dynamicSuffix)))
                .body(hasXPath("/program/@deleted", isEmptyOrNullString()));
    }

    @Test
    public void test07DeleteClip() throws UnsupportedEncodingException, InterruptedException {
        given()
                .auth()
                .basic(USERNAME, PASSWORD)
                .log().all()
        .when()
                .delete(MEDIA_URL + "/" + clipMid)
        .then()
                .log().all()
                .statusCode(202);

        // Wait for posted clip to be deleted
        Thread.sleep(1000);

        given()
                .auth()
                .basic(USERNAME, PASSWORD)
                .contentType("application/xml")
                .log().all()
        .when()
                .get(MEDIA_URL + "/" + clipMid)
        .then()
                .log().all()
                .statusCode(200)
                .body(hasXPath("/program/@deleted", equalTo("true")));
    }

    private Program createClip(String crid, String dynamicSuffix, List<Segment> segments) throws ModificationException {

        return MediaTestDataBuilder.program()
                .validNew()
                .crids(crid)
                .mid(null)
                .clearBroadcasters()
                .broadcasters("VPRO")
                .type(ProgramType.CLIP)
                .avType(AVType.MIXED)
                .broadcasters(Broadcaster.of("VPRO"))
                .segments(segments.toArray(new Segment[segments.size()]))
                .title("hoi " + dynamicSuffix)
                .build();
    }

    private Segment createSegment(String crid, String dynamicSuffix, String midRef) throws ModificationException {
        return MediaTestDataBuilder.segment()
                .validNew()
                .crids(crid)
                .mid(null)
                .clearBroadcasters()
                .broadcasters("VPRO")
                .title("hoi (1) " + dynamicSuffix)
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
