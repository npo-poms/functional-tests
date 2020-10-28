package nl.vpro.poms.selenium.tests.poms.npogebruiker;

import org.assertj.core.api.SoftAssertions;
import org.junit.*;
import org.openqa.selenium.By;

import nl.vpro.domain.media.AVType;
import nl.vpro.domain.media.MediaType;
import nl.vpro.poms.selenium.tests.poms.AbstractPomsTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.pages.poms.*;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

public class NPOGebruikerTest extends AbstractPomsTest {

    public NPOGebruikerTest(WebDriverFactory.Browser browser) {
        super(browser);
    }


    @Before
    public void setup() {
        login().speciaalNPOGebruiker();
    }

    @Test
    public void testNPOGebruiker() {
        Search search = new Search(webDriverUtil);
        search.goToAccountInstellingen();

        AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(webDriverUtil);

        assertThat(overlayPage.getRoles()).contains("MEDIA_SCHEDULE", "MEDIA_USER", "MEDIA_MIS");
    }

    @Test

    public void checkCurrentUser() {
        Search search = new Search(webDriverUtil);
        assertThat(search.getCurrentUser()).isEqualTo("NPO Test");
    }

    /**
     * Klik op zoeken
     * Bij mediaType kies "Uitzending"
     * Bij Criteria kies "Mag schrijven" én "Beschikbaar op streaming platform"
     * Bij avType kies "Video"
     * Bij zenders kies "Nederland 2"
     * Dubbelklik op een willekeurig resultaat in het zoekresultaat
     * Klik op "Type Uitzending"
     * Selecteer in de dropdown "Clip"
     */
    @Test
    public void testWijzigUitzendingInClip() {
        Search search = getSearch();
        String mediatype = search.clickRow(0)
                .changeMediaType(MediaType.CLIP.getDisplayName())
                .getMediaType();
        assertThat(mediatype).isEqualTo("Clip");
    }

    @Test
    @Ignore("FAILS on DEV")
    public void testVervroegUitzending() {
        Search search = getSearch();
        MediaItemPage media = new MediaItemPage(webDriverUtil);
        MediaItemPage itemPage = search.clickRow(1);
        String title = itemPage.getMediaItemTitle();
        System.out.println(title);
        String sorteerDatumTijd = itemPage.getSorteerDatumTijd();
        itemPage.getUitzendigData();

        itemPage.clickMenuItem("Uitzendingen");
        itemPage.doubleClickUitzending(sorteerDatumTijd);

        /** @@@@@ Variabelen voor Test @@@@@@@ */

        String startDate = "01-01-2020 13:00";
        String endDate = "01-01-2020 16:00";
        String randomTitel = randomAlphanumeric(12);
        String randomAflevering = randomAlphanumeric(8);
        String randomTitelKort = randomAlphanumeric(6);
        String randomAfkorting = randomAlphanumeric(3);
        String randomWerkTitel = randomAlphanumeric(10);
        String randomOrgineleTitel = randomAlphanumeric(10);
        String randomBeschrijving = randomAlphanumeric(50);
        String randomKorteOmschrijving = randomAlphanumeric(50);
        String randomEenRegelBeschrijving = randomAlphanumeric(20);

        itemPage.changeKanaal("Nederland 1");
        itemPage.changeStartDate(startDate);
        itemPage.changeEndDate(endDate);
        itemPage.inputValueInInput("mainTitle", randomTitel);
        itemPage.inputValueInInput("subTitle", randomAflevering);
        itemPage.inputValueInInput("shortTitle", randomTitelKort);
        itemPage.inputValueInInput("abbreviationTitle", randomAfkorting);
        itemPage.inputValueInInput("workTitle", randomWerkTitel);
        itemPage.inputValueInInput("originalTitle", randomOrgineleTitel);
        itemPage.inputValueInInput("mainDescription", randomBeschrijving);
        itemPage.inputValueInInput("shortDescription", randomKorteOmschrijving);
        itemPage.inputValueInInput("kickerDescription", randomEenRegelBeschrijving);

        itemPage.clickOpslaan();
        itemPage.checkOfPopupUitzendingDissappear();
        itemPage.clickMenuItem("Algemeen");

        itemPage.refreshUntilUitzendingGegevensWithStartDate(startDate);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(itemPage.getSorteerDatumTijd()).isEqualTo(startDate);
        softly.assertThat(itemPage.getUitzendigData()).isEqualTo("" + startDate + " (Nederland 1)");

        media.moveToElement(By.xpath("//td/descendant::*[@ng-switch-when='channel']"));
        softly.assertThat(itemPage.getUitzendingGegevensEersteKanaal()).isEqualTo("Nederland 1");
        softly.assertThat(itemPage.getUitzendingGegevensEersteDatum()).isEqualTo(startDate);
        softly.assertThat(itemPage.getUitzendingTitel()).isEqualTo(randomTitel);
        softly.assertThat(itemPage.getUitzendingOmschrijving()).isEqualTo(randomBeschrijving);

        itemPage.doubleClickUitzending(startDate);
        softly.assertThat(itemPage.getValueForInInputWithName("mainTitle")).isEqualTo(randomTitel);
        softly.assertThat(itemPage.getValueForInInputWithName("subTitle")).isEqualTo(randomAflevering);
        softly.assertThat(itemPage.getValueForInInputWithName("shortTitle")).isEqualTo(randomTitelKort);
        softly.assertThat(itemPage.getValueForInInputWithName("abbreviationTitle")).isEqualTo(randomAfkorting);
        softly.assertThat(itemPage.getValueForInInputWithName("workTitle")).isEqualTo(randomWerkTitel);
        softly.assertThat(itemPage.getValueForInInputWithName("originalTitle")).isEqualTo(randomOrgineleTitel);
        softly.assertThat(itemPage.getValueForInInputWithName("mainDescription")).isEqualTo(randomBeschrijving);
        softly.assertThat(itemPage.getValueForInInputWithName("shortDescription")).isEqualTo(randomKorteOmschrijving);
        softly.assertThat(itemPage.getValueForInInputWithName("kickerDescription")).isEqualTo(randomEenRegelBeschrijving);
        itemPage.klikOpKnopMetNaam("Annuleer");

        // Verder gaan met @@@@@ controleren op title @@@@@
        softly.assertAll();

    }

    private Search getSearch() {
        Search search = new Search(webDriverUtil);
        //        search.clickNew();
        search.selectOptionFromMenu("MediaType", MediaType.BROADCAST.getDisplayName());
        search.selectOptionFromMenu("Criteria", "Mag schrijven");
        //        search.selectOptionFromMenu("Criteria", "Beschikbaar op streaming platform");
        search.selectOptionFromMenu("avType", AVType.VIDEO.getDisplayName());
        //search.selectOptionFromMenu("Zenders", "Nederland 1");
        return search;
    }
}
