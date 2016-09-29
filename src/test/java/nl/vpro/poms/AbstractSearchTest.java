package nl.vpro.poms;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.BeforeClass;

import nl.vpro.api.client.resteasy.NpoApiClients;
import nl.vpro.jackson2.Jackson2Mapper;
import nl.vpro.test.util.jackson2.Jackson2TestUtil;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class AbstractSearchTest<T, S> {
    private static final boolean writeTempFiles = false;
    Map<String, Consumer<S>> TESTERS = new HashMap<>();

    String name;
    T form;
    String profile;

    static NpoApiClients clients;

    @BeforeClass
    public static void initialize() throws IOException {
        clients = NpoApiClients.configured(Config.FILE.getAbsolutePath()).build();
        clients.setTrustAll(true);
    }


    public AbstractSearchTest(String name, T form, String profile) {
        this.name = name;
        this.form = form;
        this.profile = profile;
    }


    protected OutputStream getTempStream(String name) throws IOException {
        if (writeTempFiles) {
            Path tempFile = Files.createTempFile(name.replaceAll("/", "_"), ".json");
            return Files.newOutputStream(tempFile);
        } else {
            return System.out;
        }
    }
    
    protected <U> void test(String name, U object) throws Exception {
        Jackson2TestUtil.roundTrip(object);
      /*  
        try (
                OutputStream out = getTempStream(name)) {
            Jackson2Mapper.getPrettyInstance().writeValue(out, object);
        }*/
    }
    
}
