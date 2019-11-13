package nl.vpro.poms.selenium.util;


import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.*;
import org.slf4j.Logger;

import com.google.common.cache.*;

import static nl.vpro.poms.selenium.util.Config.CONFIG;


@Slf4j
public class WebDriverFactory {

    public static boolean headless;

    static {
        headless = Boolean.parseBoolean(CONFIG.getProperty("headless"));
    }

    private static LoadingCache<DriverManagerType, WebDriverManager> CACHE = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<DriverManagerType, WebDriverManager>() {
                @Override
                public WebDriverManager load(@Nonnull DriverManagerType key) {
                    WebDriverManager instance = WebDriverManager.getInstance(key);
                    instance.setup();
                    return instance;
                }
            });


    @SneakyThrows
    public static WebDriver getWebDriver(Browser browser) {
        return browser.asWebDriver();
    }

    @EqualsAndHashCode
    public static class Browser {
        final DriverManagerType type;
        final String version;
        WebDriver driver;

        public Browser(DriverManagerType type, String version) {
            this.type = type;
            this.version = version;
        }

        @SneakyThrows
        public  WebDriver asWebDriver() {
            try {
                CACHE.get(type);
                switch (type) {
                    case CHROME:
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--incognito");
                        options.addArguments("--lang=en");
                        options.addArguments("--start-maximized");
                        options.setHeadless(headless);
                        return new ChromeDriver(options);
                    case FIREFOX:
                        FirefoxProfile profile = new FirefoxProfile();
                        profile.setPreference("intl.accept_languages", "en");

                        FirefoxOptions ffoptions = new FirefoxOptions();
                        ffoptions.addArguments("--incognito");
                        ffoptions.setProfile(profile);
                        ffoptions.setHeadless(headless);
                        return new FirefoxDriver(ffoptions);
                    default:
                        throw new UnsupportedOperationException();
                }
            } catch (ExecutionException e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
        public WebDriver getDriver() {
            if (driver == null) {
                try {
                    driver = asWebDriver();
                    // The dimension of the browser should be big enough, (headless browser seem to be small!), otherwise test will keep waiting forever
                    Dimension d = new Dimension(1200, 1000);
                    driver.manage().window().setSize(d);
                } catch (Exception e) {
                    log.error("Could not create driver for " + this + ":" + e.getMessage(), e);
                    throw e;
                }
            }
            return driver;
        }

        public WebDriverUtil getUtil(Logger log) {
            return new WebDriverUtil(getDriver(), log);
        }
    }
}
