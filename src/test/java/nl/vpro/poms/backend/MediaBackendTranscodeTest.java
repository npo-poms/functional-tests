package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.Encryption;
import nl.vpro.domain.media.MediaBuilder;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.TranscodeRequest;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.rules.DoAfterException;

import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeTrue;

/**
 * Tests whether adding and modifying locations via the POMS backend API works.
 *
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MediaBackendTranscodeTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

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
    public void test01Transcode() throws IOException {
        assumeTrue(backendVersionNumber > 5.6f);

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



}
