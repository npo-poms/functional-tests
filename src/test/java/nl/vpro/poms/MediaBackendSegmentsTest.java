package nl.vpro.poms;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import nl.vpro.rs.media.MediaRestClient;

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

}
