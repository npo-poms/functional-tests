package nl.vpro.poms;

import java.io.IOException;

import javax.xml.bind.JAXB;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.update.ImageLocation;
import nl.vpro.domain.media.update.ImageUpdate;
import nl.vpro.rs.media.MediaRestClient;
import nl.vpro.util.Env;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MediaBackendTest extends AbstractApiTest {
    static final MediaRestClient backend;

    static {
        try {
            backend = new MediaRestClient().configured(Env.PROD);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }


    @Test
    public void test01addImage() {
        ImageUpdate update = new ImageUpdate(ImageType.PICTURE, "BLA", null, new ImageLocation("https://placeholdit.imgix.net/~text?txtsize=15&txt=image1&w=120&h=120"));
        JAXB.marshal(update, System.out);
        backend.getBackendRestService().addImage(update, null, "WO_VPRO_025057", true, "michiel.meeuwissen@gmail.com");
    }

}
