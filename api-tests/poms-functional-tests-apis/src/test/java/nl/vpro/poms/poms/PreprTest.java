package nl.vpro.poms.poms;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Predicate;

import javax.management.*;
import javax.management.openmbean.TabularDataSupport;

import org.junit.jupiter.api.*;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.testutils.JMXSupport;
import nl.vpro.testutils.Utils;

import static nl.vpro.testutils.JMXSupport.getMBeanServerConnection;
import static nl.vpro.testutils.JMXSupport.getObjectName;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michiel Meeuwissen
 */
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PreprTest  extends AbstractApiMediaBackendTest {
    static final String SERIES = "RBX_S_VPRO_13100068";
    static final String GROUP = "POMS_S_VPRO_6022303";
    static final Predicate<MediaObject> CORRECT = (mo)-> mo.getMemberOf().stream().anyMatch(r -> r.getMediaRef().equals(GROUP));


    static final ObjectName PREPR = getObjectName("nl.vpro.media:name=prepr");
    static final ObjectName MBEANS = getObjectName("nl.vpro.jmx:name=nl.vpro.jmx.MBeansUtils#0,type=MBeansUtils");

    static JMXSupport.JMXContainer pomsJMX = getMBeanServerConnection(CONFIG.getProperties(Config.Prefix.poms));

    @AfterAll
    public static void shutdown() {
        pomsJMX.shutdown();
    }

    @Test
    @Order(1)
    public void ensureGroup() {
        MediaObject series= backend.getFull(SERIES);
        if (! CORRECT.test(series)) {
            backend.createMember(GROUP, SERIES, 1);

            Utils.waitUntil(ACCEPTABLE_DURATION_BACKEND,
                () -> backend.getFull(SERIES),
                Utils.Check.<MediaObject>builder()
                    .predicate(CORRECT)
                    .description("{} is in {}", SERIES, GROUP)
                    .build()
            );
        }

    }


    @Test
    @Order(9) // TODO, on dev's gives unique constraint
    public void resyncBroadcast() throws IOException, MBeanException, InstanceNotFoundException, ReflectionException {


        Object syncBroadcast = pomsJMX.connection.invoke(PREPR, "syncBroadcast",
            new Object[]{"PREPR_VPRO_15979404"}, new String[]{
                String.class.getName()
            });
        log.info("{}", syncBroadcast);
    }

    @Test
    @Order(10)
    public void resyncDay() throws IOException, MBeanException, InstanceNotFoundException, ReflectionException {

        Object syncDay = pomsJMX.connection.invoke(PREPR, "sync",
            new Object[]{"RAD3", "2020-02-15", null}, new String[] {
                String.class.getName(),
                String.class.getName(),
                String.class.getName()});
        log.info("{}", syncDay);

        // wait, may be do something with events
        Utils.waitUntil(
            Duration.ofMinutes(5),
            () ->  {
                try {
                    return (TabularDataSupport) pomsJMX.connection.getAttribute(MBEANS, "Running");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return null;
                }
            },
            Utils.Check.<TabularDataSupport>builder()
                .description("No JMX running any more")
                .predicate(TabularDataSupport::isEmpty).build()
        );

    }

    @Test
    @Order(20)
    public void stillEnsureGroup() {
        MediaObject series= backend.getFull(SERIES);
        assertThat(CORRECT.test(series)).isTrue();
    }
}
