package nl.vpro.poms.backend;

import lombok.extern.log4j.Log4j2;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import org.junit.jupiter.api.*;

import nl.vpro.domain.media.*;
import nl.vpro.domain.media.update.ProgramUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.testutils.Utils;
import nl.vpro.testutils.Utils.Check;

import static nl.vpro.domain.media.GeoRoleType.SUBJECT;
import static nl.vpro.domain.media.IntentionType.ENTERTAINMENT_INFORMATIVE;
import static nl.vpro.domain.media.IntentionType.INFORM_INDEPTH;
import static nl.vpro.domain.media.TargetGroupType.KIDS_12;
import static nl.vpro.domain.media.TargetGroupType.KIDS_6;
import static nl.vpro.domain.media.support.OwnerType.BROADCASTER;
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
    static String mid = "POMS_VPRO_3320036"; // just t be able to run a single test. just change this in the last one if a test needs fixing.

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
        addCredits(builder);


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
                    ENTERTAINMENT_INFORMATIVE,
                    INFORM_INDEPTH))
                .build()
        );
    }

    @Test
    @Order(10)
    public void checkIntentions() {
        assertThat(created.getIntentions()).hasSize(1);
        assertThat(created.getIntentions().first().getOwner()).isEqualTo(BROADCASTER);
        assertThat(created.getIntentions().first()
            .getValues().stream().map(Intention::getValue))
            .containsExactly(ENTERTAINMENT_INFORMATIVE, INFORM_INDEPTH);
    }

    private void addTargetGroups(MediaBuilder.ProgramBuilder  builder) {
        builder.targetGroups(
            TargetGroups.builder()
                .values(Arrays.asList(KIDS_6, KIDS_12))
                .build()
        );
    }

    @Test
    @Order(11)
    public void checkTargetGroups() {
        assertThat(created.getTargetGroups()).hasSize(1);
        assertThat(created.getTargetGroups().first().getOwner()).isEqualTo(BROADCASTER);
        assertThat(created.getTargetGroups().first()
            .getValues().stream().map(TargetGroup::getValue))
            .containsExactly(KIDS_6, KIDS_12);
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
        assertThat(created.getTopics().first().getOwner()).isEqualTo(BROADCASTER);
        assertThat(created.getTopics().first().getValues()
            .stream().map(Topic::getName))
            .containsExactly("honden", "planeten");
    }

    private void addGeolocations(MediaBuilder.ProgramBuilder  builder) {
        builder.geoLocations(SUBJECT,
            URI.create("http://data.beeldengeluid.nl/gtaa/43919"), // ulft
            URI.create("http://data.beeldengeluid.nl/gtaa/43996") // utrecht
        );
    }

    @Test
    @Order(13)
    public void checkGeolocations() {
        assertThat(created.getGeoLocations()).hasSize(1);
        assertThat(created.getGeoLocations().first().getOwner()).isEqualTo(BROADCASTER);
        assertThat(created.getGeoLocations().first().getValues()
            .stream().map(GeoLocation::getName))
            .containsExactly("Ulft", "Utrecht (stad)");
        assertThat(created.getGeoLocations().first().getValues()
            .stream().map(GeoLocation::getRole))
            .containsExactly(SUBJECT, SUBJECT);
    }

    private void addCredits(MediaBuilder.ProgramBuilder  builder) {
        builder.credits(
            Person.builder().uri(URI.create("http://data.beeldengeluid.nl/gtaa/149017")).role(RoleType.GUEST).build(),
            Person.builder().givenName("pietje").familyName("Puk").role(RoleType.INTERVIEWER).build(),
            Name.builder().uri(URI.create("http://data.beeldengeluid.nl/gtaa/51771")).role(RoleType.COMPOSER).build()
        );
    }

    @Test
    @Order(14)
    public void checkCredits() {
        assertThat(created.getCredits()).hasSize(3);
        assertThat(created.getCredits().get(0).getName()).isEqualTo("Rutte, Mark");
        assertThat(created.getCredits().get(0)).isInstanceOf(Person.class);
        assertThat(created.getCredits().get(1).getName()).isEqualTo("Puk, pietje");
        assertThat(created.getCredits().get(1)).isInstanceOf(Person.class);
        assertThat(created.getCredits().get(2).getName()).isEqualTo("Doe maar");
        assertThat(created.getCredits().get(2)).isInstanceOf(Name.class);
    }

    @Test
    @Order(50)
    @Tag("clean")
    public void setEmptyAgain() {
        created.getIntentions().first().clear();
        created.getTargetGroups().first().clear();
        created.getGeoLocations().first().clear();
        created.getTopics().first().clear();
        ProgramUpdate clip = ProgramUpdate.create(created);
        mid = backend.set(clip);

    }
    @Test
    @Order(51)
    @Tag("clean")
    public void checkSetEmpty() {
        MediaObject updated = Utils.waitUntil(ACCEPTABLE_DURATION,
            () -> backend.getFull(mid),
            Check.<MediaObject>builder()
                .predicate(Objects::nonNull)
                .description("is not null")
                .build(),
            Check.<MediaObject>builder()
                .predicate(m -> m.getIntentions().first().isEmpty())
                .description(mid + " has no intentions")
                .build()
        );
        assertThat(updated.getIntentions().first().isEmpty()).isTrue();
        assertThat(updated.getTargetGroups().first().isEmpty()).isTrue();
        assertThat(updated.getGeoLocations().first().isEmpty()).isTrue();
        assertThat(updated.getTopics().first().isEmpty()).isTrue();
    }


    @SuppressWarnings("UnusedReturnValue")
    private Program getCreated() {
        if (created == null && mid != null) {
            created = backend.getFull(mid);
        }
        return created;
    }

}
