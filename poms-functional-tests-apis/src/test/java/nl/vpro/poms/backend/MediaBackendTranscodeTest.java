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
import nl.vpro.util.Env;

import static nl.vpro.domain.media.update.TranscodeStatus.Status.COMPLETED;
import static nl.vpro.domain.media.update.TranscodeStatus.Status.FAILED;
import static nl.vpro.testutils.Utils.waitUntils;

/**
 * Tests if files can be uploaded, and be correctly handled.
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@ExtendWith(AbortOnException.class)
class MediaBackendTranscodeTest extends AbstractApiMediaBackendTest {

    static String fileName = MediaBackendTranscodeTest.class.getSimpleName() + "-" + SIMPLE_NOWSTRING;

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

    String uploadFileName = fileName + "-manual.mp4";
    String nonexistingFile = fileName + "-doesntexist.mp4";

    String uploadAndTrancodeFileName = fileName + "-uploadAndTranscode.mp4";

    String newMid;

    @BeforeAll
    public static void test() {
        if (CONFIG.env() != Env.PROD) {
            throw new IllegalStateException("It is known currently not to work in staging @ NEP");
        }
    }

    @Test
    @Order(0)
    @Tag("errorneous")
    void transcodeErrorneousFile() {
        TranscodeRequest request =
            TranscodeRequest.builder()
                .mid(MID)
                .encryption(Encryption.NONE)
                .fileName(nonexistingFile)
                .build();

        String result = backend.transcode(request);
        log.info("{}: {}", newMid, result);
    }

    @Test
    @Order(1)
    @Tag("errorneous")
    void checkTranscodeErrorneousFile() {
        check(uploadStart, FAILED);
    }

    @Test
    @Order(10)
    @Tag("manual")
    void uploadFile() throws IOException {
        uploadStart = Instant.now();
        long upload = uploadService.upload(SimpleLogger.slfj4(log), uploadFileName, 1279795L, getClass().getResourceAsStream("/test.mp4"), true);

        log.info("Uploaded {}: {}", uploadFileName, upload);

    }

    @Test
    @Order(11)
    @Tag("manual")
    void transcodeAfterManualUpload() {
        TranscodeRequest request =
            TranscodeRequest.builder()
                .mid(MID)
                .encryption(Encryption.NONE)
                .fileName(uploadFileName)
                .build();

        String result = backend.transcode(request);
        log.info("{}: {}", newMid, result);
    }

    @Test
    @Order(12)
    @Tag("manual")
    void checkStatusAfterManualUpload() {
        XmlCollection<TranscodeStatus> vpro = backend.getBackendRestService().getTranscodeStatusForBroadcaster(
            uploadStart.minus(Duration.ofDays(3)), /*TranscodeStatus.Status.RUNNING* doesn't work on acc*/ null, null);
        log.info("{}", vpro);

        check(uploadStart, COMPLETED);
    }

    @Test
    @Order(20)
    @Tag("viaapi")
    void uploadAndTranscode() throws IOException {
        transcodeStart = Instant.now();

        try(Response response = backend.getBackendRestService().uploadAndTranscode(
            MID,
            Encryption.NONE,
            TranscodeRequest.Priority.NORMAL,
            uploadAndTrancodeFileName,
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
    @Order(21)
    @Tag("viaapi")
    void checkUploadAndTranscode() {
        check(transcodeStart, COMPLETED);
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


    protected  List<TranscodeStatus>  check(Instant after, TranscodeStatus.Status expectedStatus) {
        List<TranscodeStatus> transcodeStatus = waitUntils(Duration.ofMinutes(20), "transcoding finished with status " + expectedStatus, () -> {
                List<TranscodeStatus> list = backend.getBackendRestService()
                    .getTranscodeStatus(EntityType.NoGroups.media, MID)
                    .stream()
                    .filter(ts -> ts.getStartTime().isAfter(after))
                    .collect(Collectors.toList());
                log.info("Found {}", list);
                return list;
            },
            (list) -> list.size() > 0,
            (list) -> list.get(0).getStatus() == expectedStatus
        );
        log.info("{}", transcodeStatus);
        return transcodeStatus;
    }

}
