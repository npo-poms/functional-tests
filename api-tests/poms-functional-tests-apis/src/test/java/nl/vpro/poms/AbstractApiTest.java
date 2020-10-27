package nl.vpro.poms;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.api.client.frontend.NpoApiClients;
import nl.vpro.api.client.utils.*;
import nl.vpro.domain.api.*;
import nl.vpro.domain.api.media.*;
import nl.vpro.domain.classification.CachedURLClassificationServiceImpl;
import nl.vpro.domain.classification.ClassificationServiceLocator;
import nl.vpro.domain.media.Schedule;
import nl.vpro.junit.extensions.*;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.testutils.*;
import nl.vpro.util.*;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@ExtendWith({AllowUnavailable.class, AllowNotImplemented.class, AbortOnException.class, TestMDC.class})
@Timeout(value = 30, unit = TimeUnit.MINUTES)
@AbortOnException.OnlyIfOrdered
public abstract class AbstractApiTest extends AbstractTest  {

    protected static final Duration ACCEPTABLE_DURATION_FRONTEND = Duration.ofMinutes(15);

    protected static final String DASHES = new String(new char[100]).replace('\0', '-');

    protected static final OffsetDateTime NOW = ZonedDateTime.now(Schedule.ZONE_ID).toOffsetDateTime();
    protected static final Instant NOWI = NOW.toInstant();
    protected static final String NOWSTRING = NOW.toString();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");
    protected static final String SIMPLE_NOWSTRING = FORMATTER.format(NOW);

    private static final List<MediaChange> CHANGES = new CopyOnWriteArrayList<>();

    /**
     * If it is necessary to set this to anything bigger then {@link Duration#ZERO} then this indicates some bug.
     */

    protected static  boolean changesListening = false;
    private static Future<Instant> changesFuture;
    private static final Consumer<MediaChange> listener =  new Consumer<>() {
        @Override
        public void accept(MediaChange change) {
            if (!change.isTail()) {
                LOG.info("Received {}", change);
                CHANGES.add(change);
                ChangesNotifier.notifyChanges();
            }
        }
    };


    protected String title;

    @BeforeAll
    public static void changesListening() {
        changesListening = true;
        CHANGES.clear();
        changesFuture = mediaUtil.subscribeToChanges(null, NOWI, Deletes.ID_ONLY, ()-> changesListening,  listener);
    }


    /**
     * All tests are ready, we started changes listening. All tests took some time. If we ask als changes starting from the same initial time instance, until now, we should receive exactly the same MID's.
     */
    @Test
    @Order(Integer.MAX_VALUE)
    public void checkAllChanges(TestInfo context) throws Exception {
        Assumptions.assumeTrue(context.getTestClass().get().getAnnotation(TestMethodOrder.class) != null);
        changesListening = false;

        Instant until = changesFuture.get();
        log.info("Getting all changes between {} and {} again. There must be {}", NOWI, until, CHANGES.size());


        // Initalially a list of all changes we received
        // we'll do a new changes call, and remove the ones we'll find again.
        List<MediaChange> unmatchedChanges = new ArrayList<>(CHANGES);

        // If during that we encounter changes wich we didn't find before (this couldn't happen), we store it in this:
        List<MediaChange> newChangesNotMatched = new ArrayList<>();

        try (CountedIterator<MediaChange> changes = mediaUtil.changes(NOWI)) {
            while (changes.hasNext()) {
                MediaChange change = changes.next();
                if (!change.getPublishDate().isAfter(until)) {
                    boolean found = unmatchedChanges.removeIf((c) -> c.getMid().equals((change.getMid())));
                    if (! found) {
                        newChangesNotMatched.add(change);
                    }
                } else {
                    log.info("Ignoring for now {}", change);
                }
            }
        }
        if (! unmatchedChanges.isEmpty()) {
            log.info("Some previously received changes not found: {}. Listening for them now (they may have received another change)", unmatchedChanges);
            // There may be some pending changes on object that were changed before that are not yet published again
            Instant start = Instant.now();
            Future<Instant> f = mediaUtil.subscribeToChanges(until, ()-> ! unmatchedChanges.isEmpty() && Duration.between(start, Instant.now()).compareTo(ACCEPTABLE_DURATION_FRONTEND) < 0,  (change) -> {
                if (unmatchedChanges.removeIf((mc) -> mc.getMid().equals(change.getMid()))) {
                    log.info("Found a change to remove {}", change);
                }
            });
            f.get();
        }

        assertThat(unmatchedChanges)
            .withFailMessage("The following changes were previously received but we didn't find them again %s", unmatchedChanges)
            .isEmpty();
        assertThat(newChangesNotMatched)
            .withFailMessage("We received the following changes, but they were not matched previously: %s", newChangesNotMatched)
            .isEmpty();

    }

    @SneakyThrows
    protected static void awaitChanges(Collection<Predicate<MediaChange>> predicates) {
        ChangesNotifier.interestedInChanges(() -> {
            waitUntil(ACCEPTABLE_DURATION_FRONTEND.plus(Duration.ofSeconds(30)),
                () -> "" + predicates.size() + " expected changes",
                () -> {
                    boolean result = true;
                    for (Predicate<MediaChange> expectedChange : predicates) {
                        result &= CHANGES.stream().anyMatch(expectedChange);
                    }
                    if (!result) {
                        LOG.info(CHANGES.stream().map(MediaChange::toString).collect(Collectors.joining("\n")));
                    }
                    return result;

                });

            for (Predicate<MediaChange> expectedChange : predicates) {
                assertThat(CHANGES.stream().filter(expectedChange).findFirst())
                    .withFailMessage(() -> "Doest contain expected: \n" +
                        CHANGES.stream().map(MediaChange::toString).collect(Collectors.joining("\n"))
                    )
                    .isPresent();
            }
            return null;
        });
    }

    @AfterAll
    public static void shutdown() {
        changesFuture.cancel(true);
    }

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
        NpoApiClients.configured(Utils.CONFIG.env(), Utils.CONFIG.getProperties(Config.Prefix.npo_api))
            .warnThreshold(Duration.ofMillis(500))
            .connectTimeout(Duration.ofSeconds(10))
            .socketTimeout(Duration.ofSeconds(60))
            .accept(MediaType.APPLICATION_XML_TYPE)
            .properties(Constants.PROPERTIES_ALL)
            .registerMBean(false)
            .build();

    protected static final NpoApiMediaUtil mediaUtil = new NpoApiMediaUtil(clients);
    protected static final NpoApiPageUtil pageUtil = new NpoApiPageUtil(clients);
    protected static final NpoApiImageUtil imageUtil = new NpoApiImageUtil(Utils.CONFIG.getProperties(Config.Prefix.images).get("baseUrl"));

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
            apiVersionNumber = Version.of(5, 14);
        }
        Compatibility.setCompatibility(apiVersionNumber);
        mediaUtil.setCacheExpiry("1S");

        ClassificationServiceLocator.setInstance(new CachedURLClassificationServiceImpl(
            Utils.CONFIG.requiredOption(Config.Prefix.poms, "baseUrl")));
        LOG.debug("Installed {}", ClassificationServiceLocator.getInstance());


        LOG.info("Using {} ({}, {})", clients, apiVersion, Utils.CONFIG.env());

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
