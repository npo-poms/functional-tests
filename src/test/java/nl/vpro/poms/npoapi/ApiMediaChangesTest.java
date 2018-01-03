package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import nl.vpro.domain.api.Deletes;
import nl.vpro.domain.api.MediaChange;
import nl.vpro.domain.api.Order;
import nl.vpro.domain.media.Schedule;
import nl.vpro.jackson2.JsonArrayIterator;
import nl.vpro.poms.AbstractApiTest;

import static nl.vpro.api.client.utils.MediaRestClientUtils.sinceString;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@Slf4j
public class ApiMediaChangesTest extends AbstractApiTest {


    private Instant FROM = Instant.now().minus(Duration.ofDays(14));

    private int CHANGES_MAX = 100;

    int couchdbSince;


    @Before
    public void setup() {
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
    public void testChangesNoProfile() throws IOException {
        testChanges(null, FROM, CHANGES_MAX);
    }


    @Test
    public void testCloseChanges() throws IOException {

        testChanges(null,
            LocalDateTime.of(2017, 6, 12, 10, 32).atZone(Schedule.ZONE_ID).toInstant(), 40000);
    }

    @Test
    public void testChangesWithProfile() throws IOException {
        testChanges("vpro-predictions", FROM, CHANGES_MAX);
    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testChangesMissingProfile() throws IOException {
        testChanges("bestaatniet", FROM, CHANGES_MAX);
    }

    @Test
    public void testChangesWithOldNoProfile() throws IOException {
        testChangesWithOld(null, CHANGES_MAX);
    }

    @Test
    public void testChangesWithOldAndProfile() throws IOException {
        testChangesWithOld("vpro-predictions", CHANGES_MAX);
    }


    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testChangesOldMissingProfile() throws IOException {
        testChangesWithOld("bestaatniet", CHANGES_MAX);
    }


    @Test
    public void testChangesNoProfileCheckSkipDeletesMaxOne() throws IOException {
        assumeTrue(apiVersionNumber >= 5.4);
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
                .changes("vpro", null,null, sinceString(start, mid), null, 1, false, Deletes.EXCLUDE, null, null);

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
                assertThat(change.getPublishDate()).isGreaterThanOrEqualTo(prev);
                assertThat(change.getRevision() == null || change.getRevision() > 0).isTrue();
                assertThat(change.getMid()).withFailMessage(change.getMid() + " should be different from " + mid).isNotEqualTo(mid);
                prev = change.getPublishDate();
                mid = change.getMid();
                log.info("{}", change);
                foundWithMaxOne.add(change);
            }
        }
        List<MediaChange> foundWithMax100 = new ArrayList<>();
        try (JsonArrayIterator<MediaChange> changes = mediaUtil.changes("vpro", false, JAN2017, null,  Order.ASC, toFind, Deletes.EXCLUDE)) {
            while (changes.hasNext()) {
                MediaChange change = changes.next();
                foundWithMax100.add(change);
            }
        }

        assertThat(foundWithMaxOne).containsExactlyElementsOf(foundWithMax100);
        // assertThat(duplicateDates).isGreaterThan(0); TODO Find an example
    }


    protected void testChanges(String profile, Instant from, Integer max) throws IOException {
        Instant start = Instant.now();
        final AtomicInteger i = new AtomicInteger();
        Instant prev = from;
        try(JsonArrayIterator<MediaChange> changes = mediaUtil.changes(profile, false,  from, null, Order.ASC, max, Deletes.ID_ONLY)) {
            while (changes.hasNext()) {
                MediaChange change = changes.next();
                if (!change.isTail()) {

                    assertThat(change.getSequence()).isNull();
                    assertThat(change.getPublishDate()).withFailMessage("%s has no publish date", change).isNotNull();
                    assertThat(change.getPublishDate()).isGreaterThanOrEqualTo(prev);
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
                    if (apiVersionNumber < 5.3) {
                        assertThat(change.getSequence()).isNotNull();
                    }
                    assertThat(change.getRevision() == null || change.getRevision() > 0).isTrue();
                    if (! change.isDeleted()) {
                        // cannot be filled by couchdb.
                        assertThat(change.getPublishDate()).isNotNull();
                    }
                    if (prev != null) {
                        if (change.getPublishDate() != null) { // couchdb?
                            assertThat(change.getPublishDate())
                                .isGreaterThanOrEqualTo(prev.minus(1, ChronoUnit.MINUTES)
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
