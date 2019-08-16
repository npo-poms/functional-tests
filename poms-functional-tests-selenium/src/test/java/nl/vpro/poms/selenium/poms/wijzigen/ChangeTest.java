package nl.vpro.poms.selenium.poms.wijzigen;

import nl.vpro.domain.image.ImageType;
import nl.vpro.domain.support.License;
import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

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
	private static final String randomTitel = randomAlphanumeric(15);
	private static final String randomDescription = randomAlphanumeric(35);

	@Test
	@Ignore("FAILS on DEV")
	public void SPOMSEDITUPLOAD1() throws URISyntaxException {

		MediaItemPage item = new Search(webDriverUtil).searchAndOpenClip();
		item.moveToAfbeeldingen();
		item.clickOnAfbeeldingToevoegen();
		item.upLoadAfbeeldingMetNaam("owl.jpeg");
		item.imageAddTitle(randomTitel);
		item.imageAddDescription(randomDescription);
		item.imageAddType(ImageType.PICTURE);
		item.imageLicentie(License.COPYRIGHTED); // doesn't work yet..

		item.clickButtonMaakAan();

		log.info("Ready creating an image");
		// FAILING because test does not fill license information
		// Shouldn't we add some actual tests here?

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
