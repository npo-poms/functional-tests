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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GidsTest extends AbstractTest {

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

        //        assertThat(search.getSearchRowSorteerDatumKanaal()).isEqualTo("(NED1)");
    }

    @Test
    public void SPOMSGIDS2(){
        Search search = new Search(driver);
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        search.enterSorteerdatumDates(DateFactory.getToday(),DateFactory.getToday());
        search.clickZoeken();
        assertThat(search.getSearchRowSorteerDatumKanaal(DateFactory.getToday())).isEqualTo("(NED1)");
    }

    public void SPOMSGIDS3(){
        Search search = new Search(driver);
        search.selectOptionFromMenu("Zenders", "Nederland 1");
        search.enterSorteerdatumDates(DateFactory.getToday(),DateFactory.getToday());
        search.clickZoeken();
        assertThat(search.getSearchRowSorteerDatumKanaal(DateFactory.getToday())).isEqualTo("(NED1)");
    }



    @After
    public void logOut(){
        logout();
    }

}
