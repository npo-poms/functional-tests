package nl.vpro.poms.selenium.poms.admin;

import org.junit.Test;

import nl.vpro.poms.selenium.pages.AddNewObjectOverlayPage;
import nl.vpro.poms.selenium.pages.OmroepenOverlayPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.DateFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory;


public class AdminTest extends AbstractTest {

	public AdminTest(WebDriverFactory.Browser browser) {
		super(browser);
	}

	@Test
	public void testAdmin() {
		login().speciaalAdminGebruiker();
		
		Search search = new Search(driver);
		search.clickNew();
		AddNewObjectOverlayPage addOverlay = new AddNewObjectOverlayPage(driver);
		String title = "Test " + DateFactory.getNow();
		addOverlay.enterTitle(title);
		addOverlay.chooseMediaType("Clip");
		addOverlay.chooseAvType("Video");
		addOverlay.chooseGenre("Jeugd");
		addOverlay.clickMaakAan();
		
		addOverlay.clickHerlaad();
		
		search.enterQuery(title);
		search.clickZoeken();
//		logout();
	}
	
	@Test
	public void testAddAndRemoveOmroep() {
		login().speciaalAdminGebruiker();
		Search search = new Search(driver);


		{
			// add
			search.clickAdminItem("omroepen");
			waitForAngularRequestsToFinish();
			OmroepenOverlayPage overlay = new OmroepenOverlayPage(driver);
			overlay.addOmroep("Test");
			overlay.close();
		}
		{ // delete again
			search.clickAdminItem("omroepen");

			OmroepenOverlayPage overlay = new OmroepenOverlayPage(driver);
			overlay.deleteOmroep("Test");
			overlay.close();
		}
		logout();
	}
	


}
