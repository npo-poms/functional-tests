package nl.vpro.poms.client;

import org.junit.Test;

import nl.vpro.domain.media.update.MediaUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
public class BackendApiClientTest extends AbstractApiMediaBackendTest  {

    @Test
    public void getByCrid() {
        MediaUpdate<?> mediaUpdate = backend.get("crid://tmp.fragment.mmbase.vpro.nl/43084334");
        assertThat(mediaUpdate).isNotNull();
        assertThat(mediaUpdate.getMid()).isEqualTo("WO_VPRO_034420");
    }
}
