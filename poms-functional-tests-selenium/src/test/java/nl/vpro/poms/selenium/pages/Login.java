package nl.vpro.poms.selenium.pages;

import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.util.WebDriverUtil;

import static nl.vpro.poms.selenium.util.Config.CONFIG;

public class Login extends AbstractPage {

    private static final By usernameBy = By.id("username");

    private static final By passwdBy = By.id("password");

    private static final By loginBy = By.cssSelector("input.btn-submit");

    private static final String URL = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl");

    public Login(WebDriverUtil driver) {
        super(driver);
    }
    public void gotoPage() {
//        log.info("poms {}", URL);
        driver.navigate().to(URL);
    }

    public void login(String user, String passwd) {
//  log.info("Log in user {}", user);
        Assume.assumeNotNull(user, passwd);
        webDriverUtil.waitAndSendkeys(usernameBy, user);
        webDriverUtil.waitAndSendkeys(passwdBy, passwd);
        WebElement login = driver.findElement(loginBy);
        login.click();
    }
}
