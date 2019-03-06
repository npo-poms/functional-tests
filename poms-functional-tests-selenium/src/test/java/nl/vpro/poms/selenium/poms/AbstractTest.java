package nl.vpro.poms.selenium.poms;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;

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
}
