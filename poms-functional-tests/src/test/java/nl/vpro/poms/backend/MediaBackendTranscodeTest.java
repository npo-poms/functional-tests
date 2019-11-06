package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.domain.media.Encryption;
import nl.vpro.domain.media.MediaBuilder;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.domain.media.update.TranscodeRequest;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.util.Version;

import static org.assertj.core.api.Assumptions.assumeThat;

/**
 * Tests if files can be uploaded, and be correctly handled.
 *
 *
 *
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Slf4j
@ExtendWith(AbortOnException.class)
class MediaBackendTranscodeTest extends AbstractApiMediaBackendTest {


    @Test
    void test01Transcode() {
        assumeThat(backendVersionNumber).isGreaterThanOrEqualTo((Version.of(5, 6)));

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
    @Disabled("Not yet implemented")
    void test02CreatePredictions() {
        // TODO
    }


    @Test
    @Disabled("Not yet implemented")
    void test03CheckForLocationsToArriveFromNEP() {
        // TODO

    }


}
