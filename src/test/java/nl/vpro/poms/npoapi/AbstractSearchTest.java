package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.Before;

import com.google.common.collect.Sets;

import nl.vpro.poms.AbstractApiTest;
import nl.vpro.test.util.jackson2.Jackson2TestUtil;

import static org.junit.Assume.assumeTrue;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractSearchTest<T, S> extends AbstractApiTest {
    private static final boolean writeTempFiles = false;
    protected Map<Pattern, Function<S, Boolean>> TESTERS = new HashMap<>();
    protected static Map<String, AtomicInteger> USED = new HashMap();
    protected static Set<String> AVAILABLE = new HashSet<>();
    protected Map<Pattern, Supplier<Boolean>> ASSUMERS =  new HashMap<>();


    protected String name;
    protected T form;
    protected String profile;
    protected Function<S, Boolean> tester;
    protected MediaType accept;


    protected void  addTester(String pattern, Consumer<S> consumer) {
        addTester(pattern, (s) -> {
            consumer.accept(s);
            return true;
        });
    }

    protected void addTester(String pattern, Function<S, Boolean> consumer) {
        Pattern p = Pattern.compile(pattern);
        TESTERS.put(p, consumer);
        AVAILABLE.add(p.pattern());
    }


    protected void addAssumer(String pattern, Supplier<Boolean> consumer) {
        ASSUMERS.put(Pattern.compile(pattern), consumer);
    }


    @Before
    public void setUp() {
        for (Map.Entry<Pattern, Supplier<Boolean>> e : ASSUMERS.entrySet()) {
            if (e.getKey().matcher(name).matches()) {
                assumeTrue("Skipping in " + this + " because of " + e, e.getValue().get());
            }
        }


        final List<Function<S, Boolean>> result = new ArrayList<>();
        for (Map.Entry<Pattern, Function<S, Boolean>> e : TESTERS.entrySet()) {
            if (e.getKey().matcher(name).matches()) {
                result.add(e.getValue());
                AtomicInteger atomicInteger = USED.computeIfAbsent(e.getKey().pattern(), (k) -> new AtomicInteger(0));
                atomicInteger.incrementAndGet();
            }
        }
        final boolean log = ! result.isEmpty();
        if (result.isEmpty()) {
            result.add((s) -> {
                System.out.println("No predicate defined for " + name);
                return true;
            });
        }
        tester = s -> {
            boolean bool = true;
            for (Function<S, Boolean> tester1 : result) {
                if (log) {
                    System.out.println("USING  TESTER " + tester1 + " for " + name);
                }
                bool &= tester1.apply(s);

            }
            return bool;

        };
        clients.setAccept(accept);
        clients.setContentType(accept);
    }

    @AfterClass
    public static void shutdown() {
        Sets.SetView<String> difference = Sets.difference(AVAILABLE, USED.keySet());
        if (! difference.isEmpty()) {
            throw new RuntimeException("Not all testers were used: " + difference);
        }
        log.info(
            USED.entrySet().stream()
                .map((e) -> e.getKey() + " was used " + e.getValue().intValue() + " times")
                .collect(Collectors.joining("\n"))
        );
    }


    public AbstractSearchTest(String name, T form, String profile, MediaType mediaType) {
        this.name = name;
        this.form = form;
        this.profile = profile;
        this.accept = mediaType;
    }


    protected static Supplier<Boolean> minVersion(final double minVersion) {
        return new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return apiVersionNumber > minVersion;
            }
            @Override
            public String toString() {
                return "" + apiVersionNumber + " > " +  minVersion;
            }
        };
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
