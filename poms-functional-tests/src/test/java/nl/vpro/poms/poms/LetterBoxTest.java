package nl.vpro.poms.poms;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.domain.media.Platform;
import nl.vpro.domain.media.update.PredictionUpdate;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.rules.AllowUnavailable;
import nl.vpro.rules.TestMDC;

import static io.restassured.RestAssured.given;
import static nl.vpro.api.client.utils.Config.Prefix.poms;
import static org.assertj.core.api.Assertions.assertThat;


/**

 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class LetterBoxTest extends AbstractApiMediaBackendTest {

    @Rule
    public AllowUnavailable allowUnavailable = new AllowUnavailable();

    @Rule
    public TestMDC testMDC = new TestMDC();

    private static final String IMPORT_URL = CONFIG.url(poms, "import/");
    private static final String USERNAME = CONFIG.configOption(poms, "lettercase-user").orElse("vpro-cms");
    private static final String PASSWORD = CONFIG.requiredOption(poms, "lettercase-password");

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
    }

    static String nepEndpoint = null;


    @Test
    public void test01GetList() {
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

        String[] split = s.split("\n");
        for (String e : split) {
            String endpoint = e.split("\t", 2)[0];
            log.info(endpoint);
            if (endpoint.endsWith("/import/nep")) {
                nepEndpoint = endpoint;
            }
        }
        assertThat(nepEndpoint).isNotNull();
    }



    @Test
    public void test02PostToNEP() throws IOException {
        given()
            .auth().basic(USERNAME, PASSWORD)
            .log().ifValidationFails()
            .when()
            .  body("<notify drm=\"false\"\n" +
                "        type=\"ONLINE\"\n" +
                "        mid=\"" + MID + "\" timestamp=\"2017-04-21T16:09:19\" xmlns=\"urn:vpro:media:notify:2017\" />")
            .  contentType("application/xml")
            .  post(nepEndpoint)
            . then()
            .    log().all()
            .    statusCode(200)
            .     extract().asString();

        backend.getBackendRestService().setPrediction(null, MID, Platform.INTERNETVOD, true, null, PredictionUpdate.builder().platform(Platform.INTERNETVOD).build());


    }


}
