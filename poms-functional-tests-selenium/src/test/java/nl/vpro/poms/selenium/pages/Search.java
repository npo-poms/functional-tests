package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Search extends AbstractPage {

	private static final By newBy = By.xpath("//a[@title='Nieuw item toevoegen']");
    private static final By logoutBy = By.xpath("//a[text()='log uit']");
    private static final By accountInstellingenBy = By.xpath("//a[contains(text(),'account-instellingen')]");
    private static final By loggedOutBy = By.xpath("//div[@id='msg']/h2");
    private static final By menuBy = By.xpath(
        "//div[@class='header-account-buttons']/div[@class='header-account-link'][1]/span[1]");
    private static final By overlayFormBy = By.name("createForm");

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
		clickMenu();
		WebElement accountInstellingenElement = driver.findElement(accountInstellingenBy);
		accountInstellingenElement.click();
	}

	private void clickMenu() {
		WebElement menuElement = driver.findElement(menuBy);
		menuElement.click();
	}
}
