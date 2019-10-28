package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.*;

import com.google.common.collect.Sets;

import nl.vpro.poms.AbstractApiTest;
import nl.vpro.test.util.jackson2.Jackson2TestUtil;
import nl.vpro.util.IntegerVersion;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractSearchTest<T, S> extends AbstractApiTest {
    private Map<Pattern, Function<S, Boolean>> TESTERS = new HashMap<>();
    private static Map<String, AtomicInteger> USED = new HashMap<>();
    private static Set<String> AVAILABLE = new HashSet<>();
    private Map<Pattern, Supplier<Boolean>> ASSUMERS =  new HashMap<>();


    String name;
    T form;
    String profile;
    Function<S, Boolean> tester;
    private MediaType accept;


    void  addTester(IntegerVersion minVersion, String pattern, Consumer<S> consumer) {
        if (minVersion == null || apiVersionNumber.isNotBefore(minVersion)) {
            addTester(pattern, (s) -> {
                consumer.accept(s);
                return true;
            });
        }
    }

     void  addTester(String pattern, Consumer<S> consumer) {
         addTester(null, pattern, consumer);
     }

    private void addTester(String pattern, Function<S, Boolean> consumer) {
        Pattern p = Pattern.compile(pattern);
        TESTERS.put(p, consumer);
        AVAILABLE.add(p.pattern());
    }


    void addAssumer(String pattern, Supplier<Boolean> consumer) {
        ASSUMERS.put(Pattern.compile(pattern), consumer);
    }

    @BeforeAll
    public static void clean() {
        USED.clear();
        AVAILABLE.clear();
    }

    @BeforeEach
    public void setUp() {
        for (Map.Entry<Pattern, Supplier<Boolean>> e : ASSUMERS.entrySet()) {
            if (e.getKey().matcher(name).matches()) {
                assumeTrue(e.getValue().get(), "Skipping in " + this + " because of " + e);

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
        final boolean doLog = ! result.isEmpty();
        if (result.isEmpty()) {
            result.add((s) -> {
                log.debug("No predicate defined for " + name);
                return true;
            });
        }
        tester = s -> {
            boolean bool = true;
            for (Function<S, Boolean> tester1 : result) {
                if (doLog) {
                    log.info("USING  TESTER " + tester1 + " for " + name);
                }
                bool &= tester1.apply(s);

            }
            return bool;

        };
        clients.setAccept(accept);
        clients.setContentType(accept);
    }

    @AfterAll
    public static void shutdown() {
        Sets.SetView<String> difference = Sets.difference(AVAILABLE, USED.keySet());
        if (! difference.isEmpty()) {
            throw new RuntimeException("Not all testers were used: " + difference);
        }
        USED.entrySet().stream()
            .map((e) -> e.getKey() + " was used " + e.getValue().intValue() + " times")
            .forEach(log::info);

    }


    AbstractSearchTest(String name, T form, String profile, MediaType mediaType) {
        this.name = name;
        this.form = form;
        this.profile = profile;
        this.accept = mediaType;
    }


    private static Supplier<Boolean> minVersion(final IntegerVersion minVersion) {
        return new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return apiVersionNumber.isNotBefore(minVersion);
            }
            @Override
            public String toString() {
                return "" + apiVersionNumber + " < " +  minVersion;
            }
        };
    }
    static Supplier<Boolean> minVersion(int... parts) {
        return minVersion(IntegerVersion.of(parts));
    }



    <U> void test(String name, U object) throws Exception {
        Jackson2TestUtil.roundTrip(object);
      /*
        try (
                OutputStream out = getTempStream(name)) {
            Jackson2Mapper.getPrettyInstance().writeValue(out, object);
        }*/
    }

}
