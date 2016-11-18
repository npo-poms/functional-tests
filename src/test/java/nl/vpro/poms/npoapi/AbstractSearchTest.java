package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.junit.Before;

import nl.vpro.poms.AbstractApiTest;
import nl.vpro.test.util.jackson2.Jackson2TestUtil;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractSearchTest<T, S> extends AbstractApiTest {
    private static final boolean writeTempFiles = false;
    protected Map<Pattern, Function<S, Boolean>> TESTERS = new HashMap<>();

    protected String name;
    protected T form;
    protected String profile;
    protected Function<S, Boolean> tester;


    protected void  addTester(String pattern, Consumer<S> consumer) {
        TESTERS.put(Pattern.compile(pattern), (s) -> {
            consumer.accept(s);
            return true;
        });
    }


    @Before
    public void getTester() {
        final List<Function<S, Boolean>> result = new ArrayList<>();
        for (Map.Entry<Pattern, Function<S, Boolean>> e : TESTERS.entrySet()) {
            if (e.getKey().matcher(name).matches()) {
                result.add(e.getValue());
            }
        }
        if (result.isEmpty()) {
            result.add((s) -> {
                System.out.println("No predicate defined for " + name);
                return true;
            });
        }
        tester = s -> {
            boolean bool = true;
            for (Function<S, Boolean> tester1 : result) {
                System.out.println("USING  TESTER " + tester1 + " for " + name);
                bool &= tester1.apply(s);
            }
            return bool;

        };
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
