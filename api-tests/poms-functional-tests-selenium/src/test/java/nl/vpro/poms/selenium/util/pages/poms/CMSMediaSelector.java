package nl.vpro.poms.selenium.util.pages.poms;


import lombok.extern.log4j.Log4j2;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.util.pages.AbstractPage;
import nl.vpro.poms.selenium.util.WebDriverUtil;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static nl.vpro.poms.selenium.tests.poms.AbstractPomsTest.CONFIG;
import static org.assertj.core.api.Fail.fail;


/**
 */

@Log4j2
public class CMSMediaSelector extends AbstractPage {
    final String url = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl") + "/CMSSelector/example/";
    WebDriverWait wait;

    public CMSMediaSelector(WebDriverUtil driver) {
        super(driver);
        this.wait = new WebDriverWait(webDriverUtil.getDriver(),
            ofSeconds(15), ofMillis(250));
    }

    public void openUrlCmsMediaSelector() {
        driver.navigate().to(url);
    }

    public void clickButtonSelect() {
        WebDriverWait wait = new WebDriverWait(driver, ofSeconds(15), ofMillis(25));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button#select")));
        webDriverUtil.waitAndClick(By.cssSelector("button#select"));
    }

    public void switchToPomsWindows() {
        webDriverUtil.switchToWindowWithTitle("POMS");
        webDriverUtil.w().until(ExpectedConditions.titleContains("POMS"));
    }

    public void switchToCMSWindow() {
        webDriverUtil.switchToWindowWithTitle("POMS Media selector");
        wait.until(ExpectedConditions.titleContains("POMS Media selector"));
    }

    public String getResult() {
        webDriverUtil.waitForVisible(By.cssSelector("input#value"));
        Object value = ((JavascriptExecutor) driver).executeScript("return document.querySelector('input#value').value");
        String returnValue = "";
        if (value instanceof String) {
            returnValue = value.toString();
        } else {
            fail("Error in the javascript on the page");
            log.error("Error in the javascript on the page");
        }
        return returnValue;
    }

}
