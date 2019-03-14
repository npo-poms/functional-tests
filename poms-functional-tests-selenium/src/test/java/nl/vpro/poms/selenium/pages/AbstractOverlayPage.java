package nl.vpro.poms.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractOverlayPage extends AbstractPage {

	private static final By closeBy = By.cssSelector("div.modal-close-button");

	protected AbstractOverlayPage(WebDriver driver) {
		super(driver);
	}

	public void close() {
		WebElement closeElement = driver.findElement(closeBy);
		closeElement.click();
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver localDriver) {
				return localDriver.findElements(closeBy).size() == 0;
			}
		});
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver localDriver) {
				return localDriver.findElements(By.cssSelector("div.modal-backdrop.fade.ng-animate.in-remove.in-remove-active")).size() == 0;
			}
		});
	}
}
