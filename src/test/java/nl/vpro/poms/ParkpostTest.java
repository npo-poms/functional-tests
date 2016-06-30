package nl.vpro.poms;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.RestAssured;

import nl.vpro.parkpost.ProductCode;
import nl.vpro.parkpost.promo.bind.PromoEvent;

import static com.jayway.restassured.RestAssured.given;
import static nl.vpro.poms.Config.configOption;
import static nl.vpro.poms.Config.requiredOption;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParkpostTest {


    private static final String PARKPOST = configOption("backendapi.url").orElse("https://api-dev.poms.omroep.nl/") + "parkpost/";

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
    }

    @Test(timeout = 100000)
    public void test001() {
        PromoEvent promoEvent = new PromoEvent();

        promoEvent.setProductCode("1P0203MO_JOCHEMMY");
        promoEvent.setPromotedProgramProductCode("WO_VPRO_025057");
        promoEvent.setPromoType(ProductCode.Type.P);
        promoEvent.setProgramTitle("Promo title");
        String result =
            given()
                .auth().basic(
                configOption("parkpost.user").orElse("vpro-cms"),
                requiredOption("parkpost.password"))
                .contentType("application/xml")
                .body(promoEvent)
                .when()
                .   post(PARKPOST + "promo")
                .then()
                .   log().all()
                .   statusCode(202)
                .   extract().asString();
    }



}
