package nl.vpro.poms.selenium.poms.wijzigen;

import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class ChangeTest extends AbstractTest {

	public ChangeTest(WebDriverFactory.Browser browser) {
		super(browser);
	}
	private static final String randomTitel = randomAlphanumeric(15);
	private static final String randomDescription = randomAlphanumeric(35);

	@Test
	public void SPOMSEDITUPLOAD1() throws InterruptedException, URISyntaxException {
		login().speciaalVf();
		Search search = new Search(driver);
		search.selectOptionFromMenu("Omroepen", "VPRO");
		search.selectOptionFromMenu("MediaType", "Clip");
		search.clickOnColum("Sorteerdatum");
		MediaItemPage item = search.clickRow(0);
		item.moveToAfbeeldingen();
//		Letop nog klaar stuk vergeten. Input toevoegen en script aanpassen!
		item.clickOnAfbeeldingToevoegen();
		item.upLoadAfbeeldingMetNaam("afbeeldingtest.png");
		item.imageAddTitle(randomTitel);
		item.imageAddDescription(randomDescription);
		item.imageAddType("Afbeelding");
		item.clickButtonMaakAan();

//		logout();
	}
	
	@Test
	public void testWissen() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.selectOptionFromMenu("Omroepen", "VPRO");
		search.selectOptionFromMenu("MediaType", "Clip");
		search.clickWissen();
		logout();
	}
	
	@Test
	public void testNietBewerken() {
		login().speciaalVf();
		Search search = new Search(driver);
		search.clickWissen();
		search.selectOptionFromMenu("Omroepen", "NPO");
		search.selectOptionFromMenu("Criteria", "Mag schrijven");
		
		logout();
	}


}
