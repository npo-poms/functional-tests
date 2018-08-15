package nl.vpro.poms.config;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import nl.vpro.api.client.utils.Config;

public abstract class Webtest {
    protected static ChromeDriver driver;

    protected static final Config CONFIG = new Config("npo-browser-tests.properties");



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
}
