package nl.vpro.poms.selenium.poms.tests.maken;

import javax.annotation.Nonnull;

import org.junit.*;
import org.junit.jupiter.api.Assertions;

import nl.vpro.domain.media.AVType;
import nl.vpro.domain.media.MediaType;
import nl.vpro.poms.selenium.poms.tests.AbstractPomsTest;
import nl.vpro.poms.selenium.poms.pages.*;
import nl.vpro.poms.selenium.util.DateFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**

 */
public class CreateObjectsAsUserNormalUser extends AbstractPomsTest {

    public CreateObjectsAsUserNormalUser(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Before
    public void setup() {
        login().speciaalVf();
    }

    @Override
    @After
    public void logout() {
        super.logout();
    }

    /**
     * - Kies Nieuw en kies media Type ""Clip""
     * - Vul het formulier onvolledig in, er ontbreken nog verplichte(*) velden
     * - Druk op 'Maak aan'"
     */
    @Test
    public void testMaakAanDisabled() {
        HomePage homepage = new HomePage(webDriverUtil);
        AddNewObjectOverlayPage overlay = homepage.clickNew();
        overlay.chooseMediaType(MediaType.CLIP);

        assertFalse(overlay.isEnabledMaakAan(), "Button 'Maak Aan' must be disabled");
        overlay.close();
    }

    /**
     * "- Log in als omroep-gebruiker
     * - Kies Nieuw en kies media Type ""Clip""
     * - Vul het formulier volledig in, totdat er geen velden meer in te vullen zijn.
     * - Druk op 'Maak aan'"
     * <p>
     * "Het object wordt opgeslagen en krijgt:
     * - Een MID
     * - Een URN
     * - Een status 'Voor publicatie'
     * - De tab is bovenaan gearceerd
     * "
     */
    @Test
    public void testMaakNieuweClip() {
        HomePage homepage = new HomePage(webDriverUtil);
        homepage.clickNew()
                .enterTitle("Clip" + DateFactory.getNow())
                .chooseMediaType(MediaType.CLIP)
                .chooseAvType(AVType.VIDEO)
                .chooseGenre("Jeugd")
                .selectPublicationPeriod(DateFactory.getToday(), DateFactory.getToday())
                .clickMaakAan();

        MediaItemPage itemPage = new MediaItemPage(webDriverUtil);
        assertFalse(itemPage.getMID().isEmpty());
        assertEquals("Voor publicatie", itemPage.getStatus());
        assertFalse(itemPage.getURN().isEmpty());
    }

    @Test
    @Ignore("Fails")
    public void testWijzigStandaardOmroep() {
        Assertions.fail("Bug gemeld");
    }

    @Test
    @Ignore("Fails")
    public void testVoegTweeStandaardOmroepenToe() {
        Assertions.fail("Bug gemeld");
    }

    @Test
    @Ignore("Fails")
    public void testPersistStandaardOmroep() {
        Assert.fail("Bug gemeld");
    }


}
