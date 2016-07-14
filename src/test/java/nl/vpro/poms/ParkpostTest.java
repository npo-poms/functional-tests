package nl.vpro.poms;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import nl.vpro.parkpost.ProductCode;
import nl.vpro.parkpost.promo.bind.PromoEvent;

import static com.jayway.restassured.RestAssured.given;
import static nl.vpro.poms.Config.configOption;
import static nl.vpro.poms.Config.requiredOption;
import static nl.vpro.poms.Config.url;
import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParkpostTest {


    private static final String PARKPOST = url("backendapi.url", "parkpost/");

    private static final String EXAMPLE = "<NPO_gfxwrp>\n" +
        "  <ProductCode>2P0108MO_BLAUWBLO</ProductCode>\n" +
        "  <OrderCode>2P140801_EO___BLAUW_BL_MOR</OrderCode>\n" +
        "  <Broadcaster>EO</Broadcaster>\n" +
        "  <PromotedProgramProductCode>VPWON_1227699</PromotedProgramProductCode>\n" +
        "  <Referrer/>\n" +
        "  <MXF_Name>91345392</MXF_Name>\n" +
        "  <ProgramTitle>Blauw Bloed Extra: Prinses Irene - 75 jaar</ProgramTitle>\n" +
        "  <EpisodeTitle>Blauw Bloed Extra: Prinses Irene - 75 jaar</EpisodeTitle>\n" +
        "  <PromoType>P</PromoType>\n" +
        "  <TrailerTitle>Blauw Bloed Extra: Prinses Irene - 75 jaar</TrailerTitle>\n" +
        "  <SerieTitle>Blauw Bloed Extra: Prinses Irene - 75 jaar</SerieTitle>\n" +
        "  <FrameCount>750</FrameCount>\n" +
        "  <VideoFormat>HD</VideoFormat>\n" +
        "  <FirstTransmissionDate>2014-08-01T16:59:04+00:00</FirstTransmissionDate>\n" +
        "  <PlacingWindowStart>2014-07-31T06:00:00+02:00</PlacingWindowStart>\n" +
        "  <PlacingWindowEnd>2014-08-01T06:00:00+02:00</PlacingWindowEnd>\n" +
        "  <Files>\n" +
        "    <File Filename=\"2P0108MO_BLAUWBLO.ismv\"/>\n" +
        "    <File Filename=\"2P0108MO_BLAUWBLO.ismc\"/>\n" +
        "    <File Filename=\"2P0108MO_BLAUWBLO.ism\"/>\n" +
        "  </Files>\n" +
        "</NPO_gfxwrp>\n";

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
        //promoEvent.setFrameCount(100L);
        //promoEvent.setBroadcaster("VPRO");
        String result =
            given()
                .auth().basic(
                configOption("parkpost.user").orElse("vpro-cms"),
                requiredOption("parkpost.password"))
                .contentType(ContentType.XML.withCharset(Charset.defaultCharset()))
                .body(promoEvent)
                .when()
                .   post(PARKPOST + "promo")
                .then()
                .   log().all()
                .   statusCode(202)
                .   extract().asString();

        assertThat(result).isEqualTo("Promo ProgramUpdate for WO_VPRO_025057 has been processed.");
    }



}
