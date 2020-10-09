package nl.vpro.poms.poms;

import static nl.vpro.api.client.utils.Config.Prefix.poms;
import static nl.vpro.testutils.Utils.CONFIG;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class ScheduleTest {

    private static final String SCHEDULE = CONFIG.url(poms, "domain/schedule/RAD1/2020-06-6");

/*
    @Test
    public void getSchedule() {
        String s =
            given()
                .auth().basic(USERNAME, PASSWORD)
                .log().ifValidationFails()
                .when()
                .  get(IMPORT_URL)
                .then()
                .  log().ifError()
                .  statusCode(200)
                .  extract().asString();

    }


curl -H'content-type=application/json' --cookie 'JSESSIONID=EDC4FCBD8427D80B413D6CFC7BD1C2E2.pomz4aas' https://poms-dev.omroep.nl:443/domain/media/table/POW_04675811,POW_04087220,POW_04560805,POW_04671562,POW_04671563,POW_04671564,POW_04087219,AT_2128559,AT_2136686,POW_04506065,VPWON_1319536,VPWON_1307469,BV_101396192,AT_2140845,POW_04507467,POW_04322773,AT_2130556,POW_04509089,POW_04508129,AT_2135684,POW_04644654,VPWON_1320031,POW_04508524,VPWON_1311373,BV_101398001,POW_04672762,POW_04508928,BV_101400306,POW_04672762,AT_2135684,POW_04657329,POW_04508928,POW_04508524?withSeries=true*/
}
