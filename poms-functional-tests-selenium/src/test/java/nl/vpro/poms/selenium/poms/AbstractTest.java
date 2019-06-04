package nl.vpro.poms.selenium.poms;

import io.github.bonigarcia.wdm.DriverManagerType;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.junit.*;
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
 *
 */
@RunWith(Parameterized.class)
@Slf4j
public abstract class AbstractTest {

    public static final Config CONFIG =
            new Config("npo-functional-tests.properties", "npo-browser-tests.properties");

    public static final String MID = "WO_VPRO_025057";

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.MINUTES);

    @Rule
    public TestMDC testMDC = new TestMDC();

    private final Browser browser;

    protected WebDriver driver;

    protected static Map<Browser, WebDriver> staticDrivers = new HashMap<>();

    protected boolean setupEach = true;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][]{
                        {new Browser(DriverManagerType.CHROME, "2.41")} // 2.41 corresponds with the chrome on jenkins.

                        , {new Browser(DriverManagerType.FIREFOX, null)}
                }
        );
    }

    protected AbstractTest(@Nonnull Browser browser) {
        this.browser = browser;
        this.setupEach = this.getClass().getAnnotation(FixMethodOrder.class) == null;
        if (!this.setupEach) {
            //log.info("Running with fixed method order, so keeping the driver between the tests");
        }
    }

    @Before
    public void setUp() {
        if (setupEach) {
            driver = browser.asWebDriver();
        } else {
            driver = staticDrivers.computeIfAbsent(browser, Browser::asWebDriver);
        }
    }

    @After
    public void tearDown() {
        if (setupEach) {
            driver.quit();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        for (WebDriver wd : staticDrivers.values()) {
            wd.close();
            wd.quit();
        }
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
