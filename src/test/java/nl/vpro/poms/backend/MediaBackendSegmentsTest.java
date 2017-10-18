package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

import javax.xml.bind.JAXB;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.*;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.SegmentUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static nl.vpro.poms.Utils.waitUntil;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assume.assumeNotNull;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MediaBackendSegmentsTest extends AbstractApiMediaBackendTest {

    private static final String MID = "WO_VPRO_025057";
    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    private static String segmentMid;
    private static String segmentTitle;
    private static String updatedSegmentTitle;

    private static String programMid;


    @Before
    public void setup() {

    }

    @Test
    public void test01createSegment() {
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
    public void test02WaitFor() throws Exception {
        assumeNotNull(segmentMid);
        waitUntil(ACCEPTABLE_DURATION,
            segmentMid + " in backend",
            () -> {
            SegmentUpdate up = backend.get(segmentMid);
            return up != null;
        });
    }


    @Test
    public void test03WaitForInFrontend() throws Exception {
        assumeNotNull(segmentMid);
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
    public void test04CreateProgramWithSegment() {

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
                .segments(segment));
        JAXB.marshal(update, System.out);
        programMid = backend.set(update);
        log.info("Created " + programMid);
    }

    @Test
    public void test05WaitFor() throws Exception {
        assumeNotNull(programMid);
        waitUntil(ACCEPTABLE_DURATION,
            programMid + " in backend",
            () -> {
            ProgramUpdate up = backend.get(programMid);
            return up != null;
        });
    }

    @Test
    public void test06CheckResult() throws Exception {
        assumeNotNull(programMid);
        ProgramUpdate up = backend.get(programMid);
        assertThat(up).isNotNull();
        assertThat(up.getMid()).isEqualTo(programMid);
        assertThat(up.getSegments()).hasSize(1);
        assertThat(up.getVersion()).isEqualTo(backendVersionNumber);
    }


    @Test
    public void test07UpdateSegmentDirectly() throws Exception {
        assumeNotNull(segmentMid);

        SegmentUpdate up = backend.get(segmentMid);
        assertThat(up.getMidRef()).isEqualTo(MID);
        updatedSegmentTitle = up.fetch().getMainTitle() + " -> " + title;
        up.setMainTitle(updatedSegmentTitle);

        backend.set(up);
    }

    @Test
    public void test08WaitFor() throws Exception {
        assumeNotNull(segmentMid);
        waitUntil(ACCEPTABLE_DURATION,
            segmentMid + " has title " + updatedSegmentTitle,
            () -> {
            SegmentUpdate up = backend.get(segmentMid);
            return up.fetch().getMainTitle().equals(updatedSegmentTitle);
        });
    }

    @Test
    public void test09UpdateSegmentViaProgram() throws Exception {
        assumeNotNull(segmentMid);
        ProgramUpdate programUpdate = backend.get(MID);

        SegmentUpdate segmentUpdate = programUpdate.getSegments().stream().filter(s -> s.getMid().equals(segmentMid)).findFirst().orElseThrow(IllegalStateException::new);
        updatedSegmentTitle = segmentUpdate.fetch().getMainTitle() + " -> " + title;
        segmentUpdate.setMainTitle(updatedSegmentTitle);

        backend.set(programUpdate);
    }


    @Test
    public void test10WaitFor() throws Exception {
        assumeNotNull(segmentMid);
        waitUntil(ACCEPTABLE_DURATION,
            segmentMid + "has title " + updatedSegmentTitle,
            () -> {
            SegmentUpdate up = backend.get(segmentMid);
            return up.fetch().getMainTitle().equals(updatedSegmentTitle);
        });
    }


    @Test
    public void test99Cleanup() throws Exception {
        Program program = backend.getFullProgram(MID);
        assumeNotNull(program);
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
    @Ignore
    public void testSegment() {
        Segment segment = (Segment) clients.getMediaService().load("POMS_VPRO_1460016", null, null);
        assertThat(segment.getMidRef()).isNotNull();
    }


}
