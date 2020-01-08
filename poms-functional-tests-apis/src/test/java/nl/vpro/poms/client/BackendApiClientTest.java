package nl.vpro.poms.client;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.*;

import nl.vpro.domain.media.update.MediaUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@Log4j2
class BackendApiClientTest extends AbstractApiMediaBackendTest  {


    private List<LoggingEvent> logging = new ArrayList<>();

    @BeforeEach
    void initLogging() {
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getRootLogger().addAppender(new AppenderSkeleton() {
            {
                this.name = "capture";
            }
            @Override
            protected void append(LoggingEvent event) {
                logging.add(event);
            }
            @Override
            public void close() {
            }

            @Override
            public boolean requiresLayout() {
                return false;
            }

        });
    }
    @AfterEach
    void shutdownLogging() {
        Logger.getRootLogger().removeAppender("capture");
    }


    @Test
    void getByCrid() {
        MediaUpdate<?> mediaUpdate = backend.get("crid://tmp.fragment.mmbase.vpro.nl/43084334");
        assertThat(mediaUpdate).isNotNull();
        assertThat(mediaUpdate.getMid()).isEqualTo("WO_VPRO_034420");
    }

    @Test
    void test404() {
        backend.get("bestaatniet");
        List<LoggingEvent> errors =
            logging.stream().filter((l) -> l.getLevel().isGreaterOrEqual(Level.WARN)).collect(Collectors.toList());
        assertThat(errors).isEmpty();

         List<LoggingEvent> info =
             logging.stream().filter((l) -> ! l.getLevel().isGreaterOrEqual(Level.WARN)).collect(Collectors.toList());
        assertThat(info.get(0).getMessage().toString()).hasLineCount(6);
    }
}
