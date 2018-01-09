package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.MediaTestDataBuilder;
import nl.vpro.domain.media.update.MediaUpdateList;
import nl.vpro.domain.media.update.MemberUpdate;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.rules.DoAfterException;

import static nl.vpro.poms.Utils.waitUntil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;

/**
 * Tests whether adding and modifying images via the POMS backend API works.
 *
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MediaBackendTest extends AbstractApiMediaBackendTest {



    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);
    private static final List<String> titles = new ArrayList<>();

    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        if (! (t instanceof AssumptionViolatedException)) {
            MediaBackendTest.exception = t;
        }
    });

    private static Throwable exception = null;

    @Before
    public void setup() {
        assumeNoException(exception);
    }



    static String newMid;
    @Test
    public void test01CreateObjectWithMembers() {
        ProgramUpdate clip = ProgramUpdate.create(
            MediaTestDataBuilder.clip()
                .title(title)
                .broadcasters("VPRO")
                .constrainedNew()
        );

        newMid = backend.set(clip);
        assertThat(newMid).isNotEmpty();
        log.info("Created {}", newMid);

        ProgramUpdate member = ProgramUpdate
            .create(
                MediaTestDataBuilder.clip()
                    .title(title + "_members")
                    .broadcasters("VPRO")
                    .constrainedNew());
        log.info("Created {} too", newMid);


        // TODO: this will happen via queue in ImportRoute
        String memberMid = backend.set(member);

        waitUntil(Duration.ofMinutes(5),
            "Waiting until " + memberMid + " see also MSE-3836",
            () -> backend.get(memberMid) != null);

        // This won't so it may be executed earlier and hence fail (MSE-3836)
        backend.createMember(newMid, memberMid, 1);

    }

    @Test
    public void test02Checkarrived() {
        assumeNotNull(newMid);


        MediaUpdateList<MemberUpdate> memberUpdates = waitUntil(ACCEPTABLE_DURATION,
            newMid + " exists and has one member",
            () -> backend.getGroupMembers(newMid),
            (groupMembers) -> groupMembers.size() == 1
        );
        assertThat(memberUpdates).hasSize(1);
    }



}
