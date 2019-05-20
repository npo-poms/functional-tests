package nl.vpro.poms.selenium.poms.Gids;

import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.PortableServer.ServantActivatorHelper;
import org.openqa.selenium.By;

public class GidsTest extends AbstractTest {

    @Before
    public void setup() {
        loginOmroepGebruiker();
    }

    @Test
    public void SPOMSGIDS1(){
        Search search = new Search(driver);

        search.clickWissen();
        // Hier gebleven nog uitzoeken enterDates
        //          search.enterDates(By by, , String end);

    }

    @After
    public void logOut(){
        logout();
    }

}
