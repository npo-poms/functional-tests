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

    static NEPSSHJUploadServiceImpl uploadService;



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

    Instant transcodeRelativeStart = Instant.now();
    Instant transcodeAbsoluteStart = Instant.now();
    Instant uploadAndTranscodeStart = Instant.now();

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
        check(NOW.toInstant(), FAILED);
    }

    @Test
    @Order(10)
    @Tag("manual")
    void uploadFile() throws IOException {
        long upload = uploadService.upload(SimpleLogger.slfj4(log), uploadFileName, 1279795L, getClass().getResourceAsStream("/test.mp4"), true);

        log.info("Uploaded {}: {}", uploadFileName, upload);

    }

    @Test
    @Order(11)
    @Tag("manual")
    void transcodeWithAbsolutePathAfterManualUpload() {
        transcodeAbsoluteStart = Instant.now();
        TranscodeRequest request =
            TranscodeRequest.builder()
                .mid(MID)
                .encryption(Encryption.NONE)
                .fileName("/" + uploadService.getUsername() + "/" + uploadFileName)
                .build();

        String result = backend.transcode(request);
        log.info("{}: {}", newMid, result);
    }

    @Test
    @Order(12)
    @Tag("manual")
    void checkStatusAfterManualUploadAndTranscodeWithAbsolutePath() {
        XmlCollection<TranscodeStatus> vpro = backend.getBackendRestService().getTranscodeStatusForBroadcaster(
            NOW.toInstant().minus(Duration.ofDays(3)), /*TranscodeStatus.Status.RUNNING* doesn't work on acc*/ null, null);
        log.info("{}", vpro);

        check(transcodeAbsoluteStart, COMPLETED);
    }


    @Test
    @Order(13)
    @Tag("manual")
    void transcodeWithRelativePathAfterManualUpload() {
        transcodeRelativeStart = Instant.now();
        TranscodeRequest request =
            TranscodeRequest.builder()
                .mid(MID)
                .encryption(Encryption.NONE)
                .fileName(uploadFileName) // we use the account that poms will prefix
                .build();

        String result = backend.transcode(request);
        log.info("{}: {}", newMid, result);
    }

    @Test
    @Order(14)
    @Tag("manual")
    void checkStatusAfterManualUploadAndTranscodeWithRelativePath() {
        check(transcodeRelativeStart, COMPLETED);
    }

    @Test
    @Order(20)
    @Tag("viaapi")
    void uploadAndTranscode() throws IOException {
        uploadAndTranscodeStart = Instant.now();

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
        check(uploadAndTranscodeStart, COMPLETED);
    }

    @Test
    @Disabled("Not yet implemented")
    void test02CreatePredictions() {
        // TODO
        // This can only be tested on new objects. But we cant run on dev then, because there is no environment at NEP which looks at DEV.
        // Silly MID  checks. I would prefer they ditch it.
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
