package nl.vpro.poms.selenium.pages;


import nl.vpro.poms.selenium.poms.AbstractTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import static nl.vpro.poms.selenium.poms.AbstractTest.CONFIG;


public class CMSMediaSelector extends AbstractPage {

    String mainCMSWindow;
    String pomsWindow;
    String UrlCmsMediaSelector = "https://poms-test.omroep.nl/CMSSelector/example/";
    WebDriverWait wait ;


    public CMSMediaSelector(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, 15, 250);
    }

    public void openUrlCmsMediaSelector(){
        driver.navigate().to(UrlCmsMediaSelector);
    }

    public void clickButtonSelect() {

        waitUtil.waitAndClick(By.cssSelector("button#select"));
    }

    public void switchToPomsWindows() {
        mainCMSWindow = driver.getWindowHandle();

        driver.getWindowHandles().forEach(windowHandle -> {
            driver.switchTo().window(windowHandle);
        });
        wait.until(ExpectedConditions.titleContains("Publieke Omroep Media Service (POMS)"));
    }

    public void switchToCMSWindow() {
        pomsWindow = driver.getWindowHandle();

        driver.getWindowHandles().forEach(windowHandle -> {
            driver.switchTo().window(windowHandle);
        });
        wait.until(ExpectedConditions.titleContains("POMS Media selector"));
    }

    public void loginNPOGebruikerMediaSelector() {
        Login login = new Login(driver);
        String user = CONFIG.getProperty("SpeciaalVfGebruiker.LOGIN");
        String password = CONFIG.getProperty("SpeciaalVfGebruiker.PASSWORD");
        login.login(user, password);
    }

    public void checkIfNotDisplayedTables() {
        waitUtil.waitForInvisible(By.cssSelector("tr"));
    }

    public void checkLoginTextBoxes() {
        waitUtil.waitForVisible(By.id("username"));
        waitUtil.waitForVisible(By.id("password"));
    }

    public String getResult() {
        return waitUtil.waitAndGetText(By.cssSelector("input#value"));
    }
}
