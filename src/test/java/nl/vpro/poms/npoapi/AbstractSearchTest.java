package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import nl.vpro.poms.AbstractApiTest;
import nl.vpro.test.util.jackson2.Jackson2TestUtil;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractSearchTest<T, S> extends AbstractApiTest {
    private static final boolean writeTempFiles = false;
    protected Map<String, Consumer<S>> TESTERS = new HashMap<>();

    protected String name;
    protected T form;
    protected String profile;


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
