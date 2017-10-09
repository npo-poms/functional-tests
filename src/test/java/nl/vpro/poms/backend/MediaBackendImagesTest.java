package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.AVType;
import nl.vpro.domain.media.ProgramType;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.domain.media.update.ImageUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.support.License;
import nl.vpro.logging.LoggerOutputStream;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.DoAfterException;

import static nl.vpro.domain.media.MediaBuilder.program;
import static nl.vpro.poms.Utils.waitUntil;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeThat;

/**
 * Tests whether adding and modifying images via the POMS backend API works.
 *
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MediaBackendImagesTest extends AbstractApiMediaBackendTest {

    private static final String MID = "WO_VPRO_025057";
    private static final String ANOTHER_MID = "WO_KRO_475084";
    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);
    private static final List<String> titles = new ArrayList<>();

    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        if (! (t instanceof AssumptionViolatedException)) {
            MediaBackendImagesTest.exception = t;
        }
    });

    private static Throwable exception = null;

    @Before
    public void setup() {
        assumeNoException(exception);
    }


    @Test
    public void test00setup() {
        ProgramUpdate existing = backend.get(MID);
        if (existing == null) {
            log.info(backend.set(ProgramUpdate.create(program().broadcasters("VPRO").mid(MID).avType(AVType.VIDEO).type(ProgramType.CLIP).mainTitle("test"))));
        }
        ProgramUpdate existing2 = backend.get(ANOTHER_MID);
        if (existing2 == null) {
            log.info(backend.set(ProgramUpdate.create(program().broadcasters("VPRO").mid(ANOTHER_MID).avType(AVType.VIDEO).type(ProgramType.CLIP).mainTitle("test"))));
        }
    }



    @Test
    public void test01addRedirectingImage() throws UnsupportedEncodingException {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(5.0f));
        titles.add(title);
        ImageUpdate update = random(title)
            .type(ImageType.LOGO) // different types make the image unique without id.
            .imageUrl("https://goo.gl/fF1Laz") // redirects
            .build();

        backend.addImage(update, MID);
    }


    @Test
    public void test02addImage() throws UnsupportedEncodingException {
        titles.add(title);

        ImageUpdate update = random(title)
            .type(ImageType.BACKGROUND)
            .build();
        backend.addImage(update, MID);
    }

    @Test
    public void test10checkArrived() throws Exception {
        checkArrived();
    }


    @Test
    public void test11addImageToObject() throws UnsupportedEncodingException {
        titles.add(title);
        ImageUpdate imageUpdate  = random(title)
            .type(ImageType.ICON)
            .build();

        ProgramUpdate update = backend.get(MID);
        update.getImages().add(imageUpdate);
        backend.set(update);
        JAXB.marshal(update, LoggerOutputStream.debug(log));
    }


    @Test
    public void test12addWikimediaImage() throws UnsupportedEncodingException {
        Image image = new Image(OwnerType.BROADCASTER, "https://commons.wikimedia.org/wiki/Category:Photos_by_User:CaribDigita/Barbados#/media/File:Barbados_Flag_fountain,_Bridgetown,_Barbados.jpg");

        image.setTitle("wiki-image");
        ImageUpdate update = new ImageUpdate(image);


        backend.addImage(update, MID);

        //assertThat(update.getCredits().equals("CaribDigita"));
    }



    @Test
    public void test20checkArrived() throws Exception {
        checkArrived();
    }


    @Test
    public void test21updateImageInObject() throws Exception {
        final ProgramUpdate[] update = new ProgramUpdate[1];
        update[0] = backend.get(MID);
        Instant yesterday = Instant.now().minus(Duration.ofDays(1));

        ImageUpdate image = update[0].getImages().get(0);
        String urn = image.getUrn();
        image.setPublishStopInstant(yesterday);

        // and add one too
        ImageUpdate newImage = random(title)
            .type(ImageType.PORTRAIT)
            .build();

        update[0].getImages().add(newImage);

        backend.set(update[0]);

        waitUntil(ACCEPTABLE_DURATION,
            MID + " has image " + urn + " that expires " + yesterday,
            () -> {
                update[0] = backend.get(MID);
                return update[0].getImages().stream().anyMatch(iu -> Objects.equals(iu.getPublishStopInstant(), yesterday));
            });

        assertThat(update[0].getImages().stream().filter(iu -> Objects.equals(iu.getPublishStopInstant(), yesterday)).findFirst().orElseThrow(IllegalStateException::new).getUrn()).isEqualTo(urn);

        // The new image must have arrived any way:
        assertThat(update[0].getImages().stream().anyMatch(i -> i.getTitle().equals(title))).isTrue();
    }

    @Test
    public void test22updateImageInObjectButCleanUrn() throws Exception {
        final ProgramUpdate[] update = new ProgramUpdate[1];
        update[0] = backend.get(MID);

        for (ImageUpdate i : update[0].getImages()) {
            i.setId(null);
        }

        ImageUpdate image = update[0].getImages().get(0);
        String url = (String) image.getImage();
        String newDescription = title;
        image.setDescription(newDescription);

        // and add one too
        ImageUpdate newImage = random(title)
            .type(ImageType.PROMO_LANDSCAPE)
            .build();

        update[0].getImages().add(newImage);

        //JAXB.marshal(update[0], System.out);

        backend.set(update[0]);

        waitUntil(ACCEPTABLE_DURATION,
            MID + " has image " + url + " that has description " + newDescription,
            () -> {
                update[0] = backend.get(MID);
                return update[0].getImages().stream().anyMatch(iu -> Objects.equals(iu.getDescription(), newDescription));
            });

        assertThat(update[0].getImages().stream().filter(iu -> Objects.equals(iu.getDescription(), title)).findFirst().orElseThrow(IllegalStateException::new).getImage()).isEqualTo(url);

        // The new image must have arrived any ways:
        assertThat(update[0].getImages().stream().anyMatch(i -> i.getTitle().equals(title))).isTrue();
    }


    @Test
    public void test30copyImageToOtherObject() throws Exception {
        final ProgramUpdate[] updates = new ProgramUpdate[2];
        updates[0] = backend.get(MID);

        int count = updates[0].getImages().size();
        ImageUpdate first =  updates[0].getImages().get(0);


        updates[1] = backend.get(ANOTHER_MID);


        updates[1].getImages().add(first);

        //JAXB.marshal(update[0], System.out);

        backend.set(updates[1]);
        Function<ImageUpdate, String> getUrn = (iu) -> ((String) iu.getImage());
        String firstUrl = getUrn.apply(first);
        Predicate<ImageUpdate> match = (iu) -> Objects.equals(getUrn.apply(iu), firstUrl);

        waitUntil(ACCEPTABLE_DURATION,
            ANOTHER_MID + " has image " + firstUrl,
            () -> {
                updates[1] = backend.get(ANOTHER_MID);
                return updates[1].getImages().stream()
                    .anyMatch(match)
                    ;
            });

        assertThat(updates[1].getImages().stream()
            .filter(match).findFirst().orElseThrow(IllegalStateException::new).getId()).isNotEqualTo(first.getId());

        updates[0] = backend.get(MID);
        assertThat(updates[0].getImages().stream()
            .filter(match).findFirst().orElseThrow(IllegalStateException::new).getId()).isEqualTo(first.getId());
    }

    @Test
    public void test98Cleanup() throws Exception {
        backend.getBrowserCache().clear();
        ProgramUpdate update = backend.get(MID);
        log.info("Removing images " + update.getImages());
        update.getImages().clear();
        backend.set(update);

        update = backend.get(ANOTHER_MID);
        log.info("Removing images " + update.getImages());
        update.getImages().clear();
        backend.set(update);
    }


    @Test
    public void test99CleanupCheck() throws Exception {
        final ProgramUpdate[] update = new ProgramUpdate[1];
        waitUntil(ACCEPTABLE_DURATION,
            MID + " has no images any more",
            () -> {
                update[0] = backend.get(MID);
                return update[0].getImages().isEmpty();
            });
        assertThat(update[0].getImages()).isEmpty();

        waitUntil(ACCEPTABLE_DURATION,
            ANOTHER_MID + " has no images any more",
            () -> {
                update[0] = backend.get(ANOTHER_MID);
                return update[0].getImages().isEmpty();
            });
        assertThat(update[0].getImages()).isEmpty();
    }


    protected void checkArrived() throws Exception {
        if (exception == null) {
            final List<String> currentTitles = new ArrayList<>();
            waitUntil(ACCEPTABLE_DURATION,
                MID + " in backend with images " + titles,
                () -> {
                ProgramUpdate update = backend.get(MID);
                currentTitles.clear();
                currentTitles.addAll(update.getImages().stream().map(ImageUpdate::getTitle).collect(Collectors.toList()));
                return currentTitles.containsAll(titles);
            });

            assertThat(currentTitles).containsAll(titles);
        }
    }

    protected ImageUpdate.Builder random(String title) throws UnsupportedEncodingException {
        /*return ImageUpdate.builder()
            .type(ImageType.PICTURE)
            .title(title)
            .imageUrl("https://dummyimage.com/150x150&text=" + URLEncoder.encode(title, "UTF-8"))
            .license(License.CC_BY)
            .sourceName("dummyimage")
            .source("http://dummyimage.com/")
            .credits(getClass().getName());
            */
        /*
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
            .imageUrl("http://images.poms.omroep.nl/image/s" + (testNumber.intValue() + 10) + "/7617.jpg?" + URLEncoder.encode(title, "UTF-8"))
            .license(License.CC_BY)
            .sourceName("vpro")
            .source("https://www.vpro.nl/")
            .credits(getClass().getName());
        log.info("Creating image {}", builder);
        return builder;
    }


}
