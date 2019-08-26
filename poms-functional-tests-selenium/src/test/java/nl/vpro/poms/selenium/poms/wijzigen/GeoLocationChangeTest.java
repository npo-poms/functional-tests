package nl.vpro.poms.selenium.poms.wijzigen;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import nl.vpro.poms.selenium.pages.MediaItemPage;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.By.*;

@Ignore("TODO")
public class GeoLocationChangeTest extends AbstractTest {

	public GeoLocationChangeTest(WebDriverFactory.Browser browser) {
		super(browser);
	}
	private static final String POPUP_TITLE = "GTAA";

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
		String poms_title = driver.getTitle();
		item.moveToElement(By.id("geolocations"));

		//Remove eventual geolocations
//		List<WebElement> removeButtons = driver.findElement(By.xpath("//poms-geolocations")).findElements(By.className("media-concept-remove"));
//		for(WebElement button: removeButtons){
//			button.click();
//			webDriverUtil.waitForAngularRequestsToFinish();
//		}
//		assertThat(removeButtons.size()).isEqualTo(0);

		WebElement addButton = driver.findElement(By.id("addGeoLocation"));
		addButton.click();
		webDriverUtil.waitForAngularRequestsToFinish();
		webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);
		waitUntilSuggestionReady();
		// first suggestion should be it
		WebElement webElement = driver.findElements(By.xpath("//ul/li")).get(0).findElement(tagName("a"));
		webElement.click();
		webDriverUtil.click("submit");
		webDriverUtil.waitForWindowToClose();
		webDriverUtil.switchToWindowWithTitle(poms_title);

		// Then check wether submitting works.
 		List<WebElement> newRemoveButtons = driver.findElement(By.xpath("//poms-geolocations")).findElements(By.className("media-concept-remove"));
		assertThat(newRemoveButtons.size()).isEqualTo(1);

	}

	private void waitUntilSuggestionReady() {
		wait.until(webDriver ->
				! webDriver.findElement(id("searchValue")).getAttribute("class").contains("waiting"));
	}

}
