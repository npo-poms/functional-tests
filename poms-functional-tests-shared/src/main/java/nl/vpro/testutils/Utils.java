package nl.vpro.testutils;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */

@Slf4j
public class Utils {

    private final static Duration WAIT = Duration.ofSeconds(15);

    public static final ThreadLocal<Runnable> clearCaches = ThreadLocal.withInitial((Supplier<Runnable>) () -> () -> {});

    private static void waitUntil(Duration acceptable, Callable<Boolean> r)  {
        clearCaches.get().run();
        Instant start = Instant.now();
        try {
            Thread.sleep(Duration.ofSeconds(1).toMillis());
            while (true) {
                boolean result = r.call();
                if (result) {
                    log.info("{} evaluated true", r);
                    assertThat(result).isTrue();
                    return;
                }
                Duration duration = Duration.between(start, Instant.now());
                if (duration.compareTo(acceptable) > 0) {
                    assertThat(result).withFailMessage("{} didn't evaluate to true after {} in less than {}", r, duration, acceptable).isFalse();
                }
                log.info("{} didn't evaluate to true yet after {}. Waiting another {}", r, duration, WAIT);
                Thread.sleep(WAIT.toMillis());
            }
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void waitUntil(Duration acceptable, String callableToDescription, final Callable<Boolean> r)  {
        log.info("Waiting until " + callableToDescription);
        clearCaches.get().run();
        waitUntil(acceptable, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return r.call();
            }

            public String toString() {
                return "(" + callableToDescription + ")";
            }
        });

    }

    public static <T> T waitUntilNotNull(Duration acceptable, Supplier<T> r) {
        return waitUntil(acceptable, r + " != null", r, (o) -> true);
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
    /**
     * Call supplier until its result evaluates true according to given predicate or the acceptable duration elapses.
     */
    public static <T> T waitUntil(
        Duration acceptable,
        String predicateDescription,
        Supplier<T> r,
        Predicate<T> predicate,
        Function<T, String> failureDescription) {
        final T[] result = (T[]) new Object[1];
        waitUntil(acceptable, predicateDescription, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                result[0] = r.get();
                return result[0] != null && predicate.test(result[0]);
            }

            @Override
            public String toString() {
                return predicate + " supplies: " + r + " current value: " + result[0];
            }
        });
        assertThat(result[0]).withFailMessage(predicateDescription + ":" + r + "supplied null").isNotNull();
        assertThat(predicate.test(result[0])).withFailMessage(failureDescription.apply(result[0])).isTrue();
        return result[0];
    }
}

