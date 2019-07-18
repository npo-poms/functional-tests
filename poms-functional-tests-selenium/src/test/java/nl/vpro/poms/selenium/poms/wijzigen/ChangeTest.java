package nl.vpro.poms.selenium.poms.wijzigen;

import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class ChangeTest extends AbstractTest {

	public ChangeTest(WebDriverFactory.Browser browser) {
		super(browser);
	}
	private static final String randomTitel = randomAlphanumeric(15);
	private static final String randomDescription = randomAlphanumeric(35);

	@Before
	public void setup() {
		login().speciaalVf();
	}

	@After
	public void teardown() {
		logout();
	}

	@Test
	public void SPOMSEDITUPLOAD1() throws InterruptedException, URISyntaxException {
		Search search = new Search(driver);
		search.selectOptionFromMenu("Omroepen", "VPRO");
		search.selectOptionFromMenu("MediaType", "Clip");
		search.clickOnColum("Sorteerdatum");
		MediaItemPage item = search.clickRow(0);
		item.moveToAfbeeldingen();
		item.clickOnAfbeeldingToevoegen();
		item.upLoadAfbeeldingMetNaam("owl.jpeg");
		item.imageAddTitle(randomTitel);
		item.imageAddDescription(randomDescription);
		item.imageAddType("Afbeelding");
		item.clickButtonMaakAan();
	}
	
	@Test
	public void testWissen() {
		Search search = new Search(driver);
		search.selectOptionFromMenu("Omroepen", "VPRO");
		search.selectOptionFromMenu("MediaType", "Clip");
		search.clickWissen();
	}
	
	@Test
	public void testNietBewerken() {
		Search search = new Search(driver);
		search.clickWissen();
		search.selectOptionFromMenu("Omroepen", "NPO");
		search.selectOptionFromMenu("Criteria", "Mag schrijven");

	}


}
