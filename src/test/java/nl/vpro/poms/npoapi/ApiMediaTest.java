package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.Order;
import nl.vpro.domain.api.media.MediaFormBuilder;
import nl.vpro.domain.api.media.MediaResult;
import nl.vpro.domain.api.media.MediaSearchResult;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.MediaType;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(Parameterized.class)
@Slf4j
public class ApiMediaTest extends AbstractApiTest {

    public ApiMediaTest(String properties) {
        clients.setProperties(properties);

    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        List<Object[]> result = new ArrayList<>();
        for (String properties : Arrays.asList(null, "none", "all")) {
            result.add(new Object[] {properties});
        }
        return result;
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
        assertThat(result.getSize()).isEqualTo(0);

    }

    @Test
    public void findMembers() {
        MediaSearchResult result = clients.getMediaService().findMembers(MediaFormBuilder.emptyForm(), "POMS_S_VPRO_407881", null, null, 0L, 100);
        assertThat(result.getSize()).isGreaterThan(1);

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void test404() {
        clients.getMediaService().load("BESTAATNIET", null, null);
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


}
