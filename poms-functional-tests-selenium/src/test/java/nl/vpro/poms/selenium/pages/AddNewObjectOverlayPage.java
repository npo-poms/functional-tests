package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AddNewObjectOverlayPage extends AbstractPage {
	
	private static final By titleInputBy = By.id("inputTitle");
	
	private static final String dropDownTemplate = "//span[contains(text(),'%s')]";
	
	private static final By mediaTypeBy = getDropDownBy("Media Type");
	
	private static final By avTypeBy = getDropDownBy("AV Type");
	
	private static final By genreBy = getDropDownBy("Genre");
	
	private static final String typeOptionTemplate = "//div[contains(text(),'%s')]";
	
	private static final By maakAanButtonBy = By.xpath("//button[contains(text(),'Maak aan')]");
	
	private static final By closeBy = By.xpath("//div[@class='modal-close-button']");

	public AddNewObjectOverlayPage(WebDriver driver) {
		super(driver);
	}
	
	private static By getDropDownBy(String dropDown) {
		return By.xpath(String.format(dropDownTemplate, dropDown));
	}
	
	public void enterTitle(String title) {
		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
		wait.until(ExpectedConditions.elementToBeClickable(titleInputBy));
		WebElement titleInputElement = driver.findElement(titleInputBy);
		titleInputElement.sendKeys(title);
	}

	public void chooseMediaType(String mediaType) {
		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
		wait.until(ExpectedConditions.elementToBeClickable(mediaTypeBy));
		WebElement mediaTypeElement = driver.findElement(mediaTypeBy);
		mediaTypeElement.click();
		clickOption(mediaType);
	}
	
	public void chooseAvType(String avType) {
		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
		wait.until(ExpectedConditions.elementToBeClickable(avTypeBy));
		WebElement avTypeElement = driver.findElement(avTypeBy);
		avTypeElement.click();
		clickOption(avType);
	}

	public void chooseGenre(String genre) {
		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
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

	public void close() {
		WebElement closeElement = driver.findElement(closeBy);
		closeElement.click();
	}

}
