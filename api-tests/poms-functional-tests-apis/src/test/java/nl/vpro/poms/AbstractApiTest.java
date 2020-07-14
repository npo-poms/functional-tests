package nl.vpro.poms;

import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.api.client.frontend.NpoApiClients;
import nl.vpro.api.client.utils.*;
import nl.vpro.domain.api.Constants;
import nl.vpro.domain.api.media.*;
import nl.vpro.domain.classification.CachedURLClassificationServiceImpl;
import nl.vpro.domain.classification.ClassificationServiceLocator;
import nl.vpro.domain.media.Schedule;
import nl.vpro.junit.extensions.*;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.testutils.AbstractTest;
import nl.vpro.testutils.Utils;
import nl.vpro.util.IntegerVersion;
import nl.vpro.util.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@ExtendWith({AllowUnavailable.class, AllowNotImplemented.class, AbortOnException.class, TestMDC.class, Require.class})
@Timeout(value = 30, unit = TimeUnit.MINUTES)
@AbortOnException.OnlyIfOrdered
public abstract class AbstractApiTest extends AbstractTest  {

    protected static final Duration ACCEPTABLE_DURATION_FRONTEND = Duration.ofMinutes(15);

    protected static final String DASHES = new String(new char[100]).replace('\0', '-');

    public static final Config CONFIG = new Config("npo-functional-tests.properties");

    protected static final OffsetDateTime NOW = ZonedDateTime.now(Schedule.ZONE_ID).toOffsetDateTime();
    protected static final Instant NOWI = NOW.toInstant();
    protected static final String NOWSTRING = NOW.toString();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");
    protected static final String SIMPLE_NOWSTRING = FORMATTER.format(NOW);

    protected String title;

    @BeforeEach
    public void setupTitle(TestInfo testInfo) {
        Utils.CLEAR_CACHES.set(this::clearCaches);
        title = TestMDC.getTestNumber() + ":" + NOWSTRING + " " + testInfo.getDisplayName() + " Caf\u00E9 \u6C49"; // testing encoding too!

        log.info("Running {} with title {}", testInfo.getTestMethod().map(Method::toString).orElse("<no method?>"), title);
        if (!Objects.equals(log, LOG)) {
            LOG = log;
        }
    }

    @AfterEach
    public void cleanClient() {
        clients.setProfile(null);
        clients.setProperties(Constants.PROPERTIES_ALL);
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



    protected static final NpoApiClients clients =
        NpoApiClients.configured(CONFIG.env(), CONFIG.getProperties(Config.Prefix.npo_api))
            .warnThreshold(Duration.ofMillis(500))
            .connectTimeout(Duration.ofSeconds(10))
            .socketTimeout(Duration.ofSeconds(60))
            .accept(MediaType.APPLICATION_XML_TYPE)
            .properties(Constants.PROPERTIES_ALL)
            .build();

    protected static final NpoApiMediaUtil mediaUtil = new NpoApiMediaUtil(clients);
    protected static final NpoApiPageUtil pageUtil = new NpoApiPageUtil(clients);
    protected static final NpoApiImageUtil imageUtil = new NpoApiImageUtil(CONFIG.getProperties(Config.Prefix.images).get("baseUrl"));

    private static final String apiVersion = clients.getVersion();
    protected static IntegerVersion apiVersionNumber;

    private static Redirector redirector;
    /**
     * TODO: can't we determin this automaticly?
     */
    public static final IntegerVersion DOMAIN_VERSION = Version.of(5, 13);

    static {
        try {
            apiVersionNumber = clients.getVersionNumber();
        } catch (Exception  e) {
            LOG.warn(e.getMessage());
            apiVersionNumber = Version.of(5, 9);
        }
        Compatibility.setCompatibility(apiVersionNumber);
        mediaUtil.setCacheExpiry("1S");

        ClassificationServiceLocator.setInstance(new CachedURLClassificationServiceImpl(
            CONFIG.requiredOption(Config.Prefix.poms, "baseUrl")));
        LOG.debug("Installed {}", ClassificationServiceLocator.getInstance());


        LOG.info("Using {} ({}, {})", clients, apiVersion, CONFIG.env());

        LOG.info("Image server: {}", imageUtil);
    }

    public static RedirectList getRedirectsList() {
        try (Response response = clients.getMediaService().redirects(null)) {
            assertThat(response.getStatus()).isEqualTo(200);
            RedirectList list = response.readEntity(RedirectList.class);
            assertThat(list).isNotEmpty();
            return list;
        }
    }

    public static Redirector getRedirector() {
        if (redirector == null) {
            RedirectList list = getRedirectsList();
            redirector = () ->  list;
        }
        return redirector;
    }

}
