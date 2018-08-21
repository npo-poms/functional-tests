package nl.vpro.poms.config;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import nl.vpro.api.client.utils.Config;
import nl.vpro.rules.TestMDC;

@Slf4j
public abstract class Webtest {

    @Rule
    public TestMDC testMDC = new TestMDC();


    protected static ChromeDriver driver;

    protected static final Config CONFIG = new Config("npo-functional-tests.properties", "npo-browser-tests.properties");



    protected static void login(String address, String userName, String password) {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
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

}
