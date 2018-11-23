package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.junit.Assume.assumeTrue;

/**
 * 2018-08-14
 * 5.9-SNAPSHOT @ dev :ok
 * 5.7.9 @ test: 403 permission denied (we moeten hiervoor een account hebben, anders kunnen we niet testen!)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AddFrameTest extends AbstractApiMediaBackendTest {


    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    private static final Duration offset = Duration.ofMinutes(10).plus(Duration.ofMinutes((int) (20f * Math.random())));

    @Test
    public void test01() {
        backend.getFrameCreatorRestService().createFrame(MID, offset, null, null, getClass().getResourceAsStream("/VPRO.png"));
        final Program[] update = new Program[1];

        waitUntil(ACCEPTABLE_DURATION,
            MID + " has image STILL with offset " + offset,
            () -> {
                update[0] = backend.getFullProgram(MID);
                return update[0] != null &&
                    update[0].getImages()
                        .stream()
                        .anyMatch(iu -> iu != null && iu.getOffset() != null && iu.getOffset().equals(offset) && iu.getType() == ImageType.STILL);
            });
    }


    @Test
    public void test98Cleanup() {
        ProgramUpdate update = backend.get(MID);
        assumeTrue(update != null);
        log.info("Removing images " + update.getImages());
        update.getImages().clear();
        backend.set(update);
    }

}