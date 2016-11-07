package nl.vpro.poms;

import org.junit.Test;

import nl.vpro.domain.api.media.MediaForm;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ApiMediaSearchTest extends AbstractApiTest {


    @Test
    public void searchWithProfileNone()  {
        assertThat(clients.getMediaService().find(new MediaForm(), null, "none", 0L, 10).asResult()).hasSize(10);

    }


}
