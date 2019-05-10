package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

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

    public String getUitzendigData(){
        waitUtil.waitForVisible(By.cssSelector("[title='bekijk alle uitzenddata']"));
        return driver.findElement(By.cssSelector("bekijk alle uitzenddata")).getText();
    }

    public void clickMenuItem(String menuItem) {
        waitUtil.waitAndClick(By.xpath("(//span[contains(text(), '" + menuItem +"')])[last()]"));
        waitUtil.waitForVisible(By.xpath("//li[@class='media-item-navigation-link active']/descendant::*[contains(text(), '" + menuItem + "')]"));
    }

    public void clickAlgemeen() {
        waitUtil.waitAndClick(By.xpath("(//span[contains(text(), 'Uitzendingen')])[last()]"));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doubleClickUitzending(String date) {
        //waitUtil.waitForVisible(By.xpath("//span[contains(text(), '" + date + "')]/../../../tr"));
        //waitUtil.isElementPresent(By.xpath("//span[contains(text(), '" + date + "')]/../../../tr"));
        Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(By.xpath("//span[contains(text(), '" + date + "')]/../../../tr"))).doubleClick().perform();
    }

    public void changeStartDate(String date) {
        waitUtil.waitAndSendkeys(By.name("start"), date);
    }

    public void clickOpslaan() {
        waitUtil.waitAndClick(By.xpath("//button[contains(text(), 'Bewaar')]"));
    }

    public void changeKanaal(String kanaal){
        Select itemKanaal = new Select(driver.findElement(By.name("channel")));
        itemKanaal.selectByVisibleText(kanaal);
    }

    public void changeEndDate(String date) {
        waitUtil.waitAndSendkeys(By.name("stop"), date);
    }

    public void inputValueInInput(String name, String value) {
        waitUtil.waitAndSendkeys(By.name(name), value);
    }

    public String checkUitzendingGegevensKanaal(String kanaal){
        waitUtil.waitForVisible(By.xpath("//td[@class='column-events-channel']/descendant::*[contains(text(), '" + kanaal + "')]"));
        return driver.findElement(By.xpath("//td[@class='column-events-channel']/descendant::*[contains(text(), '" + kanaal + "'")).getText();
    }

    public String checkUitzendingGegevensDatum(String datum){
        waitUtil.waitForVisible(By.xpath("//td[@class='column-events-start']/descendant::*[contains(text(), '" + datum + "')]"));
        return driver.findElement(By.xpath("//td[@class='column-events-start']/descendant::*[contains(text(), '" + datum + "')]")).getText();
    }
}
