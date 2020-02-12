package nl.vpro.poms.integration;

import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import javax.xml.bind.JAXB;

import org.junit.jupiter.api.*;

import nl.vpro.domain.media.*;
import nl.vpro.domain.media.support.*;
import nl.vpro.domain.media.update.GroupUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.logging.Log4j2OutputStream;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;

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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
public class MediaITest extends AbstractApiMediaBackendTest {

    private static String groupMid;
    private static String offlineGroup;
    private static String clipMid;
    private static String clipTitle;
    private static String clipDescription;

    @Test
    @Order(1)
    void createMedia() {
        clipTitle = title;
        Image expiredImage = createImage();
        expiredImage.setTitle("OFFLINE " + title);
        expiredImage.setPublishStopInstant(Instant.now().minus(Duration.ofMinutes(1)));

        Image publishedImage = createImage();
        publishedImage.setTitle("PUBLISHED " + title);
        publishedImage.setPublishStopInstant(Instant.now().plus(Duration.ofMinutes(10)));

        Segment expiredSegment= createSegment(1);
        expiredSegment.setMainTitle("OFFLINE " + title);
        expiredSegment.setPublishStopInstant(Instant.now().minus(Duration.ofMinutes(1)));

        Segment publishedSegment = createSegment(2);
        publishedSegment.setMainTitle("PUBLISHED " + title);
        publishedSegment.setPublishStopInstant(Instant.now().plus(Duration.ofMinutes(10)));

        Location expiredLocation = createLocation(1);
        expiredLocation.setPublishStopInstant(Instant.now().minus(Duration.ofMinutes(1)));

        Location publishedLocation = createLocation(2);
        publishedLocation.setPublishStopInstant(Instant.now().plus(Duration.ofMinutes(10)));

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
        JAXB.marshal(clip, Log4j2OutputStream.debug(log));
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
                    .publishStop(Instant.now().minus(Duration.ofMinutes(5)))
                    .clearBroadcasters()
                    .withAgeRating()
                    .broadcasters("VPRO")
                    .build()
            ));
        waitUntil(Duration.ofMinutes(2),
            () -> "clip:" + clipMid + " available",
            () -> backend.getFull(clipMid) != null);


        waitUntil(Duration.ofMinutes(2),
            () -> "group:" + groupMid + " available",
            () -> backend.getFull(groupMid) != null);


        waitUntil(Duration.ofMinutes(2),
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
        Program clip = waitUntil(Duration.ofMinutes(10),
            clipMid + " is a memberof " + groupMid,
            () -> mediaUtil.findByMid(clipMid),
            (c) -> !c.getMemberOf().isEmpty()
        );
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
    @Order(10)
    void updateTitle() {
        //clipMid = "POMS_VPRO_3322744";
        assumeThat(clipMid).isNotNull();
        ProgramUpdate mediaUpdate = backend.get(clipMid);
        clipTitle = title;
        mediaUpdate.setMainTitle(clipTitle);
        backend.set(mediaUpdate);
    }

    @Test
    @Order(11)
    void checkUpdateTitleInFrontendApi() {
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
    @Order(20)
    void updateDescription() {
        assumeThat(clipMid).isNotNull();
        ProgramUpdate mediaUpdate = backend.get(clipMid);
        clipDescription = title;
        assumeThat(mediaUpdate).isNotNull();
        mediaUpdate.setMainDescription(clipDescription);
        backend.set(mediaUpdate);
    }

    @Test
    @Order(21)
    void checkUpdateDescriptionInFrontendApi() {
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
    @Order(30)
    void waitForImageRevocation() {
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
    @Order(40)
    void waitForSegmentRevocation() {
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
    @Order(40)
    void waitForLocationsRevocation() {
        assumeThat(clipMid).isNotNull();
        Program clip = waitUntil(Duration.ofMinutes(20),
            clipMid + " has no locations any more",
            () -> mediaUtil.findByMid(clipMid),
            (c) -> c.getLocations().isEmpty());
        assertThat(clip).isNotNull();
        assertThat(clip.getLocations()).isEmpty();
    }

    @Test
    @Order(100)
    @AbortOnException.NoAbort
    void test100Delete() {
        assumeThat(clipMid).isNotNull();
        backend.delete(clipMid);
        backend.delete(groupMid);
        backend.delete(offlineGroup);
    }

    @Test
    @Order(101)
    @AbortOnException.NoAbort
    void test101CheckDeletedInFrontendApi() {
        assumeThat(clipMid).isNotNull();
        waitUntil(Duration.ofMinutes(10),
            () -> clipMid + " disappeared",
            () -> mediaUtil.findByMid(clipMid) == null
        );

    }
}
