package nl.vpro.poms.selenium.util;

import com.paulhammant.ngwebdriver.NgWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDriverUtil {

    private WebDriver driver;
    private WebDriverWait wait;
    private NgWebDriver ngWait;

    public WebDriverUtil(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 15, 250);
        this.ngWait = new NgWebDriver((JavascriptExecutor) driver);
    }

    public void waitAndClick(By by) {
//        ngWait.waitForAngularRequestsToFinish();
        wait.until(ExpectedConditions.elementToBeClickable(by));
        driver.findElement(by).click();
        ngWait.waitForAngularRequestsToFinish();
    }

    public void waitAndSendkeys(By by, String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        driver.findElement(by).clear();
        driver.findElement(by).sendKeys(text);
    }

    public void waitForVisible(By by) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void waitForInvisible(By by) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
        //Sleeper.sleep(250);
    }

    public void waitForTextToBePresent(By by, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(by, text));

    }

    public boolean isElementPresent(By by) {
        return driver.findElements(by).size() > 0;
    }

    public String getAtrributeFrom(By by, String attribute) {
        this.waitForVisible(by);
        return driver.findElement(by).getAttribute(attribute);
    }


    public void clickIfAvailable(By xpath) {
        driver.findElements(xpath).stream().filter(WebElement::isDisplayed)
                .forEach(WebElement::click);
    }
}
