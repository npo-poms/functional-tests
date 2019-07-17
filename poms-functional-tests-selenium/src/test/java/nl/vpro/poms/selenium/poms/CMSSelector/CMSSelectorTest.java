package nl.vpro.poms.selenium.poms.CMSSelector;

import javax.annotation.Nonnull;

import org.junit.Test;

import nl.vpro.poms.selenium.pages.CMSMediaSelector;
import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class CMSSelectorTest extends AbstractTest {


    public CMSSelectorTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Test
    public void SPOMSCMSSELECTOR1(){
        login().speciaalNPOGebruiker();
        CMSMediaSelector cms = new CMSMediaSelector(webDriverUtil);
        cms.clickButtonSelect();

//        Hier gebleven geeft error uitzoeken
        cms.switchToPomsWindows();
        Search search = new Search(webDriverUtil);
        search.clickRow(0);

        cms.switchToPomsWindows();
        cms.checkLoginTextBoxes();
        cms.checkIfNotDisplayedTables();
        cms.loginNPOGebruikerMediaSelector();
        search.addOrRemoveColumn("MID");
        String MID = search.getMidFromColum(1);
        MediaItemPage itemPage = search.clickRow(0);
        cms.switchToCMSWindow();
// Hier gebleven en nog aanpassen, schakelen tussen popup en main scherm !!!
        assertThat(cms.getResult()).isEqualTo(MID);

    }


}
