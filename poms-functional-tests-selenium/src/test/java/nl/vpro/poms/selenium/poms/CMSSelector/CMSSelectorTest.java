package nl.vpro.poms.selenium.poms.CMSSelector;

import nl.vpro.poms.selenium.pages.CMSMediaSelector;
import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CMSSelectorTest extends AbstractTest {

    @Test
    public void SPOMSCMSSELECTOR1(){
        loginOmroepGebruiker();
        logout();
        openCMSSelectorPage();
        CMSMediaSelector cms = new CMSMediaSelector(driver);
        cms.clickButtonSelect();
        cms.switchToPomsWindows();
        cms.checkLoginTextBoxes();
        cms.checkIfNotDisplayedTables();
        cms.loginNPOGebruikerMediaSelector();
        Search search = new Search(driver);
        search.addOrRemoveColumn("MID");
        String MID = search.getMidFromColum(1);
        MediaItemPage itemPage = search.clickRow(0);
        cms.switchToCMSWindow();
// Hier gebleven en nog aanpassen, schakelen tussen popup en main scherm !!!
        assertThat(cms.getResult()).isEqualTo(MID);
    }


}
