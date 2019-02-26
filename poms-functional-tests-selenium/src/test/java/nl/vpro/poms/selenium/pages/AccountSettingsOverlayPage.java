package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AccountSettingsOverlayPage extends AbstractPage {

	private static final By standaardOmroepDropdownBy = By.xpath("//span[contains(text(),'Standaard-omroepen')]");

	private static final String standaardOmroepTemplate = "//div[contains(text(),'%s')]";
	
	private static final By formBy = By.xpath("//div[contains(@class,'modal-backdrop')]");

	private static final By opslaanBy = By.xpath("//button[contains(text(),'Opslaan')]");

	public AccountSettingsOverlayPage(WebDriver driver) {
		super(driver);
	}

	public void addStandaardOmroep(String omroep) {
		WebElement standaardOmroepDropdown = driver.findElement(standaardOmroepDropdownBy);
		standaardOmroepDropdown.click();
		By omroepBy = By.xpath(String.format(standaardOmroepTemplate, omroep));
		WebElement omroepElement = driver.findElement(omroepBy);
		omroepElement.click();
	}

	public void clickOpslaan() {
		WebElement opslaanElement = driver.findElement(opslaanBy);
		opslaanElement.click();
		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver localDriver) {
				return localDriver.findElements(formBy).size() == 0;
			}
		});
	}
}
