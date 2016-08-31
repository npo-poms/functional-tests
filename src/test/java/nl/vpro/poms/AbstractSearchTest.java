package nl.vpro.poms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.BeforeClass;

import nl.vpro.api.client.resteasy.NpoApiClients;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class AbstractSearchTest<T, S> {

    Map<String, Consumer<S>> TESTERS = new HashMap<>();



    String name;
    T form;
    String profile;

    static NpoApiClients clients;

    @BeforeClass
    public static void initialize() throws IOException {
        clients = NpoApiClients.configured(Config.FILE.getAbsolutePath()).build();
    }


    public AbstractSearchTest(String name, T form, String profile) {
        this.name = name;
        this.form = form;
        this.profile = profile;
    }
}
