package nl.vpro.poms.backend;

import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import javax.xml.bind.JAXB;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import nl.vpro.api.client.media.ResponseError;
import nl.vpro.domain.media.*;
import nl.vpro.domain.media.update.*;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.util.Version;

import static nl.vpro.testutils.Utils.waitUntil;
import static nl.vpro.testutils.Utils.waitUntilNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;


/*
 * 2018-08-17:
 * 5.9-SNAPSHOT @ dev : allemaal ok
 */
/**
 * @author Michiel Meeuwissen
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
class MediaBackendTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    @BeforeEach
    public void setup() {
        log.info("Mailing errors to {}", backend.getErrors());
        assumeThat(backend.getErrors()).isNotEmpty();

    }


    private static String newMid;

    @Test
    @Order(1)
    public void createObjectWithMembers() {
        ProgramUpdate clip = ProgramUpdate.create(
            backend.getVersionNumber(),
            MediaTestDataBuilder.clip()
                //.ageRating(AgeRating.ALL)
                .title(title)
                .broadcasters("VPRO")
                .portals("NETINNL")
                .languages("ZH")
                .predictions(Prediction.builder().platform(Platform.INTERNETVOD).encryption(Encryption.NONE).plannedAvailability(true).build())
                .constrainedNew()
                .build()
        );

        JAXB.marshal(clip, System.out);
        newMid = backend.set(clip);
        assertThat(newMid).isNotEmpty();

        log.info("Created {}", newMid);

        ProgramUpdate member = ProgramUpdate
            .create(
                MediaTestDataBuilder.clip()
                    .title(title + "_members")
                    .broadcasters("VPRO")
                    .constrainedNew()
                    .build());

        // TODO: this will happen via queue in ImportRoute
        String memberMid = backend.set(member);
        log.info("Created {} too", memberMid);


        waitUntil(Duration.ofMinutes(5),
            "Waiting until " + memberMid + " see also MSE-3836",
            () -> backend.get(memberMid) != null);

        // This won't so it may be executed earlier and hence fail (MSE-3836)
        backend.createMember(newMid, memberMid, 1);

    }

    @Test
    @Order(2)
    public void checkArrivedCreateObjectWithMembers() {
        assumeThat(newMid).isNotNull();

        ProgramUpdate u = waitUntil(
            ACCEPTABLE_DURATION,
            newMid + " exists ",
            () -> backend.get(newMid),
            Objects::nonNull);
        //assertThat(u.getSegments()).hasSize(1);
        assertThat(u.getLanguages()).containsExactly(new Locale("ZH"));
        assertThat(u.getPredictions()).hasSize(1);
        assertThat(u.getPredictions().first().getPlatform()).isEqualTo(Platform.INTERNETVOD);
        assertThat(u.getPortals()).hasSize(1);
        assertThat(u.getPortals().get(0)).isEqualTo("NETINNL");

        MediaUpdateList<MemberUpdate> memberUpdates = waitUntil(ACCEPTABLE_DURATION,
            newMid + " exists and has one member",
            () -> backend.getGroupMembers(newMid),
            (groupMembers) -> groupMembers.size() == 1
        );
        assertThat(memberUpdates).hasSize(1);



    }

   /* @Test
    public void test03UpdateClip() {
        ProgramUpdate clip = ProgramUpdate.create(
            MediaTestDataBuilder.clip()
                .title(title)
                .crids("crid://backendtests/clip/" + NOW)
                .broadcasters("VPRO")
                .constrainedNew()
                .segments(MediaTestDataBuilder
                    .segment()
                    .title("segment of " + title)
                    .broadcasters("VPRO")
                    .constrainedNew()
                    .build()
                )
        );
        String foundMid = backend.set(clip);


    }
*/


    /**
     * It should simple provisionlly accept.
     */
    @Test
    @Order(3)
    public void createObjectWithoutBroadcaster() {
        backend.setValidateInput(false);
        ProgramUpdate clip = ProgramUpdate.create(
            MediaBuilder.clip()
                .ageRating(AgeRating.ALL)
                .mainTitle(title)
                .languages("ZH")
                .build()
        );
        clip.setVersion(Version.of(5, 5));
        try {
            String mid = backend.set(clip);
            log.info("Found mid {}", mid);
            //fail("Should give error on creating object without any broadcasters. But created  " + mid);
        } catch (ResponseError re) {
            log.info("Response: {}", re.getMessage(), re);
            assertThat(re.getStatus()).isEqualTo(401);
        }

    }

    private static final String CRID = "crid://test.poms/1";

    @Test
    @Order(4)
    public void deleteForCridIfExists() {
        log.info("{}", backend.deleteIfExists(CRID));
        Optional<ProgramUpdate> pu = waitUntil(
            ACCEPTABLE_DURATION,
            CRID + " does not exists (or is deleted)",
            () -> backend.optional(CRID),
            o -> o.isEmpty() || o.get().isDeleted());
        pu.ifPresent(
            programUpdate -> log.info("Found {}", programUpdate)
        );


    }

    private static String midWithCrid;
    private static String againMidWithCrid;
    private static String againMidWithStolenCrid;


    @Test
    @Order(5)
    public void createObjectWithCrids() {
        backend.setLookupCrids(false);
        ProgramUpdate clip = ProgramUpdate.create(
            MediaBuilder.clip()
                .ageRating(AgeRating.ALL)
                .broadcasters("VPRO")
                .mainTitle(title)
                .crids("crid://test.poms/1")
                .build()
        );
        midWithCrid = backend.set(clip);
        log.info("Found mid {}", midWithCrid);
        ProgramUpdate created = waitUntil(ACCEPTABLE_DURATION,
            midWithCrid + " exists",
            () -> backend.get(midWithCrid),
            Objects::nonNull);
        assertThat(created.getCrids()).contains(CRID);
    }

    @Test
    @Order(6)
    public void createObjectWithCridsAgain() {
        backend.setLookupCrids(false);
        ProgramUpdate clip = ProgramUpdate.create(
            MediaBuilder.clip()
                .ageRating(AgeRating.ALL)
                .broadcasters("VPRO")
                .mainTitle(title)
                .crids("crid://test.poms/1")
                .build()
        );
        againMidWithCrid = backend.set(clip);
        log.info("Found another mid {}. This clip may not actually appear!", againMidWithCrid);
    }

    @Test
    @Order(7)
    public void createObjectWithStolenCrids() {
        backend.setLookupCrids(false);
        backend.setStealCrids(AssemblageConfig.Steal.YES);
        ProgramUpdate clip = ProgramUpdate.create(
            MediaBuilder.clip()
                .ageRating(AgeRating.ALL)
                .broadcasters("VPRO")
                .mainTitle(title)
                .ageRatingAllIfUnset()

                .crids("crid://test.poms/1")
                .build()
        );
        againMidWithStolenCrid = backend.set(clip);
        log.info("Found another mid {}", againMidWithStolenCrid);
        waitUntil(ACCEPTABLE_DURATION,
            CRID + " exists ",
            () -> backend.get(againMidWithStolenCrid),
            (Predicate<MediaUpdate<?>>) u -> u != null && u.getCrids().contains(CRID));
    }


    @Test
    @Order(8)
    public void checkObjectsWithCrids() {
        assumeThat(midWithCrid).isNotNull();
        assumeThat(againMidWithCrid).isNotNull();
        assumeThat(againMidWithStolenCrid).isNotNull();
        waitUntil(ACCEPTABLE_DURATION,
            CRID + " exists ",
            () -> backend.get(midWithCrid),
            (Predicate<MediaUpdate<?>>) u -> u != null && !u.getCrids().contains(CRID));

        Assertions.assertThat((Object) backend.get(againMidWithCrid)).isNull();
    }

    private static String midForPortal;

    @Test
    @Order(9)
    public void createObjectForPortal() {
        ProgramUpdate clip = ProgramUpdate.create(
            backend.getVersionNumber(),
            MediaTestDataBuilder.clip()
                .ageRating(AgeRating.ALL)
                .title(title)
                .portals("NETINNL")
                .predictions(Prediction.builder().platform(Platform.INTERNETVOD).encryption(Encryption.NONE).plannedAvailability(true).build())
                .constrainedNew()
                .clearBroadcasters()
                .broadcasters("NOS")
                .build()
        );

        JAXB.marshal(clip, System.out);
        midForPortal = backend.set(clip);
        assertThat(midForPortal).isNotEmpty();

        log.info("Created {}", midForPortal);

    }
    @Test
    @Order(10)
    public void checkObjectForPortal() {
        assumeThat(midForPortal).isNotNull();
        waitUntilNotNull(ACCEPTABLE_DURATION,
            midForPortal + " exists ",
            () -> backend.get(midForPortal));
    }

    @Test
    @Disabled
    public void tryToPinDownDamnServerErrorsOnDev() throws InterruptedException {
        for (int i = 0 ; i < 100; i++) {
            ProgramUpdate update = backend_authority.get(MID);
            assertThat(update).isNotNull();
            log.info("{}: {}", i, update);
            Thread.sleep(1000);
        }
    }

}
