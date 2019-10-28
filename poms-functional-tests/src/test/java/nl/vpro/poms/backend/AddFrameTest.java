package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.*;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static nl.vpro.testutils.Utils.waitUntil;

/**
 * 2018-08-14
 * 5.9-SNAPSHOT @ dev :ok
 * 2019-05-06
 * 5.11-SNAPSHOT @ dev :ok
 *
 * @ test: 403 permission denied (we moeten hiervoor een account hebben, anders kunnen we niet testen!)
 * 5.7.9 @ test: 403 permission denied (we moeten hiervoor een account hebben, anders kunnen we niet testen!)
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Slf4j
class AddFrameTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    private static final Duration offset = Duration.ofMinutes(10).plus(Duration.ofMinutes((int) (20f * Math.random())));

    //private static long jpegSizeOfImage = 13991L;
    private static long originalSizeOfImage = 2621;

    @BeforeAll
    static void init() {
        log.info("Offset for this test {}", offset);
    }


    @Test
    void test01() throws UnsupportedEncodingException {

        Program fullProgram = backend.getFullProgram(MID);
        if (fullProgram.getImage(ImageType.PICTURE) == null) {
            log.info("No image with type PICTURE yet present");
            log.info(backend.addImage(randomImage(title).build(), MID));
        } else {

        }


        try (Response response = backend.getFrameCreatorRestService().createFrame(MID, offset, null, null, getClass().getResourceAsStream("/VPRO.png"))) {
            log.info("Response: {}", response);
        }

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
                                imageUtil.getSize(iu).orElse(-1L) != originalSizeOfImage
                        );
            });
    }



    @Test
    void test01Overwrite() {
        try (Response response = backend.getFrameCreatorRestService().createFrame(MID, offset, null, null, getClass().getResourceAsStream("/VPRO1970's.png"))) {
            log.info("{}", response);
        }
        waitUntil(ACCEPTABLE_DURATION,
            MID + " has STILL image with offset " + offset + " and size " + originalSizeOfImage,
            () -> {
                ProgramUpdate p  = backend_authority.get(MID);
                return p != null &&
                    p.getImages()
                        .stream()
                        .anyMatch(iu ->
                            iu != null &&
                                iu.getOffset() != null &&
                                iu.getOffset().equals(offset) &&
                                iu.getType() == ImageType.STILL
                                // && imageUtil.getSize(iu).orElse(-1L)  == originalSizeOfImage
                        );
            });
    }


    @Test
    void test98Cleanup() {
        ProgramUpdate update = backend_authority.get(MID);
        Assumptions.assumeTrue(update != null);
        log.info("Removing images " + update.getImages());
        update.getImages().clear();
        backend_authority.set(update);

    }


    @Test
    void test99CheckCleanup() {
         waitUntil(ACCEPTABLE_DURATION,
            MID + " has no stills",
            () -> {
                try {
                    log.info("Getting full {}", MID);
                    ;
                    Program p = backend.getFullProgram(MID);
                    log.info("Found images for {}: {}", MID, p.getImages());
                    return
                        p.getImages()
                            .stream()
                            .filter(iu -> iu != null && iu.getOwner() == OwnerType.AUTHORITY && iu.getType() == ImageType.STILL)
                            .collect(Collectors.toList()).size() == 0;

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return false;
                }
            });


    }

}
