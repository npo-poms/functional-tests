package nl.vpro.poms.backend;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.support.License;
import nl.vpro.domain.media.update.ImageLocation;
import nl.vpro.domain.media.update.ImageUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.DoAfterException;

import static nl.vpro.poms.Utils.waitUntil;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeTrue;

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
        assumeTrue(backendVersionNumber >= 5.0);
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

        if(backendVersionNumber >=5.0 ) {
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
        if (backendVersionNumber >= 5.0) {
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
    public void test99Cleanup() throws Exception {
        final ProgramUpdate[] update = new ProgramUpdate[1];
        update[0]= backend.get(MID);
        System.out.println("Removing images " + update[0].getImages());
        update[0].getImages().clear();
        backend.set(update[0]);
        waitUntil(ACCEPTABLE_DURATION, () -> {
            update[0] = backend.get(MID);
            return update[0].getImages().isEmpty();
        });
        assertThat(update[0].getImages()).isEmpty();
    }



    protected void checkArrived() throws Exception {
        if (exception == null) {
            final List<String> currentTitles = new ArrayList<>();
            waitUntil(ACCEPTABLE_DURATION, () -> {
                ProgramUpdate update = backend.get(MID);
                currentTitles.clear();
                currentTitles.addAll(update.getImages().stream().map(ImageUpdate::getTitle).collect(Collectors.toList()));
                return currentTitles.containsAll(titles);
            });

            assertThat(currentTitles).containsAll(titles);
        }
    }
}
