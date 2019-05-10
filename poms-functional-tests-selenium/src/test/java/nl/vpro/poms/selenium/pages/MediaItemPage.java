package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class MediaItemPage extends AbstractPage {

    public MediaItemPage(WebDriver driver) {
        super(driver);
    }

    public String getMID() {
        waitUtil.clickIfAvailable(By.xpath("//span[text() = 'Mid' and not(contains(@class, 'active'))]"));
        return waitUtil.getAtrributeFrom(By.cssSelector("[field='media.mid'] > input"), "value");
    }

    public String getURN() {
        waitUtil.clickIfAvailable(By.xpath("//span[text() = 'Urn' and not(contains(@class, 'active'))]"));
        return waitUtil.getAtrributeFrom(By.cssSelector("[field='media.urn'] > input"), "value");
    }

    public String getStatus() {
        return driver.findElement(By.xpath("//h2[text() = 'Status']/../*/p")).getText();
    }

    public String getMediaType() {
        return driver.findElement(By.xpath("//h2[text() = 'Type']/../p[contains(@class, 'editable')]")).getText();
    }

    public MediaItemPage changeMediaType(String mediaType) {
        waitUtil.waitAndClick(By.xpath("//h2[text() = 'Type']"));
        waitUtil.waitAndClick(By.cssSelector("input[value='" + mediaType.toUpperCase() + "']"));
        waitUtil.waitAndClick(By.cssSelector("button[type='submit']"));
        return this;
    }

    public String getSorteerDatumTijd() {
        waitUtil.waitForVisible(By.xpath("//h2[text() = 'Sorteerdatum']/../p"));
        return driver.findElement(By.xpath("//h2[text() = 'Sorteerdatum']/../p")).getText();
    }

    public void clickUitzendingen() {
        waitUtil.waitAndClick(By.xpath("//span[contains(text(), 'Uitzendingen')]"));
    }

    public void doubleClickUitzending(String date) {
        Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(By.xpath("//span[contains(text(), '" + date + "')]/../../../tr"))).doubleClick().perform();
    }

    public void changeStartDate(String date) {
        waitUtil.waitAndSendkeys(By.name("start"), date);
    }

    public void clickOpslaan() {
        waitUtil.waitAndClick(By.xpath("//button[contains(text(), 'Bewaar')]"));
    }
}
