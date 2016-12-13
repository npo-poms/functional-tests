package nl.vpro.poms.backend;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.util.SortedSet;

import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import nl.vpro.domain.media.Schedule;
import nl.vpro.domain.media.update.MediaUpdateList;
import nl.vpro.domain.media.update.MemberUpdate;
import nl.vpro.domain.media.update.RelationUpdate;
import nl.vpro.parkpost.ProductCode;
import nl.vpro.parkpost.promo.bind.PromoEvent;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static com.jayway.restassured.RestAssured.given;
import static nl.vpro.poms.Config.Prefix.backendapi;
import static nl.vpro.poms.Config.Prefix.parkpost;
import static nl.vpro.poms.Config.*;
import static nl.vpro.poms.Utils.waitUntilNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assume.assumeTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParkpostTest extends AbstractApiMediaBackendTest {

    private static final LocalDate today = LocalDate.now(Schedule.ZONE_ID);
    private static final String PRODUCTCODE = "1P0203MO_JOCHEMMY_" + today.toString().replace('-','_');

    private static final String PARKPOST = url(backendapi, "parkpost/");
    private static final String PROMOTED_MID = "WO_VPRO_025057";
    private static String promotionTitle;
    private static String result;

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
        System.out.println("Testing with " + PARKPOST);
    }



    @Test
    public void test001() {

        PromoEvent promoEvent = new PromoEvent();
        promotionTitle = title;
        promoEvent.setProductCode(PRODUCTCODE);
        promoEvent.setPromotedProgramProductCode(PROMOTED_MID);
        promoEvent.setPromoType(ProductCode.Type.P);
        promoEvent.setProgramTitle(promotionTitle);
        //promoEvent.setFrameCount(100L);
        //promoEvent.setBroadcaster("VPRO");
        result =
            given()
                .auth().basic(
                configOption(parkpost, "user").orElse("vpro-cms"),
                requiredOption(parkpost, "password"))
                .contentType(ContentType.XML.withCharset(Charset.defaultCharset()))
                .body(promoEvent)
                .when()
                .   post(PARKPOST + "promo")
                .then()
                .   log().all()
                .   statusCode(AnyOf.anyOf(equalTo(202), equalTo(503)))
                .   extract().asString();

        assertThat(result).isEqualTo("Promo ProgramUpdate for WO_VPRO_025057 has been processed.");
    }


    @Test
    public void test002arrived() throws Exception {
        assumeTrue(result != null);
        assumeTrue(promotionTitle != null);
        MemberUpdate update = waitUntilNotNull(Duration.ofMinutes(1), () -> {
            MediaUpdateList<MemberUpdate> groupMembers = backend.getGroupMembers(PROMOTED_MID);
            return groupMembers.stream().filter(mu -> mu.getMediaUpdate().getTitles().first().equals(promotionTitle)).findFirst().orElse(null);
            }
        );
        assertThat(update).isNotNull();
        SortedSet<RelationUpdate> relations = update.getMediaUpdate().getRelations();
        assertThat(relations.stream().filter(ru -> ru.getType().equals("PROMO_PRODUCTCODE")).findFirst().map(RelationUpdate::getText).orElse(null)).isEqualTo(PRODUCTCODE);
        assertThat(update.getMediaUpdate().getTitles().first()).isEqualTo(promotionTitle);
    }


}
