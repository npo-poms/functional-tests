package nl.vpro.poms.npoapi;

import org.junit.jupiter.api.Test;

import nl.vpro.domain.api.media.MediaForm;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;

class ApiMediaSearchTest extends AbstractApiTest {


    @Test
    void searchWithProfileNone()  {
        assertThat(clients.getMediaService().find(new MediaForm(), null, "none", 0L, 10).asResult()).hasSize(10);

    }


}
