package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;

import nl.vpro.poms.selenium.util.WebDriverUtil;

public class HomePage extends AbstractPage {
    private static final By newBy = By.cssSelector(".header-link-new");

    public HomePage(WebDriverUtil driver) {
        super(driver);
    }

    public AddNewObjectOverlayPage clickNew() {
        webDriverUtil.waitAndClick(newBy);
        return new AddNewObjectOverlayPage(webDriverUtil);
    }
}
