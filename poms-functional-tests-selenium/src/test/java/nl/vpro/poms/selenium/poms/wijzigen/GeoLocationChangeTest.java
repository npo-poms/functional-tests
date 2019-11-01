package nl.vpro.poms.selenium.poms.wijzigen;

import java.util.List;

import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import nl.vpro.poms.selenium.poms.AbstractPomsTest;
import nl.vpro.poms.selenium.poms.pages.MediaItemPage;
import nl.vpro.poms.selenium.poms.pages.Search;
import nl.vpro.poms.selenium.util.WebDriverFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.openqa.selenium.By.id;

@FixMethodOrder(NAME_ASCENDING)
public class GeoLocationChangeTest extends AbstractPomsTest {

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
	public void test01addGeoLocations() {
		MediaItemPage item = new Search(webDriverUtil).searchAndOpenClip();
		item.moveToElement(By.id("geolocations"));

		WebElement addButton = driver.findElement(By.id("addGeoLocation"));
		addButton.click();
		webDriverUtil.waitForAngularRequestsToFinish();
		driver.switchTo().frame("modal_iframe");
		waitUntilSuggestionReady();
		// first suggestion should be it
		driver.findElement(By.id("searchValue")).sendKeys("Amsterdam");
		webDriverUtil.waitForAngularRequestsToFinish();
		wait.until(webDriver -> driver.findElement(By.xpath("//ul/li/a[contains(@class, 'status-approved')]")));
		WebElement resultItem = driver.findElements(By.xpath("//ul/li/a[contains(@class, 'status-approved')]")).get(0);
		resultItem.click();
		webDriverUtil.click("submit");
		driver.switchTo().defaultContent();
		webDriverUtil.waitForAngularRequestsToFinish();
		// Then check whether submitting works.
 		List<WebElement> newRemoveButtons = driver.findElement(By.xpath("//poms-geolocations")).findElements(By.className("media-concept-remove"));
 		//This test is flaky, some records will not start with 0 geolocations.
		assertThat(newRemoveButtons.size()).isGreaterThanOrEqualTo(1);

	}

	private void waitUntilSuggestionReady() {
		wait.until(webDriver ->
				! webDriver.findElement(id("searchValue")).getAttribute("class").contains("waiting"));
	}

}
