package nl.vpro.poms.selenium.pages;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import nl.vpro.api.client.utils.Config;

import static nl.vpro.poms.selenium.util.Config.CONFIG;

public class Login extends AbstractPage {

    private static final By usernameBy = By.id("username");

    private static final By passwdBy = By.id("password");

    private static final By loginBy = By.xpath("//input[@name='submit' and @value='LOGIN']");

    private static final String URL = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl");

    public Login(WebDriver driver ) {
        super(driver);
    }
    public void gotoPage() {
//        log.info("poms {}", URL);
        driver.navigate().to(URL);
    }

    public void login(String user, String passwd) {
//    	log.info("Log in user {}", user);
        Assume.assumeNotNull(user, passwd);
        WebDriverWait wait = new WebDriverWait(driver, 30, 100);
        wait.until(ExpectedConditions.elementToBeClickable(usernameBy));
        WebElement usernameElement = driver.findElement(usernameBy);
        usernameElement.sendKeys(user);
        WebElement passwdElement = driver.findElement(passwdBy);
        passwdElement.sendKeys(passwd);
        WebElement login = driver.findElement(loginBy);
        login.click();
    }
}
