package nl.vpro.poms.backend;

import lombok.extern.log4j.Log4j2;

import java.net.URI;
import java.time.Duration;
import java.util.*;

import org.junit.jupiter.api.*;

import nl.vpro.domain.media.*;
import nl.vpro.domain.media.support.OwnerType;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static nl.vpro.testutils.Utils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MediaBackendAddFieldValues extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    static Program created;
    static String mid = "POMS_VPRO_3319601";

    @BeforeEach
    public void init() {
        getCreated();
    }

    /**
     * At the moment we only save Intentions and TargetGroups for the
     * same owner that is sending the data.
     * An owner is not able to change data from a different one.
     */
    @Test
    @Order(1)
    public void createObjectWithFields() {

        MediaBuilder.ProgramBuilder builder = MediaBuilder
            .clip()
            .ageRating(AgeRating.ALL)
            .mainTitle(title)
            .broadcasters("VPRO");

        addIntentions(builder);
        addTargetGroups(builder);
        addTopics(builder);
        addGeolocations(builder);


        ProgramUpdate clip = ProgramUpdate.create(builder.build());

        //When we save the media
        mid = backend.set(clip);
        log.info("Found mid {}", mid);


        created = waitUntil(ACCEPTABLE_DURATION,
                mid + " exists",
                () -> backend.getFull(mid),
                Objects::nonNull);

    }



    private void addIntentions(MediaBuilder.ProgramBuilder  builder) {
        builder.intentions(
            Intentions.builder()
                .values(Arrays.asList(
                    IntentionType.ENTERTAINMENT_INFORMATIVE,
                    IntentionType.INFORM_INDEPTH))
                .build()
        );
    }

    @Test
    @Order(10)
    public void checkIntentions() {
        assertThat(created.getIntentions()).hasSize(1);
        assertThat(created.getIntentions().first().getOwner()).isEqualTo(OwnerType.BROADCASTER);
        assertThat(created.getIntentions().first().getValues().stream().map(Intention::getValue)).containsExactly(IntentionType.ENTERTAINMENT_INFORMATIVE, IntentionType.INFORM_INDEPTH);


    }

    private void addTargetGroups(MediaBuilder.ProgramBuilder  builder) {
        builder.targetGroups(
            TargetGroups.builder()
                .values(Arrays.asList(TargetGroupType.KIDS_6, TargetGroupType.KIDS_12))
                .build()
        );
    }

    @Test
    @Order(11)
    public void checkTargetGroups() {
        assertThat(created.getTargetGroups()).hasSize(1);
        assertThat(created.getTargetGroups().first().getOwner()).isEqualTo(OwnerType.BROADCASTER);
        assertThat(created.getTargetGroups().first().getValues().stream().map(TargetGroup::getValue)).containsExactly(TargetGroupType.KIDS_6, TargetGroupType.KIDS_12);


    }


    private void addTopics(MediaBuilder.ProgramBuilder  builder) {
        builder.topics(
            URI.create("http://data.beeldengeluid.nl/gtaa/25890"), // honden
            URI.create("http://data.beeldengeluid.nl/gtaa/26526") // planeten
        );

    }

    @Test
    @Order(12)
    public void checkTopics() {
        assertThat(created.getTopics()).hasSize(1);
        assertThat(created.getTopics().first().getOwner()).isEqualTo(OwnerType.BROADCASTER);
        assertThat(created.getTopics().first().getValues()
            .stream().map(Topic::getName)).containsExactly("honden", "planeten");

    }

    private void addGeolocations(MediaBuilder.ProgramBuilder  builder) {
        builder.geoLocations(GeoRoleType.SUBJECT,
            URI.create("http://data.beeldengeluid.nl/gtaa/43919"), // ulft
            URI.create("http://data.beeldengeluid.nl/gtaa/43996") // utrecht
        );

    }
    @Test
    @Order(13)
    public void checkGeolocations() {
        assertThat(created.getGeoLocations()).hasSize(1);
        assertThat(created.getGeoLocations().first().getOwner()).isEqualTo(OwnerType.BROADCASTER);
        assertThat(created.getGeoLocations().first().getValues()
            .stream().map(GeoLocation::getName)).containsExactly("Ulft", "Utrecht (stad)");
        assertThat(created.getGeoLocations().first().getValues()
            .stream().map(GeoLocation::getRole)).containsExactly(GeoRoleType.SUBJECT, GeoRoleType.SUBJECT);

    }

    private Program getCreated() {
        if (created == null && mid != null) {
            created = backend.getFull(mid);
        }
        return created;
    }

}
