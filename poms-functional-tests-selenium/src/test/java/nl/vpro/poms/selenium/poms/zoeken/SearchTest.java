package nl.vpro.poms.selenium.poms.zoeken;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.DateFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory;

public class SearchTest extends AbstractTest {

	public SearchTest(WebDriverFactory.Browser browser, String version) {
		super(browser, version);
	}

	@Test
	public void testSearch() {
		login().speciaalVf();
		
		Search search = new Search(driver);
		search.enterQuery("klusjesmannen");
		Assert.assertTrue("Items 'Klusjesmannen' found",search.itemFound("De Klusjesmannen"));
		logout();
	 }
	
	@Test
	public void testSearchTwice() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("klusjesmannen");
		search.closeTab();
		search.enterQuery("klusjesmannen");
		Assert.assertTrue("Items 'Klusjesmannen' found",search.itemFound("De Klusjesmannen"));
		logout();
	}
	
	@Test
	public void testSearchWithAv() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("klusjesmannen");
		search.selectOptionFromMenu("avType", "Audio");
		search.clickZoeken();
		logout();
	}

	@Test
	public void testSearchWithClip() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("klusjesmannen");
		search.selectOptionFromMenu("MediaType", "Clip");
		search.clickZoeken();
		logout();
	}
	
	@Test
	public void testSearchWithOmroep() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.selectOptionFromMenu("Omroepen", "VARA");
		search.clickZoeken();
		logout();
	}
	
	@Test
	public void testSearchWithCriteria() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.selectOptionFromMenu("Criteria", "Niet op radio/tv");
		search.clickZoeken();
		logout();
	}
	
	@Test
	public void testWithSorteerData() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterSorteerdatumDates("01-01-2001", DateFactory.getToday()); 
		
		logout();
	}
	
	@Test
	public void testWithUitzendData() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterUitzenddatumDates("01-01-2001", DateFactory.getToday()); 
		
		logout();
	}
	
	@Test
	public void testWithGewijzigdData() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterGewijzigdDates("01-01-2001", DateFactory.getToday()); 
		
		logout();
	}
	
	@Test
	public void testWithAangemaaktData() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterUitzenddatumDates("01-01-2001", DateFactory.getToday()); 
		
		logout();
	}
	
	@Test
	public void testWithTags() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterTags("Dassenbos");
		
		logout();
	}
	
	@Test
	public void testWithTitleAndOmroep() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.selectOptionFromMenu("Omroepen", "AVRO");
		search.enterQuery("pluk");
		search.removeSelectedOption("AVRO");
		logout();
	}
	
	@Test
	public void testWithTitleSuggestion() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.enterQuery("pluk");
		List<WebElement> suggestions = search.getSuggestions("pluk");
		Assert.assertFalse(suggestions.isEmpty());
		logout();
	}
	
	@Test
	public void testAddAndRemoveColumn() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.addOrRemoveColumn("MID");
		Assert.assertTrue(search.isColumnSelectorChecked("MID"));
		logout();
		login().speciaalVf();
		Assert.assertTrue(search.isColumnSelectorChecked("MID"));
		search.addOrRemoveColumn("MID");
		Assert.assertFalse(search.isColumnSelectorChecked("MID"));
		logout();
	}
	
	 

}
