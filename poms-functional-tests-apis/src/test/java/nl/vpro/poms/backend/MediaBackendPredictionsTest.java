package nl.vpro.poms.backend;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.*;

import nl.vpro.domain.media.Encryption;
import nl.vpro.domain.media.Platform;
import nl.vpro.domain.media.update.PredictionUpdate;
import nl.vpro.domain.media.update.collections.XmlCollection;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.testutils.Utils;
import nl.vpro.testutils.Utils.Check;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assumptions.assumeThat;



/***
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
class MediaBackendPredictionsTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    @BeforeEach
    public void setup() {
        log.info("Mailing errors to {}", backend.getErrors());
        assumeThat(backend.getErrors()).isNotEmpty();
    }


    @Test
    @Tag("prediction")
    @Order(1)
    public void setPrediction() throws IOException {
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
                         .build())
        ) {

            log.info("{}", response.getEntity());
        }
    }


    @Test
    @Tag("prediction")
    @Order(2)
    public void checkSetPrediction() {
        Utils.waitUntil(ACCEPTABLE_DURATION,
            () -> getPredictions(MID),
            Check.<XmlCollection<PredictionUpdate>>description("prediction of " + MID + " has publishStart " + NOW)
                .predicate((l) ->
                        l.stream()
                            .map(e -> e.getPlatform() == Platform.INTERNETVOD && e.getPublishStart().equals(NOW.toInstant()))
                            .findFirst().isPresent()
                ),
            Check.<XmlCollection<PredictionUpdate>>description("prediction of " + MID + " has encryption NONE")
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
        waitUntil(ACCEPTABLE_DURATION,
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


