package nl.vpro.poms.selenium.poms.tests.CMSSelector;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.*;
import org.openqa.selenium.By;

import nl.vpro.poms.selenium.poms.pages.CMSMediaSelector;
import nl.vpro.poms.selenium.poms.pages.Search;
import nl.vpro.poms.selenium.poms.tests.AbstractPomsTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class CMSSelectorTest extends AbstractPomsTest {

    private CMSMediaSelector cms;

    public CMSSelectorTest(WebDriverFactory.@NonNull Browser browser) {
        super(browser);
    }


    @Before
    public void beforeLogin() {
        cms = new CMSMediaSelector(webDriverUtil);
        // TODO, it is silly to login on poms first. It should work to login in the popup.
        login().speciaalNPOGebruiker();
    }

    @Test
    public void SPOMSCMSSELECTOR1() {

        cms.openUrlCmsMediaSelector();
        cms.clickButtonSelect();
        cms.switchToPomsWindows();

        Search search = new Search(webDriverUtil);
        search.addOrRemoveColumn("MID"); // FAILS
        String MID = search.getMidFromColum(1);
        search.clickRow(0);
        cms.switchToCMSWindow();
        assertThat(cms.getResult()).isEqualTo(MID);
    }

    @Test
    public void SPOMSCMSSELECTOR2() {
        Search search = openCmsPopupAddSearch();
        search.selectOptionFromMenu("MediaType", "Uitzending");
        search.clickOnColum("Type");
        search.getMultibleRowsAndCheckTextEquals(By.xpath("//td[@class='column-type']/child::*/child::*"), "Uitzending");
        search.removeSelectedOption("Uitzending");

        search.selectOptionFromMenu("Omroepen", "TROS");
        search.addOrRemoveColumn("Omroep");
        search.getMultibleRowsAndCheckTextEquals(By.xpath("//td[@class='column-broadcasters']/child::*/child::*"), "TROS");
        search.removeSelectedOption("TROS");
    }

    @Test
    public void SPOMSCMSSELECTOR3() {
        Search Search = openCmsPopupAddSearch();
        Search.addOrRemoveColumn("Gewijzigd op");
        assertThat(Search.checkIfColumnNameExists("Gewijzigd op")).isEqualTo(true);
    }

    private Search openCmsPopupAddSearch() {
        cms.openUrlCmsMediaSelector();
        cms.clickButtonSelect();
        cms.switchToPomsWindows();
        Search search = new Search(webDriverUtil);
        return search;
    }
}
