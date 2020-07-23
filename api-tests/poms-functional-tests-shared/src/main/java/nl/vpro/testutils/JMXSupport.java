package nl.vpro.testutils;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.security.Permission;
import java.util.*;
import java.util.function.Consumer;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.*;

import org.junit.jupiter.api.AfterAll;

import com.sun.tools.attach.*;

import nl.vpro.util.CommandExecutor;
import nl.vpro.util.CommandExecutorImpl;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
@Log4j2
public class JMXSupport {

    @SneakyThrows
    public static ObjectName getObjectName(String objectName) {
         return new ObjectName(objectName);
    }


    @SneakyThrows
    public static JMXContainer getMBeanServerConnection(Map<String, String> properties) {
        String ssh = properties.get("ssh-host");
        String host = properties.get("jmx-host");
        final JMXContainer container = new JMXContainer();

        CommandExecutorImpl tunnel = CommandExecutorImpl.builder()
            .executablesPaths("/usr/bin/ssh")
            .build();

        Consumer<Integer> ready = (i) -> {
            log.info("Finished {}", i);
        };
        Consumer<Process> consumer = (p) -> {
            synchronized (container) {
                container.pid = p.pid();
                container.notifyAll();
            }
        };
        CommandExecutor.Parameters.builder()
            .args("-L", "8686:" + host +":8686", "-L", "8687:" + host + ":8687", ssh)
            .consumer(consumer)
            .submit(ready, tunnel);

        log.info("Waiting for pid");
        synchronized (container) {
            while (container.pid == 0) {
                container.wait();
            }
        }
        Thread.sleep(1000);

        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {

            }
        });
        String jmxUrl = properties.get("jmx-url");
        JMXServiceURL url = jmxUrl.startsWith("localhost:") ? localhost(jmxUrl.substring("localhost:".length())) : new JMXServiceURL(jmxUrl);
        Map<String, Object> env = new HashMap<>();
        String[] credentials = {
            properties.get("jmx-username"),
            properties.get("jmx-password")
        };
        env.put(JMXConnector.CREDENTIALS, credentials);
        container.connector = JMXConnectorFactory.connect(url, env);
        container.connector.connect();

        container.connection = container.connector.getMBeanServerConnection();
        return container;
    }

    @AfterAll
    public static void killTunnel(long pid) {
        if (pid > 0) {
            log.info("Killing {}", pid);
            CommandExecutorImpl.builder()
                .executablesPaths("/bin/kill")
                .build()
                .execute(String.valueOf(pid));
        }

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

    public static class JMXContainer {
        JMXConnector connector;
        public MBeanServerConnection connection;
        long pid;

        @SneakyThrows
        public void shutdown() {
            try {
                connector.close();
            } catch (Exception e){
                log.warn(e.getMessage());
            }
            try {
                killTunnel(pid);
             } catch (Exception e){
                log.warn(e.getMessage());
            }

        }
    }
}
