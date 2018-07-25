package nl.vpro.poms.poms;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.rules.AllowUnavailable;
import nl.vpro.rules.TestMDC;

import static io.restassured.RestAssured.given;
import static nl.vpro.api.client.utils.Config.Prefix.poms;
import static nl.vpro.poms.AbstractApiTest.CONFIG;


/**

 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class LetterBoxTest {

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

    @Test
    public void test01GetList() {
        String s =
            given()
                .auth().basic(USERNAME, PASSWORD)
                .log().all()
                .when()
                .  get(IMPORT_URL)
                .then()
                .  log().all()
                .  statusCode(200)
                .  extract().asString();

        String[] split = s.split("\t");

    }


}
