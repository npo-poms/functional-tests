package nl.vpro.poms.selenium.util;

import lombok.Getter;

import java.util.function.Function;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import com.paulhammant.ngwebdriver.NgWebDriver;

@Getter
public class WebDriverUtil {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final NgWebDriver ngWait;
    private final Logger log;


    public WebDriverUtil(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 15, 250);
        this.wait.ignoring(NoSuchElementException.class);
        this.ngWait = new NgWebDriver((JavascriptExecutor) driver);
        this.log = logger;
    }

    public void waitAndClick(By by) {
        ngWait.waitForAngularRequestsToFinish();
        WebElement element = driver.findElement(by);
        scrollIntoView(element);
        wait.until(ExpectedConditions.elementToBeClickable(element));
        driver.findElements(by)
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst().get().click();
        ngWait.waitForAngularRequestsToFinish();
    }

    public void waitAndSendkeys(By by, String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        driver.findElement(by).clear();
        driver.findElements(by)
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst().get().sendKeys(text);
        //driver.findElement(by).sendKeys(text);
    }

    public void waitForVisible(By by) {
        ngWait.waitForAngularRequestsToFinish();
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void waitForInvisible(By by) {
        ngWait.waitForAngularRequestsToFinish();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
        ngWait.waitForAngularRequestsToFinish();
    }

    public void waitForTextToBePresent(By by, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(by, text));

    }

    public boolean isElementPresent(By by) {
        return driver.findElements(by).size() > 0;
    }

    public String getAtrributeFrom(By by, String attribute) {
        this.waitForVisible(by);
        return driver.findElements(by)
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst().get().getAttribute(attribute);
    }

    public String waitAndGetText(By by) {
        this.waitForVisible(by);
        return driver.findElements(by)
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst().get().getText();
    }

    public void clickIfAvailable(By xpath) {
        driver.findElements(xpath).stream().filter(WebElement::isDisplayed)
                .forEach(WebElement::click);
    }

    public void refreshUntilVisible(String elementxpath) {
        wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                driver.navigate().refresh();
                return driver.findElement(By.xpath(elementxpath));
            }
        });
    }

    public void waitForAngularRequestsToFinish() {
        ngWait.waitForAngularRequestsToFinish();
    }

    public void scrollIntoView(WebElement element) {
        log.info("moving to {}", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        /*Actions actions = new Actions(driver);
        actions.moveToElement(element);
        actions.perform();*/
        waitForAngularRequestsToFinish();
    }
}
