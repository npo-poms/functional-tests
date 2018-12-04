package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.ClientErrorException;

import org.junit.Test;

import nl.vpro.domain.api.media.MediaResult;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ApiMediaListTest extends AbstractApiTest {


    @Test
    public void list() {
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
    @Test(expected = ClientErrorException.class)
    public void listWithHugeOffset() {
        MediaResult result = mediaUtil.getClients()
            .getMediaService()
            .list(null, "ASC", 388560L, 240);

        assertThat(result.getSize()).isGreaterThan(0);
        for (MediaObject o : result) {
            log.info("{}", o);

        }
    }


}
