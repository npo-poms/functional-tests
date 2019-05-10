package nl.vpro.poms.selenium.poms.npogebruiker;

import nl.vpro.poms.selenium.pages.AccountSettingsOverlayPage;
import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.types.AvType;
import nl.vpro.poms.selenium.util.types.MediaType;
import org.junit.Before;
import org.junit.Test;

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
        search.selectOptionFromMenu("Criteria", "Beschikbaar op streaming platform");
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
        MediaItemPage itemPage = search.clickRow(0);
        String sorteerDatumTijd = itemPage.getSorteerDatumTijd();

        itemPage.clickUitzendingen();
        itemPage.doubleClickUitzending(sorteerDatumTijd);
        itemPage.changeStartDate("31-12-2015 20:55");
        itemPage.clickOpslaan();
    }


}
