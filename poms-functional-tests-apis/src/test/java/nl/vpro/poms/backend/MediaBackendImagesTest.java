package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.api.client.media.ResponseError;
import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.update.ImageUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.logging.LoggerOutputStream;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.util.Version;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/*
 * 2018-08-15
 * 5.9-SNAPSHOT @ dev : allemaal ok
 * 5.7.9 @ test: de eerste test gaat fout.
 * 2019-05-06
 *  5.11-SNAPSHOT @ dev :ok
 */


/**
 * Tests whether adding and modifying images via the POMS backend API works.
 *
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Slf4j
@ExtendWith(AbortOnException.class)
class MediaBackendImagesTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);
    private static final List<String> titles = new ArrayList<>();


    @Test
    void test00setup() {
        cleanup();
        cleanupCheck();
    }



    @Test
    void test01addRedirectingImage() throws UnsupportedEncodingException {
        assumeThat(backendVersionNumber).isGreaterThanOrEqualTo(Version.of(5));
        titles.add(title);
        ImageUpdate update = randomImage(title)
            .type(ImageType.LOGO) // different types make the image unique without id.
            .source("https://google.com/")
            .imageUrl("https://goo.gl/fKL1rj") // redirects
            .build();

        backend.addImage(update, MID);
    }


    @Test
    void test02addImage() throws UnsupportedEncodingException {
        titles.add(title);

        ImageUpdate update = randomImage(title)
            .type(ImageType.BACKGROUND)
            .build();
        backend.addImage(update, MID);
    }

    @Test
    void test10checkArrived() {
        checkArrived();
    }


    @Test
    void test11addImageToObject() throws UnsupportedEncodingException {
        titles.add(title);
        ImageUpdate imageUpdate  = randomImage(title)
            .type(ImageType.ICON)
            .build();

        ProgramUpdate update = backend.get(MID);
        update.getImages().add(imageUpdate);
        backend.set(update);
        JAXB.marshal(update, LoggerOutputStream.debug(log));
    }


    @Test
    void test12checkArrived() {
        // Test 11 happens via object (not via addImage), so goes via broadcaster cues.
        // If 13 is executed before 11 fully handled, 13 will fail.
        // therefor we added this intermediate check.
        checkArrived();
    }

    private static String wikiImageTitle;
    private static String tineyeImageTitle;

    @Test
    void test13addWikimediaImage() {
        titles.add(title);
        wikiImageTitle = title;

        ImageUpdate update = ImageUpdate.builder()
            .imageUrl("https://commons.wikimedia.org/wiki/Category:Photos_by_User:CaribDigita/Barbados#/media/File:Barbados_Flag_fountain,_Bridgetown,_Barbados.jpg")
            .type(ImageType.PICTURE)
            .title(title)
            .build();

        backend.setImageMetaData(true);

        backend.addImage(update, MID);
    }


    @Test
    //@Ignore
    void test14addTineyeImage() {
        titles.add(title);
        tineyeImageTitle = title;

        ImageUpdate update = ImageUpdate.builder()
            .imageUrl("http://files.vpro.nl/test/a.png")
            .type(ImageType.PICTURE)
            .title(title)
            .build();

        backend.setImageMetaData(true);
        backend.addImage(update, MID);
    }


    @Test
    void test20checkArrived() {
        checkArrived();
        assumeTrue(wikiImageTitle != null);
        ProgramUpdate update = backend.get(MID);

        assertThat(update.getImages().stream()
            .filter(i -> i.getTitle().equals(wikiImageTitle)).findFirst()
            .orElseThrow(IllegalStateException::new).getCredits()).isEqualTo("CaribDigita");
    }


    @Test
    void test21updateImageInObject() throws Exception {
        final ProgramUpdate[] update = new ProgramUpdate[1];
        update[0] = backend.get(MID);
        Instant yesterday = Instant.now().minus(Duration.ofDays(1));

        ImageUpdate image = update[0].getImages().get(0);
        String urn = image.getUrn();
        image.setPublishStopInstant(yesterday);

        // and add one too
        ImageUpdate newImage = randomImage(title)
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
    void test22updateImageInObjectButCleanUrn() throws Exception {
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
        ImageUpdate newImage = randomImage(title)
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
    void test30copyImageToOtherObject() {
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
    void test31addInvalidImage() {
        Assertions.assertThrows(ResponseError.class, () -> {
            assumeThat(backendVersionNumber).isGreaterThanOrEqualTo(Version.of(5, 8));
            titles.add(title);
            ImageUpdate update = randomImage(title)
                .type(ImageType.LOGO) // different types make the image unique without id.
                .source("bla") // invalid!
                .imageUrl("https://goo.gl/fKL1rj") // redirects
                .build();

            backend.addImage(update, MID);
        });
    }


    @Test
    void test98Cleanup() {
        cleanup();
    }


    @Test
    void test99CleanupCheck() {
        cleanupCheck();
    }




    void cleanup() {
        backend.getBrowserCache().clear();

        ProgramUpdate update;
        update = backend.get(ANOTHER_MID);
        log.info("Removing images " + update.getImages());
        update.getImages().clear();
        backend.set(update);

        update = backend.get(MID);
        log.info("Removing images " + update.getImages());
        update.getImages().clear();
        backend.set(update);


    }


    void cleanupCheck() {
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

    void checkArrived() {
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
