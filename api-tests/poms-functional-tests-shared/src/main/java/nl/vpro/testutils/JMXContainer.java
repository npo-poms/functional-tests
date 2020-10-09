package nl.vpro.testutils;

import lombok.*;
import lombok.extern.log4j.Log4j2;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

/**
 * Wrapper for some objects needed when maintaing JMX connection
 * @author Michiel Meeuwissen
 */
@Log4j2
public class JMXContainer {

    @Getter
    JMXConnector connector;
    long pid;

    @Getter
    MBeanServerConnection connection;


    @SneakyThrows
    public void shutdown() {
        try {
            connector.close();
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        try {
            JMXSupport.killTunnel(pid);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

    }
}
