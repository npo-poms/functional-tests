package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends AbstractPage {
    private static final By newBy = By.cssSelector(".header-link-new");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public AddNewObjectOverlayPage clickNew() {
        waitUtil.waitAndClick(newBy);
        return new AddNewObjectOverlayPage(driver);
    }
}
