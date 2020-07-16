package nl.vpro.poms.selenium.pages;

import nl.vpro.poms.selenium.util.WebDriverUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CasLogin extends AbstractLogin {

    private static final By usernameBy = By.id("username");

    private static final By passwdBy = By.id("password");

    private static final By loginBy = By.cssSelector("input.btn-submit");


    public CasLogin(@NonNull String url, @NonNull WebDriverUtil driver) {
        super(url, driver);
    }
    @Override
    public void login(String user, String passwd) {
        Assume.assumeNotNull(user, passwd);
        webDriverUtil.waitAndSendkeys(usernameBy, user);
        webDriverUtil.waitAndSendkeys(passwdBy, passwd);
        WebElement login = driver.findElement(loginBy);
        login.click();
    }
}
