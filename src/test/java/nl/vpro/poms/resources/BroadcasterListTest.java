package nl.vpro.poms.resources;

import java.net.URI;
import java.util.Arrays;
import java.util.Properties;

import org.junit.Test;

import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.poms.Config;
import nl.vpro.util.URLResource;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
public class BroadcasterListTest {


    @Test
    public void testAvailable() {
        String baseUrl = Config.requiredOption(Config.Prefix.poms, "baseUrl");

        URLResource<Properties> broadcasters = URLResource.properties(URI.create(baseUrl + "/broadcasters/"));

        assertThat(broadcasters.get().get("VPRO")).isEqualTo("VPRO");
        assertThat(broadcasters.get().size()).isGreaterThan(10);
    }


    @Test
    public void testAvailableWhatson() {
        String baseUrl = Config.requiredOption(Config.Prefix.poms, "baseUrl");

        for (OwnerType ot : Arrays.asList(OwnerType.NEBO, OwnerType.MIS, OwnerType.WHATS_ON)) {
            URLResource<Properties> broadcasters = URLResource.properties(URI.create(baseUrl + "/broadcasters/" + ot));

            assertThat(broadcasters.get().get("VPRO")).isEqualTo("VPRO");
            assertThat(broadcasters.get().size()).isGreaterThan(10);
        }
    }
}
