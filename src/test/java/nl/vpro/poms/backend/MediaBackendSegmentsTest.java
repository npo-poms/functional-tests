package nl.vpro.poms.backend;

import java.time.Duration;
import java.time.Instant;

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
import nl.vpro.util.DateUtils;
import nl.vpro.util.TimeUtils;

import static nl.vpro.poms.Utils.waitUntil;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assume.assumeNotNull;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
        System.out.println("Created " + segmentMid);

    }

    @Test
    public void test02WaitFor() throws Exception {
        assumeNotNull(segmentMid);
        waitUntil(ACCEPTABLE_DURATION, () -> {
            SegmentUpdate up = backend.get(segmentMid);
            return up != null;
        });
    }


    @Test
    public void test03WaitForInFrontend() throws Exception {
        assumeNotNull(segmentMid);
        Segment[] segments = new Segment[1];
        waitUntil(ACCEPTABLE_DURATION_FRONTEND, () -> {
            segments[0] = mediaUtil.loadOrNull(segmentMid);
            if (segments[0] == null) {
                return false;
            }
            Instant lastPublished = DateUtils.toInstant(segments[0].getLastPublished());
            if (lastPublished == null) {
                throw new IllegalStateException("The last published field of " + segments[0] + " is null!");
            }
            return TimeUtils.isLarger(TimeUtils.between(lastPublished, Instant.now()), Duration.ofMinutes(20));
        });
        assertThat(segments[0]).isNotNull();
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
        System.out.println("Created " + programMid);
    }

    @Test
    public void test05WaitFor() throws Exception {
        assumeNotNull(programMid);
        waitUntil(ACCEPTABLE_DURATION, () -> {
            ProgramUpdate up = backend.get(programMid);
            return up != null;
        });
    }

    @Test
    public void test06CheckResult() throws Exception {
        assumeNotNull(programMid);
        ProgramUpdate up = backend.get(programMid);
        assertThat(up.getMid()).isEqualTo(programMid);
        assertThat(up.getSegments()).hasSize(1);
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
        waitUntil(ACCEPTABLE_DURATION, () -> {
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
        waitUntil(ACCEPTABLE_DURATION, () -> {
            SegmentUpdate up = backend.get(segmentMid);
            return up.fetch().getMainTitle().equals(updatedSegmentTitle);
        });
    }


    @Test
    public void test11DeleteSegementsViaProgram() throws Exception {
        Program programUpdate = backend.getFullProgram(MID);

        programUpdate.getSegments().forEach((segment) -> {
            if (DateUtils.toInstant(segment.getLastModified()).isBefore(Instant.now().minus(Duration.ofDays(3)))) {
                System.out.println("Deleting " + segment);
                backend.delete(segment.getMid());
            }
        });
    }


    @Test
    @Ignore
    public void testSegment() {
        Segment segment = (Segment) clients.getMediaService().load("POMS_VPRO_1460016", null, null);
        assertThat(segment.getMidRef()).isNotNull();
    }


}
