package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import javax.ws.rs.BadRequestException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import nl.vpro.domain.api.Error;
import nl.vpro.domain.api.Order;
import nl.vpro.domain.api.media.*;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.MediaType;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class ApiMediaTest extends AbstractApiTest {

    ApiMediaTest(String properties) {
        clients.setProperties(properties);

    }
    @ValueSource(strings = {"none", "all"})
    @NullSource
    @interface Properties {

    }

    @ParameterizedTest
    @Properties
    public void members(String properties) throws Exception {
        clients.setProperties(properties);
        MediaObject o = mediaUtil.loadOrNull("POMS_S_NCRV_096754");
        assertThat(o).isNotNull();
        MediaResult result = mediaUtil.listMembers(o.getMid(), Order.ASC, (m) -> true, 100);
        assertThat(result.getSize()).isGreaterThan(10);
    }

    @ParameterizedTest
    @Properties
    public void zeroMembers(String properties) throws Exception {
        clients.setProperties(properties);
        MediaObject o = mediaUtil.loadOrNull("POMS_S_NCRV_096754");
        assertThat(o).isNotNull();
        MediaResult result = clients.getMediaService().listMembers(o.getMid(),  null, null, "ASC", 0L, 0);
        assertThat(result.getTotal()).isGreaterThan(10);
        assertThat(result.getSize()).isEqualTo(0);

    }

    @ParameterizedTest
    @Properties
    public void findMembers(String properties) {
        clients.setProperties(properties);
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

    @Test
    void test404Youtube() {
        assertThatThrownBy(() -> {
            // FAILS on DEV
            clients.getMediaService()
                .load(
                    "https://www.youtube.com/watch?v=1XiY_mhzd3Q",
                    null, null);
        }).isInstanceOf(javax.ws.rs.NotFoundException.class);
    }



    @Test
    void test404LoadOrNull() throws IOException {
        assertThat((MediaObject) mediaUtil.loadOrNull("https://www.youtube.com/watch?v=1XiY_mhzd3Q")).isNull();
    }

    @ParameterizedTest
    @Properties
    void descendants(String properties) {
        clients.setProperties(properties);
        MediaResult result = mediaUtil.listDescendants("RBX_S_NTR_553927",
            Order.DESC, input -> input.getMediaType() == MediaType.BROADCAST, 123);
        assertThat(result.getSize()).isEqualTo(123);

    }

    /**
     * See NPA-488
     */
    @Test
    void badRequestOnOffset() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            MediaResult result = clients.getMediaService().listDescendants("RBX_S_NTR_553927", null, null, Order.DESC.toString(),
                -1L, 0);
        });

    }

    @Test
    void badRequestOnMax() {
        Assertions.assertThrows(BadRequestException.class, () -> {

            MediaResult result = clients.getMediaService().listDescendants("RBX_S_NTR_553927", null, null, Order.DESC.toString(),
                0L, 1_000_000);
        });

    }

    @ParameterizedTest
    @Properties
    void related(String properties) {
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
