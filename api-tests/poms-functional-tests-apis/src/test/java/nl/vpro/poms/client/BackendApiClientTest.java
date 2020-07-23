package nl.vpro.poms.client;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.*;

import nl.vpro.domain.media.update.MediaUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.Require;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@Log4j2
class BackendApiClientTest extends AbstractApiMediaBackendTest  {


    private final ListAppender logging = new ListAppender();

    @BeforeEach
    void initLogging() {
        backend.getBackendRestService();
        LoggerContext loggerContext = LoggerContext.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();

        configuration.getRootLogger().setLevel(Level.INFO);
        configuration.getRootLogger().addAppender(logging, Level.DEBUG, null);
        logging.start();
    }
    @AfterEach
    void shutdownLogging() {
        logging.stop();
        LoggerContext loggerContext = LoggerContext.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        configuration.getRootLogger().removeAppender(logging.getName());
    }

    @Test
    @Require.Needs("crid://tmp.fragment.mmbase.vpro.nl/43084334")
    void getByCrid() {
        MediaUpdate<?> mediaUpdate = backend.get("crid://tmp.fragment.mmbase.vpro.nl/43084334");
        assertThat(mediaUpdate).isNotNull();
        assertThat(mediaUpdate.getMid()).isEqualTo("WO_VPRO_034420");
    }

    @Test
    void test404() {
        backend.get("bestaatniet");
        List<LogEvent> errors = logging.list.stream().filter((l) -> l.getLevel().isMoreSpecificThan(Level.WARN)).collect(Collectors.toList());
        assertThat(errors).isEmpty();

         List<LogEvent> info =
             logging.list.stream()
                 .filter((l) -> l.getLoggerName().endsWith(".4.04") && l.getLevel().isLessSpecificThan(Level.INFO))
                 .collect(Collectors.toList());
        assertThat(info).hasSizeGreaterThan(0);
        assertThat(info.get(0).getMessage().toString())
            .withFailMessage(info.get(0).getMessage().toString())
            .hasLineCount(6);
    }

    public static class ListAppender extends AbstractAppender {
        List<LogEvent> list = new ArrayList<>();
        ListAppender() {
            super("capture", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            list.add(event);
        }
    }
}
