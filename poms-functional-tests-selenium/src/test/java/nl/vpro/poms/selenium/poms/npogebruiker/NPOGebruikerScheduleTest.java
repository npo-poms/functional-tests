package nl.vpro.poms.selenium.poms.npogebruiker;

import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import org.junit.Test;

public class NPOGebruikerScheduleTest extends AbstractTest {

    @Test
    public void checkUitzendtijden() {
        loginNPOGebruiker();
        Search search = new Search(driver);
        search.selectOptionFromMenu("Zenders", "Nederland 2");
        search.clickZoeken();

    }

    @Test
    public void checkUpdateUitzendtijden() {

    }

    @Test
    public void checkHeropenUitzending() {

    }
}
