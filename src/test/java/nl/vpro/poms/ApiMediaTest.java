package nl.vpro.poms;

import java.time.Duration;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.domain.api.Change;
import nl.vpro.domain.api.Order;
import nl.vpro.domain.api.media.MediaResult;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.jackson2.JsonArrayIterator;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ApiMediaTest extends AbstractApiTest {


    @Before
    public void setup() {

    }

    @Test
    public void members() throws Exception {
        MediaObject o = mediaUtil.loadOrNull("POMS_S_VPRO_548106");
        assertThat(o).isNotNull();
        MediaResult result = mediaUtil.listMembers(o.getMid(), Order.ASC, (m) -> true, 100);
        assertThat(result.getSize()).isGreaterThan(10);
    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void test404() {
        clients.getMediaService().load("BESTAATNIET", null, null);
    }

    @Test
    public void testChanges() {
        JsonArrayIterator<Change> changes = mediaUtil.changes("vpro", Instant.now().minus(Duration.ofDays(2)), Order.DESC, 100);
        while(changes.hasNext()) {
            System.out.println(changes.next());
        }
    }


}
