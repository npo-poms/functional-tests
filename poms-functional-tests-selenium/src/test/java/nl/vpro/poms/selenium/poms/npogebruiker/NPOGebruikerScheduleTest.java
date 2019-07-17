package nl.vpro.poms.selenium.poms.npogebruiker;

import javax.annotation.Nonnull;

import org.junit.Test;

import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

public class NPOGebruikerScheduleTest extends AbstractTest {

    public NPOGebruikerScheduleTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Test
    public void checkUitzendtijden() {
        login().speciaalNPOGebruiker();
        Search search = new Search(webDriverUtil);
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
