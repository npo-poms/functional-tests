package nl.vpro.poms.selenium.pages;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import org.openqa.selenium.By;


@Log4j2
public class KeycloakLogin extends AbstractLogin {

    private static final By usernameBy = By.id("username");

    private static final By passwdBy = By.id("password");

    private static final By loginBy = By.cssSelector("input.btn-primary");

    public KeycloakLogin(@NonNull String url, WebDriverUtil util) {
        super(url, util);
    }

    @Override
    public void login(@NonNull String user, @NonNull String passwd) {
        webDriverUtil.waitAndSendkeys(usernameBy, user);
        webDriverUtil.waitAndSendkeys(passwdBy, passwd);
        webDriverUtil.waitAndClick(loginBy);
    }
}
