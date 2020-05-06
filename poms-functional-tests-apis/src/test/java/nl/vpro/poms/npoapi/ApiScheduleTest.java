package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.time.*;
import java.util.*;

import org.assertj.core.api.Fail;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.api.ApiScheduleEvent;
import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.media.*;
import nl.vpro.domain.media.*;
import nl.vpro.domain.user.Broadcaster;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.util.Env;

import static nl.vpro.domain.media.Channel.*;
import static nl.vpro.util.Env.PROD;
import static org.assertj.core.api.Assertions.assertThat;


@Log4j2
class ApiScheduleTest extends AbstractApiTest {


    private static LocalDate today = LocalDate.now(Schedule.ZONE_ID);

    static {
        log.info("Today : " + today);
    }

    ApiScheduleTest() {


    }

    @BeforeEach
    void setup() {

    }


    @Test
    void list() {
        ScheduleResult o = clients.getScheduleService().list(today, null, null, null, "ASC", 0L, 240);
        assertThat(o.getSize()).isGreaterThan(10);
    }

    @Test
    void listBroadcaster() {
        int sizeOfWeek = 0;
        List<ApiScheduleEvent> items = new ArrayList<>();
        for(LocalDate date = today; date.isAfter(today.minusDays(7)); date = date.minusDays(1)) {
            ScheduleResult o = clients.getScheduleService().listBroadcaster("VPRO", date, null, null, "broadcasters", "ASC", 0L, 240);
            sizeOfWeek += o.getSize();
            items.addAll(o.getItems());
        }
        assertThat(sizeOfWeek).isGreaterThan(10);
        for (ApiScheduleEvent item : items) {
            //log.info("item  " + i++ + " " + item.getMediaObject().getMid());
            assertThat(item.getParent().getBroadcasters()).contains(new Broadcaster("VPRO"));
        }
    }


    @Test
    void listChannel() {
        ScheduleResult o = clients.getScheduleService().listChannel("NED1", today, null, null, null, "ASC", 0L, 240);
        assertThat(o.getSize()).isGreaterThan(10);
        for (ApiScheduleEvent item : o.getItems()) {
            assertThat(item.getChannel()).isEqualTo(NED1);
        }
    }

    @Test
    void listNet() {
        ScheduleResult o = clients.getScheduleService().listNet("ZAPP", today, null, null, null, "ASC", 0L, 240);
        assertThat(o.getSize()).isGreaterThan(2);
        for (ApiScheduleEvent item : o.getItems()) {
            assertThat(item.getNet()).isNotNull();
            assertThat(item.getNet()).isEqualTo(new Net("ZAPP"));

        }
    }



    @Test
    void nowForBroadcaster() {
        try {
            ApiScheduleEvent o = clients.getScheduleService().nowForBroadcaster("VPRO", "broadcasters", true, null);
            assertThat(o.getParent().getBroadcasters()).contains(new Broadcaster("VPRO"));
        } catch (javax.ws.rs.NotFoundException nfe) {
            log.info("Ok, no current schedule for VPRO");
        }
    }

    @Test
    void nowForBroadcasterAt() {
        try {
            // Reproduces an issue when it ran at the given time (solution, set the properties!)
            ApiScheduleEvent o = clients.getScheduleService().nowForBroadcaster("VPRO", "broadcasters", true, LocalDateTime.of(2020, 1, 13, 0, 35).atZone(Schedule.ZONE_ID).toInstant());
            assertThat(o.getParent().getBroadcasters()).contains(new Broadcaster("VPRO"));
        } catch (javax.ws.rs.NotFoundException nfe) {
            log.info("Ok, no current schedule for VPRO");
        }
    }

    @Test
    void nowForBroadcasterNotFound() {
        Assertions.assertThrows(javax.ws.rs.NotFoundException.class, () -> {

            clients.getScheduleService().nowForBroadcaster("TELEAC", null, true, null);
        });
    }

    @Test
    void nextForBroadcaster() {
        ApiScheduleEvent o = clients.getScheduleService().nextForBroadcaster("VPRO", null, null);
        log.info("{}", o);
        assertThat(o.getParent().getBroadcasters()).contains(new Broadcaster("VPRO"));


    }


    @Test
    void nowForChannel() {
        try {
            ApiScheduleEvent o = clients.getScheduleService().nowForChannel("NED1", null, null);
            log.info("{}", o);
            assertThat(o.getChannel()).isEqualTo(NED1);
        } catch (javax.ws.rs.NotFoundException nfe) {
            log.warn("Ok, no current schedule for NED1");
        }

    }

    @Test
    void nowForChannelNotFound() {
        Assertions.assertThrows(javax.ws.rs.NotFoundException.class, () -> {
            ApiScheduleEvent o = clients.getScheduleService().nowForChannel("H1NL", null, null);
            log.error("Found {}", o);
        });

    }

    @Test
    void nextForChannel() {
        ApiScheduleEvent o = clients.getScheduleService().nextForChannel("NED1", null, null);
        log.info("{}", o);
        assertThat(o.getChannel()).isEqualTo(NED1);


    }

    @Test
    // https://jira.vpro.nl/browse/MSE-3533
    void testWithProperties() {
        MediaForm mediaForm = MediaFormBuilder.form().mediaIds("NCRV_1347071").build();
        ScheduleForm form = ScheduleForm.from(mediaForm);
        ScheduleSearchResult result = clients.getScheduleService().find(form, null, "descendantOf", 0L, 4);
        assertThat(result.getItems().size()).isGreaterThanOrEqualTo(1);
        for (SearchResultItem<? extends ApiScheduleEvent> e : result) {
            // NCRV_1347071 is descendant!
            assertThat(e.getResult().getParent().getDescendantOf()).isNotEmpty();
        }
    }

    static List<Channel> getChannelsWithCurrentBroadcasts() {
          List<Channel> knownsChannels = Arrays.asList(
              NED1,
              NED2,
              NED3,
              RAD1,
              RAD2,
              RAD3,
              RAD4,
              RAD5,
              RAD6,

              BVNT,

              NOSJ,
              CULT,
              _101_,
              PO24,
              // HOLL, BESTAAT NIET MEER?
              // GESC, Bestaat niet meer?
              OPVO,
              FUNX,


              // I don't know why
              NOSJ,
              _101_,
              PO24,

              OFRY,
              NOOR,
              //RTVD, // zeker TVDR geworden?
              OOST,
              GELD,
              FLEV,
              BRAB,
              REGU,
              NORH,
              WEST,
              RIJN,
                L1TV,
              OZEE,
              TVDR
          );

        return knownsChannels;

    }
    static Set<Channel> channelsNotSupported = null;
    static Set<Channel> getChannelsNotSupported() {
        if (channelsNotSupported == null) {

            if (CONFIG.env() == PROD || Env.valueOf(CONFIG.requiredOption(Config.Prefix.npo_api, "es_env").toUpperCase()) == PROD) {
                channelsNotSupported =  Collections.emptySet();
            } else {
                channelsNotSupported = new HashSet<>(Arrays.asList(
                    // these come from imports which are not necessarly present on other environments then prod
                    // I don't know why
                    NOSJ,
                    _101_,
                    PO24,


                    // xml's not shipped to dev/test
                    OFRY,
                    NOOR,
                    //RTVD, // zeker TVDR geworden?
                    OOST,
                    GELD,
                    FLEV,
                    BRAB,
                    REGU,
                    NORH,
                    WEST,
                    RIJN,
                    L1TV,
                    OZEE,
                    TVDR

                ));
            }
        }
        return channelsNotSupported;
    }



    /**
     * NPA-537 For all poms supported channels we should find a valid 'now for channel'.
     */
    @ParameterizedTest
    @MethodSource("getChannelsWithCurrentBroadcasts")
    void nowForChannel(Channel channel) {
        nowForChannelAt(channel, null);
    }

     /**
     * NPA-537 For all poms supported channels we should find a valid 'now for channel'.
     */
    @ParameterizedTest
    @MethodSource("getChannelsWithCurrentBroadcasts")
    void nowForChannelAt(Channel channel) {
        Instant instant = LocalDateTime.of(2020, 5, 1, 20, 30).atZone(Schedule.ZONE_ID).toInstant();
        nowForChannelAt(channel, instant);
    }

     void nowForChannelAt(Channel channel, @Nullable Instant instant) {
        try {
            ApiScheduleEvent o = clients.getScheduleService().nowForChannel(
                channel.name(), null, instant);
            log.info("{}", o);
            assertThat(o.getChannel()).isEqualTo(channel);
            if (getChannelsNotSupported().contains(channel)) {
                throw new IllegalArgumentException("This unexpectedly didn't fail. Change assumption?");
            }
        } catch (javax.ws.rs.NotFoundException nfe) {
            if (getChannelsNotSupported().contains(channel)) {
                throw new TestAbortedException("Channel " + channel + " is known not to work at " + CONFIG.env());
            } else {
                Fail.fail("No current broadcast found for %s (%s) at %s", channel, channel.name(), instant);
            }
        }

    }


}
