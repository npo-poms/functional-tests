package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import nl.vpro.poms.selenium.util.Sleeper;

public class AccountSettingsOverlayPage extends AbstractPage {

//	private static final By standaardOmroepDropdownBy = By.xpath("//span[contains(text(),'Standaard-omroepen')]");
	private static final By standaardOmroepDropdownBy = By.cssSelector("poms-ui-select-multi[name=Standaard-omroepen] > span");
	
	private static final String selectedStandaardOmroepTemplate = "//div[@class='dropdown-selected' and contains(text(), '%s')]";

	private static final String standaardOmroepTemplate = "//div[contains(text(),'%s')]/..";
	
	private static final By formBy = By.xpath("//div[contains(@class,'modal-backdrop')]");
	
	private static final String selectedOmroepTemplate = "//div[@class='dropdown-selected' and contains(text(),'%s')]";

	private static final By opslaanBy = By.xpath("//button[contains(text(),'Opslaan')]");

	public AccountSettingsOverlayPage(WebDriver driver) {
		super(driver);
	}

	public boolean isVisibleStandaardOmroep(String omroep) {
		By selectedOmroepBy = By.xpath(String.format(selectedOmroepTemplate, omroep));
		try {
			WebElement selectedOmroepElement = driver.findElement(selectedOmroepBy);
			return selectedOmroepElement.isDisplayed();			
		} catch (NoSuchElementException nse) {
			return true;
		}
	}

	public void addStandaardOmroep(String omroep) {
		clickStandaardOmroepDropdown();
		By omroepBy = By.xpath(String.format(standaardOmroepTemplate, omroep));
		WebElement omroepElement = driver.findElement(omroepBy);
		((JavascriptExecutor) driver).executeScript("arguments[0].click()", omroepElement);
//		omroepElement.click();
		Sleeper.sleep(5000);
	}

	public void removeStandaardOmroep(String omroep) {
		clickStandaardOmroepDropdown();
		By selectedStandaardOmroepBy = By.xpath(String.format(selectedStandaardOmroepTemplate, omroep));
		WebElement selectedStandaardOmroepElement = driver.findElement(selectedStandaardOmroepBy);
		selectedStandaardOmroepElement.click();
	}

	private void clickStandaardOmroepDropdown() {
		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
		wait.until(ExpectedConditions.elementToBeClickable(standaardOmroepDropdownBy));
		WebElement standaardOmroepDropdown = driver.findElement(standaardOmroepDropdownBy);
		standaardOmroepDropdown.click();
	}

	public void clickOpslaan() {
//		Sleeper.sleep(5000);
		WebElement opslaanElement = driver.findElement(opslaanBy);
		opslaanElement.click();
		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver localDriver) {
				return localDriver.findElements(formBy).size() == 0;
			}
		});
		Sleeper.sleep(2000);
	}
}
