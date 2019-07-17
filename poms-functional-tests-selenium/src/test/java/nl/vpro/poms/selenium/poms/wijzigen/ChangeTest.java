package nl.vpro.poms.selenium.poms.wijzigen;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

public class ChangeTest extends AbstractTest {

	public ChangeTest(WebDriverFactory.Browser browser) {
		super(browser);
	}

	@Before
	public void firstLogin() {
		login().speciaalVf();
	}
	@After
	public void afterwardsLogout() {
		logout();
	}

	@Test
	public void testWijzig() {
		Search search = new Search(webDriverUtil);
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
		webDriverUtil.waitForAngularRequestsToFinish();
		search.scrollToAfbeeldingen();
	}

	@Test
	public void testWissen() {
		Search search = new Search(webDriverUtil);
		search.selectOptionFromMenu("Omroepen", "VPRO");
		search.selectOptionFromMenu("MediaType", "Clip");
		search.clickWissen();
	}

	@Test
	public void testNietBewerken() {
		Search search = new Search(webDriverUtil);
		search.clickWissen();
		search.selectOptionFromMenu("Omroepen", "NPO");
		search.selectOptionFromMenu("Criteria", "Mag schrijven");
	}


}
