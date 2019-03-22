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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

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
        return browser.asWebDriver();
    }

    @EqualsAndHashCode
    public static class Browser {
        final DriverManagerType type;
        final String version;

        public Browser(DriverManagerType type, String version) {
            this.type = type;
            this.version = version;
        }

        @SneakyThrows
        public WebDriver asWebDriver() {
            CACHE.get(type);
            switch (type) {
                case CHROME:

                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--incognito");
                    options.setHeadless(headless);
                    return new ChromeDriver(options);
                case FIREFOX:
                        FirefoxOptions ffoptions = new FirefoxOptions();
                    ffoptions.addArguments("--incognito");
                    ffoptions.setHeadless(headless);
                    return new FirefoxDriver(ffoptions);
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }
}
