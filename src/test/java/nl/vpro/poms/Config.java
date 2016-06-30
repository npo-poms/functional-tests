package nl.vpro.poms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michiel Meeuwissen

 */
public class Config {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    static Properties PROPERTIES = new Properties();
    static {

        try {
            PROPERTIES.load(new FileInputStream(new File(System.getProperty("user.home") + File.separator + "conf" + File.separator + "poms-functional-tests.properties")));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static Optional<String> configOption(String prop) {
        return Optional.ofNullable(getProperties().getProperty(prop));
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }
}
