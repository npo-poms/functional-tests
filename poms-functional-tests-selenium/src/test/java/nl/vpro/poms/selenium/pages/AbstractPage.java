package nl.vpro.poms.selenium.pages;

import lombok.extern.slf4j.Slf4j;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public abstract class AbstractPage {

    WebDriver driver;
    WebDriverUtil waitUtil;
	WebDriverWait wait;

    
    protected AbstractPage(WebDriver driver) {
        this.waitUtil = new WebDriverUtil(driver);
        this.driver = driver;
    }

}
