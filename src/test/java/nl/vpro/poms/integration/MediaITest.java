package nl.vpro.poms.integration;

import java.time.Duration;
import java.time.Instant;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.MediaTestDataBuilder;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.update.GroupUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiTest;

import static nl.vpro.poms.Utils.waitUntil;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assume.assumeNotNull;

/**
 * Create items, and check them in frontend api
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MediaITest extends AbstractApiTest {

    String groupMid;
    String clipMid;
    String clipTitle;

    @Test
    public void test001CreateMedia() {
        clipTitle = title;
        clipMid = backend.set(
            ProgramUpdate
                .create(
                    MediaTestDataBuilder
                        .clip()
                        .mainTitle(clipTitle)
                        .broadcasters("VPRO")
                        .withAgeRating()
                        .withSegments()
                        .withImagesWithCredits()
                )
        );
        groupMid = backend.set(
            GroupUpdate.create(
                MediaTestDataBuilder
                .playlist()
                .mainTitle(title)
                .broadcasters("VPRO")
        ));
        String offlineGroup = backend.set(
            GroupUpdate.create(
                MediaTestDataBuilder
                    .playlist()
                    .mainTitle(title + " offline")
                    .publishStop(Instant.now().minus(Duration.ofMinutes(5)))
                    .broadcasters("VPRO")
            ));
        backend.createMember(offlineGroup, clipMid, 1);
        backend.createMember(groupMid, clipMid, 2);
    }

    @Test
    public void test101CheckFrontendApi() throws Exception {
        assumeNotNull(clipMid);
        Program clip = waitUntil(Duration.ofMinutes(10), () -> mediaUtil.findByMid(clipMid), (c) -> !c.getMemberOf().isEmpty());
        assertThat(clip.getMainTitle()).isEqualTo(clipTitle);
        assertThat(clip.getMemberOf().first().getMediaRef()).isEqualTo(groupMid);
        assertThat(clip.getMemberOf().first().getNumber()).isEqualTo(2);
        assertThat(clip.getMemberOf()).hasSize(1);


    }
}