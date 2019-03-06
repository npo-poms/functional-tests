package nl.vpro.poms.selenium.pages;

import java.util.Date;
import java.util.List;

import javax.xml.stream.events.StartElement;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.paulhammant.ngwebdriver.NgWebDriver;

import nl.vpro.poms.selenium.util.Sleeper;

public class Search extends AbstractPage {

	private static final By newBy = By.cssSelector(".header-link-new");
    private static final By logoutBy = By.xpath("//a[text()='log uit']");
    private static final By accountInstellingenBy = By.xpath("//a[contains(text(),'account-instellingen')]");
    private static final By loggedOutBy = By.cssSelector("div#msg > h2");
    private static final By menuBy = 
    		By.cssSelector(".header-account-buttons > .header-account-link:first-child > span");
    private static final By overlayFormBy = By.cssSelector("div.modal-backdrop");
    
    private static final By queryBy = By.cssSelector("input#query");
	private static final By zoekenBy = By.cssSelector("button#submit");
	private static final By resultTableBy = By.cssSelector("table.search-results-list");
	private static final String foundItemTemplate = "span[title='%s']";
	private static final By closeTabBy = By.cssSelector("span.tab-close");
	private static final String criteriaMenuTemplate = ".poms-uiselect[name=%s]";
	private static final String menuOptionTemplate = "//div[contains(text(),'%s')]";
	private static final By datePersonMenuBhy = By.xpath("//span[contains(text(), 'Datum & Persoon')]");
	private static final By uitzenddatumBy = By.cssSelector("input#search-datetype-scheduleEventDate");
	private static final By gewijzigdBy = By.cssSelector("input#search-datetype-modified");
	private static final By aangemaaktBy = By.cssSelector("input#search-datetype-created");
	private static final By sorteerdatumBy = By.cssSelector("input#search-datetype-sortdate");
	private static final By vanBy = By.cssSelector("input[name=fromdate]");
	private static final By totEnMetBy = By.cssSelector("input[name=todate]");
	private static final By zoekMetDatumBy = By.cssSelector("a.search-daterange-submit");
	private static final By tagMenuBy = By.cssSelector("[name=Tags] > span");
	private static final By tabInputBy = By.cssSelector("[name=Tags] input");
	private static final String selectedOptionTemplate = "//*[contains(@class,'dropdown-selected') and contains(text(), '%s')]";
    
    public Search(WebDriver driver) {
        super(driver);
    }
    
    public void clickNew() {
    	WebDriverWait wait = new WebDriverWait(driver, 30, 100);
    	wait.until(ExpectedConditions.elementToBeClickable(newBy));
		WebElement element = driver.findElement(newBy);
		element.click();
	}

    public void logout() {
    	WebDriverWait wait = new WebDriverWait(driver, 30, 100);
    	wait.until(new ExpectedCondition<Boolean>() {
						@Override
						public Boolean apply(WebDriver localDriver) {
							return localDriver.findElements(overlayFormBy).size() == 0;
						}
    	});
    	wait.until(ExpectedConditions.elementToBeClickable(menuBy));
        clickMenu();
        wait.until(ExpectedConditions.elementToBeClickable(logoutBy));
        WebElement logoutElement = driver.findElement(logoutBy);
		logoutElement.click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            loggedOutBy, "Succesvol uitgelogd."));
    }

	public void goToAccountInstellingen() {
		NgWebDriver ngWebDriver = new NgWebDriver((JavascriptExecutor) driver);
		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
		clickMenu();
		wait.until(ExpectedConditions.elementToBeClickable(accountInstellingenBy));
		WebElement accountInstellingenElement = driver.findElement(accountInstellingenBy);
		accountInstellingenElement.click();
		ngWebDriver.waitForAngularRequestsToFinish();
	}

	private void clickMenu() {
		NgWebDriver ngWebDriver = new NgWebDriver((JavascriptExecutor) driver);
		ngWebDriver.waitForAngularRequestsToFinish();
		WebElement menuElement = driver.findElement(menuBy);
		menuElement.click();
		ngWebDriver.waitForAngularRequestsToFinish();
		Sleeper.sleep(5000);
	}

	public void enterQuery(String query) {
//		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
		WebElement queryElement = driver.findElement(queryBy);
		queryElement.sendKeys(query);
		clickZoeken();
//		wait.until(ExpectedConditions.presenceOfElementLocated(resultTableBy));
		Sleeper.sleep(1000);
	}

	public void clickZoeken() {
		WebElement zoekenElement = driver.findElement(zoekenBy );
		zoekenElement.click();
	}
	
	public boolean itemFound(String title) {
		By findItemBy = By.cssSelector(String.format(foundItemTemplate, title));
		List<WebElement> findItemElements = driver.findElements(findItemBy);
		return findItemElements.size() > 0;
	}

	public void closeTab() {
		WebElement closeTabElement = driver.findElement(closeTabBy);
		closeTabElement.click();
	}

	public void selectOptionFromMenu(String menu, String option) {
		clickCriteriaMenu(menu);
		By menuOptionBy = By.xpath(String.format(menuOptionTemplate, option));
		WebElement menuOptionElement = driver.findElement(menuOptionBy);
		menuOptionElement.click();
	}
	
	public void clickCriteriaMenu(String menu) {
		By criteriaMenuBy = By.cssSelector(String.format(criteriaMenuTemplate, menu));
		WebElement criteriaMenuElement = driver.findElement(criteriaMenuBy);
		criteriaMenuElement.click();
	}

	public void enterSorteerdatumDates(String start, String end) {
		// TODO: Add date kind: Sorteerdatum/Uitzenddatum, Uitzenddatum, Gewijzigd, Aangemaakt
		clickDatePersonMenu();
		WebElement uitzenddatumElement = driver.findElement(sorteerdatumBy);
		uitzenddatumElement.click();
		enterDates(start, end);
		zoekMetDatum();
	}
	
	public void enterUitzenddatumDates(String start, String end) {
		// TODO: Add date kind: Sorteerdatum/Uitzenddatum, Uitzenddatum, Gewijzigd, Aangemaakt
		clickDatePersonMenu();
		WebElement uitzenddatumElement = driver.findElement(uitzenddatumBy);
		uitzenddatumElement.click();
		enterDates(start, end);
		zoekMetDatum();
	}

	public void enterGewijzigdDates(String start, String end) {
		clickDatePersonMenu();
		WebElement uitzenddatumElement = driver.findElement(gewijzigdBy);
		uitzenddatumElement.click();
		enterDates(start, end);
		zoekMetDatum();
	}

	public void enterAangemaaktDates(String start, String end) {
		clickDatePersonMenu();
		WebElement uitzenddatumElement = driver.findElement(aangemaaktBy);
		uitzenddatumElement.click();
		enterDates(start, end);
		zoekMetDatum();
	}
	
	private void clickDatePersonMenu() {
		WebElement datePersonElement = driver.findElement(datePersonMenuBhy);
		datePersonElement.click();
	}
	
	private void enterDates(String start, String end) {
		WebElement startElement = driver.findElement(vanBy);
		startElement.sendKeys(start);
		WebElement endElement = driver.findElement(totEnMetBy);
		endElement.sendKeys(end);
	}
	
	private void zoekMetDatum() {
		WebElement zoekElement = driver.findElement(zoekMetDatumBy);
		zoekElement.click();
	}

	public void enterTags(String tag) {
		WebElement tagElement = driver.findElement(tagMenuBy);
		tagElement.click();
		WebElement tagInputElement = driver.findElement(tabInputBy);
		tagInputElement.sendKeys(tag);
	}
	
	public void removeSelectedOption(String option) {
		WebDriverWait wait = new WebDriverWait(driver, 30, 100);
		By selectedOptionBy = By.xpath(String.format(selectedOptionTemplate, option));
		WebElement selectedOptionElement = driver.findElement(selectedOptionBy);
		wait.until(ExpectedConditions.elementToBeClickable(selectedOptionElement));
		selectedOptionElement.click();
	}
}
