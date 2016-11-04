package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import nl.vpro.util.Env;
import nl.vpro.util.XTrustProvider;

/**
 * @author Michiel Meeuwissen

 */
@Slf4j
public class Config {

    private static Properties PROPERTIES = new Properties();
    private static File  CONFIG_FILE = new File(System.getProperty("user.home") + File.separator + "conf" + File.separator + "poms-functional-tests.properties");

    public enum Prefix {
        npoapi,
        backendapi,
        parkpost
    }

    static {

        try {

            log.info("Reading {} configuration from {}", env(), CONFIG_FILE);

            PROPERTIES.put(Prefix.backendapi.name() + ".url.localhost", "http://localhost:8080/rs/");
            PROPERTIES.put(Prefix.backendapi.name() + ".url.dev", "https://api-dev.poms.omroep.nl/");
            PROPERTIES.put(Prefix.backendapi.name() + ".url.test", "https://api-test.poms.omroep.nl/");
            PROPERTIES.put(Prefix.backendapi.name() + ".url.prod", "https://api.poms.omroep.nl/");

            PROPERTIES.put(Prefix.npoapi.name() + ".apiBaseUrl.localhost", "http://localhost:8070/v1/");
            PROPERTIES.put(Prefix.npoapi.name() + ".apiBaseUrl.dev", "https://rs-dev.poms.omroep.nl/v1/");
            PROPERTIES.put(Prefix.npoapi.name() + ".apiBaseUrl.test", "https://rs-test.poms.omroep.nl/v1/");
            PROPERTIES.put(Prefix.npoapi.name() + ".apiBaseUrl.prod", "https://rs.poms.omroep.nl/v1/");
            PROPERTIES.put(Prefix.npoapi.name() + ".apiBaseUrl.proda", "https://rs-a.poms.omroep.nl/v1/");
            PROPERTIES.put(Prefix.npoapi.name() + ".apiBaseUrl.prodb", "https://rs-b.poms.omroep.nl/v1/");


            PROPERTIES.load(new FileInputStream(CONFIG_FILE));

            XTrustProvider.install();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static Optional<String> configOption(Prefix pref, String prop) {
        String value = PROPERTIES.getProperty(pref + "." + prop + "." + env(), PROPERTIES.getProperty(pref + "." + prop));
        return Optional.ofNullable(value);
    }

    public static String requiredOption(Prefix pref, String prop) {
        return configOption(pref, prop).orElseThrow(notSet(prop));
    }


    public static String url(Prefix pref, String prop, String path) {
        String base  = requiredOption(pref, prop);
        if (! base.endsWith("/")) {
            base = base + "/";
        }
        return base + path;

    }

    public static Map<String, String> getProperties(Prefix prefix) {
        return Maps.fromProperties(PROPERTIES).entrySet()
            .stream()
            .filter(e -> prefix == null || e.getKey().startsWith(prefix.name()))
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().substring(prefix.name().length() + 1), e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Supplier<RuntimeException> notSet(String prop) {
        return () -> new RuntimeException(prop + " is not set in " + CONFIG_FILE);
    }

    private static Env env() {
        String pref = System.getProperty("env");
        if (pref == null) {
            return Env.valueOf(PROPERTIES.getProperty("env", "test").toUpperCase());
        } else {
            return Env.valueOf(pref.toUpperCase());
        }
    }

}
