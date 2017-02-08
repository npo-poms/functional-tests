package nl.vpro.poms.integration;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Locale;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import nl.vpro.domain.subtitles.StandaloneCue;
import nl.vpro.domain.subtitles.Subtitles;
import nl.vpro.domain.subtitles.SubtitlesType;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static nl.vpro.poms.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SubtitlesITest extends AbstractApiMediaBackendTest {

    private static final String MID = "WO_VPRO_025057";
    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    @Before
    public void setup() {

    }

    @Test
    public void test01addSubtitles() {
        //assumeThat(backendVersionNumber, greaterThanOrEqualTo(5.1f));

        Subtitles subtitles = Subtitles.webvttTranslation(MID, Duration.ZERO, Locale.JAPAN,
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
    public void test02WaitForInFrontend() throws Exception {
        PeekingIterator<StandaloneCue> cueIterator = waitUntil(ACCEPTABLE_DURATION, () ->
                Iterators.peekingIterator(mediaUtil.getClients().getSubtitlesRestService()
                    .get(MID, Locale.JAPANESE, SubtitlesType.TRANSLATION)
                )
        , (pi) -> pi != null && pi.peek().getContent().equals(title));
        assertThat(cueIterator).hasSize(3);
    }

}
