package nl.vpro.poms.selenium.poms.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.paulhammant.ngwebdriver.NgWebDriver;

import nl.vpro.poms.selenium.util.WebDriverUtil;

public class OmroepenOverlayPage extends AbstractOverlayPage {

	private static final By addOmroepBy = By.cssSelector("button.modal-broadcasters-add");

	private static final By idBy = By.cssSelector("input[name=id]");
	private static final By omroepBy = By.cssSelector("input[name=text]");
	private static final By wonIdBy = By.cssSelector("input[name=wonId]");
	private static final By neboIdBy = By.cssSelector("input[name=neboId]");
	private static final By pdIdBy = By.cssSelector("input[name=pdId]");
	private static final By bewaarBy = By.xpath("//button[contains(text(), 'bewaar')]");

	private static final By overlayBy = By.cssSelector("div.modal-backdrop.fade.ng-animate.in-remove.in-remove-active");

	public OmroepenOverlayPage(WebDriverUtil driver) {
		super(driver);
	}

	public void addOmroep(String omroep) {
		NgWebDriver ngWebDr = new NgWebDriver((JavascriptExecutor) driver);
		WebElement addOmroepButton = driver.findElement(addOmroepBy);
		webDriverUtil.w().until(ExpectedConditions.elementToBeClickable(addOmroepBy));
		ngWebDr.waitForAngularRequestsToFinish();
		addOmroepButton.click();
		enterText(idBy, omroep);
		enterText(omroepBy, omroep);
		enterText(wonIdBy, omroep);
		enterText(neboIdBy, omroep);
		enterText(pdIdBy, omroep);
		WebElement bewaarButton = driver.findElement(bewaarBy);
		bewaarButton.click();
	}

	public void deleteOmroep(String omroep) {
		NgWebDriver ngWebDr = new NgWebDriver((JavascriptExecutor) driver);
		// TODO
	}

	private void enterText(By by, String text) {
		webDriverUtil.w().until(ExpectedConditions.elementToBeClickable(by));
		WebElement element = driver.findElement(by);
		element.sendKeys(text);
	}

	@Override
	public void close() {
		super.close();
//		wait.until(ExpectedConditions.invisibilityOfElementLocated(overlayBy));
	}
}
