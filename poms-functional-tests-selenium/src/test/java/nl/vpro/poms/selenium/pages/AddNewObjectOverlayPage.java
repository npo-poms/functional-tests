package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import nl.vpro.domain.media.AVType;
import nl.vpro.domain.media.MediaType;
import nl.vpro.poms.selenium.util.WebDriverUtil;

public class AddNewObjectOverlayPage extends AbstractOverlayPage {

    private static final By titleInputBy = By.id("inputTitle");

    private static final String dropDownTemplate = "//span[contains(text(),'%s')]";

    private static final By mediaTypeBy = getDropDownBy("Media Type");

    private static final By avTypeBy = getDropDownBy("AV Type");

    private static final By genreBy = getDropDownBy("Genre");

    private static final String typeOptionTemplate = "//div[contains(text(),'%s')]";

    private static final String selectedOmroepTemplate = "//div[@class='dropdown-selected' and contains(text(),'%s')]";

    private static final By maakAanButtonBy = By.xpath("//button[contains(text(),'Maak aan')]");

    public AddNewObjectOverlayPage(WebDriverUtil driver) {
        super(driver);
    }

    private static By getDropDownBy(String dropDown) {
        return By.xpath(String.format(dropDownTemplate, dropDown));
    }

    public AddNewObjectOverlayPage enterTitle(String title) {
        webDriverUtil.waitAndSendkeys(titleInputBy, title);
        return this;
    }

    public AddNewObjectOverlayPage chooseMediaType(MediaType mediaType) {
        webDriverUtil.waitAndClick(mediaTypeBy);
        clickOption(mediaType.getDisplayName());
        return this;
    }

    public AddNewObjectOverlayPage chooseAvType(AVType avType) {
        webDriverUtil.waitAndClick(avTypeBy);
        clickOption(avType.getDisplayName());
        return this;
    }

    public boolean omroepIsSelected(String omroep) {
        By omroepSelectedBy = By.xpath(String.format(selectedOmroepTemplate, omroep));
        WebElement omroepSelectedElement = driver.findElement(omroepSelectedBy);
        return omroepSelectedElement.isDisplayed();
    }

    public AddNewObjectOverlayPage chooseGenre(String genre) {
        webDriverUtil.waitAndClick(genreBy);
        clickOption(genre);
        return this;
    }

    private void clickOption(String type) {
        webDriverUtil.waitAndClick(By.xpath(String.format(typeOptionTemplate, type)));
    }

    public boolean isEnabledMaakAan() {
        return driver.findElement(maakAanButtonBy).isEnabled();
    }

    public void clickMaakAan() {
        webDriverUtil.waitAndClick(maakAanButtonBy);
    }

    /*
     * This method should be unnecessary! Clicks away the error message page.
     */
    @Deprecated
    public void clickHerlaad() {
        By herlaadButtonBy = By.xpath("//button[contains(text(), 'herlaad')]");
        webDriverUtil.getWait().until(ExpectedConditions.elementToBeClickable(herlaadButtonBy));
        WebElement herlaadButton = driver.findElement(herlaadButtonBy);
        herlaadButton.click();
    }

    public AddNewObjectOverlayPage selectPublicationPeriod(String start, String end) {
        webDriverUtil.waitAndClick(By.xpath("//span[contains(text(), 'Publicatiestart')]"));

        webDriverUtil.waitAndSendkeys(By.cssSelector("[ng-model='media.publication.start']"), start);
        webDriverUtil.waitAndSendkeys(By.cssSelector("[ng-model='media.publication.stop']"), end);
        return this;
    }
}
