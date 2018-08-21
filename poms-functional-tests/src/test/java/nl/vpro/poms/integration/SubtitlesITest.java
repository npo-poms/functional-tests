package nl.vpro.poms.integration;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import nl.vpro.api.client.utils.MediaRestClientUtils;
import nl.vpro.domain.media.AvailableSubtitles;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.subtitles.StandaloneCue;
import nl.vpro.domain.subtitles.Subtitles;
import nl.vpro.domain.subtitles.SubtitlesType;
import nl.vpro.domain.subtitles.SubtitlesUtil;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SubtitlesITest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(12);

    public static final AvailableSubtitles JAPANESE_TRANSLATION = new AvailableSubtitles(Locale.JAPANESE, SubtitlesType.TRANSLATION);


    @Before
    public void setup() {

    }

    private static String firstTitle;

    private static boolean arrivedInBackend = false;

    @Test
    public void test01addSubtitles() {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(5.1f));
        assumeThat(backend.getFullProgram(MID_WITH_LOCATIONS).getLocations(), not(empty()));

        firstTitle = title;

        Subtitles subtitles = Subtitles.webvttTranslation(MID_WITH_LOCATIONS, Duration.ZERO, Locale.JAPANESE,
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
    public void test02checkArrivedInBackend() {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(5.3f));


        waitUntil(ACCEPTABLE_DURATION,
            MID_WITH_LOCATIONS + " has " + JAPANESE_TRANSLATION,
            () -> {
                MediaObject mo = backend.getFull(MID_WITH_LOCATIONS);
                return mo.getAvailableSubtitles().contains(JAPANESE_TRANSLATION);
            });
        arrivedInBackend = true;

    }

    @Test
    public void test03WaitForCuesAvailableInFrontend() {
        assumeNotNull(firstTitle);
        assumeTrue(arrivedInBackend);

        PeekingIterator<StandaloneCue> cueIterator = waitUntil(ACCEPTABLE_DURATION,
            MID_WITH_LOCATIONS + "/" + Locale.JAPANESE + "[0]=" + firstTitle,
            () -> {
                clearCaches();
                try {
                    return Iterators.peekingIterator(
                        SubtitlesUtil.standaloneStream(
                            MediaRestClientUtils.loadOrNull(mediaUtil.getClients().getSubtitlesRestService(),
                            MID_WITH_LOCATIONS, Locale.JAPANESE), false, false).iterator()
                    );
                } catch (IOException ioe) {
                    log.warn(ioe.getMessage());
                    return null;
                }
            }
        , (pi) -> {
                if (pi == null ||  !pi.hasNext()) {
                    log.info("No results yet");
                    return false;
                }
                StandaloneCue peek = pi.peek();
                String content = peek.getContent();
                if (!content.equals(firstTitle)) {
                    log.info("Found cue {} != {} yet", content, firstTitle);
                    return false;
                }
                return true;
            }
        );
        assertThat(cueIterator).hasSize(3);
    }


    @Test
    public void test04WaitForInMediaFrontend() {
        assumeNotNull(firstTitle);
        assumeTrue(arrivedInBackend);

        waitUntil(ACCEPTABLE_DURATION,
            MID_WITH_LOCATIONS + " has " + JAPANESE_TRANSLATION,
            () -> mediaUtil.findByMid(MID_WITH_LOCATIONS).getAvailableSubtitles().contains(JAPANESE_TRANSLATION)
        );
    }

    @Test
    public void test05RevokeLocations() {
        Instant now = Instant.now();
        ProgramUpdate o = backend.get(MID_WITH_LOCATIONS);
        o.getLocations().forEach(l -> l.setPublishStopInstant(now));
        backend.set(o);
    }

    @Test
    public void test06WaitForCuesDisappearedInFrontend() {
        assumeNotNull(firstTitle);
        assumeTrue(arrivedInBackend);


        waitUntil(ACCEPTABLE_DURATION,
            MID_WITH_LOCATIONS + " has no locations",
            () -> mediaUtil.load(MID_WITH_LOCATIONS)[0].getLocations().isEmpty());

        waitUntil(ACCEPTABLE_DURATION,
                MID_WITH_LOCATIONS + " has no subtitles for JAPAN",
            () -> MediaRestClientUtils.loadOrNull(mediaUtil.getClients().getSubtitlesRestService(), MID_WITH_LOCATIONS, Locale.JAPAN) == null);
    }

    @Test
    public void test07PublishLocations() {
        assumeTrue(arrivedInBackend);

        ProgramUpdate o = backend.get(MID_WITH_LOCATIONS);
        o.getLocations().forEach(l -> l.setPublishStopInstant(null));
        backend.set(o);
    }

    @Test
    public void test08WaitForCuesAvailableInFrontend() {
        assumeTrue(arrivedInBackend);

        test03WaitForCuesAvailableInFrontend();
    }

    @Test
    public void test90Cleanup() {
        backend.getBackendRestService()
            .deleteSubtitles(MID_WITH_LOCATIONS, Locale.JAPANESE, SubtitlesType.TRANSLATION, true, null);
    }

    @Test
    public void test91checkCleanup() {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(5.3f));

        waitUntil(ACCEPTABLE_DURATION,
            MID_WITH_LOCATIONS + " has no " + JAPANESE_TRANSLATION,
            () -> {
                MediaObject mo = backend.getFull(MID_WITH_LOCATIONS);
                return ! mo.getAvailableSubtitles().contains(JAPANESE_TRANSLATION);
            });


    }

    @Test
    public void test92checkCleanupFrontend() {

        assumeThat(backendVersionNumber, greaterThanOrEqualTo(5.3f));
        waitUntil(ACCEPTABLE_DURATION,
            MID_WITH_LOCATIONS + " has no " + JAPANESE_TRANSLATION,
            () -> ! mediaUtil.findByMid(MID_WITH_LOCATIONS).getAvailableSubtitles().contains(JAPANESE_TRANSLATION));


        // TODO: FAILS
        waitUntil(ACCEPTABLE_DURATION,
            MID_WITH_LOCATIONS + " has no subtitles for JAPAN",
            () -> MediaRestClientUtils.loadOrNull(mediaUtil.getClients().getSubtitlesRestService(), MID_WITH_LOCATIONS, Locale.JAPAN) == null);
    }

}
