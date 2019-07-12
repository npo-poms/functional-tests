package nl.vpro.poms.selenium.poms.wijzigen;

import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.junit.Test;

import java.net.URISyntaxException;

public class ChangeTest extends AbstractTest {

	public ChangeTest(WebDriverFactory.Browser browser) {
		super(browser);
	}

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
		item.upLoadAfbeeldingMetNaam("afbeeldingtest.png");
		Thread.sleep(4000);
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
