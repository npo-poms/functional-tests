package nl.vpro.poms.selenium.poms.admin;

import javax.annotation.Nonnull;

import org.junit.Test;

import nl.vpro.poms.selenium.pages.AddNewObjectOverlayPage;
import nl.vpro.poms.selenium.pages.Login;
import nl.vpro.poms.selenium.pages.OmroepenOverlayPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.DateFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.types.AvType;
import nl.vpro.poms.selenium.util.types.MediaType;

public class AdminTest extends AbstractTest {


	public AdminTest(@Nonnull WebDriverFactory.Browser browser) {
		super(browser);
	}

	@Test
	public void testAdmin() {
		loginSpeciaalAdminGebruiker();

		Search search = new Search(webDriverUtil);
		search.clickNew();
		AddNewObjectOverlayPage addOverlay = new AddNewObjectOverlayPage(webDriverUtil);
		String title = "Test " + DateFactory.getNow();
		addOverlay.enterTitle(title);
		addOverlay.chooseMediaType(MediaType.CLIP);
		addOverlay.chooseAvType(AvType.VIDEO);
		addOverlay.chooseGenre("Jeugd");
		addOverlay.clickMaakAan();

		addOverlay.clickHerlaad();

		search.enterQuery(title);
		search.clickZoeken();
//		logout();
	}

	@Test
	public void testAddAndRemoveOmroep() {
		loginSpeciaalAdminGebruiker();
		Search search = new Search(webDriverUtil);
		search.clickAdminItem("omroepen");
		OmroepenOverlayPage overlay = new OmroepenOverlayPage(webDriverUtil);
		overlay.addOmroep("Test");
		overlay.close();

		search.clickAdminItem("omroepen");


//		logout();
	}

	private void loginSpeciaalAdminGebruiker() {
		Login login = new Login(webDriverUtil);
		login.gotoPage();
		String user = CONFIG.getProperty("AdminGebruiker.LOGIN");
		String password = CONFIG.getProperty("AdminGebruiker.PASSWORD");
		login.login(user, password);
	}

}
