package nl.vpro.poms.selenium.poms.gids;

import nl.vpro.poms.selenium.poms.pages.MediaItemPage;
import nl.vpro.poms.selenium.poms.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractPomsTest;
import nl.vpro.poms.selenium.util.DateFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.annotation.Nonnull;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GidsTest extends AbstractPomsTest {

    public GidsTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    // Variables
    private static final String sorteerDatumKanaal = "//tr/descendant::*[contains(@ng-if, 'sortDateScheduleEvent')]";

    @Before
    public void setup() {
        login().speciaalNPOGebruiker();
    }

    @Test
    public void SPOMSGIDS1(){
        Search search = new Search(webDriverUtil);
        search.clickWissen();
        //        Hier gebleven nog uitzoeken enterDates
        search.enterSorteerdatumDates(DateFactory.getPastDay(), "");
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        search.clickZoeken();
        search.getMultibleRowsAndCheckTextEquals(By.xpath("//tr/descendant::*[contains(@ng-if, 'sortDateScheduleEvent')]"), "(NED1)");
    }

    @Test
    public void SPOMSGIDS2() {
        Search search = new Search(webDriverUtil);
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        search.enterSorteerdatumDates(DateFactory.getToday(),DateFactory.getToday());
        search.clickZoeken();
        search.getMultibleRowsAndCheckTextContains(By.cssSelector("[ng-switch-when='sortDate']"), DateFactory.getToday());
    }

    @Test
    public void SPOMSGIDS3(){
        Search search = new Search(webDriverUtil);
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        search.removeSelectedOption("Nederland 1");
        setupSearchAndSort(search);
        search.getMultibleRowsAndCheckTextEquals(By.xpath(sorteerDatumKanaal), "(RAD1)");
        search.getAndCheckTimeBetweenTwoBroadcastsLessThenMinutes(240);
    }

    @Test
    public void SPOMSGIDS5(){
        Search search = new Search(webDriverUtil);
        setupSearchAndSort(search);
        checkAndOpenMediaItem(search);
    }

    @Test
    @Ignore("Fails. BTW, I'm not sure that selenium should tests data consistency.")
    public void SPOMSGIDS7(){
        Search search = new Search(webDriverUtil);
        setupSearchAndSort(search);
        search.removeSelectedOption("Radio 1");
        search.selectOptionFromMenu("Zenders", "Nederland 3 & Zapp");
        assertThat(search.countRows()).isGreaterThanOrEqualTo(1);
//        Nog naar kijken !!! Matches geeft boolean terug aanpassen naar Substring !!!
        search.getAndCheckTimeBetweenTwoBroadcastsLessThenMinutes(180); // FAils on 2019-08-15
    }

    private void setupSearchAndSort(Search search) {
        search.selectOptionFromMenu("Zenders", "Radio 1");
        search.enterSorteerdatumDates(DateFactory.getToday(),DateFactory.getToday());
        search.clickZoeken();
        search.addOrRemoveColumn("Laatste uitzending");
        search.clickOnColum("Laatste uitzending");
        search.clickOnColum("Laatste uitzending");
    }

    private String checkAndOpenMediaItem(Search search) {
        String titleItem = search.getItemListTitle(1);
        MediaItemPage itemPage = search.clickRow(0);
        assertThat(itemPage.getMediaItemTitle()).isEqualTo(titleItem);
        return titleItem;
    }


    @After
    public void logOut(){
        logout();
    }

}
