package nl.vpro.junit.extensions;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import org.junit.jupiter.api.extension.*;

import nl.vpro.api.client.utils.Config;
import nl.vpro.testutils.*;

import static nl.vpro.testutils.JMXSupport.getMBeanServerConnection;

/**
 * Adds support for test arguments of the type {@link JMXContainer}
 *
 * The parameter must also be annotated with {@link For} to indicate for which environment you would like JMX support to be injected.
 *
 * @author Michiel Meeuwissen
 */
public class JMXContainers implements ParameterResolver, AfterAllCallback {

    static Map<Config.Prefix, JMXContainer> map = new ConcurrentHashMap<>();

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(JMXContainer.class) || parameterContext.getParameter().getType().isAssignableFrom(MBeanServerConnection.class) || parameterContext.getParameter().getType().isAssignableFrom(JMXConnector.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        For env = parameterContext.findAnnotation(For.class).orElseThrow(() -> new IllegalArgumentException("No env specified"));
        JMXContainer result = map.computeIfAbsent(env.value(), (e) -> getMBeanServerConnection(Utils.CONFIG.getProperties(e)));
        if (parameterContext.getParameter().getType().isAssignableFrom(MBeanServerConnection.class)) {
            return result.getConnection();
        }
        if (parameterContext.getParameter().getType().isAssignableFrom(JMXConnector.class)) {
            return result.getConnector();
        }

        return result;
    }

    @Override
    public void afterAll(ExtensionContext context)  {
        map.values().forEach(JMXContainer::shutdown);
    }

}
