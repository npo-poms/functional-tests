package nl.vpro.poms.npoapi;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.domain.api.ApiScheduleEvent;
import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.media.*;
import nl.vpro.domain.media.Channel;
import nl.vpro.domain.media.Net;
import nl.vpro.domain.media.Schedule;
import nl.vpro.domain.user.Broadcaster;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Java6Assertions.assertThat;


public class ApiScheduleTest extends AbstractApiTest {


    private static LocalDate today = LocalDate.now(Schedule.ZONE_ID);

    static {
        System.out.println("Today : " + today);
    }

    public ApiScheduleTest() {


    }

    @Before
    public void setup() {

    }


    @Test
    public void list() throws Exception {
        ScheduleResult o = clients.getScheduleService().list(today, null, null, null, "ASC", 0L, 240);
        assertThat(o.getSize()).isGreaterThan(10);
    }

    @Test
    public void listBroadcaster() throws Exception {
        ScheduleResult o = clients.getScheduleService().listBroadcaster("VPRO", today, null, null, "broadcasters", "ASC", 0L, 240);
        assertThat(o.getSize()).isGreaterThan(10);
        int i = 0;
        for (ApiScheduleEvent item : o.getItems()) {
            //System.out.println("item  " + i++ + " " + item.getMediaObject().getMid());
            assertThat(item.getMediaObject().getBroadcasters()).contains(new Broadcaster("VPRO"));
        }
    }


    @Test
    public void listChannel() throws Exception {
        ScheduleResult o = clients.getScheduleService().listChannel("NED1", today, null, null, null, "ASC", 0L, 240);
        assertThat(o.getSize()).isGreaterThan(10);
        for (ApiScheduleEvent item : o.getItems()) {
            assertThat(item.getChannel()).isEqualTo(Channel.NED1);
        }
    }

    @Test
    public void listNet() throws Exception {
        ScheduleResult o = clients.getScheduleService().listNet("ZAPP", today, null, null, null, "ASC", 0L, 240);
        assertThat(o.getSize()).isGreaterThan(2);
        for (ApiScheduleEvent item : o.getItems()) {
            assertThat(item.getNet()).isNotNull();
            assertThat(item.getNet()).isEqualTo(new Net("ZAPP"));

        }
    }



    @Test
    public void nowForBroadcaster() throws Exception {
        try {
            ApiScheduleEvent o = clients.getScheduleService().nowForBroadcaster("VPRO", null);
            assertThat(o.getMediaObject().getBroadcasters()).contains(new Broadcaster("VPRO"));
        } catch (javax.ws.rs.NotFoundException nfe) {
            System.out.println("Ok, no current schedule for VPRO");
        }
    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void nowForBroadcasterNotFound() throws Exception {
        clients.getScheduleService().nowForBroadcaster("TELEAC", null);
    }

    @Test
    public void nextForBroadcaster() throws Exception {
        ApiScheduleEvent o = clients.getScheduleService().nextForBroadcaster("VPRO", null);
        System.out.println(o);
        assertThat(o.getMediaObject().getBroadcasters()).contains(new Broadcaster("VPRO"));


    }


    @Test
    public void nowForChannel() throws Exception {
        try {
            ApiScheduleEvent o = clients.getScheduleService().nowForChannel("NED1", null);
            System.out.println(o);
            assertThat(o.getChannel()).isEqualTo(Channel.NED1);
        } catch (javax.ws.rs.NotFoundException nfe) {
            System.out.println("Ok, no current schedule for NED1");
        }

    }

    @Test(expected = javax.ws.rs.NotFoundException.class)
    public void nowForChannelNotFound() throws Exception {
        ApiScheduleEvent o = clients.getScheduleService().nowForChannel("H1NL", null);
        System.out.println(o);

    }

    @Test
    public void nextForChannel() throws Exception {
        ApiScheduleEvent o = clients.getScheduleService().nextForChannel("NED1", null);
        System.out.println(o);
        assertThat(o.getChannel()).isEqualTo(Channel.NED1);


    }

    @Test
    // https://jira.vpro.nl/browse/MSE-3533
    public void testWithProperties() {
        MediaForm mediaForm = MediaFormBuilder.form().mediaIds("NCRV_1347071").build();
        ScheduleForm form = ScheduleForm.from(mediaForm);
        ScheduleSearchResult result = clients.getScheduleService().find(form, null, "descendantOf", 0L, 4);
        assertThat(result.getItems().size()).isGreaterThanOrEqualTo(1);
        for (SearchResultItem<? extends ApiScheduleEvent> e : result) {
            // NCRV_1347071 is descenant!
            assertThat(e.getResult().getMediaObject().getDescendantOf()).isNotEmpty();
        }
    }


}
