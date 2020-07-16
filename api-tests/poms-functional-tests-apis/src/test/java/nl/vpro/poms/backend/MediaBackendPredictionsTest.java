package nl.vpro.poms.backend;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import nl.vpro.domain.media.Encryption;
import nl.vpro.domain.media.Platform;
import nl.vpro.domain.media.update.PredictionUpdate;
import nl.vpro.domain.media.update.TranscodeRequest;
import nl.vpro.domain.media.update.collections.XmlCollection;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.Require.Needs;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.testutils.Utils;
import nl.vpro.testutils.Utils.Check;
import org.junit.jupiter.api.*;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assumptions.assumeThat;



/***
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
class MediaBackendPredictionsTest extends AbstractApiMediaBackendTest {


    @BeforeEach
    public void setup() {
        log.info("Mailing errors to {}", backend.getErrors());
        assumeThat(backend.getErrors()).isNotEmpty();
    }


    @Test
    @Tag("prediction")
    @Order(1)
    @Needs(MID)
    public void setPrediction() throws IOException {
        TranscodeRequest request = TranscodeRequest.builder()
            .encryption(Encryption.NONE)
            .mid(MID)
            .fileName("bla.mp4")
            .build();

        log.info("Setting predictionw ith publish start {}", NOW);
        try (Response response =
                 backend.getBackendRestService().setPrediction(
                     null,
                     MID,
                     Platform.INTERNETVOD,
                     null,
                     null,
                     PredictionUpdate.builder()
                         .encryption(Encryption.NONE)
                         .publishStart(NOW.toInstant())
                         .build());
             Response trancodeResponse = backend.getBackendRestService()
                 .transcode(null, null, request)
        ) {

            log.info("{} / {} ", response.getEntity(), trancodeResponse);
        }
    }


    @Test
    @Tag("prediction")
    @Order(2)
    public void checkSetPrediction() {
        Utils.waitUntil(ACCEPTABLE_DURATION_BACKEND,
            () -> getPredictions(MID),
            Check.<XmlCollection<PredictionUpdate>>description("prediction of {} has publishStart {}", MID, NOW)
                .predicate((l) ->
                        l.stream()
                            .map(e -> e.getPlatform() == Platform.INTERNETVOD && e.getPublishStart().equals(NOW.toInstant()))
                            .findFirst().isPresent()
                ),
            Check.<XmlCollection<PredictionUpdate>>description("prediction of {} has encryption NONE", MID)
                .predicate((l) ->
                    l.stream()
                        .map(e -> e.getPlatform() == Platform.INTERNETVOD && Objects.equals(e.getEncryption(), Encryption.NONE))
                        .findFirst().isPresent()
                )
        );
    }

    private static final Instant OTHERTIME = NOW.toInstant().minus(Duration.ofMinutes(5));

    @Test
    @Tag("predictions")
    @Order(3)
    public void setPredictions() throws IOException {
        try (Response response =
                 backend.getBackendRestService().setPredictions(
                     null,
                     MID,
                     null,
                     null,
                     new XmlCollection<>(
                         PredictionUpdate.builder()
                             .encryption(Encryption.DRM)
                             .platform(Platform.INTERNETVOD)
                             .publishStart(OTHERTIME)
                             .build()))
        ) {

            log.info("{}", response.getEntity());
        }
    }

    /**
     * Reproduces MSE-4674
     */
    @Test
    @Tag("predictions")
    @Order(4)
    public void checkSetPredictions() {
        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            () -> getPredictions(MID),
            Check.<XmlCollection<PredictionUpdate>>builder()
                .description("prediction of " + MID + " has publishStart " + OTHERTIME)
                .predicate((l) ->
                        l.stream()
                            .map(e -> e.getPlatform() == Platform.INTERNETVOD && e.getPublishStart().equals(OTHERTIME))
                            .findFirst().isPresent())
                .build(),
            Check.<XmlCollection<PredictionUpdate>>builder()
                .description("prediction of " + MID + " has encryption DRM")
                .predicate((l) ->
                    l.stream()
                        .map(e -> e.getPlatform() == Platform.INTERNETVOD && Objects.equals(e.getEncryption(), Encryption.DRM))
                        .findFirst().isPresent())
                .build()
        );
    }

      /**
     * Reproduces MSE-4674
     */
    @Test
    @Tag("predictions")
    @Order(100)
    @AbortOnException.NoAbort
    public void deletePredictions() {
        waitUntil(ACCEPTABLE_DURATION_BACKEND,
            () -> getPredictions(MID),
            Check.<XmlCollection<PredictionUpdate>>builder()
                .description("prediction of " + MID + " has publishStart " + OTHERTIME)
                .predicate((l) ->
                        l.stream()
                            .map(e -> e.getPlatform() == Platform.INTERNETVOD && e.getPublishStart().equals(OTHERTIME))
                            .findFirst().isPresent())
                .build(),
            Check.<XmlCollection<PredictionUpdate>>builder()
                .description("prediction of " + MID + " has encryption DRM")
                .predicate((l) ->
                    l.stream()
                        .map(e -> e.getPlatform() == Platform.INTERNETVOD && Objects.equals(e.getEncryption(), Encryption.DRM))
                        .findFirst().isPresent())
                .build()
        );
    }

    @SneakyThrows
    private XmlCollection<PredictionUpdate> getPredictions(String mid) {
        return backend.getBackendRestService().getPredictions(null, mid, null);
    }
}


