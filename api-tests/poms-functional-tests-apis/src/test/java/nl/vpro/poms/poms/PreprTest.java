package nl.vpro.poms.poms;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Predicate;

import javax.management.*;
import javax.management.openmbean.TabularDataSupport;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import nl.vpro.domain.media.MediaObject;
import nl.vpro.junit.extensions.For;
import nl.vpro.junit.extensions.JMXContainers;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.testutils.Utils;

import static nl.vpro.api.client.utils.Config.Prefix.poms;
import static nl.vpro.testutils.JMXSupport.getObjectName;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michiel Meeuwissen
 */
@Log4j2
@ExtendWith(JMXContainers.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PreprTest  extends AbstractApiMediaBackendTest {
    static final String SERIES = "RBX_S_VPRO_13100068";
    static final String GROUP = "POMS_S_VPRO_6022303";
    static final Predicate<MediaObject> CORRECT = (mo)-> mo.getMemberOf().stream().anyMatch(r -> r.getMediaRef().equals(GROUP));


    static final ObjectName PREPR = getObjectName("nl.vpro.media:name=prepr");
    static final ObjectName MBEANS = getObjectName("nl.vpro.jmx:name=nl.vpro.jmx.MBeansUtils#0,type=MBeansUtils");


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
    @Order(9)
    public void resyncBroadcast(@For(poms) MBeanServerConnection pomsJMX) throws IOException, MBeanException, InstanceNotFoundException, ReflectionException {

        Object syncBroadcast = pomsJMX.invoke(PREPR, "syncBroadcast",
            new Object[]{"PREPR_VPRO_15979404"}, new String[]{
                String.class.getName()
            });
        log.info("{}", syncBroadcast);
    }

    @Test
    @Order(10)
    public void resyncDay(@For(poms) MBeanServerConnection pomsJMX) throws IOException, MBeanException, InstanceNotFoundException, ReflectionException {

        Object syncDay = pomsJMX.invoke(PREPR, "sync",
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
                    return (TabularDataSupport) pomsJMX.getAttribute(MBEANS, "Running");
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
