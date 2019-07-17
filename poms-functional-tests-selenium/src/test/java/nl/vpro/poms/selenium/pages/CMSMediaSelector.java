package nl.vpro.poms.selenium.pages;


import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import nl.vpro.poms.selenium.util.WebDriverUtil;

import static nl.vpro.poms.selenium.poms.AbstractTest.CONFIG;


/**
 * TODO: Doesn't work, should it nowt at least open the correct url?
 */

public class CMSMediaSelector extends AbstractPage {

    String mainCMSWindow;
    String pomsWindow;


    public CMSMediaSelector(WebDriverUtil driver) {
        super(driver);
    }


    public void clickButtonSelect() {
        webDriverUtil.waitAndClick(By.cssSelector("button#select"));
    }

    public void switchToPomsWindows() {
        mainCMSWindow = driver.getWindowHandle();

        driver.getWindowHandles().forEach(windowHandle -> {
            driver.switchTo().window(windowHandle);
        });
        webDriverUtil.getWait().until(ExpectedConditions.titleContains("POMS"));
    }

    public void switchToCMSWindow() {
        pomsWindow = driver.getWindowHandle();
        driver.getWindowHandles().forEach(windowHandle -> {
            driver.switchTo().window(windowHandle);
        });
        webDriverUtil.getWait().until(ExpectedConditions.titleContains("POMS Media selector"));
    }

    public void loginNPOGebruikerMediaSelector() {
        Login login = new Login(webDriverUtil);
        String user = CONFIG.getProperty("SpeciaalVfGebruiker.LOGIN");
        String password = CONFIG.getProperty("SpeciaalVfGebruiker.PASSWORD");
        login.login(user, password);
    }

    public void checkIfNotDisplayedTables() {
        webDriverUtil.waitForInvisible(By.cssSelector("tr"));
    }

    public void checkLoginTextBoxes() {
        webDriverUtil.waitForVisible(By.id("username"));
        webDriverUtil.waitForVisible(By.id("password"));
    }

    public String getResult() {
        return webDriverUtil.waitAndGetText(By.cssSelector("input#value"));
    }
}
