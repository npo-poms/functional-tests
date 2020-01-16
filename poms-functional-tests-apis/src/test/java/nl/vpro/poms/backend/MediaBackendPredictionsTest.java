package nl.vpro.poms.backend;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.*;

import nl.vpro.domain.media.*;
import nl.vpro.domain.media.update.PredictionUpdate;
import nl.vpro.domain.media.update.collections.XmlCollection;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.testutils.Utils;

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
        waitUntil(ACCEPTABLE_DURATION,
            () -> {
                try {
                    return backend.getBackendRestService().getPredictions(null, MID, null);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    return null;
                }
            },
            Utils.Check.<XmlCollection<PredictionUpdate>>builder()
                .description("prediction has publishStart " + NOW)
                .predicate((l) ->
                        l.stream()
                            .map(e -> e.getPlatform() == Platform.INTERNETVOD && e.getPublishStart().equals(NOW.toInstant()))
                            .findFirst().isPresent())
                .build(),
            Utils.Check.<XmlCollection<PredictionUpdate>>builder()
                .description("prediction has encryption NONE")
                .predicate((l) ->
                    l.stream()
                        .map(e -> e.getPlatform() == Platform.INTERNETVOD && Objects.equals(e.getEncryption(), Encryption.NONE))
                        .findFirst().isPresent())
                .build()
        );
    }



    @Test
    @Tag("prediction")
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
                             .publishStart(NOW.toInstant().minus(Duration.ofMinutes(5)))
                             .build()))
        ) {

            log.info("{}", response.getEntity());
        }
    }

    @Test
    @Tag("prediction")
    @Order(4)
    public void checkSetPredictions() {
        waitUntil(ACCEPTABLE_DURATION,
            () -> backend.getFull(MID),
            Utils.Check.<MediaObject>builder()
                .description("prediction has publishStart " + NOW)
                .predicate((m) -> m.findOrCreatePrediction(Platform.INTERNETVOD).getPublishStartInstant().equals(NOW.toInstant().minus(Duration.ofMinutes(5))))
                .build(),
            Utils.Check.<MediaObject>builder()
                .description("prediction is with DRM")
                .predicate((m) -> Objects.equals(m.findOrCreatePrediction(Platform.INTERNETVOD).getEncryption(), Encryption.DRM))
                .build()
        );
    }
}
