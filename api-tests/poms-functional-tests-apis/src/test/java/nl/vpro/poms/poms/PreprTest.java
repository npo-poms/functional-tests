package nl.vpro.poms.poms;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.management.*;
import javax.management.remote.*;

import org.junit.jupiter.api.*;

import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.testutils.Utils;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michiel Meeuwissen
 */
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PreprTest  extends AbstractApiMediaBackendTest {
    static final String SERIES = "RBX_S_VPRO_13100068";
    static final String GROUP = "POMS_S_VPRO_6022303";
    static final Predicate<MediaObject> CORRECT = (mo)-> mo.getMemberOf().stream().anyMatch(r -> r.getOwner() == OwnerType.BROADCASTER && r.getMediaRef().equals(GROUP));

    static MBeanServerConnection mBeanServerConnection;

    @BeforeAll
    public static void initJMX() throws IOException {
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {

            }
        });
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://pomz4aas:48113/jndi/rmi://pomz4aas:38113/jmxrmi");
        Map<String, Object> env = new HashMap<>();
        String[] credentials = {"admin", CONFIG.getProperty("jmx.password") };
        env.put(JMXConnector.CREDENTIALS, credentials);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
        jmxc.connect();
        mBeanServerConnection = jmxc.getMBeanServerConnection();

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
                    .description("{} must be in {}", SERIES, GROUP)
                    .build()
            );
        }

    }

    @Test
    @Order(2)
    public void resyncDay() throws IOException, MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException, InterruptedException {

        Object sync = mBeanServerConnection.invoke(new ObjectName("nl.vpro.media:name=prepr"), "sync",

            new Object[]{"RAD3", "2020-02-15", null}, new String[] {
                String.class.getName(),
                String.class.getName(),
                String.class.getName()});
        log.info("{}", sync);

        // wait, may be do something with events.
        Thread.sleep(10000L);
    }


    @Test
    @Order(4)
    public void stillEsureGroup() {
        MediaObject series= backend.getFull(SERIES);
        assertThat(CORRECT.test(series)).isTrue();
    }
}
