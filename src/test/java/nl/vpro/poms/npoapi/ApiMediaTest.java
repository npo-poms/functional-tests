package nl.vpro.poms.npoapi;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.domain.api.Change;
import nl.vpro.domain.api.Order;
import nl.vpro.domain.api.media.MediaResult;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.jackson2.JsonArrayIterator;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ApiMediaTest extends AbstractApiTest {


    @Before
    public void setup() {

    }

    @Test
    public void members() throws Exception {
        MediaObject o = mediaUtil.loadOrNull("POMS_S_NCRV_096754");
        assertThat(o).isNotNull();
        MediaResult result = mediaUtil.listMembers(o.getMid(), Order.ASC, (m) -> true, 100);
        assertThat(result.getSize()).isGreaterThan(10);
    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void test404() {
        clients.getMediaService().load("BESTAATNIET", null, null);
    }

    @Test
    public void testChangesNoProfile() {
        testChanges(null);
    }
    @Test
    public void testChangesWithProfile() {
        testChanges("vpro");
    }
    protected void testChanges(String profile) {
        final AtomicInteger i = new AtomicInteger();
        Instant start = Instant.now().minus(Duration.ofDays(14));
        final Instant[] prev = new Instant[] {start};
        JsonArrayIterator<Change> changes = mediaUtil.changes(profile, start, Order.ASC, 10);
        changes.forEachRemaining(change -> {
            if (!change.isTail()) {
                i.incrementAndGet();
                assertThat(change.getSequence()).isNull();
                assertThat(change.getPublishDate()).isGreaterThanOrEqualTo(prev[0]);
                prev[0] = change.getPublishDate();
                System.out.println(change);
            }
        });
        assertThat(i.intValue()).isEqualTo(10);
    }

    @Test
    public void testChangesWithOldNoProfile() {
        testChangesWithOld(null);
    }

    @Test
    public void testChangesWithOldAndProfile() {
        testChangesWithOld("vpro");
    }
    void testChangesWithOld(String profile) {
        final AtomicInteger i = new AtomicInteger();
        long startSequence = 1209428L;
        JsonArrayIterator<Change> changes = mediaUtil.changes(profile, startSequence, Order.ASC, 100);
        changes.forEachRemaining(change -> {
            if (!change.isTail()) {
                i.incrementAndGet();
                assertThat(change.getSequence()).isNotNull();
                System.out.println(change);
            }
        });
        assertThat(i.intValue()).isEqualTo(100);

    }


}
