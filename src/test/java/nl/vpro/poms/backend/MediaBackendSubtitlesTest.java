package nl.vpro.poms.backend;


import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.Duration;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.runners.MethodSorters;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import nl.vpro.api.rs.subtitles.Constants;
import nl.vpro.domain.media.AVType;
import nl.vpro.domain.media.MediaTestDataBuilder;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.subtitles.*;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.rules.DoAfterException;

import static io.restassured.RestAssured.given;
import static nl.vpro.api.client.utils.Config.Prefix.npo_backend_api;
import static nl.vpro.poms.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assume.*;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MediaBackendSubtitlesTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        if (! (t instanceof AssumptionViolatedException)) {
            MediaBackendSubtitlesTest.exception = t;
        }
    });

    private static Throwable exception = null;

    private static String firstTitle;

    @Before
    public void setup() {
        assumeNoException(exception);
    }

    @Test
    public void test01addSubtitles() {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(5.1f));

        firstTitle = title;
        Subtitles subtitles = Subtitles.webvttTranslation(MID, Duration.ofMinutes(2), Locale.CHINESE,
            "WEBVTT\n" +
                "\n" +
                "1\n" +
                "00:00:02.200 --> 00:00:04.150\n" +
                "" + title + "\n" +
                "\n" +
                "2\n" +
                "00:00:04.200 --> 00:00:08.060\n" +
                "*'k Heb een paar puntjes die ik met je wil bespreken\n" +
                "\n" +
                "3\n" +
                "00:00:08.110 --> 00:00:11.060\n" +
                "*Dat wil ik doen in jouw mobiele bakkerij\n" +
                "\n" +
                ""
        );
        backend.setSubtitles(subtitles);
    }



    @Test
    public void test02CheckArrived() {
        assumeNotNull(firstTitle);

        PeekingIterator<StandaloneCue> iterator = waitUntil(ACCEPTABLE_DURATION,
            MID + "/" + Locale.CHINESE + "[0]=" + firstTitle,
            () -> Iterators.peekingIterator(
            SubtitlesUtil.standaloneStream(
                backend.getBackendRestService().getSubtitles(MID,
                Locale.CHINESE, SubtitlesType.TRANSLATION, true), false, false).iterator()
            )
            , (cpi) -> cpi != null && cpi.hasNext() && cpi.peek().getContent().equals(firstTitle));

        assertThat(iterator).hasSize(3);
    }


    @Test
    public void test03WebVttWithNotesWithoutCueNumbers() throws IOException {
        InputStream input = getClass().getResourceAsStream("/POMS_VPRO_4981202.vtt");
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        IOUtils.copy(input, body);
        String result = given()
            .auth()
            .basic(backend.getUserName(), backend.getPassword())
            .contentType(Constants.VTT)
            .body(body.toByteArray())
            .queryParam("errors", backend.getErrors())
            .log().all()
            .when()
            .  post(CONFIG.url(npo_backend_api, "media/subtitles/" + MID + "/ar/TRANSLATION"))
            .then()
            .  log().all()
            .statusCode(202)
            .extract().asString();
        log.info(result);
    }


    @Test
    public void test04CheckArrived() {

        PeekingIterator<StandaloneCue> iterator = waitUntil(ACCEPTABLE_DURATION,
            MID + "/ar has cues" ,
            () -> Iterators.peekingIterator(
                SubtitlesUtil.standaloneStream(
                    backend.getBackendRestService().getSubtitles(MID,
                        new Locale("ar"), SubtitlesType.TRANSLATION, true), false, false).iterator()
            )
            , (cpi) -> cpi != null && cpi.hasNext());

        assertThat(iterator).hasSize(430);
    }


    private static String newMid;

    @Test
    @Ignore("TODO: Known to fail.")
    public void test05CreateSubtitlesForNewClip() {

        ProgramUpdate clip = ProgramUpdate.create(MediaTestDataBuilder.clip()
            .mainTitle(title)
            .broadcasters("VPRO")
            .avType(AVType.VIDEO).build());

        newMid = backend.set(clip);


        log.info("New mid {}", newMid);

        Subtitles subtitles = Subtitles.webvttTranslation(newMid, Duration.ofMinutes(2), new Locale("ar"),
            "WEBVTT\n" +
                "\n" +
                "00:00:02.200 --> 00:00:04.150\n" +
                "" + title + "\n" +
                "\n" +
                "00:00:04.200 --> 00:00:08.060\n" +
                "*'مجلس النواب يريد المزيد من التدقيق في طلبات لجوء المثليين الجنسيين\n" +
                "\n" +
                ""
        );
        backend.setSubtitles(subtitles);
    }


    @Test
    public void test06CreateSubtitlesForNewClipCheckArrived() {
        assumeNotNull(newMid);

        PeekingIterator<StandaloneCue> iterator = waitUntil(ACCEPTABLE_DURATION,
            newMid + "/ar has cues",
            () -> Iterators.peekingIterator(
                SubtitlesUtil.standaloneStream(
                    backend.getBackendRestService().getSubtitles(newMid,
                        new Locale("ar"), SubtitlesType.TRANSLATION, true), false, false).iterator()
            )
            , (cpi) -> cpi != null && cpi.hasNext());

        assertThat(iterator).hasSize(2);
    }

    @Test
    public void test98CleanUp() {
        backend.deleteSubtitles(SubtitlesId.builder().mid(MID).language(new Locale("ar")).type(SubtitlesType.TRANSLATION).build());
        backend.deleteSubtitles(SubtitlesId.builder().mid(MID).language(Locale.CHINESE).type(SubtitlesType.TRANSLATION).build());
        if (newMid != null) {
            backend.delete(newMid);
        }
        //Subtitles subtitles = backend.getBackendRestService().getSubtitles(MID, new Locale("ar"), SubtitlesType.TRANSLATION, null);

        waitUntil(ACCEPTABLE_DURATION,
            MID + " ar subtitles dissappeared",
            () -> backend.getBackendRestService().getSubtitles(MID, new Locale("ar"), SubtitlesType.TRANSLATION, null) == null
        );
        waitUntil(ACCEPTABLE_DURATION,
            MID + " zh subtitles dissappeared",
            () -> backend.getBackendRestService().getSubtitles(MID, Locale.CHINESE, SubtitlesType.TRANSLATION, null) == null
        );
        if (newMid != null) {
            waitUntil(ACCEPTABLE_DURATION,
                newMid + " ar subtitles dissappeared",
                () -> backend.getBackendRestService().getSubtitles(newMid, new Locale("ar"), SubtitlesType.TRANSLATION, null) == null
            );
        }
    }



    @Test
    @Ignore
    public void testForCamielNL() throws IOException {

        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/POW_03372714.vtt"), "UTF-8");
        StringWriter writer = new StringWriter();
        IOUtils.copy(reader, writer);
        reader.close();

        Subtitles subtitles = Subtitles.webvtt("WO_VPRO_11241856", Duration.ofMinutes(0), new Locale("nl"), writer.toString());
        subtitles.setType(SubtitlesType.CAPTION);


        backend.setSubtitles(subtitles);
    }

    @Test
    @Ignore
    public void test99ForCamielAR() throws IOException {

        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/POMS_VPRO_4981202.vtt"), "UTF-8");

        StringWriter writer = new StringWriter();
        IOUtils.copy(reader, writer);
        reader.close();

        Subtitles subtitles = Subtitles.webvttTranslation("POMS_KRO_3852926", Duration.ofMinutes(0), new Locale("ar"), writer.toString());


        Subtitles corrected = Subtitles.from(subtitles.getId(), SubtitlesUtil.fillCueNumber(SubtitlesUtil.parse(subtitles, false)).iterator());


        backend.setSubtitles(corrected);
    }

    @Test
    @Ignore
    public void test99ForCamielNL2() throws IOException {

        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/POMS_VPRO_4959361.vtt"), "UTF-8");

        StringWriter writer = new StringWriter();
        IOUtils.copy(reader, writer);
        reader.close();

        Subtitles subtitles = Subtitles.webvtt("POMS_KRO_3852926", Duration.ofMinutes(0), new Locale("nl"), writer.toString());

        Subtitles corrected = Subtitles.from(subtitles.getId(), SubtitlesUtil.fillCueNumber(SubtitlesUtil.parse(subtitles, false)).iterator());


        backend.setSubtitles(corrected);
    }

    @Test
    @Ignore
    public void test99deleteCaption() {

        backend.deleteSubtitles(SubtitlesId.builder().language(new Locale("ar")).type(SubtitlesType.CAPTION).mid("POMS_KRO_3852926").build());
    }

}
