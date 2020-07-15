package nl.vpro.poms.selenium.poms.pages;

import com.paulhammant.ngwebdriver.NgWebDriver;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class AccountSettingsOverlayPage extends AbstractOverlayPage {


    private static final By standaardOmroepDropdownBy = By.cssSelector("poms-ui-select-multi[name=Standaard-omroepen] > span");

    private static final String selectedStandaardOmroepTemplate = "//div[@class='dropdown-selected' and contains(text(), '%s')]";

    private static final String standaardOmroepTemplate = "//div[contains(text(),'%s')]/..";

    private static final By formBy = By.xpath("//div[contains(@class,'modal-backdrop')]");

    private static final String selectedOmroepTemplate = "//div[@class='dropdown-selected' and contains(text(),'%s')]";

    private static final By opslaanBy = By.xpath("//button[contains(text(),'Opslaan')]");

    private static final By rolesBy = By.cssSelector("span[data-ng-repeat='role in editor.roles']");

    private static final By roleParagraphBy = By.cssSelector("p.modal-text-small");

    private static final String singleRoleTemplate = "//span[@data-ng-repeat='role in editor.roles' and contains(text(), '%s')]";

    //	private static final String ownerButtonTemplate = "input[name='ownertype][value='%s']";
    private static final String ownerButtonTemplate = "input[value='%s']";

    public AccountSettingsOverlayPage(WebDriverUtil driver) {
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
        NgWebDriver ngWebDriver = new NgWebDriver((JavascriptExecutor) driver);
        By omroepBy = By.xpath(String.format(standaardOmroepTemplate, omroep));
        WebElement omroepElement = driver.findElement(omroepBy);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click()", omroepElement);
//		omroepElement.click();
        ngWebDriver.waitForAngularRequestsToFinish();
//		Sleeper.sleep(5000);
    }

    public void removeStandaardOmroep(String omroep) {
        NgWebDriver ngWebDriver = new NgWebDriver((JavascriptExecutor) driver);
        clickStandaardOmroepDropdown();
        By selectedStandaardOmroepBy = By.xpath(String.format(selectedStandaardOmroepTemplate, omroep));
        WebElement selectedStandaardOmroepElement = driver.findElement(selectedStandaardOmroepBy);
        selectedStandaardOmroepElement.click();
        ngWebDriver.waitForAngularRequestsToFinish();
    }

    private void clickStandaardOmroepDropdown() {
        NgWebDriver ngWebDriver = new NgWebDriver((JavascriptExecutor) driver);
        webDriverUtil.waitAndClick(standaardOmroepDropdownBy);
        ngWebDriver.waitForAngularRequestsToFinish();
    }

    public void clickOpslaan() {
        NgWebDriver ngWebDriver = new NgWebDriver((JavascriptExecutor) driver);
        WebElement opslaanElement = driver.findElement(opslaanBy);
        opslaanElement.click();
        ngWebDriver.waitForAngularRequestsToFinish();
//		wait.until(new ExpectedCondition<Boolean>() {
//			@Override
//			public Boolean apply(WebDriver localDriver) {
//				return localDriver.findElements(formBy).size() == 0;
//			}
//		});
    }

    public List<String> getRoles() {
        return driver.findElements(rolesBy).stream().map(el -> el.getText().replaceAll(",", "")).collect(Collectors.toList());
    }

    public boolean hasRole(String role) {
        WebElement roleParagraphElement = driver.findElement(roleParagraphBy);
        By roleBy = By.xpath(String.format(singleRoleTemplate, role));
        System.out.println("XXX" + roleBy.toString());
        WebElement roleElement = roleParagraphElement.findElement(roleBy);
        String roleText = roleElement.getAttribute("innerHTML");
        System.out.println("###" + roleText);
        if (roleText == null) {
            return false;
        }
        return roleText.contains(role);
    }

    public void checkOwner(String owner) {
        By ownerBy = By.cssSelector(String.format(ownerButtonTemplate, owner));
        WebElement ownerElement = driver.findElement(ownerBy);
        ownerElement.click();
    }
}
