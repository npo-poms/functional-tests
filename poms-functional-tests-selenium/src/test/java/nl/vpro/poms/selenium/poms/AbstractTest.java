package nl.vpro.poms.selenium.poms;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.paulhammant.ngwebdriver.NgWebDriver;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.pages.Login;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;

/**
 * TODO: This is more or less the same idea as {@link nl.vpro.poms.config.Webtest} I think one or the other must be dropped.
 */
@RunWith(Parameterized.class)

public abstract class AbstractTest {

    public static final Config CONFIG =
        new Config("npo-functional-tests.properties", "npo-browser-tests.properties");


    private final Browser browser;
    private final String version;

	protected WebDriver driver;



    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
            new Object[][]{
                {Browser.CHROME, null},
                {Browser.CHROME, "2.41"},
                {Browser.FIREFOX}}
                );
    }

    protected AbstractTest(@Nonnull Browser browser, @Nonnull String version) {
        this.browser = browser;
        this.version = version;
    }

	@Before
    public void setUp() {
        driver = WebDriverFactory.getWebDriver(browser, version);
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    protected Login login() {
        return new Login(driver);
    }

    protected void logout() {
		Search search = new Search(driver);
		search.logout();
	}
    protected void waitForAngularRequestsToFinish() {
        new NgWebDriver((JavascriptExecutor) driver).waitForAngularRequestsToFinish();
    }
}
