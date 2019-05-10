package nl.vpro.poms.selenium.util;

import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.concurrent.TimeUnit;

import static nl.vpro.poms.selenium.util.Config.CONFIG;

import nl.vpro.poms.selenium.poms.AbstractTest;


@Slf4j
public class WebDriverFactory {

    private static boolean headless;
    static {
          headless = Boolean.parseBoolean(AbstractTest.CONFIG.getProperty("headless"));
    }
    private static LoadingCache<DriverManagerType, WebDriverManager> CACHE = CacheBuilder
        .newBuilder()
        .build(new CacheLoader<DriverManagerType, WebDriverManager>() {
            @Override
            public WebDriverManager load(@Nonnull DriverManagerType key) throws Exception {
                WebDriverManager instance = WebDriverManager.getInstance(key);
                instance.setup();
                return instance;
            }
        });


    @SneakyThrows
    public static WebDriver getWebDriver(Browser browser) {
        WebDriver driver;
        boolean headless = Boolean.parseBoolean(CONFIG.getProperties().get("headless"));
        switch (browser) {
            case CHROME:
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--incognito");
                options.addArguments("--lang=en");
//                options.setHeadless(headless);
                driver = new ChromeDriver(options);
                break;
            case FIREFOX:
            	WebDriverManager.firefoxdriver().setup();
            	FirefoxOptions ffoptions = new FirefoxOptions();
//            	ffoptions.addArguments("--incognito");
//                ffoptions.setHeadless(headless);
            	driver = new FirefoxDriver(ffoptions);
            	break;
            default:
                driver = null;
        }
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        return driver;
    }
}
