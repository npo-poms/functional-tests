package nl.vpro.poms.backend;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.Schedule;
import nl.vpro.domain.media.support.License;
import nl.vpro.domain.media.update.ImageLocation;
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
        ImageUpdate update = new ImageUpdate(ImageType.PICTURE, title, null, new ImageLocation("http://placehold.it/150/7735a")); // redirects
        update.setLicense(License.CC_BY);
        update.setSourceName("placeholdit");
        update.setCredits(getClass().getName());
        backend.addImage(update, MID);
    }


    @Test
    public void test02addImage() throws UnsupportedEncodingException {
        titles.add(title);
        ImageUpdate update = new ImageUpdate(ImageType.PICTURE, title, null, new ImageLocation("https://placeholdit.imgix.net/~text?txt=" + URLEncoder.encode(title, "UTF-8") + "&w=150&h=150"));

        if(backendVersionNumber >= 5.0f) {
            update.setLicense(License.CC_BY);
            update.setSourceName("placeholdit");
            update.setSource("https://placeholdit.imgix.net");
            update.setCredits(getClass().getName());
        }

        backend.addImage(update, MID);
    }

    @Test
    public void test10checkArrived() throws Exception {
        checkArrived();
    }


    @Test
    public void test11addImageToObject() throws UnsupportedEncodingException {
        titles.add(title);
        ImageUpdate imageUpdate  =
            new ImageUpdate(ImageType.PICTURE, title, null, new ImageLocation("https://placeholdit.imgix.net/~text?txt=" + URLEncoder.encode(title, "UTF-8") + "&w=150&h=150"));
        if (backendVersionNumber >= 5.0f) {
            imageUpdate.setLicense(License.CC_BY);
            imageUpdate.setSourceName("placeholdit");
            imageUpdate.setCredits(getClass().getName());
        }
        ProgramUpdate update = backend.get(MID);
        update.getImages().add(imageUpdate);
        backend.set(update);
        JAXB.marshal(update, System.out);
    }


    @Test
    public void test50checkArrived() throws Exception {
        checkArrived();
    }


    @Test
    public void test51updateImageInObject() throws Exception {
        final ProgramUpdate[] update = new ProgramUpdate[1];
        update[0] = backend.get(MID);
        Instant yesterday = LocalDate.now(Schedule.ZONE_ID).minusDays(1).atStartOfDay(Schedule.ZONE_ID).toInstant();

        ImageUpdate image = update[0].getImages().get(0);
        String urn = image.getUrn();
        image.setPublishStop(yesterday);

        backend.set(update[0]);

        waitUntil(ACCEPTABLE_DURATION,
            MID + " has image " + urn + " that expires " + yesterday,
            () -> {
                update[0] = backend.get(MID);
                ImageUpdate imageUpdate  = update[0].getImages().get(0);
                return Objects.equals(imageUpdate.getPublishStop(), yesterday);
            });

        assertThat(update[0].getImages().get(0).getUrn()).isEqualTo(urn);
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
