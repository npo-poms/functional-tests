 package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.media.Encryption;
import nl.vpro.domain.media.EntityType;
import nl.vpro.domain.media.update.TranscodeRequest;
import nl.vpro.domain.media.update.TranscodeStatus;
import nl.vpro.domain.media.update.collections.XmlCollection;
import nl.vpro.logging.simple.SimpleLogger;
import nl.vpro.nep.service.NEPUploadService;
import nl.vpro.nep.service.impl.NEPSSHJUploadServiceImpl;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.util.Version;

import static nl.vpro.testutils.Utils.waitUntils;
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
            properties.get("nep.gatekeeper-upload.host"),
            properties.get("nep.gatekeeper-upload.username"),
            properties.get("nep.gatekeeper-upload.password"),
            properties.get("nep.gatekeeper-upload.hostkey")
        );
        log.info("{}", uploadService);
    }

    Instant uploadStart = Instant.now();
    Instant transcodeStart = Instant.now();

    String newMid;

    @Test
    @Order(0)
    @Tag("manual")
    void uploadFile() throws IOException {
        uploadStart = Instant.now();
        assumeThat(backendVersionNumber).isGreaterThanOrEqualTo((Version.of(5, 6)));
        long upload = uploadService.upload(SimpleLogger.slfj4(log), "test.mp4", 1279795L, getClass().getResourceAsStream("/test.mp4"), true);

        log.info("Uploaded {}", upload);

    }

    @Test
    @Order(1)
    @Tag("manual")
    void transcodeAfterManualUpload() {
        TranscodeRequest request =
            TranscodeRequest.builder()
                .mid(MID)
                .encryption(Encryption.NONE)
                .fileName("test.mp4")
                .build();

        String result = backend.transcode(request);
        log.info("{}: {}", newMid, result);
    }

    @Test
    @Order(2)
    @Tag("manual")
    void checkStatusAfterManualUpload() {
        XmlCollection<TranscodeStatus> vpro = backend.getBackendRestService().getTranscodeStatusForBroadcaster(
            uploadStart.minus(Duration.ofDays(3)), /*TranscodeStatus.Status.RUNNING* doesn't work on acc*/ null, null);
        log.info("{}", vpro);

        check(uploadStart);

    }




    @Test
    @Order(10)
    @Tag("viaapi")
    void uploadAndTranscode() throws IOException {
        transcodeStart = Instant.now();

        try(Response response = backend.getBackendRestService().uploadAndTranscode(
            MID,
            Encryption.NONE,
            TranscodeRequest.Priority.NORMAL,
            "uploadAndTranscodeTest.mp4",
            getClass().getResourceAsStream("/test.mp4"),
            "video/mp4",
            null,
            true,
            true,
            null,
            null
        )) {
            log.info("{}", response);
        }
    }


    @Test
    @Order(11)
    @Tag("viaapi")
    void checkUploadAndTranscode() {
        check(transcodeStart);
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


    protected  List<TranscodeStatus>  check(Instant after) {
        List<TranscodeStatus> transcodeStatus = waitUntils(Duration.ofMinutes(20), "transcoding finished", () -> {
                List<TranscodeStatus> list = backend.getBackendRestService()
                    .getTranscodeStatus(EntityType.NoGroups.media, MID)
                    .stream()
                    .filter(ts -> ts.getStartTime().isAfter(after))
                    .collect(Collectors.toList());
                log.info("Found {}", list);
                return list;
            },
            (list) -> list.size() > 0,
            (list) -> list.get(0).getStatus() != TranscodeStatus.Status.RUNNING
        );
        log.info("{}", transcodeStatus);
        return transcodeStatus;
    }

}
