package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;

import nl.vpro.poms.selenium.util.WebDriverUtil;

public abstract class AbstractOverlayPage extends AbstractPage {

    private static final By closeBy = By.cssSelector("div.modal-close-button");

    protected AbstractOverlayPage(WebDriverUtil driver) {
        super(driver);
    }

    public void close() {
        webDriverUtil.waitAndClick(closeBy);
        webDriverUtil.waitForInvisible(By.cssSelector(".modal-dialog"));
    }
}
