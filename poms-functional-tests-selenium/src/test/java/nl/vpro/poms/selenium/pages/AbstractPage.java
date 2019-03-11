package nl.vpro.poms.selenium.pages;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import nl.vpro.api.client.utils.Config;

@Slf4j
public abstract class AbstractPage {

    WebDriver driver;
	WebDriverWait wait;
    
    protected AbstractPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 30, 100);
    }
}
