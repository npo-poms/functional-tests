package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.media.*;
import nl.vpro.domain.media.update.*;
import nl.vpro.domain.media.update.collections.XmlCollection;
import nl.vpro.nep.service.NEPUploadService;
import nl.vpro.nep.service.impl.NEPSSHJUploadServiceImpl;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.util.Version;

import static org.assertj.core.api.Assumptions.assumeThat;

/**
 * Tests if files can be uploaded, and be correctly handled.
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@ExtendWith(AbortOnException.class)
class MediaBackendTranscodeTest extends AbstractApiMediaBackendTest {


    static NEPUploadService uploadService;
    @BeforeAll
    public static void init() {
        Map<String, String> properties = CONFIG.getProperties(Config.Prefix.nep);
        uploadService = new NEPSSHJUploadServiceImpl(
            properties.get("gatekeeper-upload.host"),
            properties.get("gatekeeper-upload.username"),
            properties.get("gatekeeper-upload.password"),
            properties.get("gatekeeper-upload.hostkey")
        );
        log.info("{}", uploadService);
    }

    Instant start = Instant.now();
    String newMid;


    @Test
    @Order(1)
    void transcode() {
        assumeThat(backendVersionNumber).isGreaterThanOrEqualTo((Version.of(5, 6)));

        newMid = backend.set(ProgramUpdate.create(MediaBuilder.clip()
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
    @Order(2)
    void checkStatus() {
        if (newMid == null) {
            newMid = "POMS_VPRO_3318486";
        }
        XmlCollection<TranscodeStatus> vpro = backend.getBackendRestService().getTranscodeStatusForBroadcaster(
            start, TranscodeStatus.Status.RUNNING, 100);
        log.info("{}", vpro);

        XmlCollection<TranscodeStatus> transcodeStatus = backend.getBackendRestService().getTranscodeStatus(EntityType.NoGroups.media, newMid);
        log.info("{}", transcodeStatus.stream().findFirst().orElse(null));
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
