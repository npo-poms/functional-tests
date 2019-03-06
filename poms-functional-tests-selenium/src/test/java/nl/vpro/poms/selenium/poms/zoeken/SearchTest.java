package nl.vpro.poms.selenium.poms.zoeken;

import static nl.vpro.poms.selenium.util.Config.CONFIG;

import java.util.Date;

import org.junit.Test;

import org.junit.Assert;
import nl.vpro.poms.selenium.pages.Login;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.DateFactory;

public class SearchTest extends AbstractTest {

	@Test
	public void testSearch() {
		loginSpeciaalVf();
		
		Search search = new Search(driver);
		search.enterQuery("klusjesmannen");
		Assert.assertTrue("Items 'Klusjesmannen' found",search.itemFound("De Klusjesmannen"));
		logout();
	 }
	
	@Test
	public void testSearchTwice() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("klusjesmannen");
		search.closeTab();
		search.enterQuery("klusjesmannen");
		Assert.assertTrue("Items 'Klusjesmannen' found",search.itemFound("De Klusjesmannen"));
		logout();
	}
	
	@Test
	public void testSearchWithAv() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("klusjesmannen");
		search.selectOptionFromMenu("avType", "Audio");
		search.clickZoeken();
		logout();
	}

	@Test
	public void testSearchWithClip() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("klusjesmannen");
		search.selectOptionFromMenu("MediaType", "Clip");
		search.clickZoeken();
		logout();
	}
	
	@Test
	public void testSearchWithOmroep() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.selectOptionFromMenu("Omroepen", "VARA");
		search.clickZoeken();
		logout();
	}
	
	@Test
	public void testSearchWithCriteria() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.selectOptionFromMenu("Criteria", "Niet op radio/tv");
		search.clickZoeken();
		logout();
	}
	
	@Test
	public void testWithSorteerData() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterSorteerdatumDates("01-01-2001", DateFactory.getToday()); 
		
		logout();
	}
	
	@Test
	public void testWithUitzendData() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterUitzenddatumDates("01-01-2001", DateFactory.getToday()); 
		
		logout();
	}
	
	@Test
	public void testWithGewijzigdData() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterGewijzigdDates("01-01-2001", DateFactory.getToday()); 
		
		logout();
	}
	
	@Test
	public void testWithAangemaaktData() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterUitzenddatumDates("01-01-2001", DateFactory.getToday()); 
		
		logout();
	}
	
	@Test
	public void testWithTags() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.enterQuery("zembla");
		search.enterTags("Dassenbos");
		
		logout();
	}
	
	@Test
	public void testWithTitleAndOmroep() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.selectOptionFromMenu("Omroepen", "AVRO");
		search.enterQuery("pluk");
		search.removeSelectedOption("AVRO");
//		logout();
	}
	
	private void loginSpeciaalVf() {
		Login login = new Login(driver);
		login.gotoPage();
		String user = CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
		String password = CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");
		login.login(user, password);
	}

	private void logout() {
		Search search = new Search(driver);
		search.logout();
	}

}
