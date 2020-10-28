package nl.vpro.testutils;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.*;
import java.util.stream.Collectors;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import nl.vpro.api.client.utils.Config;
import nl.vpro.util.TextUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */

@Log4j2
public class Utils {

    public static final Config CONFIG = new Config("npo-functional-tests.properties");
    private final static Duration WAIT = Duration.ofSeconds(15);

    public static final ThreadLocal<Runnable> CLEAR_CACHES = ThreadLocal.withInitial((Supplier<Runnable>) () -> () -> {});

    private static void wait(Duration duration) throws InterruptedException {
        synchronized (ChangesNotifier.WAIT_NOTIFIABLE) {
            ChangesNotifier.WAIT_NOTIFIABLE.wait(duration.toMillis());
        }
    }

    private static void waitUntil(Duration acceptable, Callable<Boolean> r)  {
        CLEAR_CACHES.get().run();
        Instant start = Instant.now();
        try {
            wait(Duration.ofSeconds(1));
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
                    // this would fail, intentionally, because we are too late
                    //noinspection ConstantConditions
                    assertThat(result)
                        .withFailMessage("%s didn't evaluate to true after %s in less than %s", r, duration, acceptable)
                        .isTrue();
                }
                log.info("{} didn't evaluate to true yet after {} (< {}). Waiting another {}", r, duration, acceptable, WAIT);
                wait(WAIT);
            }
        } catch (RuntimeException rte) {
            throw rte;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Wait for a certain condition to become true. Waiting between checks happens on {@link ChangesNotifier#WAIT_NOTIFIABLE}. This may be notified if you suspect something may have happend already.
     *
     * @param acceptable The maximal acceptable duration for the condition to become true. If it takes longer, the test will fail.
     * @param conditionDescription  Suppliers a description for the condition. It may change in time.
     * @param condition The condition itself.
     */
    public static void waitUntil(Duration acceptable, Supplier<String> conditionDescription, final Callable<Boolean> condition)  {
        log.info("Waiting until " + conditionDescription.get());
        waitUntil(acceptable, new Callable<>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    CLEAR_CACHES.get().run();
                    return condition.call();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw e;
                }
            }

            public String toString() {
                return "(" + conditionDescription.get() + ")";
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

    /**
     * @param resultSupplier The code to produce a result. This will be repeated until it doesn't return <code>null</code> or until the acceptable duration expires.
     */
     @SuppressWarnings("unchecked")
     @SafeVarargs
     public static <T> T waitUntil(
        Duration acceptable,
        Supplier<T> resultSupplier,
        Check<T>... tests) {
         final T[] result = (T[]) new Object[1];
         final String[] predicateDescription = new String[1];
         predicateDescription[0] = Arrays.stream(tests).map(t -> t.description).collect(Collectors.joining(" AND "));
         waitUntil(acceptable, () -> predicateDescription[0], new Callable<>() {
             @Override
             public Boolean call() {
                 CLEAR_CACHES.get().run();
                 result[0] = resultSupplier.get();
                 if (result[0] == null) {
                     return false;
                 }
                 boolean success = true;
                 StringBuilder description = new StringBuilder();
                 for (Check<T> t : tests) {
                     boolean test;
                     Exception exception = null;
                     try {
                         test = t.predicate.test(result[0]);
                     } catch (Exception e) {
                         test = false;
                         exception = e;
                     }
                     success &= test;
                     if (description.length() > 0) {
                         description.append(" AND ");
                     }
                     description.append(test ? TextUtil.strikeThrough(t.description) : t.description);
                     if (! test) {
                         t.getFailureDescription().ifPresent(f -> description.append(" (").append(f.apply(result[0])).append(")"));
                     }
                     if (exception != null) {
                         description.append(" (")
                             //.append(exception.getClass().getSimpleName()).append(' ').append(exception.getMessage())
                             .append("!") // exception occured
                             .append(')');
                     }


                 }
                 predicateDescription[0] = description.toString();
                 return success;
             }

             @Override
             public String toString() {
                 return Arrays.asList(tests) + " supplies: " + resultSupplier + " current value: " + result[0];
             }
         });
        assertThat(result[0]).withFailMessage(predicateDescription[0] + ":" + resultSupplier + "supplied null").isNotNull();
        for (Check<T> t : tests) {
            Function<T, String> failDescription = t.getFailureDescription().orElse((c)->  t.description + ":" + t + " doesn't match");
            assertThat(t.predicate.test(result[0]))
                .withFailMessage(failDescription.apply(result[0]))
                .isTrue();
        }
        return result[0];
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> T waitUntil(
        Duration acceptable,
        Supplier<T> resultSupplier,
        Check.Builder<T>... tests) {
          Check<T>[] args = Arrays.stream(tests).map(Check.Builder::build).toArray(Check[]::new);
         return waitUntil(acceptable, resultSupplier, args);
    }

    public static <T> T waitUntil(
        Duration acceptable,
        Supplier<T> resultSupplier) {
        return waitUntil(acceptable, resultSupplier, Check.<T>builder()
            .description(resultSupplier + " is not null")
            .predicate(Objects::nonNull)
            .build());
    }



    @Getter
    public static class Check<T> {
        private final String description;
        private final Predicate<T> predicate;
        private final Function<T, String> failureDescription;
        private final Supplier<T> supplier;


        @lombok.Builder(builderClassName = "Builder")
        private Check(String _description, Predicate<T> predicate, Function<T, String> failureDescription, Supplier<T> supplier) {
            this.description = _description;
            this.predicate = predicate;
            this.supplier = supplier;
            this.failureDescription =  failureDescription;
        }

        public Optional<Function<T, String>> getFailureDescription() {
            return Optional.ofNullable(failureDescription);
        }

        public static <T> Check<T> notNull(String what) {
            return Check.<T>builder()
                .predicate(Objects::nonNull)
                .description(what + " is not null")
                .build();
        }

        public static <T> Check.Builder<T> description(String description, Object... args) {
            return Check
                .<T>builder()
                .description(description, args);
        }

        public static class Builder<T> {
            private Builder<T> _description(String _description) {
                this._description = _description;
                return this;
            }

            /**
             * @param params slf4j style parameters ("{}")
             */
            public Builder<T> description(String description, Object... params) {
                FormattingTuple ft = MessageFormatter.arrayFormat(description, params);
                return _description(ft.getMessage());
            }

        }
    }

}

