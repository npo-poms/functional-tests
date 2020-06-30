package nl.vpro.poms;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.net.URLEncoder;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.*;
import org.slf4j.event.Level;

import nl.vpro.api.client.media.MediaRestClient;
import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.Embargo;
import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.*;
import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.domain.media.update.*;
import nl.vpro.domain.support.License;
import nl.vpro.junit.extensions.TestMDC;
import nl.vpro.testutils.Utils;
import nl.vpro.util.IntegerVersion;
import nl.vpro.util.Version;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.vpro.api.client.Utils.*;
import static nl.vpro.domain.media.MediaBuilder.program;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Log4j2
@Timeout(value = 15, unit = TimeUnit.MINUTES)
public abstract class AbstractApiMediaBackendTest extends AbstractApiTest {

    public static final Duration ACCEPTABLE_DURATION_BACKEND = Duration.ofMinutes(5);


    public static final String    MID                = "WO_VPRO_025057";
    protected static final String MID_WITH_LOCATIONS = "WO_VPRO_025700";
    protected static final String ANOTHER_MID        = "WO_VPRO_4911154";

    private static final Duration BACKEND_SOCKET_TIMEOUT = Duration.ofSeconds(300);
    private static final Duration BACKEND_CONNECTIONREQUEST_TIMEOUT = Duration.ofSeconds(10);

    private static final int MAX_IMAGE_RESIZE = 2000;

    private static final String errorMail;
    static {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("TESTRUN", SIMPLE_NOWSTRING);
        StringSubstitutor subst = new StringSubstitutor(valuesMap);
        errorMail =  subst.replace(CONFIG.getProperties(Config.Prefix.npo_backend_api).get("errors"));
        log.info("Mailing errors to {}", errorMail);
    }
    protected static final Set<String> logged = new HashSet<>();

    protected static final MediaRestClient backend =
        MediaRestClient.configured(CONFIG.env(), CONFIG.getProperties(Config.Prefix.npo_backend_api))
            .followMerges(true)
            .validateInput(true)
            .lookupCrids(true)
            .socketTimeout(BACKEND_SOCKET_TIMEOUT)
            .connectionRequestTimeout(BACKEND_CONNECTIONREQUEST_TIMEOUT)
            .warnThreshold(Duration.ofSeconds(10))
            .errors(errorMail)
            .publishImmediately(true)
            .headerLevel((m, a) -> logged.add(methodCall(m, a)) ? Level.INFO : Level.DEBUG)
            //.version("5.7")
            .build();


    protected static final MediaRestClient backend_authority  =
        MediaRestClient.configured(CONFIG.env(), CONFIG.getProperties(Config.Prefix.npo_backend_api))
            .followMerges(true)
            .validateInput(true)
            .lookupCrids(true)
            .owner(OwnerType.AUTHORITY)
            .socketTimeout(BACKEND_SOCKET_TIMEOUT)
            .connectionRequestTimeout(BACKEND_CONNECTIONREQUEST_TIMEOUT)
            .warnThreshold(Duration.ofSeconds(10))
            .errors(errorMail)
            .publishImmediately(true)
            //.version("5.7")
            .build();
    private static final String backendVersion = backend.getVersion();
    protected static IntegerVersion backendVersionNumber;


    static {
        try {
            backendVersionNumber = backend.getVersionNumber();
        } catch (Exception e) {
            backendVersionNumber = Version.of(0);

        }
        log.info("Using {} ({} -> {})", backend, backendVersion, backendVersionNumber);
    }

    public static IntegerVersion getBackendVersionNumber() {
        return backendVersionNumber;
    }

    @SneakyThrows
    protected Image createImage(String prefix) {
        String imageTitle = prefix + title;
        Image image = new Image(OwnerType.BROADCASTER, ImageType.PICTURE, imageTitle);
        image.setImageUri("https://via.placeholder.com/150?text=" + URLEncoder.encode(imageTitle, UTF_8));
        image.setLicense(License.CC_BY);
        image.setSourceName("placeholder.com");
        image.setSource("https://via.placeholder.com/");
        image.setCredits(getClass().getName());
        return image;

    }


    protected Segment createSegment(int duration) {
        return
            MediaBuilder.segment()
                .mainTitle(title)
                .ageRating(AgeRating.ALL)
                .start(Duration.ofMinutes(duration))
                .duration(Duration.ofMinutes(duration + 1))
                .avType(AVType.MIXED)
                .build();
    }

    protected Location createLocation(int count) {
        return
            Location.builder()
                .avAttributes(AVAttributes.builder().avFileFormat(AVFileFormat.H264).build())
                //.platform(Platform.INTERNETVOD)
                .programUrl("https://www.vpro.nl/" + count)
                .build();

    }

    @BeforeEach
    public void abstractSetUp() {
        backend.setValidateInput(true);
        backend.setStealCrids(AssemblageConfig.Steal.IF_DELETED);
        backend.setLookupCrids(true);
        backend.setAccept(MediaType.APPLICATION_XML_TYPE); // e.g. subtitels are more completely represented in XML (including metadata like last modified and creation dates)
    }

    private static boolean needsCheck = true;
    @BeforeAll
    public static void checkMids() {
        try {
            if (needsCheck) {
                log.info("Checking {}", MID);
                MediaUpdate<?> mediaUpdate = backend.get(MID);
                boolean needSet = false;
                if (mediaUpdate == null) {
                    log.info("No media found {}.  Now creating", MID);
                    mediaUpdate = ProgramUpdate.create();
                    ((ProgramUpdate) mediaUpdate).setType(ProgramType.CLIP);
                    mediaUpdate.setAVType(AVType.MIXED);
                    mediaUpdate.setMid(MID);
                    mediaUpdate.setAgeRating(AgeRating.ALL);
                    needSet = true;
                }
                if (! mediaUpdate.getBroadcasters().contains("VPRO")) {
                    mediaUpdate.setBroadcasters("VPRO");
                    needSet = true;
                }
                if (!Objects.equals(mediaUpdate.getMainTitle(), "testclip michiel")) {
                    mediaUpdate.setMainTitle("testclip michiel");
                    needSet = true;

                }
                if (needSet) {
                    log.info("{}, needs fixing, posting that first", MID);
                    backend.set(mediaUpdate);
                }
            }
            if (needsCheck) {
                log.info("Checking {}", MID_WITH_LOCATIONS);
                MediaUpdate<?> mediaUpdate = backend.get(MID_WITH_LOCATIONS);
                if (mediaUpdate == null) {
                    mediaUpdate = ProgramUpdate.create();
                    ((ProgramUpdate) mediaUpdate).setType(ProgramType.CLIP);
                    mediaUpdate.setAVType(AVType.MIXED);
                    mediaUpdate.setBroadcasters("VPRO");
                    mediaUpdate.setMid(MID_WITH_LOCATIONS);
                    mediaUpdate.setMainTitle("Test");
                    mediaUpdate.setAgeRating(AgeRating.ALL);

                }
                if (mediaUpdate.getLocations().isEmpty()) {
                    log.info("No media found {} with locations.  Now creating", MID_WITH_LOCATIONS);
                    mediaUpdate.setLocations(LocationUpdate.builder()
                        .programUrl("http://content.omroep.nl/vpro/poms/world/15/04/88/63/NPO_bb.m4v")
                        .bitrate(678000)
                        .format(AVFileFormat.M4V)
                        .build());
                    backend.set(mediaUpdate);
                } else if (mediaUpdate.getLocations().stream().allMatch(Embargo::isUnderEmbargo)) {
                    log.info("All locations of {} are under embargo. This is incorrect. Publishing them all.", mediaUpdate);
                    for (LocationUpdate l : mediaUpdate.getLocations()) {
                        l.setPublishStartInstant(null);
                        l.setPublishStopInstant(null);
                    }
                    backend.set(mediaUpdate);
                }
            }
            if (needsCheck) {
                ProgramUpdate anotherProgramUpdate = backend.get(ANOTHER_MID);
                if (anotherProgramUpdate == null) {
                    log.info("No media found {}. Now creating", ANOTHER_MID);
                    log.info(
                        backend.set(
                            ProgramUpdate.create(program()
                                .broadcasters("VPRO")
                                .mid(ANOTHER_MID)
                                .avType(AVType.VIDEO)
                                .type(ProgramType.CLIP)
                                .mainTitle("test"))
                        )
                    );
                }
            }
            needsCheck = false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @AfterAll
    static void afterAll() {
        backend.close();
        backend_authority.close();

    }

    protected void cleanupAllImages(String mid) {
        backend.getBrowserCache().clear();
        MediaUpdate<?> update = backend.get(mid);
        if (!update.getImages().isEmpty()) {
            log.info("Removing images " + update.getImages());
            update.getImages().clear();
            backend.set(update);
        }
        Utils.waitUntil(Duration.ofMinutes(2),
            () -> backend.get(mid),
            Utils.Check.<MediaUpdate<?>>builder()
                .predicate(o -> o.getImages().isEmpty())
                .description("{} has no image", mid)
            .build()
        );

        update = backend_authority.get(mid);
        Assumptions.assumeTrue(update != null);
        if (!update.getImages().isEmpty()) {
            log.info("Removing images " + update.getImages());
            update.getImages().clear();
            log.info("{}", backend_authority.set(update));
        }

    }

    protected ImageUpdate.Builder randomImage(String title) {
        return _randomImage(title).credits(getClass().getName());
    }

    @SneakyThrows
    private static ImageUpdate.Builder _randomImage(String title)  {
        /*return ImageUpdate.builder()
            .type(ImageType.PICTURE)
            .title(title)
            .imageUrl("https://dummyimage.com/150x150&text=" + URLEncoder.encode(title, "UTF-8"))
            .license(License.CC_BY)
            .sourceName("dummyimage")
            .source("http://dummyimage.com/")
            .credits(getClass().getName());
            */
        /* // lorempixel geeft geen response meer...
        return ImageUpdate.builder()
            .type(ImageType.PICTURE)
            .title(title)
            .imageUrl("http://lorempixel.com/400/200/sports/T" + URLEncoder.encode(title, "UTF-8") + "/")
            .license(License.CC_BY)
            .sourceName("lorempixel")
            .source("http://lorempixel.com/")
            .credits(getClass().getName());
            */
        ImageUpdate.Builder builder = ImageUpdate.builder()
            .type(ImageType.PICTURE)
            .title(title)
            .imageUrl("https://images.poms.omroep.nl/image/s" + ((TestMDC.getTestNumber() + 10) % MAX_IMAGE_RESIZE) + "/7617.jpg?" + URLEncoder.encode(title, UTF_8))
            .license(License.CC_BY)
            .sourceName("vpro")
            .source("https://www.vpro.nl/");

        log.info("Creating image {}", builder);
        return builder;
    }



}
