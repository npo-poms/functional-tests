package nl.vpro.poms;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.domain.api.ApiScheduleEvent;
import nl.vpro.domain.api.media.ScheduleResult;
import nl.vpro.domain.media.Schedule;


public class ApiScheduleTest extends AbstractApiTest {

    public ApiScheduleTest() {


    }

    @Before
    public void setup() {

    }


    @Test
    public void list() throws Exception {
        ScheduleResult o = clients.getScheduleService().list(LocalDate.now(Schedule.ZONE_ID), null, null, null, "ASC", 0L, 240);
    }

    @Test
    public void listBroadcaster() throws Exception {
        ScheduleResult o = clients.getScheduleService().listBroadcaster("VPRO", LocalDate.now(Schedule.ZONE_ID), null, null, null, "ASC", 0L, 240);
    }


    @Test
    public void listChannel() throws Exception {
        ScheduleResult o = clients.getScheduleService().listChannel("NED1", LocalDate.now(Schedule.ZONE_ID), null, null, null, "ASC", 0L, 240);
    }

    @Test
    public void listNet() throws Exception {
        ScheduleResult o = clients.getScheduleService().listNet("ZAPPNET", LocalDate.now(Schedule.ZONE_ID), null, null, null, "ASC", 0L, 240);
    }



    @Test
    public void nowForBroadcaster() throws Exception {
        ApiScheduleEvent o = clients.getScheduleService().nowForBroadcaster("VPRO", null);
    }

    @Test
    public void nextForBroadcaster() throws Exception {
        ApiScheduleEvent o = clients.getScheduleService().nextForBroadcaster("VPRO", null);
    }


    @Test
    public void nowForChannel() throws Exception {
        ApiScheduleEvent o = clients.getScheduleService().nowForChannel("NED1", null);
    }

    @Test
    public void nextForChannel() throws Exception {
        ApiScheduleEvent o = clients.getScheduleService().nextForChannel("NED1", null);
    }


}
