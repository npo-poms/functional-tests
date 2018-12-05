package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.OwnerType;
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

    private static long jpegSizeOfImage = 13991L;

    @BeforeClass
    public static void init() {
        log.info("Offset for this test {}", offset);
    }


    @Test
    public void test01() {

        backend.getFrameCreatorRestService().createFrame(MID, offset, null, null, getClass().getResourceAsStream("/VPRO.png"));
        final ProgramUpdate[] update = new ProgramUpdate[1];

        waitUntil(ACCEPTABLE_DURATION,
            MID + " has image STILL with offset " + offset,
            () -> {
                update[0] = backend_authority.get(MID);
                return update[0] != null &&
                    update[0].getImages()
                        .stream()
                        .anyMatch(iu ->
                            iu != null &&
                                iu.getOffset() != null &&
                                iu.getOffset().equals(offset) &&
                                iu.getType() == ImageType.STILL &&
                                imageUtil.getSize(iu).get() != jpegSizeOfImage
                        );
            });
    }



    @Test
    public void test01Overwrite() {
        backend.getFrameCreatorRestService().createFrame(MID, offset, null, null, getClass().getResourceAsStream("/VPRO1970's.png"));
        waitUntil(ACCEPTABLE_DURATION,
            MID + " has image STILL with offset " + offset + " and size " + jpegSizeOfImage,
            () -> {
                ProgramUpdate p  = backend_authority.get(MID);
                return p != null &&
                    p.getImages()
                        .stream()
                        .anyMatch(iu ->
                            iu != null &&
                                iu.getOffset() != null &&
                                iu.getOffset().equals(offset) &&
                                iu.getType() == ImageType.STILL &&
                                imageUtil.getSize(iu).get()  == jpegSizeOfImage
                        );
            });
    }


    @Test
    public void test98Cleanup() {
        ProgramUpdate update = backend_authority.get(MID);
        assumeTrue(update != null);
        log.info("Removing images " + update.getImages());
        update.getImages().clear();
        backend_authority.set(update);

    }


    @Test
    public void test99CheckCleanup() {
         waitUntil(ACCEPTABLE_DURATION,
            MID + " has no stills",
            () -> {
                Program p = backend.getFullProgram(MID);
                log.info("Found images for {}: {}", MID, p.getImages());
                return
                    p.getImages()
                        .stream()
                        .filter(iu -> iu != null && iu.getOwner() == OwnerType.AUTHORITY && iu.getType() == ImageType.STILL)
                        .collect(Collectors.toList()).size() == 0;
            });


    }

}
