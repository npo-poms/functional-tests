package nl.vpro.testutils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */

@Slf4j
public class Utils {

    private final static Duration WAIT = Duration.ofSeconds(15);

    public static final ThreadLocal<Runnable> CLEAR_CACHES = ThreadLocal.withInitial((Supplier<Runnable>) () -> () -> {});

    private static void waitUntil(Duration acceptable, Callable<Boolean> r)  {
        CLEAR_CACHES.get().run();
        Instant start = Instant.now();
        try {
            Thread.sleep(Duration.ofSeconds(1).toMillis());
            while (true) {
                boolean result = false;
                try {
                    result = r.call();
                } catch (Throwable t) {
                    log.warn(t.getMessage(), t);
                }
                if (result) {
                    log.info("{} evaluated true", r);
                    assertThat(result).isTrue();
                    return;
                }
                Duration duration = Duration.between(start, Instant.now());
                if (duration.compareTo(acceptable) > 0) {
                    assertThat(result)
                        .withFailMessage("%s didn't evaluate to true after %s in less than %s", r, duration, acceptable)
                        .isTrue();
                }
                log.info("{} didn't evaluate to true yet after {} (< {}). Waiting another {}", r, duration, acceptable, WAIT);
                Thread.sleep(WAIT.toMillis());
            }
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void waitUntil(Duration acceptable, Supplier<String> callableToDescription, final Callable<Boolean> r)  {
        log.info("Waiting until " + callableToDescription.get());
        waitUntil(acceptable, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    CLEAR_CACHES.get().run();
                    return r.call();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw e;
                }
            }

            public String toString() {
                return "(" + callableToDescription.get() + ")";
            }
        });

    }


    public static void waitUntil(Duration acceptable, String callableToDescription, final Callable<Boolean> r)  {
        waitUntil(acceptable, () -> callableToDescription, r);
    }

    public static <T> T waitUntilNotNull(Duration acceptable, Supplier<T> r) {
        return waitUntilNotNull(acceptable, r + " != null", r);
    }

    public static <T> T waitUntilNotNull(Duration acceptable, String description, Supplier<T> r) {
        return waitUntil(acceptable, description, r, (o) -> true);
    }

    public static <T> T waitUntil(
        Duration acceptable,
        String predicateDescription,
        Supplier<T> r,
        Predicate<T> predicate) {
        return waitUntil(acceptable, predicateDescription, r, predicate, (result) -> predicateDescription + ": " + result + " doesn't match");
    }


    @SafeVarargs
    public static <T> T waitUntils(
        Duration acceptable,
        String predicateDescription,
        Supplier<T> r,
        Predicate<T> ... predicate) {
        Predicate<T> and = Arrays.stream(predicate).reduce(x -> true, Predicate::and);
        return waitUntil(acceptable, predicateDescription, r, and, (result) -> predicateDescription + ": " + result + " doesn't match");
    }
    /**
     * Call supplier until its result evaluates true according to given predicate or the acceptable duration elapses.
     */
    public static <T> T waitUntil(
        Duration acceptable,
        String predicateDescription,
        Supplier<T> r,
        Predicate<T> predicate,
        Function<T, String> failureDescription) {
        return waitUntil(acceptable, r, Check.<T>builder()
            .predicate(predicate)
            .failureDescription(failureDescription)
            .description(predicateDescription)
            .build());
    }

     @SafeVarargs
     public static <T> T waitUntil(
        Duration acceptable,
        Supplier<T> r,
        Check<T>... tests) {
         final T[] result = (T[]) new Object[1];
         final String[] predicateDescription = new String[1];
         predicateDescription[0] = Arrays.stream(tests).map(t -> t.description).collect(Collectors.joining(" and "));
         final String originalDescription = predicateDescription[0];
         waitUntil(acceptable, () -> predicateDescription[0], new Callable<Boolean>() {
            @Override
            public Boolean call() {
                CLEAR_CACHES.get().run();
                result[0] = r.get();
                if (result[0] == null) {
                    return false;
                }
                List<Check<T>> failing = Arrays.stream(tests).filter(t -> !t.predicate.test(result[0])).collect(Collectors.toList());
                if (failing.isEmpty()) {
                    predicateDescription[0] = originalDescription;
                    return true;
                } else {
                    predicateDescription[0] = failing.stream().map(t -> t.description).collect(Collectors.joining(" and "));
                    return false;
                }

            }

            @Override
            public String toString() {
                return Arrays.asList(tests)+ " supplies: " + r + " current value: " + result[0];
            }
        });
        assertThat(result[0]).withFailMessage(predicateDescription[0] + ":" + r + "supplied null").isNotNull();
        for (Check<T> t : tests) {
            assertThat(t.predicate.test(result[0])).withFailMessage(t.failureDescription.apply(result[0])).isTrue();
        }
        return result[0];
    }


    @Getter
    public static class Check<T> {
        private final String description;
        private final Predicate<T> predicate;
        private final Function<T, String> failureDescription;
        private final Supplier<T> supplier;

        @lombok.Builder
        private Check(String description, Predicate<T> predicate, Function<T, String> failureDescription, Supplier<T> supplier) {
            this.description = description;
            this.predicate = predicate;
            this.supplier = supplier;
            this.failureDescription = failureDescription == null ? (t) -> description + ":" + t + " doesn't match" : failureDescription;
        }
    }
}

