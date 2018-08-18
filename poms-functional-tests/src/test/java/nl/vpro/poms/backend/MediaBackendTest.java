package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.AgeRating;
import nl.vpro.domain.media.MediaBuilder;
import nl.vpro.domain.media.MediaTestDataBuilder;
import nl.vpro.domain.media.update.MediaUpdateList;
import nl.vpro.domain.media.update.MemberUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.rs.media.ResponseError;
import nl.vpro.rules.DoAfterException;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.*;


/*
 * 2018-08-17:
 * 5.9-SNAPSHOT @ dev : allemaal ok
 */
/***
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MediaBackendTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        if (! (t instanceof AssumptionViolatedException)) {
            MediaBackendTest.exception = t;
        }
    });

    private static Throwable exception = null;

    @Before
    public void setup() {
        log.info("Mailing errors to {}", backend.getErrors());
        assumeThat(backend.getErrors(), not(isEmptyOrNullString()));
        assumeNoException(exception);

    }



    static String newMid;
    @Test
    public void test01CreateObjectWithMembers() {
        ProgramUpdate clip = ProgramUpdate.create(
            MediaTestDataBuilder.clip()
                .ageRating(AgeRating.ALL)
                .title(title)
                .broadcasters("VPRO")
                .languages("ZH")
                .constrainedNew()
                .build()
        );
        clip.setVersion(5.5f);

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
    public void test02CheckArrived() {
        assumeNotNull(newMid);

        ProgramUpdate u = waitUntil(
            ACCEPTABLE_DURATION,
            newMid + " exists ",
            () -> backend.get(newMid),
            Objects::nonNull);
        //assertThat(u.getSegments()).hasSize(1);
        assertThat(u.getLanguages()).containsExactly(new Locale("ZH"));

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
     *
     */
    @Test
    public void test03CreateObjectWithoutBroadcaster() {
        backend.setValidateInput(false);
        ProgramUpdate clip = ProgramUpdate.create(
            MediaBuilder.clip()
                .ageRating(AgeRating.ALL)
                .mainTitle(title)
                .languages("ZH")
                .build()
        );
        clip.setVersion(5.5f);
        try {
            String mid = backend.set(clip);
            log.info("Found mid {}", mid);
            //fail("Should give error on creating object without any broadcasters. But created  " + mid);
        } catch (ResponseError re) {
            log.info("Response: {}", re);
            assertThat(re.getStatus()).isEqualTo(401);
        }

    }


}
