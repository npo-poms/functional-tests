package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;

import nl.vpro.domain.api.ApiScheduleEvent;
import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.media.*;
import nl.vpro.domain.media.*;
import nl.vpro.domain.user.Broadcaster;
import nl.vpro.poms.AbstractApiTest;

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
            assertThat(item.getChannel()).isEqualTo(Channel.NED1);
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
            ApiScheduleEvent o = clients.getScheduleService().nowForBroadcaster("VPRO", null, true, null);
            assertThat(o.getParent().getBroadcasters()).contains(new Broadcaster("VPRO"));
        } catch (javax.ws.rs.NotFoundException nfe) {
            log.info("Ok, no current schedule for VPRO");
        }
    }

    @Test
    void nowForBroadcasterAt() {
        try {
            // Reproduces an issue when it ran at the given time
            ApiScheduleEvent o = clients.getScheduleService().nowForBroadcaster("VPRO", null, true, LocalDateTime.of(2020, 1, 13, 0, 35).atZone(Schedule.ZONE_ID).toInstant());
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
            assertThat(o.getChannel()).isEqualTo(Channel.NED1);
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
        assertThat(o.getChannel()).isEqualTo(Channel.NED1);


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


}
