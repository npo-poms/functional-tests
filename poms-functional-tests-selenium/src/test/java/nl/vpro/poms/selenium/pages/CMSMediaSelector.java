package nl.vpro.poms.selenium.pages;

import com.paulhammant.ngwebdriver.NgWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import static nl.vpro.poms.selenium.util.Config.CONFIG;

public class CMSMediaSelector extends AbstractPage {

    String mainCMSWindow;
    String pomsWindow;

    public CMSMediaSelector(WebDriver driver) {super(driver);}

    WebDriverWait wait = new WebDriverWait(driver, 10);

    public void clickButtonSelect(){

        waitUtil.waitAndClick(By.cssSelector("button#select"));
    }

    public void switchToPomsWindows(){
        mainCMSWindow = driver.getWindowHandle();

        driver.getWindowHandles().forEach(windowHandle -> {
            driver.switchTo().window(windowHandle);
        });

//        Toevoegen return statement en parameter toevoegen?
     wait.until(ExpectedConditions.titleContains("POMS"));

    }

    public void switchToCMSWindow(){
        pomsWindow = driver.getWindowHandle();
//        driver.switchTo().defaultContent();
        driver.getWindowHandles().forEach(windowHandle -> {
            driver.switchTo().window(windowHandle);
        });
        wait.until(ExpectedConditions.titleContains("POMS Media selector"));
//
    }

    public void loginNPOGebruikerMediaSelector(){
        Login login = new Login(driver);
        String user = CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
        String password = CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");
        login.login(user, password);
    }

    public void checkIfNotDisplayedTables(){
        waitUtil.waitForInvisible(By.cssSelector("tr"));
    }

    public void checkLoginTextBoxes(){
        waitUtil.waitForVisible(By.id("username"));
        waitUtil.waitForVisible(By.id("password"));
    }

    public String getResult(){
        return waitUtil.waitAndGetText(By.cssSelector("input#value"));
    }
}
