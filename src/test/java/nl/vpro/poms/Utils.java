package nl.vpro.poms;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class Utils {


    public static boolean waitUntil(Duration acceptable, Callable<Boolean> r) throws Exception {
        Instant start = Instant.now();
        Thread.sleep(Duration.ofSeconds(10).toMillis());
        while (true) {

            if (r.call()) {
                return true;
            }
            if (Duration.between(start, Instant.now()).compareTo(acceptable) > 0) {
                return false;
            }
            Thread.sleep(Duration.ofSeconds(30).toMillis());
        }
    }
}
