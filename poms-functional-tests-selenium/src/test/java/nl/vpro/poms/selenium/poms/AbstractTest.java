package nl.vpro.poms.selenium.poms;

import nl.vpro.poms.selenium.pages.Login;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import static nl.vpro.poms.selenium.util.Config.CONFIG;

public abstract class AbstractTest {

    protected WebDriver driver;

    @Before
    public  void setUp() {
        driver = WebDriverFactory.getWebDriver(Browser.CHROME);
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    protected void loginSpeciaalVf() {
        Login login = new Login(driver);
        login.gotoPage();
        String user = CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
        String password = CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");
        login.login(user, password);
    }

    protected void loginNPOGebruiker() {
        Login login = new Login(driver);
        login.gotoPage();
        String user = CONFIG.getProperties().get("NPOGebruiker.LOGIN");
        String password = CONFIG.getProperties().get("NPOGebruiker.PASSWORD");
        login.login(user, password);
    }

    protected void loginOmroepGebruiker() {
        Login login = new Login(driver);
        login.gotoPage();
        String user = CONFIG.getProperties().get("OmroepGebruiker.LOGIN");
        String password = CONFIG.getProperties().get("OmroepGebruiker.PASSWORD");
        login.login(user, password);
    }


    protected void logout() {
        Search search = new Search(driver);
        search.logout();
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript("javascript:localStorage.clear(); javascript:sessionStorage.clear();");
    }
}
