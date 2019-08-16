package nl.vpro.poms.selenium.poms.wijzigen;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

@Ignore("TODO")
public class GeoLocationChangeTest extends AbstractTest {

	public GeoLocationChangeTest(WebDriverFactory.Browser browser) {
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
	public void clearGeoLocations()  {
		MediaItemPage item = new Search(webDriverUtil).searchAndOpenClip();
		item.moveToElement(By.id("geolocations"));

		// now clear it an add another one.

		// Then check wether submitting works.

	}


}
