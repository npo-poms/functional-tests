package nl.vpro.poms.selenium.util.pages.poms;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;

import nl.vpro.poms.selenium.util.WebDriverUtil;

public class AccountSettingsOverlayPage extends AbstractOverlayPage {


    private static final By rolesBy = By.cssSelector("span[data-ng-repeat='role in editor.roles']");

    public AccountSettingsOverlayPage(WebDriverUtil driver) {
        super(driver);
    }

    public List<String> getRoles() {
        return driver.findElements(rolesBy).stream().map(el -> el.getText().replaceAll(",", "")).collect(Collectors.toList());
    }

}
