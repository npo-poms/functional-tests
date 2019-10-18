package nl.vpro.poms.integration;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;

import javax.ws.rs.core.Response;

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
import nl.vpro.domain.subtitles.SubtitlesUtil;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.util.Version;

import static java.time.Duration.ZERO;
import static java.util.Locale.CHINESE;
import static java.util.Locale.JAPANESE;
import static nl.vpro.domain.subtitles.SubtitlesType.TRANSLATION;
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

    private static final Duration ACCEPTABLE_DURATION_BACKEND = Duration.ofMinutes(2);

    private static final Duration ACCEPTABLE_DURATION_FRONTEND = Duration.ofMinutes(15);

    public static final AvailableSubtitles JAPANESE_TRANSLATION = new AvailableSubtitles(JAPANESE, TRANSLATION);
    public static final AvailableSubtitles CHINESE_TRANSLATION= new AvailableSubtitles(CHINESE, TRANSLATION);


    @Before
    public void setup() {

    }

    private static String firstTitle;

    private static boolean arrivedInBackend = false;

    @Test
    public void test01addSubtitles() {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(Version.of(5, 1)));
        assumeThat(backend.getFullProgram(MID_WITH_LOCATIONS).getLocations(), not(empty()));

        firstTitle = title;

        String exampleContent = "WEBVTT\n" +
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
                "";
        {
            Subtitles subtitles = Subtitles.webvttTranslation(MID_WITH_LOCATIONS, ZERO, JAPANESE, exampleContent);
            backend.setSubtitles(subtitles);
        }
        {
            Subtitles subtitles = Subtitles.webvttTranslation(MID_WITH_LOCATIONS, ZERO, CHINESE, exampleContent);
            backend.setSubtitles(subtitles);
        }
    }


    @Test
    public void test02checkArrivedInBackend() {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(Version.of(5, 3)));


        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            MID_WITH_LOCATIONS + " has " + JAPANESE_TRANSLATION + " and " + CHINESE_TRANSLATION,
            () -> {
                MediaObject mo = backend.getFull(MID_WITH_LOCATIONS);
                return mo.getAvailableSubtitles().containsAll(Arrays.asList(JAPANESE_TRANSLATION, CHINESE_TRANSLATION));
            });
        arrivedInBackend = true;

    }

    @Test
    public void test03WaitForCuesAvailableInFrontend() {
        waitForCuesAvailableInFrontend(JAPANESE, CHINESE);
    }


    @Test
    public void test04WaitForInMediaFrontend() {
        assumeNotNull(firstTitle);
        assumeTrue(arrivedInBackend);

        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            MID_WITH_LOCATIONS + " has " + JAPANESE_TRANSLATION,
            () -> mediaUtil.findByMid(MID_WITH_LOCATIONS).getAvailableSubtitles().containsAll(Arrays.asList(JAPANESE_TRANSLATION, CHINESE_TRANSLATION))
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


        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            MID_WITH_LOCATIONS + " has no locations",
            () -> mediaUtil.load(MID_WITH_LOCATIONS)[0].getLocations().isEmpty());

        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
                MID_WITH_LOCATIONS + " has no subtitles in frontend for JAPAN",
            () -> MediaRestClientUtils.loadOrNull(mediaUtil.getClients().getSubtitlesRestService(), MID_WITH_LOCATIONS, Locale.JAPAN) == null);
        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
                MID_WITH_LOCATIONS + " has no subtitles in frontend for CHINESE",
            () -> MediaRestClientUtils.loadOrNull(mediaUtil.getClients().getSubtitlesRestService(), MID_WITH_LOCATIONS, CHINESE) == null);
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
        waitForCuesAvailableInFrontend(JAPANESE, CHINESE);
    }

     @Test
    public void test09DeleteJapanese() {
         try(Response response = backend.getBackendRestService()
             .deleteSubtitles(MID_WITH_LOCATIONS, JAPANESE, TRANSLATION, true, null)) {
             log.info("{}", response);
         }
    }

    @Test
    public void test10checkDeleteFrontend() {
        assumeNotNull(firstTitle);
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(Version.of(5, 3)));

        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            MID_WITH_LOCATIONS + " has no " + JAPANESE_TRANSLATION,
            () -> ! mediaUtil.findByMid(MID_WITH_LOCATIONS).getAvailableSubtitles().contains(JAPANESE_TRANSLATION));


        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            MID_WITH_LOCATIONS + " has no subtitles for JAPAN",
            () -> MediaRestClientUtils.loadOrNull(mediaUtil.getClients().getSubtitlesRestService(), MID_WITH_LOCATIONS, Locale.JAPAN) == null);

        // the chinese ones still need to be there
        waitForCuesAvailableInFrontend(CHINESE);

    }

    @Test
    public void test90Cleanup() {
        try(Response response = backend.getBackendRestService()
            .deleteSubtitles(MID_WITH_LOCATIONS, JAPANESE, TRANSLATION, true, null)) {
            log.info("{}", response);
        }
        try(Response response = backend.getBackendRestService()
            .deleteSubtitles(MID_WITH_LOCATIONS, CHINESE, TRANSLATION, true, null)) {
            log.info("{}", response);
        }
    }

    @Test
    public void test91checkCleanup() {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(Version.of(5, 3)));

        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            MID_WITH_LOCATIONS + " has no " + JAPANESE_TRANSLATION ,
            () -> {
                MediaObject mo = backend.getFull(MID_WITH_LOCATIONS);
                return ! mo.getAvailableSubtitles().contains(JAPANESE_TRANSLATION);
            });

        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            MID_WITH_LOCATIONS + " has no " + CHINESE_TRANSLATION ,
            () -> {
                MediaObject mo = backend.getFull(MID_WITH_LOCATIONS);
                return ! mo.getAvailableSubtitles().contains(CHINESE_TRANSLATION);
            });


    }

    @Test
    public void test92checkCleanupFrontend() {
        assumeNotNull(firstTitle);
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(Version.of(5, 3)));
        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            MID_WITH_LOCATIONS + " has no " + JAPANESE_TRANSLATION,
            () -> ! mediaUtil.findByMid(MID_WITH_LOCATIONS).getAvailableSubtitles().contains(JAPANESE_TRANSLATION));

        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            MID_WITH_LOCATIONS + " has no " + CHINESE_TRANSLATION,
            () -> ! mediaUtil.findByMid(MID_WITH_LOCATIONS).getAvailableSubtitles().contains(CHINESE_TRANSLATION));


        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            MID_WITH_LOCATIONS + " has no subtitles for Chinese",
            () -> MediaRestClientUtils.loadOrNull(mediaUtil.getClients().getSubtitlesRestService(), MID_WITH_LOCATIONS, CHINESE) == null);
    }


     protected void waitForCuesAvailableInFrontend(Locale... locales) {
         assumeNotNull(firstTitle);
         assumeTrue(arrivedInBackend);
         for(Locale locale : locales) {
             PeekingIterator<StandaloneCue> cueIterator = waitUntil(ACCEPTABLE_DURATION_FRONTEND,
                 MID_WITH_LOCATIONS + "/" + locale + "[0]=" + firstTitle,
                 () -> {
                     clearCaches();
                     try {
                         return Iterators.peekingIterator(
                             SubtitlesUtil.standaloneStream(
                                 MediaRestClientUtils.loadOrNull(mediaUtil.getClients().getSubtitlesRestService(),
                                     MID_WITH_LOCATIONS, locale), false, false).iterator()
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
             assertThat(cueIterator).toIterable().hasSize(3);
         }
    }


}
