package nl.vpro.poms.selenium.poms.zoeken;

import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.DateFactory;
import nl.vpro.poms.selenium.util.types.AvType;
import nl.vpro.poms.selenium.util.types.MediaType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SearchTest extends AbstractTest {

    @Before
    public void setup() {
        loginSpeciaalVf();
    }

    @After
    public void logout() {
        super.logout();
    }

    @Test
    public void testSearch() {
        Search search = new Search(driver);
        search.enterQuery("klusjesmannen");
        Assert.assertTrue("Items 'Klusjesmannen' found", search.itemFound("De Klusjesmannen"));
    }

    @Test
    public void testSearchTwice() {
        Search search = new Search(driver);
        search.enterQuery("klusjesmannen");
        search.closeTab();
        search.enterQuery("klusjesmannen");
        Assert.assertTrue("Items 'Klusjesmannen' found", search.itemFound("De Klusjesmannen"));
    }

    @Test
    public void testSearchWithAvType() {
        Search search = new Search(driver);
        search.enterQuery("klusjesmannen");
        search.selectOptionFromMenu("avType", AvType.AUDIO.getType());
        search.clickZoeken();
        Assert.assertTrue("Items 'A je to! 40 jaar Buurman en Buurman' found", search.itemFound("A je to! 40 jaar Buurman en Buurman"));
    }

    @Test
    public void testSearchWithClip() {
        Search search = new Search(driver);
        search.enterQuery("klusjesmannen");
        search.selectOptionFromMenu("avType", AvType.VIDEO.getType());
        search.selectOptionFromMenu("MediaType", MediaType.CLIP.getType());
        search.clickZoeken();
        Assert.assertTrue("Items 'Casa Jepie Makandra' found", search.itemFound("Casa Jepie Makandra"));
    }

    @Test
    public void testSearchWithOmroep() {
        Search search = new Search(driver);
        search.enterQuery("zembl");
        search.selectOptionFromMenu("Omroepen", "VARA");
        search.clickZoeken();
        Assert.assertTrue("Items 'Zembla' found", search.itemFound("Zembla"));
    }

    @Test
    public void testSearchWithCriteria() {
        Search search = new Search(driver);
        search.enterQuery("zembla");
        search.selectOptionFromMenu("Criteria", "Niet op radio/tv");
        search.clickZoeken();
        Assert.assertTrue("Items 'Zembla' found", search.itemFound("Zembla"));
    }

    @Test
    public void testWithSorteerData() {
        Search search = new Search(driver);
        search.enterQuery("zembla");
        search.enterSorteerdatumDates("01-01-2001", DateFactory.getToday());
        Assert.assertTrue("Items 'Zembla' found", search.itemFound("Zembla"));
    }

    @Test
    public void testWithUitzendData() {
        Search search = new Search(driver);
        search.enterQuery("zembla");
        search.enterUitzenddatumDates("01-01-2001", DateFactory.getToday());
        Assert.assertTrue("Items 'Zembla' found", search.itemFound("Zembla"));
    }

    @Test
    public void testWithGewijzigdData() {
        Search search = new Search(driver);
        search.enterQuery("zembla");
        search.enterGewijzigdDates("01-01-2001", DateFactory.getToday());
        Assert.assertTrue("Items 'Zembla' found", search.itemFound("Zembla"));
    }

    @Test
    public void testSearchWithAangemaaktData() {
        Search search = new Search(driver);
        search.enterQuery("zembla");
        search.enterUitzenddatumDates("01-01-2001", DateFactory.getToday());
        Assert.assertTrue("Items 'Zembla' found", search.itemFound("Zembla"));
    }

    @Test
    public void testSearchWithTags() {
        Search search = new Search(driver);
        search.enterQuery("Dokter Pop");
        search.enterTags("secret garden");
        search.clickZoeken();
        Assert.assertTrue("Items 'Dokter Pop: een liedje van Springsteen dat lijkt o' found", search.itemFound("Dokter Pop: een liedje van Springsteen dat lijkt o"));
    }

    @Test
    public void testSearchWithTitleAndOmroep() {
        Search search = new Search(driver);
        search.selectOptionFromMenu("Omroepen", "AVRO");
        search.enterQuery("pluk");
        Assert.assertTrue("Items 'Pluk van de Petteflet' found", search.itemFound("Pluk van de Petteflet"));
    }

    @Test
    public void testWithTitleSuggestion() {
        Search search = new Search(driver);
        search.enterQuery("pluk");
        List<WebElement> suggestions = search.getSuggestions("pluk");
        Assert.assertFalse(suggestions.isEmpty());
    }

    @Test
    public void testAddAndRemoveColumn() {
        Search search = new Search(driver);
        search.addOrRemoveColumn("MID");
        Assert.assertTrue(search.isColumnSelectorChecked("MID"));
        driver.navigate().refresh();
        Assert.assertTrue(search.isColumnSelectorChecked("MID"));
        search.addOrRemoveColumn("MID");
        Assert.assertFalse(search.isColumnSelectorChecked("MID"));
    }


}
