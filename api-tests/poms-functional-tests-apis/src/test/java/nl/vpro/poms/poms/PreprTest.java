package nl.vpro.poms.poms;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.security.Permission;
import java.util.*;
import java.util.function.Predicate;

import javax.management.*;
import javax.management.remote.*;

import org.junit.jupiter.api.*;

import com.sun.tools.attach.*;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.media.MediaObject;
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
    static final Predicate<MediaObject> CORRECT = (mo)-> mo.getMemberOf().stream().anyMatch(r -> r.getMediaRef().equals(GROUP));


    static final ObjectName PREPR = getObjectName("nl.vpro.media:name=prepr");

    @SneakyThrows
    static ObjectName getObjectName(String objectName) {
         return new ObjectName(objectName);
    }

    static MBeanServerConnection mBeanServerConnection;

    @BeforeAll
    public static void initJMX() throws IOException {
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {

            }
        });
        String jmxUrl = CONFIG.getProperties(Config.Prefix.poms).get("jmx-url");
        JMXServiceURL url = jmxUrl.startsWith("localhost:") ? localhost(jmxUrl.substring("localhost:".length())) : new JMXServiceURL(jmxUrl);
        Map<String, Object> env = new HashMap<>();
        String[] credentials = {
            CONFIG.getProperties(Config.Prefix.poms).get("jmx-username"),
            CONFIG.getProperties(Config.Prefix.poms).get("jmx-password")
        };
        env.put(JMXConnector.CREDENTIALS, credentials);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
        jmxc.connect();
        mBeanServerConnection = jmxc.getMBeanServerConnection();
    }

    public static JMXServiceURL localhost(String pid) throws IOException {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor desc : vms) {
            VirtualMachine vm;
            try {
                vm = VirtualMachine.attach(desc);
            } catch (AttachNotSupportedException e) {
                log.info("Attach not supported for {}", desc);
                continue;
            } catch (IOException ioe) {
                log.info("IO {}", desc);
                continue;
            }
            if (!vm.id().equals(pid)) {
                continue;
            }
            Properties props = vm.getAgentProperties();
            String connectorAddress =
                props.getProperty("com.sun.management.jmxremote.localConnectorAddress");
            if (connectorAddress == null) {
                continue;
            }
            JMXServiceURL url = new JMXServiceURL(connectorAddress);
            return url;
        }
        return null;
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
    public void resyncBroadcast() throws IOException, MBeanException, InstanceNotFoundException, ReflectionException, InterruptedException {


        Object syncBroadcast = mBeanServerConnection.invoke(PREPR, "syncBroadcast",
            new Object[]{"PREPR_VPRO_15979404"}, new String[]{
                String.class.getName()
            });
        log.info("{}", syncBroadcast);
    }

    @Test
    @Order(10)
    public void resyncDay() throws IOException, MBeanException, InstanceNotFoundException, ReflectionException, InterruptedException {

        Object syncDay = mBeanServerConnection.invoke(PREPR, "sync",
            new Object[]{"RAD3", "2020-02-15", null}, new String[] {
                String.class.getName(),
                String.class.getName(),
                String.class.getName()});
        log.info("{}", syncDay);

        // wait, may be do something with events.
        Thread.sleep(10000L);
    }


    @Test
    @Order(20)
    public void stillEnsureGroup() {
        MediaObject series= backend.getFull(SERIES);
        assertThat(CORRECT.test(series)).isTrue();
    }
}
