package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AddNewObjectOverlayPage extends AbstractOverlayPage {
	
	private static final By titleInputBy = By.id("inputTitle");
	
	private static final String dropDownTemplate = "//span[contains(text(),'%s')]";
	
	private static final By mediaTypeBy = getDropDownBy("Media Type");
	
	private static final By avTypeBy = getDropDownBy("AV Type");
	
	private static final By genreBy = getDropDownBy("Genre");
	
	private static final String typeOptionTemplate = "//div[contains(text(),'%s')]";
	
	private static final String selectedOmroepTemplate = "//div[@class='dropdown-selected' and contains(text(),'%s')]";
	
	private static final By maakAanButtonBy = By.xpath("//button[contains(text(),'Maak aan')]");
	
	public AddNewObjectOverlayPage(WebDriver driver) {
		super(driver);
	}
	
	private static By getDropDownBy(String dropDown) {
		return By.xpath(String.format(dropDownTemplate, dropDown));
	}
	
	public void enterTitle(String title) {
		wait.until(ExpectedConditions.elementToBeClickable(titleInputBy));
		WebElement titleInputElement = driver.findElement(titleInputBy);
		titleInputElement.sendKeys(title);
	}

	public void chooseMediaType(String mediaType) {
		wait.until(ExpectedConditions.elementToBeClickable(mediaTypeBy));
		WebElement mediaTypeElement = driver.findElement(mediaTypeBy);
		mediaTypeElement.click();
		clickOption(mediaType);
	}
	
	public void chooseAvType(String avType) {
		wait.until(ExpectedConditions.elementToBeClickable(avTypeBy));
		WebElement avTypeElement = driver.findElement(avTypeBy);
		avTypeElement.click();
		clickOption(avType);
	}
	
	public boolean omroepIsSelected(String omroep) {
		By omroepSelectedBy = By.xpath(String.format(selectedOmroepTemplate, omroep));
		WebElement omroepSelectedElement = driver.findElement(omroepSelectedBy);
		return omroepSelectedElement.isDisplayed();
	}

	public void chooseGenre(String genre) {
		wait.until(ExpectedConditions.elementToBeClickable(genreBy));
		WebElement genreElement = driver.findElement(genreBy);
		genreElement.click();
		clickOption(genre);
	}
	
	private void clickOption(String type) {
		By optionBy = By.xpath(String.format(typeOptionTemplate, type));
		WebElement optionElement = driver.findElement(optionBy);
		optionElement.click();
	}
	
	public boolean isDisabledMaakAan() {
		WebElement maakAan = driver.findElement(maakAanButtonBy);
		String attribute = maakAan.getAttribute("disabled");
		return "true".equals(attribute);
	}

	public void clickMaakAan() {
		WebElement maakAanButton = driver.findElement(maakAanButtonBy);
		maakAanButton.click();
	}

	/*
	 * This method should be unnecessary! Clicks away the error message page.
	 */
	@Deprecated
	public void clickHerlaad() {
		By herlaadButtonBy = By.xpath("//button[contains(text(), 'herlaad')]");
		wait.until(ExpectedConditions.elementToBeClickable(herlaadButtonBy));
		WebElement herlaadButton = driver.findElement(herlaadButtonBy );
		herlaadButton.click();
	}

}
