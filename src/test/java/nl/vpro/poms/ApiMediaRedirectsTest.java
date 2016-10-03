package nl.vpro.poms;

import javax.ws.rs.core.Response;

import org.junit.Before;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class ApiMediaRedirectsTest extends AbstractApiTest {


    @Before
    public void findRedirects() {
        Response redirects = clients.getMediaService().redirects(null);
    }
}
