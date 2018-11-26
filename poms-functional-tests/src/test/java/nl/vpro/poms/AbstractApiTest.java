package nl.vpro.poms;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;

import nl.vpro.api.client.resteasy.NpoApiClients;
import nl.vpro.api.client.utils.Config;
import nl.vpro.api.client.utils.NpoApiImageUtil;
import nl.vpro.api.client.utils.NpoApiMediaUtil;
import nl.vpro.api.client.utils.NpoApiPageUtil;
import nl.vpro.domain.api.media.Compatibility;
import nl.vpro.domain.classification.CachedURLClassificationServiceImpl;
import nl.vpro.domain.classification.ClassificationServiceLocator;
import nl.vpro.domain.media.Schedule;
import nl.vpro.rules.AllowNotImplemented;
import nl.vpro.rules.AllowUnavailable;
import nl.vpro.rules.TestMDC;
import nl.vpro.testutils.AbstractTest;
import nl.vpro.testutils.Utils;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public abstract class AbstractApiTest extends AbstractTest  {


    protected static final String DASHES = "---------------------------------------------------------------------------------";


    public static final Config CONFIG = new Config("npo-functional-tests.properties");


    @Rule
    public AllowUnavailable unavailable = new AllowUnavailable();

    @Rule
    public AllowNotImplemented notImplemented = new AllowNotImplemented();

    protected static final String NOW = ZonedDateTime.now(Schedule.ZONE_ID).toOffsetDateTime().toString();

    @Rule
    public TestName testMethod = new TestName();

    @Rule
    public TestMDC testMDC = new TestMDC();

    @Rule
    public Timeout timeout = new Timeout(30, TimeUnit.MINUTES);


    protected String title;


    @Before
    public void setupTitle() {
        Utils.CLEAR_CACHES.set(this::clearCaches);
        title = testMDC.getTestNumber() + ":" + NOW + " " + testMethod.getMethodName() + " Caf\u00E9 \u6C49"; // testing encoding too!

        log.info("Running {} with title {}", testMethod.getMethodName(), title);
        if (!Objects.equals(log, LOG)) {
            LOG = log;
        }
    }
    @After
    public void cleanClient() {
        clients.setProfile(null);
        clients.setProperties("");
        clearCaches();
    }

    @Override
    public void clearCaches() {
        if (clients.getBrowserCache() != null) {
            clients.getBrowserCache().clear();
        } else {
            log.debug("no browser cache to clear");
        }
        mediaUtil.clearCache();
    }

    protected static final Duration ACCEPTABLE_DURATION_FRONTEND = Duration.ofMinutes(10);
    protected static final NpoApiClients clients =
        NpoApiClients.configured(CONFIG.env(), CONFIG.getProperties(Config.Prefix.npo_api))
            .warnThreshold(Duration.ofMillis(500))
            .accept(MediaType.APPLICATION_XML_TYPE)
            .build();

    protected static final NpoApiMediaUtil mediaUtil = new NpoApiMediaUtil(clients);
    protected static final NpoApiPageUtil pageUtil = new NpoApiPageUtil(clients);
    protected static final NpoApiImageUtil imageUtil = new NpoApiImageUtil("https://images.poms.omroep.nl");



    protected static final String apiVersion = clients.getVersion();


    protected static Float apiVersionNumber;
    protected static Float backendVersionNumber;


    static {
        try {
            apiVersionNumber = clients.getVersionNumber();
        } catch (Exception  e) {
            LOG.warn(e.getMessage());
            apiVersionNumber = 5.4f;
        }
        Compatibility.setCompatibility(apiVersionNumber);
        mediaUtil.setCacheExpiry("1S");

        ClassificationServiceLocator.setInstance(new CachedURLClassificationServiceImpl(
            CONFIG.requiredOption(Config.Prefix.poms, "baseUrl")));
        LOG.debug("Installed {}", ClassificationServiceLocator.getInstance());


        LOG.info("Using {} ({}, {})", clients, apiVersion, CONFIG.env());
    }


}
