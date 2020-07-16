package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import javax.ws.rs.ClientErrorException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.vpro.domain.api.media.MediaResult;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class ApiMediaListTest extends AbstractApiTest {


    @Test
    void list() {
        MediaResult result = mediaUtil.getClients()
            .getMediaService()
            .list(null, "ASC", 0L, 240);

        assertThat(result.getSize()).isGreaterThan(0);
        for (MediaObject o : result) {
            log.info("{}", o);

        }
    }


    /**
     * NPA-461
     */
    @Test
    void listWithHugeOffset() {
        Assertions.assertThrows(ClientErrorException.class, () -> {
            MediaResult result = mediaUtil.getClients()
                .getMediaService()
                .list(null, "ASC", 388560L, 240);

            assertThat(result.getSize()).isGreaterThan(0);
            for (MediaObject o : result) {
                log.info("{}", o);

            }
        });
    }


}
