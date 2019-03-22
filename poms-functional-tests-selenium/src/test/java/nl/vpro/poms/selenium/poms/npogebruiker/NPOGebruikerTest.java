package nl.vpro.poms.selenium.poms.npogebruiker;


import org.junit.Assert;
import org.junit.Test;

import nl.vpro.poms.selenium.pages.AccountSettingsOverlayPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

public class NPOGebruikerTest extends AbstractTest {

	public NPOGebruikerTest(WebDriverFactory.Browser browser) {
		super(browser);
	}

	@Test
	public void testNPOGebruiker() {
		login().speciaalNPOGebruiker();
		
		Search search = new Search(driver);
    	search.goToAccountInstellingen();
    	
    	AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(driver);
    	boolean hasRole = overlayPage.hasRole("MEDIA_MIS");
    	Assert.assertTrue(hasRole);
    	overlayPage.close();
		logout();
	}
	
	@Test
	public void testEigenaar() {
		login().speciaalNPOGebruiker();
		
		Search search = new Search(driver);
    	search.goToAccountInstellingen();
    	
    	AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(driver);
    	overlayPage.checkOwner("BROADCASTER");
    	overlayPage.clickOpslaan();
    	
    	search.goToAccountInstellingen();
    	overlayPage.checkOwner("");
    	overlayPage.clickOpslaan();
    	
//    	overlayPage.close();
//		logout();
	}
	


}
