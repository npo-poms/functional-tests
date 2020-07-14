package nl.vpro.poms.backend;

import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.*;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.domain.media.update.*;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.Require;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.testutils.Utils.Check;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 2018-08-14
 * 5.9-SNAPSHOT @ dev :ok
 * 2019-05-06
 * 5.11-SNAPSHOT @ dev :ok
 *
 * @ test: 403 permission denied (we moeten hiervoor een account hebben, anders kunnen we niet testen!)
 * 5.7.9 @ test: 403 permission denied (we moeten hiervoor een account hebben, anders kunnen we niet testen!)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
public class AddFrameTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    private static final Duration OFFSET = Duration.ofMinutes(10).plus(Duration.ofMinutes((int) (20f * Math.random())));

    private static final long ORIGINAL_SIZE_OF_IMAGE = 2621; // This is the size of an image we upload in test03

    private static String createImageUri;

    @BeforeAll
    static void init() {
        log.info("Offset for this test {}", OFFSET);
    }


    @Test
    @Order(1)
    @Require.Needs(MID)
    public void addFrame() {
        Program fullProgram = backend.getFullProgram(MID);
        if (fullProgram.getImage(ImageType.PICTURE) == null) {
            log.info("No image with type PICTURE yet present");
            log.info(backend.addImage(randomImage(title).build(), MID));
        }
        try (Response response = backend.getFrameCreatorRestService().createFrame(
            MID, OFFSET, null, null, getClass().getResourceAsStream("/VPRO.png"))) {
            log.info("Response: {}", response.readEntity(String.class));
            assertThat(response.getStatus()).isEqualTo(202);

        }

    }

    @Test
    @Order(2)
    public void checkFameArrived() {
        final ImageUpdate[] update = new ImageUpdate[1];
        waitUntil(ACCEPTABLE_DURATION,
            () -> backend_authority.get(MID),
            Check.<MediaUpdate<?>>builder()
                .description("has {}", MID)
                .predicate(Objects::nonNull)
                .build(),
            Check.<MediaUpdate<?>>builder()
                .description("has image STILL with offset {}", OFFSET)
                .predicate((o) -> {
                        update[0] =
                            o.getImages()
                                .stream()
                                .filter(iu ->
                                    iu != null &&
                                        iu.getOffset() != null &&
                                        iu.getOffset().equals(OFFSET) &&
                                        iu.getType() == ImageType.STILL
                                ).findFirst().orElse(null);
                        return update[0] != null;
                    })
                .build(),
            Check.<MediaUpdate<?>>builder()
                .description("image has size != {}",   ORIGINAL_SIZE_OF_IMAGE)
                .predicate((o) -> {
                    long foundSize = imageUtil.getSize(update[0]).orElse(-1L);
                    if (foundSize == ORIGINAL_SIZE_OF_IMAGE) {
                        log.info("Found {} but the size is the original size, so this may be from test10", update[0]);
                        return false;
                    } else {
                        createImageUri = update[0].getImageUri();
                        return true;
                    }
                })
                .build()
        );
    }



    @Test
    @Order(10)
    public void overwrite() {

        try (Response response = backend.getFrameCreatorRestService().createFrame(MID, OFFSET, null, null, getClass().getResourceAsStream("/VPRO1970's.png"))) {
            log.info("{}", response);
        }
        waitUntil(ACCEPTABLE_DURATION,
            MID + " has STILL image with offset " + OFFSET + " and size " + ORIGINAL_SIZE_OF_IMAGE,
            () -> {
                ProgramUpdate p  = backend_authority.get(MID);
                if (p == null) {
                    throw new IllegalStateException("Program " + MID + " not found");
                }

                ImageUpdate foundImage = p.getImages()
                        .stream()
                        .filter(iu ->
                            iu != null &&
                                iu.getOffset() != null &&
                                iu.getOffset().equals(OFFSET) &&
                                iu.getType() == ImageType.STILL)
                        .findFirst()
                    .orElse(null)
                    ;

                if (foundImage == null) {
                    //return false;
                    throw new IllegalStateException("No image found for " + MID + " with offset " + OFFSET);
                }
                String uri = foundImage.getImageUri();
                if (uri.equals(createImageUri)) {
                    return false;
                }
                long newSize = imageUtil.getSize(foundImage).orElse(-1L);
                return newSize == ORIGINAL_SIZE_OF_IMAGE || newSize == -1L;
            });
    }


    @Test
    @AbortOnException.NoAbort
    @Order(100)
    public void cleanup() {
        cleanupAllImages(MID);
    }


    @Test
    @AbortOnException.NoAbort
    @Order(101)
    public void checkCleanup() {
         waitUntil(ACCEPTABLE_DURATION,
            MID + " has no stills",
            () -> {
                try {
                    log.info("Getting full {}", MID);

                    Program p = backend.getFullProgram(MID);
                    log.info("Found images for {}: {}", MID, p.getImages());
                    return
                        p.getImages()
                            .stream()
                            .noneMatch(iu -> iu != null && iu.getOwner() == OwnerType.AUTHORITY && iu.getType() == ImageType.STILL);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return false;
                }
            });


    }
}
