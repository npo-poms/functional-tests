package nl.vpro.poms;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.AVType;
import nl.vpro.domain.media.MediaBuilder;
import nl.vpro.domain.media.update.SegmentUpdate;
import nl.vpro.rs.media.MediaRestClient;

import static nl.vpro.poms.Utils.waitUntil;
import static org.junit.Assume.assumeNotNull;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MediaBackendSegmentsTest extends AbstractApiTest {
    static final MediaRestClient backend = new MediaRestClient().configured();

    private static final String MID = "WO_VPRO_025057";
    private static final String TITLE = Instant.now().toString();
    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    private static final List<String> titles = new ArrayList<>();

    private static String segmentMid;

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
    public void test01createSegment() {
        SegmentUpdate update = SegmentUpdate.create(
            MediaBuilder.segment()
                .avType(AVType.VIDEO)
                .broadcasters("VPRO")
                .midRef(MID)
                .start(Duration.ofMillis(0))
                .mainTitle(title));
        JAXB.marshal(update, System.out);
        segmentMid = backend.set(update);
        System.out.println("Created " + segmentMid);

    }

    @Test
    public void test02WaitFor() throws Exception {
        assumeNotNull(segmentMid);
        waitUntil(ACCEPTABLE_DURATION, () -> {
            SegmentUpdate up = backend.get(segmentMid);
            return up != null;
        });


    }

}
