package nl.vpro.poms.selenium.poms.npogebruiker;

import nl.vpro.poms.selenium.pages.AccountSettingsOverlayPage;
import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.types.AvType;
import nl.vpro.poms.selenium.util.types.MediaType;
import org.junit.Before;
import org.junit.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

public class NPOGebruikerTest extends AbstractTest {

    @Before
    public void setup() {
        loginNPOGebruiker();
    }

    @Test
    public void testNPOGebruiker() {
        Search search = new Search(driver);
        search.goToAccountInstellingen();

        AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(driver);

        assertThat(overlayPage.getRoles()).contains("MEDIA_SCHEDULE", "MEDIA_USER", "MEDIA_MIS");
    }

    @Test
    public void checkCurrentUser() {
        Search search = new Search(driver);
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
        Search search = new Search(driver);
        search.selectOptionFromMenu("MediaType", MediaType.UITZENDING.getType());
        search.selectOptionFromMenu("Criteria", "Mag schrijven");
        //        search.selectOptionFromMenu("Criteria", "Beschikbaar op streaming platform");
        search.selectOptionFromMenu("avType", AvType.VIDEO.getType());
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        String mediatype = search.clickRow(1)
                .changeMediaType(MediaType.CLIP.getType())
                .getMediaType();
        assertThat(mediatype).isEqualTo("Clip");
    }

    @Test
    public void testVervroegUitzending() {
        Search search = new Search(driver);
        search.selectOptionFromMenu("MediaType", MediaType.UITZENDING.getType());
        //search.selectOptionFromMenu("Criteria", "Mag schrijven");
        search.selectOptionFromMenu("Criteria", "Beschikbaar op streaming platform");
        search.selectOptionFromMenu("avType", AvType.VIDEO.getType());
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        MediaItemPage itemPage = search.clickRow(1);
        String sorteerDatumTijd = itemPage.getSorteerDatumTijd();

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
        itemPage.clickMenuItem("Algemeen");
        assertThat(itemPage.getSorteerDatumTijd()).isEqualTo(startDate);
        assertThat(itemPage.getUitzendigData()).isEqualTo("01-01-2020 13:00 (TV Gelderland)");

        // Hier gebleven
        // checkUitzendingGegevensKanaal(String kanaal);
        // checkUitzendingGegevensDatum(String datum)

    }
}
