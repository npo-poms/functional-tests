package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.vpro.domain.api.*;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.util.CountedIterator;

/**
 * @author Michiel Meeuwissen
 */
@Log4j2
@Disabled("This is not an actual test yet, it will simply timeout")
public class ApiLogChanges extends AbstractApiTest {



    @Test
    public void logChanges() throws InterruptedException {
        Instant start = Instant.now().minus(Duration.ofMinutes(10));
        String mid = null;
        while(true) {
            log.debug("Calling for {}/{}", start, mid);
            try (CountedIterator<MediaChange> changes = mediaUtil.changes(null, false, start, mid, Order.ASC, null, Deletes.ID_ONLY)) {
                while (changes.hasNext()) {
                    MediaChange change = changes.next();
                    if (! change.isTail()) {
                        log.info("{}", change);
                    } else {
                        log.debug("TAIL: {}", change.getPublishDate());
                    }
                    start = change.getPublishDate();
                    mid = change.getMid();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            //log.info("sleeping");
            Thread.sleep(1000L);

        }

    }

}

