package nl.vpro.poms.resources;

import java.net.URI;
import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.junit.extensions.TestMDC;
import nl.vpro.util.URLResource;

import static nl.vpro.testutils.Utils.CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@ExtendWith(TestMDC.class)
public class BroadcasterListTest {


    @Test
    public void testAvailable() {
        String baseUrl = CONFIG.requiredOption(Config.Prefix.poms, "baseUrl");

        URLResource<Properties> broadcasters = URLResource.properties(URI.create(baseUrl + "/broadcasters/"));

        assertThat(broadcasters.get().get("VPRO")).isEqualTo("VPRO");
        assertThat(broadcasters.get().size()).isGreaterThan(10);
    }


    @Test
    public void testAvailableWhatson() {
        String baseUrl = CONFIG.requiredOption(Config.Prefix.poms, "baseUrl");

        for (OwnerType ot : Arrays.asList(OwnerType.NEBO, OwnerType.MIS, OwnerType.WHATS_ON)) {
            URLResource<Properties> broadcasters = URLResource.properties(URI.create(baseUrl + "/broadcasters/" + ot));

            assertThat(broadcasters.get().get("VPRO")).isEqualTo("VPRO");
            assertThat(broadcasters.get().size()).isGreaterThan(10);
        }
    }
}
