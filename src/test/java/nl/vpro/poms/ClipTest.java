package nl.vpro.poms;

import com.jayway.restassured.RestAssured;
import nl.vpro.domain.media.*;
import nl.vpro.domain.media.exceptions.ModificationException;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.SegmentUpdate;
import nl.vpro.domain.user.Broadcaster;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@FixMethodOrder
public class ClipTest {

    // TODO: Credentials should not be checked in.
    private static final String BASE_URL = "https://api-dev.poms.omroep.nl";
    private static final String MEDIA_URL = BASE_URL + "/media/media";
    private static final String USERNAME = "vpro-mediatools";
    private static final String PASSWORD = "***REMOVED***";
    private static final String BASE_CRID = "crid://apitests";
    private static String dynamicSuffix;
    private static String randomCridId;
    private static String randomSegmentId;
    private static String randomSegmentId2;

    @BeforeClass
    public static void setUpShared() {
        dynamicSuffix =  Long.toString(System.currentTimeMillis());
        randomCridId = UUID.randomUUID().toString();
        randomSegmentId = UUID.randomUUID().toString();
        randomSegmentId2 = UUID.randomUUID().toString();
    }

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
    }

    @Test
    public void testPostClip() throws UnsupportedEncodingException, InterruptedException, ModificationException {
        String segmentCrid = BASE_CRID + "/segment/" + randomSegmentId;
        String clipCrid = BASE_CRID + "/clip/" + randomCridId;
        List<Segment> segments = Collections.singletonList(createSegment(segmentCrid, dynamicSuffix, null));
        ProgramUpdate clip = ProgramUpdate.create(createClip(clipCrid, dynamicSuffix, segments));

        given().
                auth().
                basic(USERNAME, PASSWORD).
                contentType("application/xml").
                body(clip).
                log().all().
        when().
                post(MEDIA_URL).
        then().
                log().all().
                statusCode(202).
                body(equalTo(clipCrid));
    }

    @Test
    public void testRetrieveClip() throws UnsupportedEncodingException {
        String clipCrid = BASE_CRID + "/clip/" + randomCridId;
        String encodedClipCrid = URLEncoder.encode(clipCrid, "UTF-8");
        given().
                auth().
                basic(USERNAME, PASSWORD).
                contentType("application/xml").
                log().all().
                when().
                get(MEDIA_URL + "/" + encodedClipCrid).
                then().
                log().body().
                statusCode(200).
                body("program.title", equalTo("hoi " + dynamicSuffix));
    }

    @Test
    public void testPostSegment() throws ModificationException {
        String segmentCrid = BASE_CRID + "/segment/" + randomSegmentId2;
        SegmentUpdate segment = SegmentUpdate.create(createSegment(segmentCrid, dynamicSuffix, "WO_VPRO_1425989"));

        given().
                auth().
                basic(USERNAME, PASSWORD).
                contentType("application/xml").
                body(segment).
                log().all().
        when().
                post(MEDIA_URL).
        then().
                log().all().
                statusCode(202).
                body(equalTo(segmentCrid));
    }

    private Program createClip(String crid, String dynamicSuffix, List<Segment> segments) throws ModificationException {

        return MediaTestDataBuilder.program()
                .valid()
                .crids(crid)
                .mid(null)
                .type(ProgramType.CLIP)
                .avType(AVType.MIXED)
                .broadcasters(Broadcaster.of("VPRO"))
                .segments(segments.toArray(new Segment[segments.size()]))
                .title("hoi " + dynamicSuffix)
                .build();
    }

    private Segment createSegment(String crid, String dynamicSuffix, String midRef) throws ModificationException {
        return MediaTestDataBuilder.segment()
                .valid()
                .crids(crid)
                .mid(null)
                .title("hoi (1) " + dynamicSuffix)
                .midRef(midRef)
                .build();
    }
}
