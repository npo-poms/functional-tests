package nl.vpro.poms.backend;

import java.time.Duration;
import java.util.Locale;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.subtitles.Subtitles;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.DoAfterException;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeThat;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MediaBackendSubtitlesTest extends AbstractApiMediaBackendTest {

    private static final String MID = "WO_VPRO_025057";
    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);





    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        if (! (t instanceof AssumptionViolatedException)) {
            MediaBackendSubtitlesTest.exception = t;
        }
    });

    private static Throwable exception = null;

    @Before
    public void setup() {
        assumeNoException(exception);
    }

    @Test
    public void test01addSubtitles() {
        assumeThat(backendVersionNumber, greaterThanOrEqualTo(5.1f));


        Subtitles subtitles = Subtitles.webvtt(MID, Duration.ZERO, Locale.CHINESE,
            ""
        );
        backend.setSubtitles(subtitles);
    }


}
