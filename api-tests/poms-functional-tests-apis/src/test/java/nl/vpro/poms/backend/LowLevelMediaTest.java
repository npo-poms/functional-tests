package nl.vpro.poms.backend;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.extern.log4j.Log4j2;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.media.*;
import nl.vpro.domain.media.search.*;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.SegmentUpdate;
import nl.vpro.domain.subtitles.SubtitlesFormat;
import nl.vpro.junit.extensions.AllowUnavailable;
import nl.vpro.junit.extensions.TestMDC;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.testutils.Utils;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.vpro.api.client.utils.Config.Prefix.npo_backend_api;
import static nl.vpro.domain.Xmlns.NAMESPACE_CONTEXT;
import static nl.vpro.poms.AbstractApiMediaBackendTest.MID;
import static nl.vpro.testutils.Utils.CONFIG;
import static nl.vpro.rs.media.MediaBackendRestService.ERRORS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.Matchers.*;



/*
 * 2018-08-17:
 * 5.9-SNAPSHOT @ dev : allemaal ok
 */

/**
 * Basic tests which do not even use our client (just using rest assured).
 *
 * Sometimes our client is a bit 'too good' making it hard to submit errorneous posts, or making certain choices. And though
 * we don't really expect issues of things that only work if you use our client, it's at least good to test some basic things also without it.
 *
 * @author Daan Debie
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
@ExtendWith({AllowUnavailable.class, TestMDC.class, AbortOnException.class})
public class LowLevelMediaTest {

    private static final Instant NOW = Instant.now();
    private static final LocalDate TODAY = NOW.atZone(Schedule.ZONE_ID).toLocalDate();
    private static final LocalDateTime TIME = NOW.atZone(Schedule.ZONE_ID).toLocalDateTime();
    private static final Duration ACCEPTABLE = Duration.ofMinutes(5);

    private static final String MEDIA_URL = CONFIG.url(npo_backend_api, "media/media");
    private static final String FIND_URL = CONFIG.url(npo_backend_api, "media/find");
    private static final String USERNAME = CONFIG.requiredOption(npo_backend_api, "user");
    private static final String PASSWORD = CONFIG.requiredOption(npo_backend_api, "password");
    private static final String ERRORS_EMAIL;
     static {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("TESTRUN", "" + NOW.toEpochMilli());
        StringSubstitutor subst = new StringSubstitutor(valuesMap);
        ERRORS_EMAIL =  subst.replace(CONFIG.getProperties(Config.Prefix.npo_backend_api).get("errors"));
        log.info("Mailing errors to {}", ERRORS_EMAIL);
    }


    private static final String BASE_CRID = "crid://apitests";
    //private static final String TITLE_PREFIX = LowLevelMediaTest.class.getSimpleName() + " " + TIME + " ";
    private static String dynamicSuffix;
    private static String cridIdFromSuffix;
    private static String clipMid;

    private String title;

    @BeforeEach
    public void setupTitle(TestInfo testInfo) {
        title = TestMDC.getTestNumber() + ":" + dynamicSuffix + " " + testInfo.getDisplayName();
        log.info("Running {} with title {}", testInfo.getTestMethod().map(Method::toString).orElse("<no method?>"), title);
    }

    @BeforeAll
    public static void setUpShared() {
        dynamicSuffix = TIME.toString();
        cridIdFromSuffix = dynamicSuffix.replaceAll("\\D", "");
    }

    @BeforeEach
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
    }

    public static String clipTitle;
    @Test
    @Tag("clip")
    @Tag("clips")
    @Order(1)
    public void postClip() {
        List<Segment> segments = Collections.singletonList(createSegment(null, null));
        ProgramUpdate clip =
            ProgramUpdate.create(
                AbstractApiMediaBackendTest.getBackendVersionNumber(),
                createClip(null, segments));

        clipTitle = clip.getMainTitle();

        clipMid = client(clip)
            .when()
            .  post(MEDIA_URL)
            .then()
            .  log().all()
            .  statusCode(202)
            .  body(anyOf(startsWith("POMS_VPRO"), startsWith("POMS_MOCK_VPRO_"))) // this is silly, we're testing METIS here.
            .  extract().asString();
        log.info("Using {}", clipMid);
    }

    public static String clipWithCridTitle;

    @Test
    @Tag("cridclip")
    @Tag("clips")
    @Order(2)
    public void postClipWithCrid() {

        String clipCrid = clipCrid(cridIdFromSuffix);
        List<Segment> segments = Collections.singletonList(createSegment(null, null));
        ProgramUpdate clip = ProgramUpdate.create(
            AbstractApiMediaBackendTest.getBackendVersionNumber(),
            createClip(clipCrid, segments)
        );
        clipWithCridTitle = clip.getMainTitle();
        log.info("Created clip with crid {}", clipCrid);

       client(clip)
           .when()
            .  post(MEDIA_URL)
            .then()
            .  log().all()
            .  statusCode(202)
            .  body(equalTo(clipCrid));
    }

    static protected String segmentTitle;
    @Test
    @Tag("segment")
    @Order(3)
    public void postSegment() {
        SegmentUpdate segment = SegmentUpdate.create(
            AbstractApiMediaBackendTest.getBackendVersionNumber(),
            createSegment(null, clipMid)
        );
        segmentTitle = segment.getMainTitle();

        client(segment)
            .when()
            .  post(MEDIA_URL)
            .then()
            .  log().all()
            .  statusCode(202)
            .  body(startsWith("POMS_VPRO"));
    }
    @Test
    @Tag("clip")
    @Tag("clips")
    @Order(20)
    public void retrieveClip() {
        assumeThat(clipMid).isNotNull();
        Boolean result = Utils.waitUntil(ACCEPTABLE, () -> {
            try {
                client()
                  .when()
                  .  get(MEDIA_URL + "/" + clipMid)
                  .then()
                  .  log().all()
                  .  statusCode(200)
                  .  body(hasXPath(
                      "/u:program/u:title[@type='MAIN']/text()", NAMESPACE_CONTEXT,
                      equalTo(clipTitle)
                  ))
                  .body(hasXPath("/u:program/@deleted", NAMESPACE_CONTEXT, emptyOrNullString()))
              ;
                return Boolean.TRUE;
            } catch (AssertionError ae) {
                log.info(ae.getMessage());
                return null;
            }
        },
            Utils.Check.<Boolean>builder()
                .predicate(r -> r)
                .description("Getting from api returns 200 and has title "  + clipTitle)
                .build()
        );

        assertThat(result).isTrue();
    }


    @Test
    @Tag("cridclip")
    @Tag("clips")
    @Order(21)
    public void retrieveClipWithCrid() {

        String clipCrid = clipCrid(cridIdFromSuffix);
        log.info("Retrieving clip with crid {}", clipCrid);
        String encodedClipCrid = URLEncoder.encode(clipCrid, UTF_8);
        client()
            .when()
            .  get(MEDIA_URL + "/" + encodedClipCrid)
            .then()
            .  log().all()
            .  statusCode(200)
            .  body(hasXPath("/u:program/u:title[@type='MAIN']/text()",
                NAMESPACE_CONTEXT, equalTo(clipWithCridTitle)))
            .  body(hasXPath("/u:program/@deleted", NAMESPACE_CONTEXT, emptyOrNullString()));
    }

    @Test
    @Tag("clips")
    @Order(22)
    public void findClips() {
        MediaForm search = MediaForm.builder()
            .pager(MediaPager.builder().max(50).build())
            .broadcaster("VPRO")
            .creationRange(
                new InstantRange(TODAY.atStartOfDay(), TODAY.atStartOfDay().plusHours(24))
            )
            .build();

        TitleForm form = new TitleForm(clipTitle, false);
        search.addTitle(form);
        client(search)
            .when()
            .  post(FIND_URL)
            .then()
            .  log().all()
            .  statusCode(200)
            .  body(hasXPath("/s:list/@totalCount", NAMESPACE_CONTEXT, equalTo("2")))

        ;
    }

    @Test
    @Tag("clip")
    @Order(100)
    @AbortOnException.NoAbort
    public void deleteClip() {
        client()
            .when()
            .  delete(MEDIA_URL + "/" + clipMid)
            .then()
            .  log().all()
            .  statusCode(202);


        Utils.waitUntil(ACCEPTABLE, () -> {
            try {
               client()
                    .when()
                    .  get(MEDIA_URL + "/" + clipMid)
                    .then()
                    .  log().all()
                    .  statusCode(200)
                    .  body(hasXPath("/u:program/@deleted", NAMESPACE_CONTEXT, equalTo("true")));
                return true;
            } catch (AssertionError ae) {
                log.info(ae.getMessage());
                return null;
            }
        });
    }


    @Test
    @Order(101)
    @AbortOnException.NoAbort
    public void retrieve404() {
        log.info(client()
            .when()
            .  get(MEDIA_URL + "/BESTAATNIET")
            .then()
            .  log().all()
            .statusCode(404)
            .extract()
            .asString());
    }

    /**
     * See also MSE-4676
     */
    @Test
    @Order(200)
    @AbortOnException.Ignore
    public void streamingStatus() {
        String streamingStatusEndpoint = CONFIG.url(npo_backend_api, "media/streamingstatus");
        String result = client()
            .when()
            .  get(streamingStatusEndpoint + "/" + MID)
            .then()
            .  log().all()
            .statusCode(200)
            .contentType(ContentType.XML)
            .extract()
            .asString();


        StreamingStatus streamingStatus = JAXB.unmarshal(new StringReader(result), StreamingStatusImpl.class);
        log.info("{} -> {}", result, streamingStatus);
        assertThat(streamingStatus).isNotNull();
    }


    private static String subtitlesTitle;

    @Test
    @Order(300)
    @Tag("subtitles")
    public void addSubtitles(TestInfo testInfo) {
        subtitlesTitle = testInfo.getDisplayName() + TIME;
        String subtitles = CONFIG.url(npo_backend_api, "media/subtitles");
        String result = client()
            .contentType(SubtitlesFormat.WEBVTT.getMediaType())
            .body("WEBVTT\n" +
                "X-TIMESTAMP-MAP=MPEGTS:900000,LOCAL:00:00:00.000\n" +
                "\n" +
                "1\n" +
                "00:00:02.200 --> 00:00:04.150\n" +
                "" + subtitlesTitle+ "\n" +
                "\n" +
                "2\n" +
                "00:00:04.200 --> 00:00:08.060\n" +
                "*'k Heb een paar puntjes die ik met je wil bespreken\n" +
                "\n" +
                "3\n" +
                "00:00:08.110 --> 00:00:11.060\n" +
                "*Dat wil ik doen in jouw mobiele bakkerij\n" +
                "\n" +
                "")
            .post(subtitles + "/" + MID + "/fr/TRANSLATION")
            .then()
            .log().all()
            .statusCode(202)
            .contentType(ContentType.TEXT)
            .extract()
            .asString();

        assertThat(result).matches(Pattern.compile("Subtitles \\(3 cues, \\d+ bytes\\) provisionally accepted for WO_VPRO_025057\tTRANSLATION\tfr"));
    }

    @Test
    @Order(301)
    @Tag("subtitles")
    public void checkAddSubtitlesArrived() {
        assumeThat(subtitlesTitle).isNotNull();
        String subtitles = CONFIG.url(npo_backend_api, "media/subtitles");
        Utils.waitUntil(ACCEPTABLE, () -> {
            try {
                client()
                    .when()
                    .  get(subtitles + "/" + MID + "/fr/TRANSLATION?avoidParsing=true")
                    .then()
                    .  log().all()
                    .  statusCode(200)
                    .  body(startsWith("WEBVTT\n" +
                "X-TIMESTAMP-MAP=MPEGTS:900000,LOCAL:00:00:00.000\n" +
                "\n" +
                "1\n" +
                "00:00:02.200 --> 00:00:04.150\n" +
                "" + subtitlesTitle + "\n" ));
                return Boolean.TRUE;
            } catch (AssertionError ae) {
                log.info(ae.getMessage());
                return null;
            }
        });
    }

    private Program createClip(String crid, List<Segment> segments) {

        return MediaTestDataBuilder.program()
            .validNew()
            .crids(crid)
            .clearBroadcasters()
            .broadcasters("VPRO")
            .type(ProgramType.CLIP)
            .segments(segments)
            .ageRating(AgeRating.ALL)
            .title(title)
            .build();
    }



    private Segment createSegment(String crid, String midRef) {
        return MediaTestDataBuilder.segment()
            .validNew()
            .crids(crid)
            .clearBroadcasters()
            .broadcasters("VPRO")
            .title(title)
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

    private RequestSpecification client() {
        return given()
            .urlEncodingEnabled(true)
            .log().all()
            .auth().basic(USERNAME, PASSWORD);
    }
    private RequestSpecification client(Object body) {
        return client()
            .contentType(ContentType.XML)
            .queryParam(ERRORS, ERRORS_EMAIL)
            .body(body);



    }
}
