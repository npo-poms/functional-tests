package nl.vpro.poms.selenium.util;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import nl.vpro.poms.selenium.poms.AbstractTest;


public class WebDriverFactory {
    public enum Browser {
        CHROME, 
        FIREFOX
    }
    public static WebDriver getWebDriver(Browser browser) {
        WebDriver driver;
        boolean headless = Boolean.parseBoolean(AbstractTest.CONFIG.getProperties().get("headless"));
        switch (browser) {
            case CHROME:
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--incognito");
                options.setHeadless(headless);
                driver = new ChromeDriver(options);
                break;
            case FIREFOX:
            	WebDriverManager.firefoxdriver().setup();
            	FirefoxOptions ffoptions = new FirefoxOptions();
            	ffoptions.addArguments("--incognito");
                ffoptions.setHeadless(headless);
            	driver = new FirefoxDriver(ffoptions);
            	break;
            default:
                driver = null;
        }
        return driver;
    }
}
