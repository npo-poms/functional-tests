package nl.vpro.poms.selenium.poms.CMSSelector;

import nl.vpro.poms.selenium.pages.CMSMediaSelector;
import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import org.junit.Before;
import org.junit.Test;

public class CMSSelectorTest extends AbstractTest {

    @Test
    public void SPOMSCMSSELECTOR1(){
        loginOmroepGebruiker();
        logout();
        openCMSSelectorPage();
        CMSMediaSelector cms = new CMSMediaSelector(driver);
        cms.clickButtonSelect();
//        Hier gebleven geeft error uitzoeken
        cms.switchToPomsWindows();
        Search search = new Search(driver);
        search.clickRow(0);
    }


}
