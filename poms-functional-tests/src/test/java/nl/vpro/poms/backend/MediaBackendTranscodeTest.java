package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.Encryption;
import nl.vpro.domain.media.MediaBuilder;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.TranscodeRequest;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.rules.DoAfterException;
import nl.vpro.util.Version;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeThat;

/**
 * Tests if files can be uploaded, and be correctly handled.
 *
 *
 *
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MediaBackendTranscodeTest extends AbstractApiMediaBackendTest {


    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        if (! (t instanceof AssumptionViolatedException)) {
            MediaBackendTranscodeTest.exception = t;
        }
    });

    private static Throwable exception = null;


    @Before
    public void setup() {
        assumeNoException(exception);
    }



    @Test
    public void test01Transcode() {
        assumeThat(backendVersionNumber,  greaterThanOrEqualTo(Version.of(5, 6)));

        String newMid = backend.set(ProgramUpdate.create(MediaBuilder.clip()
            .mainTitle(title)
            .broadcasters("VPRO")
            .build())
        );

        TranscodeRequest request =
            TranscodeRequest.builder()
                .mid(newMid)
                .encryption(Encryption.NONE)
                .fileName("used-by-integration-tests.m4v")
                .build();

        String result = backend.transcode(request);
        log.info("{}: {}", newMid, result);

    }


    @Test
    @Ignore("Not yet implemented")
    public void test02CreatePredictions() {
        // TODO
    }


    @Test
    @Ignore("Not yet implemented")
    public void test03CheckForLocationsToArriveFromNEP() {
        // TODO

    }


}
