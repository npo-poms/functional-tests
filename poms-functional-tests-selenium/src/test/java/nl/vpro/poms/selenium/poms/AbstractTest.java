package nl.vpro.poms.selenium.poms;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.paulhammant.ngwebdriver.NgWebDriver;

import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;

/**
 * TODO: This is more or less the same idea as {@link nl.vpro.poms.config.Webtest} I think one or the other must be dropped.
 */
public abstract class AbstractTest {

	protected WebDriver driver;

	@Before
    public void setUp() {
        driver = WebDriverFactory.getWebDriver(Browser.CHROME);
    }

    @After
    public void tearDown() {
        driver.quit();
    }
    
    protected void logout() {
		Search search = new Search(driver);
		search.logout();
	}
    protected void waitForAngularRequestsToFinish() {
        new NgWebDriver((JavascriptExecutor) driver).waitForAngularRequestsToFinish();
    }
}
