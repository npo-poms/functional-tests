package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import nl.vpro.domain.media.*;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static nl.vpro.domain.media.support.OwnerType.BROADCASTER;
import static nl.vpro.domain.media.support.OwnerType.NPO;
import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@Slf4j
public class MediaBackendIntentionsAndTargetgroups extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);


    /**
     * At the moment we only save Intentions and TargetGroups for the
     * same owner that is sending the data.
     * An owner is not able to change data from a different one.
     */
    @Test
    public void createObjectWithIntentionsAndTargetGroups() {
        //Given a new Media with intentions and targetgroups from multiple owners
        //And a clientApi configured with a specific owner
        Intentions intentions1 = Intentions.builder()
            .owner(BROADCASTER).values(Arrays.asList(
                IntentionType.ENTERTAINMENT_INFORMATIVE,
                IntentionType.INFORM_INDEPTH))
            .build();
        Intentions intentions2 = Intentions.builder()
            .owner(NPO)
            .value(IntentionType.ACTIVATING)
            .build();

        TargetGroups target1 = TargetGroups.builder()
            .value(TargetGroupType.ADULTS)
            .owner(OwnerType.BROADCASTER)

            .build();
        TargetGroups target2 = TargetGroups.builder()
            .values(Arrays.asList(TargetGroupType.KIDS_6, TargetGroupType.KIDS_12))
            .owner(OwnerType.NPO)
            .build();


        backend.setValidateInput(false);
        ProgramUpdate clip = ProgramUpdate.create(
            MediaBuilder.clip()
                        .ageRating(AgeRating.ALL)
                .mainTitle(title)
                .broadcasters("VPRO")
                .intentions(intentions1, intentions2)
                .targetGroups(target1, target2)
                .build()
        );
        backend.setOwner(OwnerType.BROADCASTER);

        //When we save the media
        String mid = backend.set(clip);
        log.info("Found mid {}", mid);

        //We expect to find only the intentions and targets related to the
        //owner that established the connection.
        ProgramUpdate created = waitUntil(ACCEPTABLE_DURATION,
                mid + " exists",
                () -> backend.get(mid),
                Objects::nonNull);
        assertThat(created.getIntentions()).contains(intentions1.getValues().get(0).getValue());
        assertThat(created.getIntentions()).contains(intentions1.getValues().get(1).getValue());
        assertThat(created.getIntentions()).doesNotContain(intentions2.getValues().get(0).getValue());
        assertThat(created.getTargetGroups()).contains(target1.getValues().get(0).getValue());
        assertThat(created.getTargetGroups()).doesNotContain(target2.getValues().get(0).getValue());

    }

}
