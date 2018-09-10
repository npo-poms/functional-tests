package nl.vpro.poms.config;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import nl.vpro.api.client.utils.Config;

@Slf4j
public abstract class Webtest {
    protected static ChromeDriver driver;

    protected static final Config CONFIG = new Config("npo-functional-tests.properties", "npo-browser-tests.properties");



    protected static void login(String address, String userName, String password) {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(false);
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.get(address);

        driver.findElement(By.id("username")).sendKeys(userName);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.name("submit")).click();
    }

    public static void loginVPROand3voor12() {
        String url = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl");
        log.info("poms {}", url);
        String user =  CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");
        Assume.assumeNotNull(user, password);
        login(
            url,
            user,
            password);
    }

    public static void loginGtaaBrowserTest() {
        ChromeOptions options = new ChromeOptions();
        String url = CONFIG.getProperties(Config.Prefix.npo_api).get("baseUrl") + "/thesaurus/example/";
        options.setHeadless(false);
        String user =  CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");

        login(url,
                user,
                password);

        //driver = new ChromeDriver(options);
        //driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        //driver.get("http://rs-dev.poms.omroep.nl/v1/thesaurus/example/");

        //driver.findElement(By.id("username")).sendKeys("<user here>");
        //driver.findElement(By.id("password")).sendKeys("<password here>");
        //driver.findElement(By.id("kc-login")).click();
    }

}
