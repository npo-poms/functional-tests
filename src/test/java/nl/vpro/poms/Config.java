package nl.vpro.poms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michiel Meeuwissen

 */
public class Config {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    private static Properties PROPERTIES = new Properties();
    private static File FILE = new File(System.getProperty("user.home") + File.separator + "conf" + File.separator + "poms-functional-tests.properties");
    static {

        try {

            LOG.info("Reading configuration from {}", FILE);
            PROPERTIES.put("localhost.backendapi.url", "http://localhost:8071/rs/");
            PROPERTIES.put("dev.backendapi.url", "https://api-dev.poms.omroep.nl");
            PROPERTIES.put("test.backendapi.url", "https://api-test.poms.omroep.nl");
            PROPERTIES.put("prod.backendapi.url", "https://api.poms.omroep.nl");

            PROPERTIES.load(new FileInputStream(FILE));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static Optional<String> configOption(String prop) {
        String value = PROPERTIES.getProperty(pref() + "." + prop, PROPERTIES.getProperty(prop));
        return Optional.ofNullable(value);
    }

    public static String requiredOption(String prop) {
        return configOption(prop).orElseThrow(notSet(prop));
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }

    public static Supplier<RuntimeException> notSet(String prop) {
        String p = pref();
        String post = StringUtils.isBlank(p) ? "" : " (or " + p + "." + prop + ")";
        return () -> new RuntimeException(prop + post  + " is not set in " + FILE);
    }

    private static String pref() {
        return PROPERTIES.getProperty("pref", "dev");
    }

}
