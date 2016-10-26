package nl.vpro.poms;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class Utils {


    protected static void waitUntil(Duration acceptable, Callable<Boolean> r) throws Exception {
        Instant start = Instant.now();
        Thread.sleep(Duration.ofSeconds(10).toMillis());
        while (true) {

            if (r.call()) {
                break;
            }
            if (Duration.between(start, Instant.now()).compareTo(acceptable) > 0) {
                break;
            }
            Thread.sleep(Duration.ofSeconds(30).toMillis());
        }
    }
}
