package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Michiel Meeuwissen

 */
@Slf4j
public class Config {

    private static Properties PROPERTIES = new Properties();
    static File FILE = new File(System.getProperty("user.home") + File.separator + "conf" + File.separator + "poms-functional-tests.properties");
    static {

        try {

            log.info("Reading {} configuration from {}", envPrefix(), FILE);

            PROPERTIES.put("localhost.backendapi.url", "http://localhost:8071/rs/");
            PROPERTIES.put("dev.backendapi.url", "https://api-dev.poms.omroep.nl/");
            PROPERTIES.put("test.backendapi.url", "https://api-test.poms.omroep.nl/");
            PROPERTIES.put("prod.backendapi.url", "https://api.poms.omroep.nl/");

            PROPERTIES.put("localhost.apiBaseUrl", "http://localhost:8070/v1/");
            PROPERTIES.put("dev.apiBaseUrl", "https://rs-dev.poms.omroep.nl/v1/");
            PROPERTIES.put("test.apiBaseUrl", "https://rs-test.poms.omroep.nl/v1/");
            PROPERTIES.put("prod.apiBaseUrl", "https://rs.poms.omroep.nl/v1/");

            PROPERTIES.load(new FileInputStream(FILE));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static Optional<String> configOption(String prop) {
        String value = PROPERTIES.getProperty(envPrefix() + "." + prop, PROPERTIES.getProperty(prop));
        return Optional.ofNullable(value);
    }

    public static String requiredOption(String prop) {
        return configOption(prop).orElseThrow(notSet(prop));
    }

    public static String url(String prop, String path) {
        String base  = requiredOption(prop);
        if (! base.endsWith("/")) {
            base = base + "/";
        }
        return base + path;

    }

    public static Properties getProperties() {
        return PROPERTIES;
    }

    public static Supplier<RuntimeException> notSet(String prop) {
        String p = envPrefix();
        String post = StringUtils.isBlank(p) ? "" : " (or " + p + "." + prop + ")";
        return () -> new RuntimeException(prop + post  + " is not set in " + FILE);
    }

    private static String envPrefix() {
        String pref = System.getProperty("env");
        if (pref == null) {
            return PROPERTIES.getProperty("env", "dev");
        } else {
            return pref;
        }
    }

}
