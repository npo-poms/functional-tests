package nl.vpro.poms.backend;

import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

import javax.xml.bind.JAXB;

import org.junit.jupiter.api.*;

import nl.vpro.domain.media.*;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.SegmentUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;



/*
 * 2018-08-20
 * 5.9-SNAPSHOT @ dev : allemaal ok

 */
/**
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Log4j2
class MediaBackendSegmentsTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    private static String segmentMid;
    private static String segmentTitle;
    private static String updatedSegmentTitle;

    private static String programMid;


    @BeforeEach
    void setup() {

    }

    @Test
    void test01createSegment() {
        segmentTitle = title;
        SegmentUpdate update = SegmentUpdate.create(
            MediaBuilder.segment()
                .avType(AVType.VIDEO)
                .broadcasters("VPRO")
                .midRef(MID)
                .start(Duration.ofMillis(0))
                .ageRating(AgeRating.ALL)
                .mainTitle(segmentTitle));
        JAXB.marshal(update, System.out);
        segmentMid = backend.set(update);
        log.info("Created " + segmentMid);

    }

    @Test
    void test02WaitForCreatedSegment() {
        assumeThat(segmentMid).isNotNull();
        waitUntil(ACCEPTABLE_DURATION,
            segmentMid + " in backend",
            () -> {
            SegmentUpdate up = backend.get(segmentMid);
            return up != null;
        });
    }


    /**
     * TODO: this actually checks the frontend. This is therefor not a pure backend test
     */
    @Test
    void test03WaitForCreatedSegmentInFrontend() {
        assumeThat(segmentMid).isNotNull();
        Segment[] segments = new Segment[1];
        waitUntil(ACCEPTABLE_DURATION_FRONTEND,
            segmentMid + " in frontend and has title " + segmentTitle,
            () -> {
            segments[0] = mediaUtil.loadOrNull(segmentMid);
            if (segments[0] == null) {
                return false;
            }
            Instant lastPublished = segments[0].getLastPublishedInstant();
            if (lastPublished == null) {
                throw new IllegalStateException("The last published field of " + segments[0] + " is null!");
            }
            return segments[0].getMainTitle().equals(segmentTitle);
        });
        assertThat(segments[0])
            .overridingErrorMessage("No segment %s found for %s", segmentMid, MID)
            .isNotNull();
        assertThat(segments[0].getMidRef()).isEqualTo(MID);
        assertThat(segments[0].getMainTitle()).isEqualTo(segmentTitle);


    }


    @Test
    void test04CreateProgramWithSegment() {
        Segment segment =
            MediaBuilder.segment()
                .avType(AVType.VIDEO)
                .start(Duration.ofMillis(0))
                .mainTitle("Segment for " + title)
                .ageRating(AgeRating.ALL)
                .build();
        ProgramUpdate update = ProgramUpdate.create(
            MediaBuilder.program()
                .broadcasters("VPRO")
                .type(ProgramType.CLIP)
                .avType(AVType.VIDEO)
                .mainTitle(title)
                .ageRating(AgeRating.ALL)
                .segments(segment)
        );
        JAXB.marshal(update, System.out);
        programMid = backend.set(update);
        log.info("Created " + programMid);
    }

    @Test
    void test05WaitForCreatedProgramWithSegmentArrived() {
        assumeThat(programMid).isNotNull();
        waitUntil(ACCEPTABLE_DURATION,
            programMid + " in backend",
            () -> {
            ProgramUpdate up = backend.get(programMid);
            return up != null;
        });
    }

    @Test
    void test06CheckResultCreatedProgramWithSegmentArrived() {
        assumeThat(programMid).isNotNull();
        ProgramUpdate up = backend.get(programMid);
        assertThat(up).isNotNull();
        assertThat(up.getMid()).isEqualTo(programMid);
        assertThat(up.getSegments()).hasSize(1);
        assertThat(up.getVersion()).isEqualTo(backendVersionNumber);
    }


    @Test
    void test07UpdateSegmentDirectly() {
        assumeThat(segmentMid).isNotNull();

        SegmentUpdate up = backend.get(segmentMid);
        assertThat(up.getMidRef()).isEqualTo(MID);
        updatedSegmentTitle = up.fetch().getMainTitle() + " -> " + title;
        up.setMainTitle(updatedSegmentTitle);

        backend.set(up);
    }

    @Test
    void test08WaitForUpdateSegmentDirectly() {
        assumeThat(segmentMid).isNotNull();
        waitUntil(ACCEPTABLE_DURATION,
            segmentMid + " has title " + updatedSegmentTitle,
            () -> {
            SegmentUpdate up = backend.get(segmentMid);
            return up.fetch().getMainTitle().equals(updatedSegmentTitle);
        });
    }

    @Test
    void test09UpdateSegmentViaProgram() {
        assumeThat(segmentMid).isNotNull();
        ProgramUpdate programUpdate = backend.get(MID);

        SegmentUpdate segmentUpdate = programUpdate.getSegments()
            .stream()
            .filter(s -> s.getMid().equals(segmentMid))
            .findFirst()
            .orElseThrow(IllegalStateException::new);
        updatedSegmentTitle = segmentUpdate.fetch().getMainTitle() + " -> " + title;
        segmentUpdate.setMainTitle(updatedSegmentTitle);

        backend.set(programUpdate);
    }


    @Test
    void test10WaitForUpdatedSegmentViaProgram() {
        assumeThat(segmentMid).isNotNull();
        waitUntil(ACCEPTABLE_DURATION,
            segmentMid + " has title " + updatedSegmentTitle,
            () -> {
            SegmentUpdate up = backend.get(segmentMid);
            return up.fetch().getMainTitle().equals(updatedSegmentTitle);
        });
    }


    @Test
    @AbortOnException.NoAbort
    void test98Cleanup() {
        Program program = backend.getFullProgram(MID);
        assumeThat(program).isNotNull();
        log.info("Found {} with {} segments", program, program.getSegments().size());
        Iterator<Segment> segments = program.getSegments().iterator();
        int count = 0;
        while(segments.hasNext()) {
            Segment segment = segments.next();
            if (segment.getCreationInstant().isBefore(Instant.now().minus(Duration.ofDays(3)))) {
                log.info("Deleting {}", segment);
                count++;
                backend.removeSegment(MID, segment.getMid());
            }
        }
        log.info("Deleted {} segments for {}", count, MID);
    }


    @Test
    @AbortOnException.NoAbort
    void test99CheckCleanup() {
        Program program = backend.getFullProgram(MID);
        assertThat(program.getSegments()).isEmpty();
    }


    @Test
    @Disabled
    void testSegment() {
        Segment segment = (Segment) clients.getMediaService().load("POMS_VPRO_1460016", null, null);
        assertThat(segment.getMidRef()).isNotNull();
    }


}
