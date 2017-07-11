package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.Change;
import nl.vpro.domain.api.Deletes;
import nl.vpro.domain.api.Order;
import nl.vpro.domain.api.media.MediaFormBuilder;
import nl.vpro.domain.api.media.MediaResult;
import nl.vpro.domain.api.media.MediaSearchResult;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.MediaType;
import nl.vpro.domain.media.Schedule;
import nl.vpro.jackson2.JsonArrayIterator;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.poms.Config;

import static nl.vpro.api.client.utils.MediaRestClientUtils.sinceString;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
@Slf4j
public class ApiMediaTest extends AbstractApiTest {


    private Instant FROM = Instant.now().minus(Duration.ofDays(14));

    int couchdbSince;


    public ApiMediaTest(String properties) {
        clients.setProperties(properties);

    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() throws IOException {
        List<Object[]> result = new ArrayList<>();
        for (String properties : Arrays.asList(null, "none", "all")) {
            result.add(new Object[] {properties});
        }
        return result;
    }

    @Before
    public void setup() {
        switch(Config.env()) {
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
    }

    @Test
    public void members() throws Exception {
        MediaObject o = mediaUtil.loadOrNull("POMS_S_NCRV_096754");
        assertThat(o).isNotNull();
        MediaResult result = mediaUtil.listMembers(o.getMid(), Order.ASC, (m) -> true, 100);
        assertThat(result.getSize()).isGreaterThan(10);
    }

    @Test
    public void zeroMembers() throws Exception {
        MediaObject o = mediaUtil.loadOrNull("POMS_S_NCRV_096754");
        assertThat(o).isNotNull();
        MediaResult result = clients.getMediaService().listMembers(o.getMid(),  null, null, "ASC", 0L, 0);
        assertThat(result.getTotal()).isGreaterThan(10);
    }

    @Test
    public void findMembers() throws Exception {
        MediaSearchResult result = clients.getMediaService().findMembers(MediaFormBuilder.emptyForm(), "POMS_S_VPRO_407881", null, null, 0L, 100);
        assertThat(result.getSize()).isGreaterThan(1);

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void test404() {
        clients.getMediaService().load("BESTAATNIET", null, null);
    }

    @Test
    public void testChangesNoProfile() throws IOException {
        testChanges(null);
    }

    @Test
    public void testChangesWithProfile() throws IOException {
        testChanges("vpro");
    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testChangesMissingProfile() throws IOException {
        testChanges("bestaatniet");
    }

    @Test
    public void testChangesWithOldNoProfile() throws IOException {
        testChangesWithOld(null);
    }

    @Test
    public void testChangesWithOldAndProfile() throws IOException {
        testChangesWithOld("vpro");
    }


    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void testChangesOldMissingProfile() throws IOException {
        testChangesWithOld("bestaatniet");
    }


    @Test
    public void testChangesNoProfileCheckSkipDeletesMaxOne() throws IOException {
        assumeTrue(apiVersionNumber >= 5.4);
        final AtomicInteger i = new AtomicInteger();
        final Instant JAN2017 = LocalDate.of(2016, 1, 1).atStartOfDay(Schedule.ZONE_ID).toInstant();
        final int toFind = 100;
        int duplicateDates = 0;
        Instant start = JAN2017;
        Instant prev = start;
        String prevMid = null;
        String mid = null;
        List<Change> foundWithMaxOne = new ArrayList<>();
        while (i.getAndIncrement() < toFind) {
            InputStream inputStream = mediaUtil.getClients().getMediaServiceNoTimeout()
                .changes("vpro", null,null, sinceString(start, mid), null, 1, false, Deletes.EXCLUDE, null, null);

            try (JsonArrayIterator<Change> changes = new JsonArrayIterator<>(inputStream, Change.class, () -> IOUtils.closeQuietly(inputStream))) {
                Change change = changes.next();
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
        List<Change> foundWithMax100 = new ArrayList<>();
        try (JsonArrayIterator<Change> changes = mediaUtil.changes("vpro", false, JAN2017, null,  Order.ASC, toFind, Deletes.EXCLUDE)) {
            while (changes.hasNext()) {
                Change change = changes.next();
                foundWithMax100.add(change);
            }
        }

        assertThat(foundWithMaxOne).containsExactlyElementsOf(foundWithMax100);
        // assertThat(duplicateDates).isGreaterThan(0); TODO Find an example
    }


    @Test
    public void descendants() {
        MediaResult result = mediaUtil.listDescendants("RBX_S_NTR_553927",
            Order.DESC, input -> input.getMediaType() == MediaType.BROADCAST, 123);
        assertThat(result.getSize()).isEqualTo(123);

    }

    @Test
    public void related() {
        String mid = "RBX_S_NTR_553927";
        MediaResult result = mediaUtil.getClients()
            .getMediaService()
            .listRelated(mid, null, null);
        assertThat(result.getSize()).isGreaterThan(0);
        log.info("Related to {}", mid);
        for (MediaObject o : result) {
            log.info("{}", o);

        }
    }

    protected void testChanges(String profile) throws IOException {
        final AtomicInteger i = new AtomicInteger();
        Instant prev = FROM;
        try(JsonArrayIterator<Change> changes = mediaUtil.changes(profile, FROM, Order.ASC, 10)) {
            while (changes.hasNext()) {
                Change change = changes.next();
                if (!change.isTail()) {
                    i.incrementAndGet();
                    if (i.get() > 100) {
                        break;
                    }
                    assertThat(change.getSequence()).isNull();
                    assertThat(change.getPublishDate()).isGreaterThanOrEqualTo(prev);
                    assertThat(change.getRevision() == null || change.getRevision() > 0).isTrue();
                    prev = change.getPublishDate();
                    log.info("{}", change);
                }
            }
        }
        assertThat(i.intValue()).isEqualTo(10);
    }

    void testChangesWithOld(String profile) throws IOException {
        final AtomicInteger i = new AtomicInteger();
        long startSequence = couchdbSince;
        Instant prev = null;
        try (JsonArrayIterator<Change> changes = mediaUtil.changes(profile, startSequence, Order.ASC, 100)) {
            while (changes.hasNext()) {
                Change change = changes.next();
                if (!change.isTail()) {
                    i.incrementAndGet();
                    if (i.get() > 100) {
                        break;
                    }
                    assertThat(change.getSequence()).isNotNull();
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
