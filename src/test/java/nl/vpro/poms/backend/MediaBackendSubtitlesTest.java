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
import nl.vpro.domain.subtitles.*;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.DoAfterException;

import static com.jayway.restassured.RestAssured.given;
import static nl.vpro.poms.Config.Prefix.backendapi;
import static nl.vpro.poms.Config.url;
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

    private static final String MID = "WO_VPRO_025057";
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
    public void test02CheckArrived() throws Exception {
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
    public void test03WebVttWithNotesWithoutCues() throws IOException {
        InputStream input = getClass().getResourceAsStream("/POMS_VPRO_4981202.vtt");
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        IOUtils.copy(input, body);
        String result = given()
            .auth()
            .basic(backend.getUserName(), backend.getPassword())
            .contentType(Constants.VTT)
            .content(body.toByteArray())
            .queryParam("errors", backend.getErrors())
            .log().all()
            .when()
            .  post(url(backendapi, "media/subtitles/" + MID + "/ar/TRANSLATION"))
            .then()
            .  log().all()
            .statusCode(202)
            .extract().asString();
        log.info(result);
    }


    @Test
    public void test04CheckArrived() throws Exception {

        PeekingIterator<StandaloneCue> iterator = waitUntil(ACCEPTABLE_DURATION,
            MID + "/ar has cues" ,
            () -> Iterators.peekingIterator(
                SubtitlesUtil.standaloneStream(
                    backend.getBackendRestService().getSubtitles(MID,
                        new Locale("ar"), SubtitlesType.TRANSLATION, true), false, false).iterator()
            )
            , (cpi) -> cpi != null && cpi.hasNext() && cpi.peek().getContent().equals(firstTitle));

        assertThat(iterator).hasSize(430);
    }


    @Test
    public void test99CleanUp() throws Exception {
        backend.deleteSubtitles(SubtitlesId.builder().mid(MID).language(new Locale("ar")).type(SubtitlesType.TRANSLATION).build());
        backend.deleteSubtitles(SubtitlesId.builder().mid(MID).language(Locale.CHINESE).type(SubtitlesType.TRANSLATION).build());

        waitUntil(ACCEPTABLE_DURATION,
            MID + " subtitles dissappeared", () -> {
                return backend.getBackendRestService().getSubtitles(MID, new Locale("ar"), SubtitlesType.TRANSLATION, null) == null;
            });
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
    public void testForCamielAR() throws IOException {

        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/POMS_VPRO_4981202.vtt"), "UTF-8");

        StringWriter writer = new StringWriter();
        IOUtils.copy(reader, writer);
        reader.close();

        Subtitles subtitles = Subtitles.webvttTranslation("VPWON_1259638", Duration.ofMinutes(0), new Locale("ar"), writer.toString());

        Subtitles corrected = Subtitles.from(subtitles.getId(), SubtitlesUtil.fillCueNumber(SubtitlesUtil.parse(subtitles, false)).iterator());


        backend.setSubtitles(corrected);
    }

    @Test
    @Ignore
    public void deleteCaption() throws IOException {

        backend.deleteSubtitles(SubtitlesId.builder().language(new Locale("ar")).type(SubtitlesType.CAPTION).mid("WO_VPRO_11241856").build());
    }

}
