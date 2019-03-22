package nl.vpro.poms.selenium.poms;

import io.github.bonigarcia.wdm.DriverManagerType;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.paulhammant.ngwebdriver.NgWebDriver;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.pages.PomsLogin;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;
import nl.vpro.rules.TestMDC;

/**
 */
@RunWith(Parameterized.class)

public abstract class AbstractTest {

    public static final Config CONFIG =
        new Config("npo-functional-tests.properties", "npo-browser-tests.properties");

    public static final String    MID                = "WO_VPRO_025057";


    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.MINUTES);


    @Rule
    public TestMDC testMDC = new TestMDC();



    private final Browser browser;

	protected WebDriver driver;



    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
            new Object[][]{
                {new Browser(DriverManagerType.CHROME, "2.41")}, // 2.41 corresponds with the chrome on jenkins.
                {new Browser(DriverManagerType.FIREFOX, null)}
            }
        );
    }

    protected AbstractTest(@Nonnull Browser browser) {
        this.browser = browser;
    }

	@Before
    public void setUp() {
        driver = browser.asWebDriver();
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    protected PomsLogin login(String url) {
        return new PomsLogin(url, driver);
    }

    protected PomsLogin login() {
        return login(null);
    }

    protected void logout() {
		Search search = new Search(driver);
		search.logout();
	}
    protected void waitForAngularRequestsToFinish() {
        new NgWebDriver((JavascriptExecutor) driver).waitForAngularRequestsToFinish();
    }
}
