package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class AbstractOverlayPage extends AbstractPage {

    private static final By closeBy = By.cssSelector("div.modal-close-button");

    protected AbstractOverlayPage(WebDriver driver) {
        super(driver);
    }

    public void close() {
        waitUtil.waitAndClick(closeBy);
        waitUtil.waitForInvisible(By.cssSelector(".modal-dialog"));
    }
}
