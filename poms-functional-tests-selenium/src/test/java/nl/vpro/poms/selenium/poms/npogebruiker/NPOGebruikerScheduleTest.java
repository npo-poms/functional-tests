package nl.vpro.poms.selenium.poms.npogebruiker;

import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import javax.annotation.Nonnull;

public class NPOGebruikerScheduleTest extends AbstractTest {

    public NPOGebruikerScheduleTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Test
    public void SPOMSNPOS1() {
//        Nog niet klaar verschil tussen handmatig en automatisch i.v.m. cookies!!!!
        login().speciaalNPOGebruiker();
        Search search = new Search(webDriverUtil);
        search.selectOptionFromMenu("Zenders", "Nederland 2");
        search.clickZoeken();
        SoftAssertions softly = new SoftAssertions();
        search.clickOnColum("Sorteerdatum");
        softly.assertThat(search.getItemListTitle(2)).contains("2Doc: Of Fathers and Sons");
        MediaItemPage itemPage = search.clickRow(2);
        softly.assertThat(itemPage.getMediaItemTitle()).contains("2Doc: Of Fathers and Sons");
        itemPage.moveToUitzendingen();
        softly.assertThat(itemPage.getUitzendingGegevensEersteKanaal()).contains("Nederland 2");
        softly.assertThat(itemPage.getUitzendingGegevensEersteDatum()).contains("21-01-2019 22:55");
        softly.assertAll();
    }

    @Test
    public void checkUpdateUitzendtijden() {

    }

    @Test
    public void checkHeropenUitzending() {

    }
}
