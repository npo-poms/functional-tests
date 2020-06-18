package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;

import nl.vpro.api.client.utils.MediaRestClientUtils;
import nl.vpro.domain.api.*;
import nl.vpro.domain.api.Order;
import nl.vpro.domain.api.profile.Profile;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.Schedule;
import nl.vpro.jackson2.JsonArrayIterator;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.util.*;

import static nl.vpro.api.client.utils.MediaRestClientUtils.sinceString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Log4j2
class ApiMediaStreamingCallsTest extends AbstractApiTest {


    private Instant FROM = Instant.now().minus(Duration.ofDays(14));

    private static final int CHANGES_MAX = 100;

    int couchdbSince;


    @BeforeEach
    void setup() {
        switch(CONFIG.env()) {
            case DEV:
                couchdbSince = 25387000;
                FROM = Instant.now().minus(Duration.ofDays(100));
                break;
            case TEST:
                couchdbSince = 19831435;
                break;
            default:
                couchdbSince = 20794000;
                break;
        }
        mediaUtil.getClients().getMediaServiceNoTimeout();
    }


    @Test
    public void testChangesNoProfile() throws Exception {
        testChanges(null, FROM, CHANGES_MAX);
    }


    @Test
    public void testCloseChanges() throws Exception {

        testChanges(null,
            LocalDateTime.of(2017, 6, 12, 10, 32).atZone(Schedule.ZONE_ID).toInstant(), 40000);
    }

    @Test
    public void testChangesWithProfile() throws Exception {
        testChanges("vpro-predictions", FROM, CHANGES_MAX);
    }

    @Test
    public void testChangesMissingProfile() {
        assertThrows(javax.ws.rs.NotFoundException.class, () -> {
            testChanges("bestaatniet", FROM, CHANGES_MAX);
        });
    }

    @Test
    public void testIterateMissingProfile() {
        assertThrows(javax.ws.rs.NotFoundException.class, () -> testIterate("bestaatniet", CHANGES_MAX));
    }

    @Test
    public void testChangesWithOldNoProfile() throws IOException {
        testChangesWithOld(null, CHANGES_MAX);
    }

    @Test
    @Disabled("No need to support this any more")
    public void testChangesWithOldAndProfile() throws IOException {
        testChangesWithOld("vpro-predictions", CHANGES_MAX);
    }


    @Test
    @Disabled("No need to support this any more")
    public void testChangesOldMissingProfile() {
        assertThrows(javax.ws.rs.NotFoundException.class, () ->
            testChangesWithOld("bestaatniet", CHANGES_MAX)
        );
    }


    @SuppressWarnings("deprecation")
    @Test
    public void testChangesNoProfileCheckSkipDeletesMaxOne() throws Exception {
        assumeTrue(apiVersionNumber.isNotBefore(Version.of(5, 4)));
        final AtomicInteger i = new AtomicInteger();
        final Instant JAN2017 = LocalDate.of(2017, 1, 1).atStartOfDay(Schedule.ZONE_ID).toInstant();
        final int toFind = 100;
        int duplicateDates = 0;
        Instant start = JAN2017;
        Instant prev = start;
        String prevMid = null;
        String mid = null;
        List<MediaChange> foundWithMaxOne = new ArrayList<>();
        while (i.getAndIncrement() < toFind) {
            InputStream inputStream = mediaUtil.getClients().getMediaServiceNoTimeout()
                .changes("vpro", null,null, sinceString(start, mid), null, 1, false, Deletes.EXCLUDE, null).readEntity(InputStream.class);

            try (JsonArrayIterator<MediaChange> changes = new JsonArrayIterator<>(inputStream, MediaChange.class, () -> IOUtils.closeQuietly(inputStream))) {
                MediaChange change = changes.next();
                start = change.getPublishDate();
                assertThat(change.getSequence()).isNull();
                assertThat(change.isDeleted()).isFalse();
                if (change.isDeleted()) {
                    assertThat(change.getMedia()).isNull();
                }

                if (change.getPublishDate().equals(prev)) {
                    log.info("Found a multiple date {}", prev);
                    duplicateDates++;
                }
                assertThat(change.getPublishDate()).isAfterOrEqualTo(prev);
                assertThat(change.getRevision() == null || change.getRevision() > 0).isTrue();
                assertThat(change.getMid()).withFailMessage(change.getMid() + " should be different from " + mid).isNotEqualTo(mid);
                prev = change.getPublishDate();
                mid = change.getMid();
                log.info("{}", change);
                foundWithMaxOne.add(change);
            }
        }
        List<MediaChange> foundWithMax100 = new ArrayList<>();
        try (CloseableIterator<MediaChange> changes = mediaUtil.changes("vpro", false, JAN2017, null,  Order.ASC, toFind, Deletes.EXCLUDE)) {
            while (changes.hasNext()) {
                MediaChange change = changes.next();
                foundWithMax100.add(change);
            }
        }

        assertThat(foundWithMaxOne).containsExactlyElementsOf(foundWithMax100);
        // assertThat(duplicateDates).isGreaterThan(0); TODO Find an example
    }


    @Test
    public void NPA_453() throws IOException {
        //https://rs.poms.omroep.nl/v1/api/media/changes?profile=bnnvara&publishedSince=2015-03-22T03%3A43%3A05Z%2CRBX_EO_667486&order=asc&max=100&checkProfile=true&deletes=INCLUDE
        Instant start = Instant.parse("2015-03-22T03:43:05Z");
        InputStream inputStream = MediaRestClientUtils.toInputStream(mediaUtil.getClients().getMediaServiceNoTimeout()
            .changes("bnnvara", null,null, sinceString(start, "RBX_EO_667486"), "asc", 100, true, Deletes.INCLUDE, Tail.IF_EMPTY));

        try (JsonArrayIterator<MediaChange> changes = new JsonArrayIterator<>(inputStream,
            MediaChange.class, () -> IOUtils.closeQuietly(inputStream))) {
            MediaChange change = changes.next();
            log.info("{}", change);
        }



    }

    @SuppressWarnings("deprecation")
    protected void testChanges(String profile, Instant from, Integer max) throws Exception {
        Instant start = Instant.now();
        final AtomicInteger i = new AtomicInteger();
        Instant prev = from;
        try(CloseableIterator<MediaChange> changes = mediaUtil.changes(profile, false,  from, null, Order.ASC, max, Deletes.ID_ONLY)) {
            while (changes.hasNext()) {
                MediaChange change = changes.next();
                if (!change.isTail()) {

                    assertThat(change.getSequence()).isNull();
                    assertThat(change.getPublishDate()).withFailMessage("%s has no publish date", change).isNotNull();
                    assertThat(change.getPublishDate()).isAfterOrEqualTo(prev);
                    assertThat(change.getRevision() == null || change.getRevision() > 0).isTrue();
                    prev = change.getPublishDate();
                    if (i.incrementAndGet() % 1000 == 0) {
                        log.info("{}: {}", i.get(), change);
                    }
                }
            }
        }
        assertThat(prev.isBefore(start.minus(Duration.ofSeconds(9))));
        if (max != null) {
            assertThat(i.get()).isLessThanOrEqualTo(max);
        }
    }



    @Test
    public void testIterate() throws Exception {
        testIterate("vpro", CHANGES_MAX);

    }


    protected void testIterate(String profile, Integer max) throws Exception {
        Profile profileObject = mediaUtil.getClients().getProfileService().load(profile, null);
        try(CountedIterator<MediaObject> iterator = MediaRestClientUtils.iterate(() -> mediaUtil.getClients().getMediaServiceNoTimeout().iterate(null, profile, null, 0L, max), true, "test")) {
            int i = 0;
            while (iterator.hasNext()) {
                MediaObject mediaObject = iterator.next();
                log.info("{}: {}", ++i, mediaObject);
                assertThat(profileObject.getMediaProfile().test(mediaObject)).isTrue();
            }
            assertThat(i).isEqualTo(max);
        }
    }

    // COUCHDB only triggered if setting mediaService.changesRepository=COUCHDB on server!
    @SuppressWarnings("deprecation")
    void testChangesWithOld(String profile, Integer max) throws IOException {
        final AtomicInteger i = new AtomicInteger();
        long startSequence = couchdbSince;
        Instant prev = null;
        try (JsonArrayIterator<MediaChange> changes = mediaUtil.changes(profile, startSequence, Order.ASC, max)) {
            while (changes.hasNext()) {
                MediaChange change = changes.next();
                if (!change.isTail()) {
                    i.incrementAndGet();
                    if (i.get() > 100) {
                        break;
                    }
                    if (apiVersionNumber.isBefore(5, 3)) {
                        assertThat(change.getSequence()).isNotNull();
                    }
                    assertThat(change.getRevision() == null || change.getRevision() > 0).isTrue();
                    if (! change.isDeleted()) {
                        if (change.getPublishDate() == null) {
                            log.warn("Publish date of {} is null", change);
                        }
                        //assertThat(change.getPublishDate()).isNotNull();
                    }
                    if (prev != null) {
                        if (change.getPublishDate() != null) { // couchdb?
                            assertThat(change.getPublishDate())
                                .isAfterOrEqualTo(prev.minus(1, ChronoUnit.MINUTES)
                                    .truncatedTo(ChronoUnit.MINUTES));
                        }
                    }
                    if (change.getPublishDate() != null) {
                        prev = change.getPublishDate();
                    }
                    log.info("{}", change);

                }
            }
        }
        assertThat(i.intValue()).isEqualTo(100);

    }

}
