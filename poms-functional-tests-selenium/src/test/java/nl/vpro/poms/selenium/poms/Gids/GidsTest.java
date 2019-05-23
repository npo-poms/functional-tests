package nl.vpro.poms.selenium.poms.Gids;

import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.DateFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.PortableServer.ServantActivatorHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import java.util.ArrayList;
import java.util.List;
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
        search.selectOptionFromMenu("Zenders", "Radio 1");
        search.enterSorteerdatumDates(DateFactory.getToday(),DateFactory.getToday());
        search.clickZoeken();
        //        Dimension ScreenSize = driver.manage().window().getSize();
        //        driver.manage().window().maximize();
        search.addOrRemoveColumn("Laatste uitzending");
        search.doubleClickOnColum("Laatste uitzending");
        search.getMultibleRowsAndCheckTextEquals(By.xpath(sorteerDatumKanaal), "(RAD1)");
        search.getAndCheckTimeBetweenTwoBroadcastsLessThenFourHours();
        // Regel 233 omzetten naar Java 8
    }

    @After
    public void logOut(){
        logout();
    }

}
