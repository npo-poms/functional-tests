package nl.vpro.poms.selenium.poms.CMSSelector;

import nl.vpro.poms.selenium.pages.CMSMediaSelector;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractPomsTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;

public class CMSSelectorTest extends AbstractPomsTest {


    public CMSSelectorTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Before
    public void beforeLogin(){
        login().speciaalNPOGebruiker();
    }

    @Test
    @Ignore("Fails")
    public void SPOMSCMSSELECTOR1(){

        CMSMediaSelector cms = new CMSMediaSelector(webDriverUtil);
        cms.openUrlCmsMediaSelector();
        cms.clickButtonSelect();

//        Hier gebleven geeft error uitzoeken
        cms.switchToPomsWindows();
        //        cms.checkLoginTextBoxes();
        //        cms.checkIfNotDisplayedTables();
        //         cms.loginNPOGebruikerMediaSelector();

        Search search = new Search(webDriverUtil);
        search.addOrRemoveColumn("MID"); // FAILS
        String MID = search.getMidFromColum(1);
        search.clickRow(0);

        cms.switchToCMSWindow();
        assertThat(cms.getResult()).isEqualTo(MID);
    }

    @Test
    public void SPOMSCMSSELECTOR2(){
        Search search = openCmsPopupAddSearch();
        search.selectOptionFromMenu("MediaType","Uitzending");
        search.clickOnColum("Type");
        search.getMultibleRowsAndCheckTextEquals(By.xpath("//td[@class='column-type']/child::*/child::*"), "Uitzending");
        search.removeSelectedOption("Uitzending");

        search.selectOptionFromMenu("Omroepen","TROS");
        search.addOrRemoveColumn("Omroep");
        search.getMultibleRowsAndCheckTextEquals(By.xpath("//td[@class='column-broadcasters']/child::*/child::*"), "TROS");
        search.removeSelectedOption("TROS");
    }

    @Test
    public void SPOMSCMSSELECTOR3(){
        Search Search = openCmsPopupAddSearch();
        Search.addOrRemoveColumn("Gewijzigd op");
        assertThat(Search.checkIfColumnNameExcists("Gewijzigd op")).isEqualTo(true);
    }

    private Search openCmsPopupAddSearch() {
        CMSMediaSelector cms = new CMSMediaSelector(webDriverUtil);
        cms.openUrlCmsMediaSelector();
        cms.clickButtonSelect();
        cms.switchToPomsWindows();
//        cms.checkLoginTextBoxes();
//        cms.checkIfNotDisplayedTables();
//        cms.loginNPOGebruikerMediaSelector();

        Search search = new Search(webDriverUtil);
        return search;
    }


}
