package nl.vpro.poms.integration;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class MediaITest extends AbstractApiTest {

    static String groupMid;
    static String clipMid;
    static String clipTitle;

    @Test
    public void test001CreateMedia() {
        clipTitle = title;
        clipMid = backend.set(
            ProgramUpdate
                .create(
                    MediaTestDataBuilder
                        .clip()
                        .constrainedNew()
                        .clearBroadcasters()
                        .mainTitle(clipTitle)
                        .broadcasters("VPRO")
                        .withAgeRating()

                        //.withImagesWithCredits()
                )
        );
        log.info("Created {} {}", clipMid, clipTitle);
        groupMid = backend.set(
            GroupUpdate.create(
                MediaTestDataBuilder
                    .playlist()
                    .constrainedNew()
                    .mainTitle(title)
                    .broadcasters("VPRO")
            ));
        String offlineGroup = backend.set(
            GroupUpdate.create(
                MediaTestDataBuilder
                    .playlist()
                    .constrainedNew()
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
        assertThat(clip).isNotNull();
        assertThat(clip.getMainTitle()).isEqualTo(clipTitle);
        assertThat(clip.getMemberOf().first().getMediaRef()).isEqualTo(groupMid);
        assertThat(clip.getMemberOf().first().getNumber()).isEqualTo(2);
        assertThat(clip.getMemberOf()).hasSize(1);


    }
}
