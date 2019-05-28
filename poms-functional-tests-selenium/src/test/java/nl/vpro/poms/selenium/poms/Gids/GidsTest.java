package nl.vpro.poms.selenium.poms.Gids;

import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.DateFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GidsTest extends AbstractTest {

    // Variables
    private static final String sorteerDatumKanaal = "//tr/descendant::*[contains(@ng-if, 'sortDateScheduleEvent')]";

    @Before
    public void setup() {
        loginOmroepGebruiker();
    }

    @Test
    public void SPOMSGIDS1(){
        Search search = new Search(driver);
        search.clickWissen();
        //        Hier gebleven nog uitzoeken enterDates
        search.enterSorteerdatumDates(DateFactory.getToday(), "");
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        search.clickZoeken();
        search.getMultibleRowsAndCheckTextEquals(By.xpath("//tr/descendant::*[contains(@ng-if, 'sortDateScheduleEvent')]"), "(NED1)");
    }

    @Test
    public void SPOMSGIDS2() {
        Search search = new Search(driver);
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        search.enterSorteerdatumDates(DateFactory.getToday(),DateFactory.getToday());
        search.clickZoeken();
        search.getMultibleRowsAndCheckTextContains(By.cssSelector("[ng-switch-when='sortDate']"), DateFactory.getToday());
    }
    
    @Test
    public void SPOMSGIDS3(){
        Search search = new Search(driver);
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        search.removeSelectedOption("Nederland 1");
        setupSearchAndSort(search);
        search.getMultibleRowsAndCheckTextEquals(By.xpath(sorteerDatumKanaal), "(RAD1)");
        search.getAndCheckTimeBetweenTwoBroadcastsLessThenFourHours();
    }

    @Test
    public void SPOMSGIDS5(){
        Search search = new Search(driver);
        setupSearchAndSort(search);
        checkAndOpenMediaItem(search);
    }

    @Test
    public void SPOMSGIDS7(){
        Search search = new Search(driver);
        setupSearchAndSort(search);
        search.clickOnColum("Laatste uitzending");
        search.removeSelectedOption("Radio 1");
        search.selectOptionFromMenu("Zenders", "Nederland 3 & Zapp");
        assertThat(search.countRows()).isGreaterThanOrEqualTo(1);
//        Nog naar kijken !!! Matches geeft boolean terug aanpassen naar Substring !!!
        search.getAndCheckTimeBetweenTwoBroadcastsLessThenFourHours();
    }

    private void setupSearchAndSort(Search search) {
        search.selectOptionFromMenu("Zenders", "Radio 1");
        search.enterSorteerdatumDates(DateFactory.getToday(),DateFactory.getToday());
        search.clickZoeken();
        search.addOrRemoveColumn("Laatste uitzending");
        search.clickOnColum("Laatste uitzending");
    }

    private String checkAndOpenMediaItem(Search search) {
        String titleItem = search.getItemListTitle(1);
        MediaItemPage media = new MediaItemPage(driver);
        MediaItemPage itemPage = search.clickRow(0);
        assertThat(itemPage.getMediaItemTitle()).isEqualTo(titleItem);
        return titleItem;
    }


    @After
    public void logOut(){
        logout();
    }

}
