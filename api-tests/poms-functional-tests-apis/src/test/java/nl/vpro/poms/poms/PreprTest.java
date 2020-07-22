package nl.vpro.poms.poms;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.*;

import org.junit.jupiter.api.Test;

import static nl.vpro.poms.AbstractApiTest.CONFIG;


/**
 * @author Michiel Meeuwissen
 * @since ...
 */
@Log4j2
public class PreprTest {

    @Test
    public void test() throws IOException {
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
    }
}
