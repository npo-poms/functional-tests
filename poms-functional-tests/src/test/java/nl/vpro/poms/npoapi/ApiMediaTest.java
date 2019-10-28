package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import javax.ws.rs.BadRequestException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.Error;
import nl.vpro.domain.api.Order;
import nl.vpro.domain.api.media.*;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.MediaType;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
@Slf4j
class ApiMediaTest extends AbstractApiTest {

    ApiMediaTest(String properties) {
        clients.setProperties(properties);

    }

    @BeforeEach
    @ValueSource(strings = {"none", "all"})
    @NullSource
    void setup(String properties) {
        clients.setProperties(properties);

    }


    @Test
    void members() throws Exception {
        MediaObject o = mediaUtil.loadOrNull("POMS_S_NCRV_096754");
        assertThat(o).isNotNull();
        MediaResult result = mediaUtil.listMembers(o.getMid(), Order.ASC, (m) -> true, 100);
        assertThat(result.getSize()).isGreaterThan(10);
    }

    @Test
    void zeroMembers() throws Exception {
        MediaObject o = mediaUtil.loadOrNull("POMS_S_NCRV_096754");
        assertThat(o).isNotNull();
        MediaResult result = clients.getMediaService().listMembers(o.getMid(),  null, null, "ASC", 0L, 0);
        assertThat(result.getTotal()).isGreaterThan(10);
        assertThat(result.getSize()).isEqualTo(0);

    }

    @Test
    void findMembers() {
        MediaSearchResult result = clients.getMediaService().findMembers(MediaFormBuilder.emptyForm(), "POMS_S_VPRO_407881", null, null, 0L, 100);
        assertThat(result.getSize()).isGreaterThan(1);

    }

    @Test
    void test404() {
        Assertions.assertThrows(javax.ws.rs.NotFoundException.class, () -> {
            clients.getMediaService().load("BESTAATNIET", null, null);
        });
    }
    @Test
    void test404WithSlash() {
        Assertions.assertThrows(javax.ws.rs.NotFoundException.class, () -> {

            try {
                clients.getMediaService().load("BESTAAT/NIET", null, null);
            } catch (javax.ws.rs.NotFoundException nfe) {
                log.info("{}", nfe.getResponse(), nfe);
                Error error = (Error) nfe.getResponse().getEntity();
                // TODO Fails@ NPO
                //assertThat(error.getMessage()).contains("BESTAAT/NIET");
                throw nfe;
            }
        });
    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    void test404Youtube() {
        // FAILS on DEV
        clients.getMediaService()
            .load(
                "https://www.youtube.com/watch?v=1XiY_mhzd3Q",
                null, null);
    }



    @Test
    void test404LoadOrNull() throws IOException {
        assertThat((MediaObject) mediaUtil.loadOrNull("https://www.youtube.com/watch?v=1XiY_mhzd3Q")).isNull();
    }

    @Test
    void descendants() {
        MediaResult result = mediaUtil.listDescendants("RBX_S_NTR_553927",
            Order.DESC, input -> input.getMediaType() == MediaType.BROADCAST, 123);
        assertThat(result.getSize()).isEqualTo(123);

    }

    /**
     * See NPA-488
     */
    @Test(expected = BadRequestException.class)
    void badRequestOnOffset() {
        MediaResult result = clients.getMediaService().listDescendants("RBX_S_NTR_553927", null, null, Order.DESC.toString(),
            -1L, 0);

    }

    @Test(expected = BadRequestException.class)
    void badRequestOnMax() {
        MediaResult result = clients.getMediaService().listDescendants("RBX_S_NTR_553927", null, null, Order.DESC.toString(),
            0L, 1_000_000);

    }

    @Test
    void related() {
        String mid = "RBX_S_NTR_553927";
        MediaResult result = mediaUtil.getClients()
            .getMediaService()
            .listRelated(mid, null, null, null);
        assertThat(result.getSize()).isGreaterThan(0);
        log.info("Related to {}", mid);
        for (MediaObject o : result) {
            log.info("{}", o);

        }
    }



}
