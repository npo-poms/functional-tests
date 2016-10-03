package nl.vpro.poms;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.domain.api.media.RedirectEntry;
import nl.vpro.domain.api.media.RedirectList;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class ApiMediaRedirectsTest extends AbstractApiTest {

    RedirectList list;

    @Before
    public void findRedirects() {
        Response response = clients.getMediaService().redirects(null);
        list = response.readEntity(RedirectList.class);
    }

    @Test
    public void testRedirect() {
        RedirectEntry entry = list.getList().get(0);

        assertThat(clients.getMediaService().load(entry.getFrom(), null, null).getMid()).isEqualTo(entry.getTo());
    }
}
