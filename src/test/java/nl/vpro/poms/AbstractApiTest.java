package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import nl.vpro.api.client.resteasy.NpoApiClients;
import nl.vpro.api.client.utils.NpoApiMediaUtil;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractApiTest {


    @Rule
    public AllowUnavailable unavailable = new AllowUnavailable();

    private static final String TITLE = Instant.now().toString();
    @Rule
    public TestName name = new TestName();
    protected String title;


    @Before
    public void setupTitle() {
        title = TITLE + " " + name.getMethodName() + " Caf\u00E9 \u6C49"; // testing encoding too!
        clients.setProfile(null);
        clients.setProperties("");
    }

    protected static final Duration ACCEPTABLE_DURATION_FRONTEND = Duration.ofMinutes(10);
    protected static final NpoApiClients clients =
        NpoApiClients.configured(Config.env(), Config.getProperties(Config.Prefix.npoapi))
            .mediaType(MediaType.APPLICATION_XML_TYPE)
            .trustAll(true)
            .build();
    protected static final MediaRestClient backend =
        MediaRestClient.configured(Config.env(), Config.getProperties(Config.Prefix.backendapi))
            .trustAll(true)
            .build();
    protected static final NpoApiMediaUtil mediaUtil = new NpoApiMediaUtil(clients);

    protected static final String apiVersion = clients.getVersion();
    protected static final String backendVersion = backend.getVersion();

    protected static Float apiVersionNumber;
    protected static Float backendVersionNumber;


    static {
        try {
             apiVersionNumber = clients.getVersionNumber();
        } catch (Exception  e) {
            apiVersionNumber = 0f;

        }
        try {
            backendVersionNumber = backend.getVersionNumber();
        } catch (Exception e) {
            backendVersionNumber = 0f;

        }
        log.info("Using {} ({}), {} ({})", clients, apiVersion, backend, backendVersion);
    }


}
