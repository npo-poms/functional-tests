package nl.vpro.poms.selenium;

import io.github.bonigarcia.wdm.DriverManagerType;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.pages.*;
import nl.vpro.poms.selenium.poms.pages.Search;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import nl.vpro.rules.TestMDC;
import nl.vpro.test.jupiter.AbortOnException;

/**
 *
 */
@Timeout(value = 5, unit = TimeUnit.MINUTES)
@ExtendWith({TestMDC.class, AbortOnException.class})
public abstract class AbstractTest {
    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractTest.class);
    protected org.slf4j.Logger log = LoggerFactory.getLogger(getClass());


    public static final Config CONFIG =
            new Config("npo-functional-tests.properties", "npo-browser-tests.properties");

    public static final String MID = "WO_VPRO_025057";



    protected final Browser browser;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected WebDriverUtil webDriverUtil;

    protected static Map<Browser, WebDriver> staticDrivers = new HashMap<>();
    protected static Map<Class, Boolean> loggedAboutSetupEach = new HashMap<>();
    protected boolean setupEach = true;


    public static Collection<Browser> browsers() {
        List<Browser> result = new ArrayList<>();
        List<String> browsers = Arrays.asList(CONFIG.getProperty("browsers").split("\\s*,\\s*"));
        if (browsers.contains("chrome")) {
            result.add(new Browser(DriverManagerType.CHROME, "2.41")); // 2.41 corresponds with the chrome on jenkins.
        }
        if (browsers.contains("firefox")) {
            result.add(new Browser(DriverManagerType.FIREFOX, null));
        }
        return result;
    }

    protected AbstractTest(@Nonnull Browser browser) {
        this.browser = browser;
        this.setupEach = this.getClass().getAnnotation(TestMethodOrder.class) == null;
        if (!this.setupEach && !loggedAboutSetupEach.getOrDefault(getClass(), false)) {
            log.info("\nRunning" + getClass() + " with fixed method order, so keeping the driver between the tests");
            loggedAboutSetupEach.put(getClass(), true);
        }
    }

    @BeforeEach
    public void setUp() {
        if (setupEach) {
            driver = createDriver(browser);
        } else {
            driver = staticDrivers.computeIfAbsent(browser, AbstractTest::createDriver);
        }
        webDriverUtil  = new WebDriverUtil(driver, log);
        wait = webDriverUtil.getWait();
    }

    private static WebDriver createDriver(Browser browser) {
        try {
            WebDriver driver = browser.asWebDriver();
            // The dimension of the browser should be big enough, (headless browser seem to be small!), otherwise test will keep waiting forever
            Dimension d = new Dimension(1200, 1000);
            driver.manage().window().setSize(d);
            return driver;
        } catch (Exception e) {
            LOG.error("Could not create driver for " + browser + ":" + e.getMessage(), e);
            throw e;
        }
    }

    @AfterEach
    public void tearDown() {
        if (setupEach) {
            if (driver != null) {
                /**
                 * In head mode we experience the issue with driver.close()
                 * org.openqa.selenium.NoSuchSessionException: Tried to run command without establishing a connection
                 */
                if (WebDriverFactory.headless) driver.close();
                driver.quit();
            }
        }
    }

    @AfterAll
    public static void tearDownClass(List<Exception> exceptions) {
        if (exceptions.isEmpty() || WebDriverFactory.headless) {
            for (WebDriver wd : staticDrivers.values()) {
                wd.quit();
            }
            staticDrivers.clear();
        } else {
            LOG.warn("Not closing browser because of test failures {}", exceptions);
        }
    }

    protected KeycloakLogin keycloakLogin(String url) {
        return new KeycloakLogin(url, webDriverUtil);
    }

    protected CasLogin casLogin(String url) {
        return new CasLogin(url, webDriverUtil);
    }
    protected abstract AbstractLogin login();

    protected void logout() {
        if (driver != null) {
            Search search = new Search(webDriverUtil);
            search.logout();
        } else {
            log.error("Cannot logout because no driver");
        }
    }

}
