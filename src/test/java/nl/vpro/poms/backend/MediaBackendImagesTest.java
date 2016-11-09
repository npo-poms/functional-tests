package nl.vpro.poms.backend;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.update.ImageLocation;
import nl.vpro.domain.media.update.ImageUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiTest;

import static nl.vpro.poms.Utils.waitUntil;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MediaBackendImagesTest extends AbstractApiTest {

    private static final String MID = "WO_VPRO_025057";
    private static final String TITLE = Instant.now().toString();
    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);
    private static final List<String> titles = new ArrayList<>();

    @Rule
    public TestName name = new TestName();

    @After
    public void cleanUp() {


    }

    private String title;

    @Before
    public void setup() {
        title = TITLE + " " + name.getMethodName();
        titles.add(title);
    }

    @Test
    @Ignore("Currently fails (enable if MSE-3475 fixed)")
    public void test01addImageRedirect() {
        ImageUpdate update = new ImageUpdate(ImageType.PICTURE, title, null, new ImageLocation("http://placehold.it/150/7735a")); // redirects
        backend.getBackendRestService().addImage(update, null, MID,  true, null);
    }


    @Test
    public void test02addImage() {
        ImageUpdate update = new ImageUpdate(ImageType.PICTURE, title, null, new ImageLocation("https://placeholdit.imgix.net/~text?txt=" + title + "&w=150&h=150"));
        backend.getBackendRestService().addImage(update, null, MID, true, null);
    }

    @Test
    public void test10checkArrived() throws Exception {
        checkArrived();
    }


    @Test
    public void test11addImageToObject() {
        ImageUpdate imageUpdate  = new ImageUpdate(ImageType.PICTURE, title, null, new ImageLocation("https://placeholdit.imgix.net/~text?txt=" + title + "&w=150&h=150"));
        ProgramUpdate update = backend.get(MID);
        update.getImages().add(imageUpdate);
        backend.set(update);
        JAXB.marshal(update, System.out);;
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
        titles.remove(title);
        List<String> copyOfTitles = new ArrayList<>(titles);
        waitUntil(ACCEPTABLE_DURATION, () -> {
                ProgramUpdate update = backend.get(MID);
                for (ImageUpdate iu : update.getImages()) {
                    copyOfTitles.remove(iu.getTitle());
                }
                System.out.println("Remaining images " + copyOfTitles);
                return copyOfTitles.isEmpty();
            }
        );
        assertThat(copyOfTitles).isEmpty();
    }
}
