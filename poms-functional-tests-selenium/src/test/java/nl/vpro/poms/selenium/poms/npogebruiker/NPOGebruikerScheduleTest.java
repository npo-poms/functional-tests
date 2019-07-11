package nl.vpro.poms.selenium.poms.npogebruiker;

import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class NPOGebruikerScheduleTest extends AbstractTest {

    public NPOGebruikerScheduleTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Test
    public void SPOMSNPOS1() {
//        Nog niet klaar verschil tussen handmatig en automatisch i.v.m. cookies!!!!
        login().speciaalNPOGebruiker();
        Search search = new Search(driver);
        search.selectOptionFromMenu("Zenders", "Nederland 2");
        search.clickZoeken();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(search.getItemListTitle(0)).contains("Een bitterzoete verleiding");
        MediaItemPage itemPage = search.clickRow(0);
        itemPage.waitAndCheckMediaItemSubTitle("Een bitterzoete verleiding");
        itemPage.moveToElementXpath("//*[@class='media-section-title'  and contains(text(), 'Uitzendingen')]");
        softly.assertThat(itemPage.getUitzendingGegevensKanaal()).contains("Nederland 2");
        softly.assertThat(itemPage.getUitzendingGegevensDatum()).contains("24-03-2008 22:10");
        softly.assertAll();
    }

    @Test
    public void checkUpdateUitzendtijden() {

    }

    @Test
    public void checkHeropenUitzending() {

    }
}
