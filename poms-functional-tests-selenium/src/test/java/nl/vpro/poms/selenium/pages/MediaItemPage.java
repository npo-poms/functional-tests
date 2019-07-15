package nl.vpro.poms.selenium.pages;

import com.paulhammant.ngwebdriver.NgWebDriver;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Fail.fail;

public class MediaItemPage extends AbstractPage {
    private static final String xpathViewerTitle = "//*[@class='viewer-title' and contains(text(), '%s')]";
    private static final String xpathViewerSubTitle = "//*[@class='viewer-subtitle' and contains(text(), '%s')]";
    private static final String xpathUitzendingen = "//*[@class='media-section-title'  and contains(text(), '%s')]";
    private static final String xpathAfbeeldingen = "//*[@class='media-section-title'  and contains(text(), '%s')]";
    private static final String buttonAfbeeldingToevoegen = "//button[contains(text(), 'Afbeelding toevoegen')]";
    private static final String cssInputUploadAfbeelding = "input#inputFile";
    private static final String cssImageTitle = "input#inputTitle";
    private static final String cssImageDescription = "textarea#inputDescription";
    private static final String xpathInputSelectImageType = "//*[contains(text(), 'Abeeldingstype')]/../descendant::input";
    private static final String xpathImageTypeOption = "//*[contains(@class, 'option')]/descendant::*[contains(text(), '%s')]";
    private static final String xpathButtonMaakAan = "//button[contains(text(), '%s')]";

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
        return driver.findElement(By.cssSelector("[title='bekijk alle uitzenddata']")).getText();
    }

    public void clickMenuItem(String menuItem) {
//        Nog verder uitzoeken
        waitUtil.waitForVisible(By.xpath("(//span[contains(text(), '" + menuItem +"')])[last()]"));
        waitUtil.waitAndClick(By.xpath("(//span[contains(text(), '" + menuItem +"')])[last()]"));
        waitUtil.waitForVisible(By.xpath("//li[@class='media-item-navigation-link active']/descendant::*[contains(text(), '" + menuItem + "')]"));
    }

    public void checkOfPopupUitzendingDissappear(){
        waitUtil.waitForInvisible(By.name("editScheduleEventForm"));
        waitUtil.waitForInvisible(By.xpath("//label[@label-for='channel' and contains(text(), 'Kanaal:')]"));
    }

    public void clickAlgemeen() {
        waitUtil.waitAndClick(By.xpath("(//span[contains(text(), 'Uitzendingen')])[last()]"));
//
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public void doubleClickUitzending(String date) {
        //waitUtil.waitForVisible(By.xpath("//span[contains(text(), '" + date + "')]/../../../tr"));
        //waitUtil.isElementPresent(By.xpath("//span[contains(text(), '" + date + "')]/../../../tr"));
        Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(By.xpath("//span[contains(text(), '" + date + "')]/../../../tr"))).doubleClick().perform();
    }

    public void moveToUitzendingen(){
        moveToElement(By.xpath(String.format(xpathUitzendingen, "Uitzendingen")));
    }

    public void moveToAfbeeldingen(){
        moveToElement(By.xpath(String.format(xpathAfbeeldingen, "Afbeeldingen")));
    }

    public void clickOnAfbeeldingToevoegen(){
        ngWait.waitForAngularRequestsToFinish();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(buttonAfbeeldingToevoegen)));
        driver.findElement(By.xpath(buttonAfbeeldingToevoegen)).click();
    }

    public void upLoadAfbeeldingMetNaam(String naam) throws URISyntaxException {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssInputUploadAfbeelding)));

        WebElement inputUploadAfbeelding = driver.findElement(By.cssSelector(cssInputUploadAfbeelding));

        URL url  = getClass().getClassLoader().getResource(naam);

        URI uri = url.toURI();

        File file = new File(uri);

        String path = file.getAbsolutePath();

        inputUploadAfbeelding.sendKeys(path);
    }

    public void imageAddTitle(String title){
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssImageTitle)));
        WebElement imageTitle = driver.findElement(By.cssSelector(cssImageTitle));
        imageTitle.sendKeys(title);
    }

    public void imageAddDescription(String description){
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssImageDescription)));
        WebElement imageDescription = driver.findElement(By.cssSelector(cssImageDescription));
        imageDescription.sendKeys(description);
    }

    public void imageAddType(String type){
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpathInputSelectImageType)));
        WebElement imageType = driver.findElement(By.xpath(xpathInputSelectImageType));
        imageType.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(String.format(xpathImageTypeOption, type))));
        WebElement option = driver.findElement(By.xpath(String.format(xpathImageTypeOption, type)));
        option.click();
    }

    public void clickButtonMaakAan(){
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(String.format(xpathButtonMaakAan, "Maak aan"))));
        WebElement buttonMaakAan = driver.findElement(By.xpath(String.format(xpathButtonMaakAan, "Maak aan")));
        buttonMaakAan.click();
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

    public String getValueForInInputWithName(String name) {
        Object value = ((JavascriptExecutor) driver).executeScript("return document.getElementsByName('" + name +"')[0].value");
        String returnValue = "";
        if (value instanceof String) {
            returnValue = value.toString();
        } else {
            fail("Error in the javascript on the page");
            System.out.println("Error in the javascript on the page");
        }
        return returnValue;
    }


    public String getUitzendingGegevensEersteKanaal(){
        return driver.findElement(By.xpath("(//td/descendant::*[@ng-switch-when='channel'])[1]")).getText();
    }

    public String getUitzendingGegevensEersteDatum(){
        return driver.findElement(By.xpath("(//td/descendant::*[@ng-switch-when='start'])[1]")).getText();
    }

    public String getUitzendingTitel(){
        return driver.findElement(By.xpath("//td/descendant::*[@ng-switch-when='title']")).getText();
    }

    public String getUitzendingOmschrijving(){
        return driver.findElement(By.xpath("//td/descendant::*[@ng-switch-when='description']")).getText();
    }

    public void klikOpKnopMetNaam(String naambutton){
        waitUtil.waitAndClick(By.xpath("//button[contains(text(), '" + naambutton + "')]"));
    }

    public String getMediaItemTitle() {
        return waitUtil.waitAndGetText(By.cssSelector("h1[class='viewer-title']"));
    }

    public void waitAndCheckMediaItemTitle(String title) {
        waitUtil.waitForVisible(By.xpath(String.format(xpathViewerTitle, title)));
    }

    public void waitAndCheckMediaItemSubTitle(String title) {
        waitUtil.waitForVisible(By.xpath(String.format(xpathViewerSubTitle, title)));
    }

    public void refreshUntilUitzendingGegevensWithStartDate(String startDate) {
        waitUtil.refreshUntilVisible("//*[@title='bekijk alle uitzenddata' and contains(text(), '" +startDate+" (Nederland 1)')]");
    }

    public void moveToElement(By by) {
        ngWait.waitForAngularRequestsToFinish();
        WebElement element = driver.findElement(by);
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", element);
    }

}
