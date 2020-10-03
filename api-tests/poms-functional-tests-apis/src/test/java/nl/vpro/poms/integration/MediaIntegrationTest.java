package nl.vpro.poms.integration;

import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import javax.xml.bind.JAXB;

import org.junit.jupiter.api.*;
import org.opentest4j.TestAbortedException;

import nl.vpro.domain.api.MediaChange;
import nl.vpro.domain.media.*;
import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.media.support.Workflow;
import nl.vpro.domain.media.update.GroupUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.logging.Log4j2OutputStream;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.testutils.Utils.Check;

import static nl.vpro.testutils.Utils.waitUntil;
import static nl.vpro.testutils.Utils.waitUntilNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

/**
 * Create and change items via the backend api, and check them in frontend api.
 *
 * This tests the complete chain:
 *  - backend api -> activemq -> poms backend -> activemq -> poms publisher -> elasticsearch -> frontend api
 *
 *  It also checks various embargos. Like locations and images which have their own publish start and stop times.
 *
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
public class MediaIntegrationTest extends AbstractApiMediaBackendTest {

    private static String groupMid;
    private static String offlineGroup;
    private static String clipMid;
    private static String clipTitle;
    private static String clipDescription;


    private static final List<Predicate<MediaChange>> expectedChanges = new CopyOnWriteArrayList<>();


    @Test
    @Order(1)
    void createMedia() {
        Instant now = Instant.now();
        clipTitle = title;
        Image expiredImage = createImage("OFFLINE ");
        expiredImage.setTitle("OFFLINE " + title);
        expiredImage.setPublishStopInstant(now.minus(Duration.ofMinutes(1)));

        Image publishedImage = createImage("PUBLISHED ");
        publishedImage.setTitle("PUBLISHED " + title);
        publishedImage.setPublishStopInstant(now.plus(Duration.ofMinutes(15)));

        Segment expiredSegment= createSegment(1);
        expiredSegment.setMainTitle("OFFLINE " + title);
        expiredSegment.setPublishStopInstant(now.minus(Duration.ofMinutes(1)));

        Segment publishedSegment = createSegment(2);
        publishedSegment.setMainTitle("PUBLISHED " + title);
        publishedSegment.setPublishStopInstant(now.plus(Duration.ofMinutes(10)));

        Location expiredLocation = createLocation(1);
        expiredLocation.setPublishStopInstant(now.minus(Duration.ofMinutes(1)));

        Location publishedLocation = createLocation(2);
        publishedLocation.setPublishStopInstant(now.plus(Duration.ofMinutes(10)));

        ProgramUpdate clip = ProgramUpdate
            .create(
                getBackendVersionNumber(),
                MediaTestDataBuilder
                    .clip()
                    .constrainedNew()
                    .clearBroadcasters()
                    .broadcasters("VPRO")
                    .mainTitle(clipTitle)
                    .withAgeRating()
                    .images(
                        expiredImage,
                        publishedImage
                    )
                    .segments(
                        expiredSegment,
                        publishedSegment
                    )
                    .locations(
                        expiredLocation,
                        publishedLocation
                    )
                .build()

            );
        JAXB.marshal(clip, Log4j2OutputStream.info(log));
        clipMid = backend.set(clip);
        log.info("Created clip {} {}", clipMid, clipTitle);

        groupMid = backend.set(
            GroupUpdate.create(
                getBackendVersionNumber(),
                MediaTestDataBuilder
                    .playlist()
                    .constrainedNew()
                    .mainTitle(title)
                    .clearBroadcasters()
                    .withAgeRating()
                    .broadcasters("VPRO")
                    .build()

            ));
        offlineGroup = backend.set(
            GroupUpdate.create(
                getBackendVersionNumber(),
                MediaTestDataBuilder
                    .playlist()
                    .constrainedNew()
                    .mainTitle(title + " offline")
                    .publishStop(NOWI.minus(Duration.ofMinutes(5)))
                    .clearBroadcasters()
                    .withAgeRating()
                    .broadcasters("VPRO")
                    .build()
            ));
        MediaObject foundClip = waitUntilNotNull(ACCEPTABLE_DURATION_BACKEND,
            "clip:" + clipMid + " available",
            () -> backend.getFull(clipMid));

        assertThat(foundClip.getImages()).withFailMessage("%s doesn't have 2 images", foundClip).hasSize(2);

        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            () -> "group:" + groupMid + " available",
            () -> backend.getFull(groupMid) != null);


        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            () -> "group:" + offlineGroup + " available",
            () -> backend.getFull(offlineGroup) != null);

        log.info("Created groups {}, {}", groupMid, offlineGroup);
        backend.createMember(offlineGroup, clipMid, 1);
        backend.createMember(groupMid, clipMid, 2);
    }

    @Test
    @Order(2)
    void checkNewObjectInFrontendApi() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            () -> mediaUtil.findByMid(clipMid),
            Check.<Program>builder()
                .description("has {}", clipMid)
                .predicate(Objects::nonNull)
                .build(),
            Check.<Program>builder()
                .description("{} is a memberOf", clipMid)
                .predicate( p -> ! p.getMemberOf().isEmpty())
                .build()
        );
        assertThat(clip).isNotNull();
        assertThat(clip.getMainTitle()).isEqualTo(clipTitle);
        assertThat(clip.getMemberOf().first().getMediaRef()).isEqualTo(groupMid);
        assertThat(clip.getMemberOf().first().getNumber()).isEqualTo(2);
        assertThat(clip.getMemberOf()).hasSize(1);
        assertThat(clip.getImages()).withFailMessage("%s doesn't have 1 image (it has: %d: %s)", clip, clip.getImages().size(), clip.getImages()).hasSize(1);
        assertThat(clip.getSegments()).hasSize(1);
        assertThat(clip.getLocations()).hasSize(1);
        assertThat(clip.getWorkflow()).isEqualTo(Workflow.PUBLISHED);
        expectedChanges.add((mc) -> clipMid.equals(mc.getMid()));

    }

    @Test
    @Order(10)
    void updateTitle() {
        if (getBackendVersionNumber().isNotAfter(5, 11, 7)) {
            // Known to fail MSE-4715
            clipTitle = null;
            throw new TestAbortedException();
        } else{
            //clipMid = "POMS_VPRO_3322744";
            assumeThat(clipMid).isNotNull();
            ProgramUpdate mediaUpdate = backend.get(clipMid);
            clipTitle = title;
            mediaUpdate.setMainTitle(clipTitle);
            backend.set(mediaUpdate);
        }
    }


    @Test
    @Order(11)
    void checkUpdateTitleInBackend() {
        //clipMid = "POMS_VPRO_3322744";
        assumeThat(clipMid).isNotNull();
        assumeThat(clipTitle).isNotNull();
        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            clipMid + " has title " + clipTitle,
            () -> backend.getFullProgram(clipMid),
            (c) -> c.getMainTitle().equals(clipTitle));
    }

    @Test
    @Order(12)
    void checkUpdateTitleInFrontendApi() {
        assumeThat(clipMid).isNotNull();
        assumeThat(clipTitle).isNotNull();
        Program clip = waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            clipMid + " has title " + clipTitle,
            () -> mediaUtil.findByMid(clipMid),
            (c) -> c.getMainTitle().equals(clipTitle));
        assertThat(clip).isNotNull();
        assertThat(clip.getMainTitle()).isEqualTo(clipTitle);
        assertThat(clip.getWorkflow()).isEqualTo(Workflow.PUBLISHED);
    }


    @Test
    @Order(20)
    void updateDescription() {
        if (getBackendVersionNumber().isNotAfter(5, 11, 7)) {
            // Known to fail MSE-4715
            clipDescription = null;
            throw new TestAbortedException();
        } else {
            assumeThat(clipMid).isNotNull();
            ProgramUpdate mediaUpdate = backend.get(clipMid);
            clipDescription = title;
            assumeThat(mediaUpdate).isNotNull();
            mediaUpdate.setMainDescription(clipDescription);
            backend.set(mediaUpdate);
        }
    }

    @Test
    @Order(21)
    void checkUpdateDescriptionInBackend() {
        assumeThat(clipMid).isNotNull();
        assumeThat(clipDescription).isNotNull();
        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            clipMid + " has description " + clipDescription,
            () -> backend.getFullProgram(clipMid),
            (c) -> c.getMainDescription().equals(clipDescription));
    }


    @Test
    @Order(21)
    void checkUpdateDescriptionInFrontendApi() {
        assumeThat(clipMid).isNotNull();
        assumeThat(clipDescription).isNotNull();
        Program clip = waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            clipMid + " has description " + clipDescription,
            () -> mediaUtil.findByMid(clipMid),
            (c) -> Objects.equals(c.getMainDescription(), clipDescription));
        assertThat(clip).isNotNull();
        assertThat(clip.getMainDescription()).isEqualTo(clipDescription);
        assertThat(clip.getMainTitle()).isEqualTo(clipTitle);
        assertThat(clip.getWorkflow()).isEqualTo(Workflow.PUBLISHED);
    }

    /**
     * All images where not published to begin with or will expire in 10 minutes
     */
    @Test
    @Order(30)
    void waitForImageRevocation() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(ACCEPTABLE_DURATION_FRONTEND.plus(Duration.ofMinutes(10)),
            clipMid + " has no images any more",
            () -> mediaUtil.findByMid(clipMid),
            (c) -> c.getImages().isEmpty());
        assertThat(clip).isNotNull();
        assertThat(clip.getImages()).isEmpty();
    }


    /**
     * All segments where not published to begin with or will expire in 10 minutes
     */
    @Test
    @Order(40)
    void waitForSegmentRevocation() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(ACCEPTABLE_DURATION_FRONTEND.plus(Duration.ofMinutes(10)),
            clipMid + " has no segments any more",
            () -> mediaUtil.findByMid(clipMid),
            (c) -> c.getSegments().isEmpty());
        assertThat(clip).isNotNull();
        assertThat(clip.getSegments()).isEmpty();
    }


    /**
     * All locations where not published to begin with or will expire in 10 minutes
     */
    @Test
    @Order(40)
    void waitForLocationsRevocation() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(ACCEPTABLE_DURATION_FRONTEND.plus(Duration.ofMinutes(10)),
            clipMid + " has no locations any more",
            () -> mediaUtil.findByMid(clipMid),
            (c) -> c.getLocations().isEmpty());
        assertThat(clip).isNotNull();
        assertThat(clip.getLocations()).isEmpty();
    }

    @Test
    @Order(100)
    @AbortOnException.NoAbort
    @Tag("cleanup")
    void delete() {
        if (clipMid == null){
            //  to debug a failure, you may explicitely set it here.
            //clipMid = "POMS_VPRO_3324281";
        }
        assumeThat(clipMid).isNotNull();
        backend.delete(clipMid);
        if (groupMid != null) {
            backend.delete(groupMid);
        }
        if (offlineGroup != null) {
            backend.delete(offlineGroup);
        }
    }

    @Test
    @Order(101)
    @AbortOnException.NoAbort
    @Tag("cleanup")
    void checkDeletedInBackendApi() {
        if (clipMid == null){
            //clipMid = "POMS_VPRO_3324281";
        }
        assumeThat(clipMid).isNotNull();
        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            () -> backend.getFull(clipMid),
            Check.<Program>builder()
                .failureDescription(c -> "Workflow is now " + c.getWorkflow())
                .description("{} is deleted", clipMid)
                .predicate(c -> Workflow.DELETES.contains(c.getWorkflow()))
            ,
            Check.<Program>builder()
                .failureDescription(c -> "Workflow is now " + c.getWorkflow())
                .description("{} delete is published", clipMid)
                .predicate(c -> c.getWorkflow() == Workflow.DELETED)

        );

    }

    @Test
    @Order(102)
    @Tag("cleanup")
    void checkDeletedInFrontendApi() {
        if (clipMid == null){
            //clipMid = "POMS_VPRO_3324155";
        }
        assumeThat(clipMid).isNotNull();
        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            () -> clipMid + " disappeared",
            () -> mediaUtil.findByMid(clipMid) == null
        );

    }

    @Test
    @Order(200)
    void checkReceivedChanges() {
        awaitChanges(expectedChanges);
    }
}
