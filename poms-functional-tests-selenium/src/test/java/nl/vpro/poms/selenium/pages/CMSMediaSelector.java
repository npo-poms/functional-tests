package nl.vpro.poms.selenium.pages;

import nl.vpro.api.client.utils.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static nl.vpro.poms.selenium.util.Config.CONFIG;

public class CMSMediaSelector extends AbstractPage {

    String mainCMSWindow;
    String pomsWindow;

    public CMSMediaSelector(WebDriver driver) {
        super(driver);
    }

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

        driver.getWindowHandles().forEach(windowHandle -> {
            driver.switchTo().window(windowHandle);
        });
        wait.until(ExpectedConditions.titleContains("POMS Media selector"));
//
    }


}
