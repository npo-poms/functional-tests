package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.*;
import nl.vpro.domain.media.support.Image;
import nl.vpro.domain.media.support.License;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractApiMediaBackendTest extends AbstractApiTest {

    protected static final MediaRestClient backend =
        MediaRestClient.configured(Config.env(),
            Config.getProperties(Config.Prefix.backendapi))

            .build();
    protected static final String backendVersion = backend.getVersion();
    protected static Float backendVersionNumber;


    static {
        try {
            backendVersionNumber = backend.getVersionNumber();
        } catch (Exception e) {
            backendVersionNumber = 0f;

        }
        log.info("Using {} ({})", backend, backendVersion);
    }

    public Image createImage() {
        Image image = new Image(OwnerType.BROADCASTER, ImageType.PICTURE, title);
        try {
            image.setImageUri("https://placeholdit.imgix.net/~text?txt=" + URLEncoder.encode(title, "UTF-8") + "&w=150&h=150");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        image.setLicense(License.CC_BY);
        image.setSourceName("placeholdit");
        image.setCredits(getClass().getName());
        return image;

    }

    public Segment createSegment() {
        return
            MediaBuilder.segment()
                .mainTitle(title)
                .ageRating(AgeRating.ALL)
                .start(Duration.ofSeconds(70))
                .build();
    }

    public Location createLocation(int count) {
        return
            Location.builder()
                .avAttributes(AVAttributes.builder().avFileFormat(AVFileFormat.H264).build())
                .platform(Platform.INTERNETVOD)
                .programUrl("https://www.vpro.nl/" + count)
                .build();

    }
}
