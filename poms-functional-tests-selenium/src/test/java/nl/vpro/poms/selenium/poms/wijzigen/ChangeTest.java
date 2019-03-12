package nl.vpro.poms.selenium.poms.wijzigen;

import static nl.vpro.poms.selenium.util.Config.CONFIG;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import nl.vpro.poms.selenium.pages.Login;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;

public class ChangeTest extends AbstractTest {

	@Test
	public void testWijzig() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.selectOptionFromMenu("Omroepen", "VPRO");
		search.selectOptionFromMenu("MediaType", "Clip");
//		List<WebElement> tableRows = search.getTableRows();
//		for (WebElement row: tableRows) {
//			System.out.println("###" + row);
//		}
//		WebElement row = tableRows.get(0);
//		Actions actions = new Actions(driver);
//		actions.doubleClick(row);
		search.clickRow(0);
		search.scrollToAfbeeldingen();
//		logout();
	}
	
	@Test
	public void testWissen() {
		loginSpeciaalVf();
		Search search = new Search(driver);
		search.selectOptionFromMenu("Omroepen", "VPRO");
		search.selectOptionFromMenu("MediaType", "Clip");
		search.clickWissen();
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
