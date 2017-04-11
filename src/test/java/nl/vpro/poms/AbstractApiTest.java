package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import nl.vpro.api.client.resteasy.NpoApiClients;
import nl.vpro.api.client.utils.NpoApiMediaUtil;
import nl.vpro.api.client.utils.NpoApiPageUtil;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractApiTest {


    @Rule
    public AllowUnavailable unavailable = new AllowUnavailable();

    @Rule
    public AllowNotImplemented notImplemented = new AllowNotImplemented();

    private static final String NOW = Instant.now().toString();

    @Rule
    public TestName name = new TestName();

    protected String title;


    @Before
    public void setupTitle() {
        title = NOW + " " + name.getMethodName() + " Caf\u00E9 \u6C49"; // testing encoding too!
    }
    @After
    public void cleanClient() {
        clients.setProfile(null);
        clients.setProperties("");
        clearCaches();
    }

    public static void clearCaches() {
        if (clients.getBrowserCache() != null) {
            clients.getBrowserCache().clear();
        } else {
            log.debug("no browser cache to clear");
        }
        mediaUtil.clearCache();
    }

    protected static final Duration ACCEPTABLE_DURATION_FRONTEND = Duration.ofMinutes(10);
    protected static final NpoApiClients clients =
        NpoApiClients.configured(Config.env(), Config.getProperties(Config.Prefix.npoapi))
            .warnThreshold(Duration.ofMillis(500))
            .mediaType(MediaType.APPLICATION_XML_TYPE)
            .build();

    protected static final NpoApiMediaUtil mediaUtil = new NpoApiMediaUtil(clients);
    protected static final NpoApiPageUtil pageUtil = new NpoApiPageUtil(clients);


    protected static final String apiVersion = clients.getVersion();


    protected static Float apiVersionNumber;
    protected static Float backendVersionNumber;


    static {
        try {
             apiVersionNumber = clients.getVersionNumber();
        } catch (Exception  e) {
            apiVersionNumber = 0f;

        }
        mediaUtil.setCacheExpiry("1S");

        log.info("Using {} ({})", clients, apiVersion);
    }


}
