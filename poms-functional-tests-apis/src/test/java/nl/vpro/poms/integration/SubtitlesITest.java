package nl.vpro.poms.integration;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.*;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import nl.vpro.api.client.utils.MediaRestClientUtils;
import nl.vpro.domain.media.AvailableSubtitles;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.subtitles.*;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.util.Version;

import static java.time.Duration.ZERO;
import static java.util.Locale.CHINESE;
import static java.util.Locale.JAPANESE;
import static nl.vpro.domain.subtitles.SubtitlesType.TRANSLATION;
import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Slf4j
public class SubtitlesITest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION_BACKEND = Duration.ofMinutes(2);

    private static final Duration ACCEPTABLE_DURATION_FRONTEND = Duration.ofMinutes(15);

    private static final AvailableSubtitles JAPANESE_TRANSLATION = AvailableSubtitles.published(JAPANESE, TRANSLATION);
    private static final AvailableSubtitles CHINESE_TRANSLATION = AvailableSubtitles.published(CHINESE, TRANSLATION);

    private static String firstTitle;

    private static boolean arrivedInBackend = false;

    @Test
    void test01addSubtitles() {
        assumeThat(backendVersionNumber).isGreaterThanOrEqualTo(Version.of(5, 1));
        assumeThat(backend.getFullProgram(MID_WITH_LOCATIONS).getLocations()).isNotEmpty();

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
    void test02checkArrivedInBackend() {
        assumeThat(backendVersionNumber).isGreaterThanOrEqualTo(Version.of(5, 3));


        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            MID_WITH_LOCATIONS + " has " + JAPANESE_TRANSLATION + " and " + CHINESE_TRANSLATION,
            () -> {
                MediaObject mo = backend.getFull(MID_WITH_LOCATIONS);
                List<AvailableSubtitles> availableSubtitles = mo.getAvailableSubtitles();
                availableSubtitles.removeIf(a -> SubtitlesWorkflow.DELETEDS.contains(a.getWorkflow()));
                log.info("{}", availableSubtitles);
                return availableSubtitles.containsAll(Arrays.asList(JAPANESE_TRANSLATION, CHINESE_TRANSLATION));
            });
        arrivedInBackend = true;

    }

    @Test
    void test03WaitForCuesAvailableInFrontend() {
        waitForCuesAvailableInFrontend(JAPANESE, CHINESE);
    }


    @Test
    void test04WaitForInMediaFrontend() {
        assumeThat(firstTitle).isNotNull();
        assumeTrue(arrivedInBackend);

        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            MID_WITH_LOCATIONS + " has " + JAPANESE_TRANSLATION,
            () -> mediaUtil.findByMid(MID_WITH_LOCATIONS).getAvailableSubtitles().containsAll(Arrays.asList(JAPANESE_TRANSLATION, CHINESE_TRANSLATION))
        );
    }

    @Test
    void test05RevokeLocations() {
        Instant now = Instant.now();
        ProgramUpdate o = backend.get(MID_WITH_LOCATIONS);
        o.getLocations().forEach(l -> l.setPublishStopInstant(now));
        backend.set(o);
    }

    @Test
    void test06WaitForCuesDisappearedInFrontend() {
        assumeThat(firstTitle).isNotNull();
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
    void test07PublishLocations() {
        assumeTrue(arrivedInBackend);

        ProgramUpdate o = backend.get(MID_WITH_LOCATIONS);
        o.getLocations().forEach(l -> l.setPublishStopInstant(null));
        backend.set(o);
    }

    @Test
    void test08WaitForCuesAvailableInFrontend() {
        waitForCuesAvailableInFrontend(JAPANESE, CHINESE);
    }

     @Test
     void test09DeleteJapanese() {
         try(Response response = backend.getBackendRestService()
             .deleteSubtitles(MID_WITH_LOCATIONS, JAPANESE, TRANSLATION, true, null)) {
             log.info("{}", response);
         }
    }

    @Test
    void test10checkDeleteFrontend() {
        assumeThat(firstTitle).isNotNull();
        assumeThat(backendVersionNumber).isGreaterThanOrEqualTo(Version.of(5, 3));

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
    void test90Cleanup() {
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
    void test91checkCleanup() {
        assumeThat(backendVersionNumber).isGreaterThanOrEqualTo(Version.of(5, 3));

        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            MID_WITH_LOCATIONS + " has no " + JAPANESE_TRANSLATION ,
            () -> {
                MediaObject mo = backend.getFull(MID_WITH_LOCATIONS);
                Optional<AvailableSubtitles> ja = mo.getAvailableSubtitles().stream().filter(a -> a.equals(JAPANESE_TRANSLATION)).findFirst();
                return ja.isPresent() && ja.get().getWorkflow() == SubtitlesWorkflow.DELETED;
            });

        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            MID_WITH_LOCATIONS + " has no " + CHINESE_TRANSLATION ,
            () -> {
                MediaObject mo = backend.getFull(MID_WITH_LOCATIONS);
                Optional<AvailableSubtitles> zh = mo.getAvailableSubtitles().stream().filter(a -> a.equals(CHINESE_TRANSLATION)).findFirst();
                return zh.isPresent() && zh.get().getWorkflow() == SubtitlesWorkflow.DELETED;
            });


    }

    @Test
    void test92checkCleanupFrontend() {
        assumeThat(firstTitle).isNotNull();
        assumeThat(backendVersionNumber).isGreaterThanOrEqualTo(Version.of(5, 3));
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


     void waitForCuesAvailableInFrontend(Locale... locales) {
         assumeThat(firstTitle).isNotNull();
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
