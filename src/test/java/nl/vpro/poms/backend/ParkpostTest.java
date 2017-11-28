package nl.vpro.poms.backend;

import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.SortedSet;

import javax.xml.bind.JAXB;

import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import nl.vpro.domain.media.MediaType;
import nl.vpro.domain.media.Program;
import nl.vpro.domain.media.ProgramType;
import nl.vpro.domain.media.Schedule;
import nl.vpro.domain.media.update.MediaUpdateList;
import nl.vpro.domain.media.update.MemberUpdate;
import nl.vpro.domain.media.update.RelationUpdate;
import nl.vpro.parkpost.ProductCode;
import nl.vpro.parkpost.promo.bind.PromoEvent;
import nl.vpro.poms.AbstractApiMediaBackendTest;

import static com.jayway.restassured.RestAssured.given;
import static nl.vpro.api.client.utils.Config.Prefix.backendapi;
import static nl.vpro.api.client.utils.Config.Prefix.parkpost;
import static nl.vpro.poms.Utils.waitUntilNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assume.assumeTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class ParkpostTest extends AbstractApiMediaBackendTest {

    private static final LocalDate today = LocalDate.now(Schedule.ZONE_ID);
    private static final String PRODUCTCODE = "1P0203MO_JOCHEMMY_" + today.toString().replace('-','_');

    private static final String PARKPOST = CONFIG.url(backendapi, "parkpost/");
    private static final String PROMOTED_MID = MID;
    private static String promotionTitle;
    private static Program result;

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

    private static final String EXAMPLE2 = "<NPO_gfxwrp>\n" +
        "    <ProductCode>3P1101MO_DODENLIE</ProductCode>\n" +
        "    <OrderCode>3P170111_WNL__DODEN_LI____</OrderCode>\n" +
        "    <PromotedProgramProductCode>POW_03455553</PromotedProgramProductCode>\n" +
        "    <Referrer></Referrer>\n" +
        "    <MXF_Name>12027626</MXF_Name>\n" +
        "    <ProgramTitle>Doden liegen niet</ProgramTitle>\n" +
        "    <EpisodeTitle>Doden liegen niet</EpisodeTitle>\n" +
        "    <PromoType>P</PromoType>\n" +
        "    <Broadcaster>WNL</Broadcaster>\n" +
        "    <TrailerTitle>Doden liegen niet</TrailerTitle>\n" +
        "    <SerieTitle>Doden liegen niet</SerieTitle>\n" +
        "    <FrameCount>750</FrameCount>\n" +
        "    <VideoFormat>HD</VideoFormat>\n" +
        "    <FirstTransmissionDate>2017-01-11T22:30:00+01:00</FirstTransmissionDate>\n" +
        "    <PlacingWindowStart>2017-01-10T06:00:00+01:00</PlacingWindowStart>\n" +
        "    <PlacingWindowEnd>2017-01-11T06:00:00+01:00</PlacingWindowEnd>\n" +
        "    <Files>\n" +
        "        <File>http://adaptive.npostreaming.nl/u/npo/promo/3P1101MO_DODENLIE/3P1101MO_DODENLIE.ismv</File>\n" +
        "        <File>http://adaptive.npostreaming.nl/u/npo/promo/3P1101MO_DODENLIE/3P1101MO_DODENLIE.ismc</File>\n" +
        "        <File>http://adaptive.npostreaming.nl/u/npo/promo/3P1101MO_DODENLIE/3P1101MO_DODENLIE.ism</File>\n" +
        "        <File format=\"MP4\" bitrate=\"1000000\">http://download.omroep.nl/npo/promo/3P1101MO_DODENLIE.mp4</File>\n" +
        "    </Files>\n" +
        "</NPO_gfxwrp>\n";
    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
        log.info("Testing with " + PARKPOST);
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
        String resultString =
            given()
                .auth().basic(
                CONFIG.configOption(parkpost, "user")
                    .orElse("vpro-cms"),
                CONFIG.requiredOption(parkpost, "password"))
                .contentType(ContentType.XML.withCharset(Charset.defaultCharset()))
                .accept(ContentType.XML)
                .body(promoEvent)
                .when()
                .   post(PARKPOST + "promo")
                .then()
                .   log().all()
                .   statusCode(AnyOf.anyOf(equalTo(202), equalTo(503)))
                .   extract().asString();
        result = JAXB.unmarshal(new StringReader(resultString), Program.class);
        assertThat(result.getType()).isEqualTo(ProgramType.PROMO);
        assertThat(result.getMemberOf().first().getMediaRef()).isEqualTo(PROMOTED_MID);
        log.info("Received {}", result);
    }


    @Test
    public void test002arrived() throws Exception {
        assumeTrue(result != null);
        assumeTrue(promotionTitle != null);
        MemberUpdate update = waitUntilNotNull(Duration.ofMinutes(5),
            () -> {
                MediaUpdateList<MemberUpdate> groupMembers = backend.getGroupMembers(PROMOTED_MID);
                return groupMembers
                    .stream()
                    .filter(mu -> mu.getMediaUpdate().getTitles().first().getTitle().equals(promotionTitle))
                    .findFirst()
                    .orElse(null);
            }
        );
        assertThat(update)
            .overridingErrorMessage("There is no member of " + PROMOTED_MID + " found with title " + promotionTitle)
            .isNotNull();

        SortedSet<RelationUpdate> relations = update.getMediaUpdate().getRelations();
        assertThat(relations
            .stream()
            .filter(ru -> ru.getType().equals("PROMO_PRODUCTCODE"))
            .findFirst()
            .map(RelationUpdate::getText)
            .orElse(null)
        ).isEqualTo(PRODUCTCODE);
        assertThat(update.getMediaUpdate().getTitles().first().getTitle()).isEqualTo(promotionTitle);
    }


    @Test
    public void test999cleanup() throws Exception {
        MediaUpdateList<MemberUpdate> promos = backend.getGroupMembers(PROMOTED_MID);
        int count = 0;
        for(MemberUpdate mu :promos) {
            if (mu.getMediaUpdate().getType().getMediaType() == MediaType.PROMO) {
                Program program = backend.getFullProgram(mu.getMediaUpdate().getMid());
                if (program.getCreationInstant().isBefore(Instant.now().minus(Duration.ofDays(3)))) {
                    log.info("Deleting {}", program);
                    backend.removeMember(PROMOTED_MID, mu.getMediaUpdate().getMid(), mu.getPosition());
                    count++;
                }
            }
        }
        log.info("Deleted {} promos for {}", count, PROMOTED_MID);
    }


}
