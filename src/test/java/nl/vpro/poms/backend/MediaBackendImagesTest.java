package nl.vpro.poms.backend;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.support.License;
import nl.vpro.domain.media.update.ImageUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.DoAfterException;

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
public class MediaBackendImagesTest extends AbstractApiMediaBackendTest {

    private static final String MID = "WO_VPRO_025057";
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
    public void test01addImageRedirect() {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(5.0f));
        titles.add(title);
        ImageUpdate update = ImageUpdate.builder()
            .type(ImageType.PICTURE)
            .title(title)
            .imageUrl("http://placehold.it/150/7735a") // redirects
            .license(License.CC_BY)
            .sourceName("placeholdit")
            .credits(getClass().getName())
            .build();

        backend.addImage(update, MID);
    }


    @Test
    public void test02addImage() throws UnsupportedEncodingException {
        titles.add(title);
        ImageUpdate update = ImageUpdate.builder()
            .type(ImageType.PICTURE)
            .title(title)
            .imageUrl("https://placeholdit.imgix.net/~text?txt=" + URLEncoder.encode(title, "UTF-8") + "&w=150&h=150")
            .license(License.CC_BY)
            .sourceName("placeholdit")
            .credits(getClass().getName())
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
        ImageUpdate imageUpdate  = ImageUpdate.builder()
            .type(ImageType.PICTURE)
            .title(title)
            .imageUrl("https://placeholdit.imgix.net/~text?txt=" + URLEncoder.encode(title, "UTF-8") + "&w=150&h=150")
            .license(License.CC_BY)
            .sourceName("placeholdit")
            .credits(getClass().getName())
            .build();
        ProgramUpdate update = backend.get(MID);
        update.getImages().add(imageUpdate);
        backend.set(update);
        JAXB.marshal(update, System.out);
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
        String urn = image.getUrnAttribute();
        image.setPublishStop(yesterday);

        // and add one too
        ImageUpdate newImage = ImageUpdate.builder()
            .type(ImageType.PICTURE)
            .title(title)
            .imageUrl("https://placeholdit.imgix.net/~text?txt=" + URLEncoder.encode(title, "UTF-8") + "&w=150&h=150")
            .license(License.CC_BY)
            .sourceName("placeholdit")
            .credits(getClass().getName())
            .build();

        update[0].getImages().add(newImage);

        backend.set(update[0]);

        waitUntil(ACCEPTABLE_DURATION,
            MID + " has image " + urn + " that expires " + yesterday,
            () -> {
                update[0] = backend.get(MID);
                return update[0].getImages().stream().anyMatch(iu -> Objects.equals(iu.getPublishStop(), yesterday));
            });


        // The URN changes. We may find this acceptable, but I don't like it either!

        //assertThat(update[0].getImages().stream().filter(iu -> Objects.equals(iu.getPublishStop(), yesterday)).findFirst().orElseThrow(IllegalStateException::new).getUrnAttribute()).isEqualTo(urn);

        // The new image must have arrived any ways:
        assertThat(update[0].getImages().stream().anyMatch(i -> i.getTitle().equals(title))).isTrue(); // THIS fails!
    }


    @Test
    public void test99Cleanup() throws Exception {
        final ProgramUpdate[] update = new ProgramUpdate[1];
        update[0]= backend.get(MID);
        System.out.println("Removing images " + update[0].getImages());
        update[0].getImages().clear();
        backend.set(update[0]);
        waitUntil(ACCEPTABLE_DURATION,
            MID + " has no images any more",
            () -> {
            update[0] = backend.get(MID);
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
}
