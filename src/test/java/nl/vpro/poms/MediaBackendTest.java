package nl.vpro.poms;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.update.ImageLocation;
import nl.vpro.domain.media.update.ImageUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.rs.media.MediaRestClient;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MediaBackendTest extends AbstractApiTest {
    static final MediaRestClient backend;

    private static final String MID = "WO_VPRO_025057";
    private static final String TITLE = Instant.now().toString();

    private static final List<String> titles = new ArrayList<>();

    static {
        try {
            backend = new MediaRestClient().configured();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Rule
    public TestName name = new TestName();

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
    public void test03addImageToObject() {
        ImageUpdate imageUpdate  = new ImageUpdate(ImageType.PICTURE, title, null, new ImageLocation("https://placeholdit.imgix.net/~text?txt=" + title + "&w=150&h=150"));
        ProgramUpdate update = backend.get(MID);
        update.getImages().add(imageUpdate);
        backend.set(update);
    }

    @Test
    public void test50CheckArrived() throws Exception {
        List<String> copyOfTitles = new ArrayList<>(titles);
        copyOfTitles.remove(title);
        waitUntil(() -> {
            ProgramUpdate update = backend.get(MID);
            for (ImageUpdate iu : update.getImages()) {
                copyOfTitles.remove(iu.getTitle());
            }
            return copyOfTitles.isEmpty();
            }
        );
        assertThat(copyOfTitles).isEmpty();
    }

    @Test
    public void test99Cleanup() throws Exception {
        final ProgramUpdate[] update = new ProgramUpdate[1];
        update[0]= backend.get(MID);
        update[0].getImages().clear();
        backend.set(update[0]);
        waitUntil(() -> {
            update[0] = backend.get(MID);
            return update[0].getImages().isEmpty();
        });
        assertThat(update[0].getImages()).isEmpty();
    }


    protected void waitUntil(Callable<Boolean> r) throws Exception {
        Instant start = Instant.now();
        Duration acceptableDuration = Duration.ofMinutes(3);
        while (true) {
            Thread.sleep(Duration.ofSeconds(10).toMillis());
            if (r.call()) {
                break;
            }
            if (Duration.between(start, Instant.now()).compareTo(acceptableDuration) > 0) {
                break;
            }
        }
    }
}
