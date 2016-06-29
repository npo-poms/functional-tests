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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import com.jayway.restassured.RestAssured;


import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@FixMethodOrder
public class ClipTest {

    // TODO: Credentials should not be checked in.
    private static final String BASE_URL = "https://api-dev.poms.omroep.nl";
    private static final String MEDIA_URL = BASE_URL + "/media/media";
    private static final String USERNAME = "vpro-mediatools";
    private static final String PASSWORD = "***REMOVED***";
    private static String suffix;
    private static String randomCridId;
    private static String randomSegmentId;
    private static String randomSegmentId2;

    @BeforeClass
    public static void setUpShared() {
        suffix =  Long.toString(System.currentTimeMillis());
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
        String segmentCrid = "crid://pyapi/segment/" + randomSegmentId;
        String clipCrid = "crid://pyapi/clip/" + randomCridId;
        List<Segment> segments = Collections.singletonList(createSegment(segmentCrid, suffix, null));
        ProgramUpdate clip = ProgramUpdate.create(createClip(clipCrid, suffix, segments));

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

//    @Test
//    public void testRetrieveClip() throws UnsupportedEncodingException {
//        String clipCrid = "crid://pyapi/clip/" + randomCridId;
//        String encodedClipCrid = URLEncoder.encode(clipCrid, "UTF-8");
//        given().
//                auth().
//                basic(USERNAME, PASSWORD).
//                contentType("application/xml").
//                log().all().
//                when().
//                get(MEDIA_URL + "/" + encodedClipCrid).
//                then().
//                log().body().
//                statusCode(200).
//                body("program.title", equalTo("hoi " + suffix));
//    }

    @Test
    public void testPostSegment() throws ModificationException {
        String segmentCrid = "crid://pyapi/segment/" + randomSegmentId2;
        SegmentUpdate segment = SegmentUpdate.create(createSegment(segmentCrid, suffix, "WO_VPRO_1425989"));

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
        Segment s = MediaTestDataBuilder.segment()
                .valid()
                .crids(crid)
                .mid(null)
                .title("hoi (1) " + dynamicSuffix)
                .midRef(midRef)
                .build();

        return s;
    }
}
