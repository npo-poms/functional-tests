package nl.vpro.poms.integration;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import javax.xml.bind.JAXB;

import org.junit.jupiter.api.*;

import nl.vpro.domain.media.*;
import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.media.support.Workflow;
import nl.vpro.domain.media.update.GroupUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.logging.LoggerOutputStream;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static nl.vpro.testutils.Utils.waitUntil;
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
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Slf4j
class MediaITest extends AbstractApiMediaBackendTest {

    private static String groupMid;
    private static String clipMid;
    private static String clipTitle;
    private static String clipDescription;

    @Test
    void test001CreateMedia() {
        clipTitle = title;
        Image expiredImage = createImage();
        expiredImage.setTitle("OFFLINE " + title);
        expiredImage.setPublishStopInstant(Instant.now().minus(Duration.ofMinutes(1)));

        Image publishedImage = createImage();
        publishedImage.setTitle("PUBLISHED " + title);
        publishedImage.setPublishStopInstant(Instant.now().plus(Duration.ofMinutes(10)));

        Segment expiredSegment= createSegment();
        expiredSegment.setMainTitle("OFFLINE " + title);
        expiredSegment.setPublishStopInstant(Instant.now().minus(Duration.ofMinutes(1)));

        Segment publishedSegment = createSegment();
        publishedSegment.setMainTitle("PUBLISHED " + title);
        publishedSegment.setPublishStopInstant(Instant.now().plus(Duration.ofMinutes(10)));

        Location expiredLocation = createLocation(1);
        expiredLocation.setPublishStopInstant(Instant.now().minus(Duration.ofMinutes(1)));

        Location publishedLocation = createLocation(2);
        publishedLocation.setPublishStopInstant(Instant.now().plus(Duration.ofMinutes(10)));

        ProgramUpdate clip = ProgramUpdate
            .create(
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
        clipMid = backend.set(clip);
        JAXB.marshal(clip, LoggerOutputStream.debug(log));
        log.info("Created clip {} {}", clipMid, clipTitle);
        groupMid = backend.set(
            GroupUpdate.create(
                MediaTestDataBuilder
                    .playlist()
                    .constrainedNew()
                    .mainTitle(title)
                    .clearBroadcasters()
                    .withAgeRating()
                    .broadcasters("VPRO")
                    .build()

            ));
        String offlineGroup = backend.set(
            GroupUpdate.create(
                MediaTestDataBuilder
                    .playlist()
                    .constrainedNew()
                    .mainTitle(title + " offline")
                    .publishStop(Instant.now().minus(Duration.ofMinutes(5)))
                    .clearBroadcasters()
                    .withAgeRating()
                    .broadcasters("VPRO")
                    .build()
            ));
        waitUntil(Duration.ofMinutes(2),
            () -> "clip:" + clipMid + " and group:" + groupMid + " available",
            () -> backend.getFull(clipMid) != null && backend.getFull(groupMid) != null
        );
        log.info("Created groups {}, {}", groupMid, offlineGroup);
        backend.createMember(offlineGroup, clipMid, 1);
        backend.createMember(groupMid, clipMid, 2);
    }

    @Test
    void test002CheckFrontendApi() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(Duration.ofMinutes(10),
            clipMid + " is a memberof " + groupMid,
            () -> mediaUtil.findByMid(clipMid),
            (c) -> !c.getMemberOf().isEmpty());
        assertThat(clip).isNotNull();
        assertThat(clip.getMainTitle()).isEqualTo(clipTitle);
        assertThat(clip.getMemberOf().first().getMediaRef()).isEqualTo(groupMid);
        assertThat(clip.getMemberOf().first().getNumber()).isEqualTo(2);
        assertThat(clip.getMemberOf()).hasSize(1);
        assertThat(clip.getImages()).hasSize(1);
        assertThat(clip.getSegments()).hasSize(1);
        assertThat(clip.getLocations()).hasSize(1);
        assertThat(clip.getWorkflow()).isEqualTo(Workflow.PUBLISHED);

    }

    @Test
    void test003UpdateTitle() {
        assumeThat(clipMid).isNotNull();
        ProgramUpdate mediaUpdate = backend.get(clipMid);
        clipTitle = title;
        mediaUpdate.setMainTitle(clipTitle);
        backend.set(mediaUpdate);
    }

    @Test
    void test004CheckFrontendApi() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(Duration.ofMinutes(10),
            clipMid + " has title " + clipTitle,
            () -> mediaUtil.findByMid(clipMid),
            (c) -> c.getMainTitle().equals(clipTitle));
        assertThat(clip).isNotNull();
        assertThat(clip.getMainTitle()).isEqualTo(clipTitle);
        assertThat(clip.getWorkflow()).isEqualTo(Workflow.PUBLISHED);
    }


    @Test
    void test005UpdateDescription() {
        assumeThat(clipMid).isNotNull();
        ProgramUpdate mediaUpdate = backend.get(clipMid);
        clipDescription = title;
        assumeThat(mediaUpdate).isNotNull();
        mediaUpdate.setMainDescription(clipDescription);
        backend.set(mediaUpdate);
    }

    @Test
    void test006CheckFrontendApi() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(Duration.ofMinutes(10),
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
    void test007WaitForImageRevocation() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(Duration.ofMinutes(20),
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
    void test008WaitForSegmentRevocation() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(Duration.ofMinutes(20),
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
    void test009WaitForLocationsRevocation() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(Duration.ofMinutes(20),
            clipMid + " has no locations any more",
            () -> mediaUtil.findByMid(clipMid),
            (c) -> c.getLocations().isEmpty());
        assertThat(clip).isNotNull();
        assertThat(clip.getLocations()).isEmpty();
    }

    @Test
    void test100Delete() {
        assumeThat(clipMid).isNotNull();

        backend.delete(clipMid);
    }

    @Test
    void test101CheckFrontendApi() {
        assumeThat(clipMid).isNotNull();
        waitUntil(Duration.ofMinutes(10),
            () -> clipMid + " disappeared",
            () -> mediaUtil.findByMid(clipMid) == null
        );

    }
}
