package nl.vpro.poms.selenium.poms.maken;

import nl.vpro.poms.selenium.pages.*;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.DateFactory;
import nl.vpro.poms.selenium.util.types.AvType;
import nl.vpro.poms.selenium.util.types.MediaType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

//import org.openqa.selenium.support.ui.Sleeper;


public class CreateUserTest extends AbstractTest {

    @Before
    public void setup() {
        loginSpeciaalVf();
    }

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
        HomePage homepage = new HomePage(driver);
        AddNewObjectOverlayPage overlay = homepage.clickNew();
        overlay.chooseMediaType(MediaType.CLIP);

        Assert.assertFalse("Button 'Maak Aan' must be disabled", overlay.isEnabledMaakAan());
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
        HomePage homepage = new HomePage(driver);
        homepage.clickNew()
                .enterTitle("Clip" + DateFactory.getNow())
                .chooseMediaType(MediaType.CLIP)
                .chooseAvType(AvType.VIDEO)
                .chooseGenre("Jeugd")
                .selectPublicationPeriod(DateFactory.getToday(), DateFactory.getToday())
                .clickMaakAan();

        MediaItemPage itemPage = new MediaItemPage(driver);
        Assert.assertTrue(!itemPage.getMID().isEmpty());
        Assert.assertTrue(itemPage.getStatus().equals("Voor publicatie"));
        Assert.assertTrue(!itemPage.getURN().isEmpty());
    }

    @Test
    public void testWijzigStandaardOmroep() {
        Assert.fail("Bug gemeldt");
    }

    @Test
    public void testVoegTweeStandaardOmroepenToe() {
        Assert.fail("Bug gemeldt");
    }

    @Test
    public void testPersistStandaardOmroep() {
        Assert.fail("Bug gemeldt");
    }


}
