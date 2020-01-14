package nl.vpro.poms.poms;

import io.restassured.RestAssured;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

import org.junit.jupiter.api.*;

import nl.vpro.poms.AbstractApiMediaBackendTest;

import static io.restassured.RestAssured.given;
import static nl.vpro.api.client.utils.Config.Prefix.poms;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * There
 * @author Michiel Meeuwissen
 */
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LetterBoxTest extends AbstractApiMediaBackendTest {

    private static final String IMPORT_URL = CONFIG.url(poms, "import/");
    private static final String USERNAME = CONFIG.configOption(poms, "lettercase-user").orElse("vpro-cms");
    private static final String PASSWORD = CONFIG.requiredOption(poms, "lettercase-password");

    @BeforeEach
    void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
    }

    private static String nepEndpoint = IMPORT_URL + "nep";

    private static String projectm = null;



    //@BeforeAll
    static void getList() {

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
            if (endpoint.endsWith("/import/projectmauthority.restriction")) {
                projectm = endpoint;
            }
        }
        assertThat(nepEndpoint).isNotNull();
    }

    @Test
    @Order(1)
    @Tag("nep")
    void security() {
        log.info("{}", USERNAME);
        String result = given()
            .auth()
            .  basic(USERNAME, "WRONG PASSWORD")
            .log()
            .  ifValidationFails()
            .when()
            .  body("<notify drm=\"false\"\n" +
                "        type=\"ONLINE\"\n" +
                "        mid=\"" + MID + "\" timestamp=\"2017-04-21T16:09:19\" xmlns=\"urn:vpro:media:notify:2017\" />")
            .  contentType("application/xml")
            .  post(nepEndpoint)
            . then()
            .    log().all()
            .    statusCode(401)
            .     extract()
            .  asString();

        log.info("{}", result);
    }




    @Test
    @Order(2)
    @Tag("nep")
    void postToNEP() throws IOException {
        String result = given()
            .auth()
            .  basic(USERNAME, PASSWORD)
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
            .     extract()
            .  asString();

        log.info("result: {}", result);
    }



    @Test
    @Order(3)
    @Tag("restriction")
    void postRestriction() throws IOException {
        String result = given()
            .auth()
            .  basic(USERNAME, PASSWORD)
            .log().ifValidationFails()
            .when()
            .  body("<notify drm=\"false\"\n" +
                "        type=\"ONLINE\"\n" +
                "        mid=\"" + MID + "\" timestamp=\"2017-04-21T16:09:19\" xmlns=\"urn:vpro:media:notify:2017\" />")
            .  contentType("application/xml")
            .  post(projectm)
            . then()
            .    log().all()
            .    statusCode(200)
            .     extract()
            .  asString();

        log.info("bla" + result);

        //backend.getBackendRestService().setPrediction(null, MID, Platform.INTERNETVOD, true, null, PredictionUpdate.builder().platform(Platform.INTERNETVOD).build());


    }


}
