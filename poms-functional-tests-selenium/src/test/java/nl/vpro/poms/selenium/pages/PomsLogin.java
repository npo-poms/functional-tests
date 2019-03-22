package nl.vpro.poms.selenium.pages;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import nl.vpro.api.client.utils.Config;

import static nl.vpro.poms.selenium.poms.AbstractTest.CONFIG;


@Slf4j
public class PomsLogin extends AbstractPage {

    private static final By usernameBy = By.id("username");

    private static final By passwdBy = By.id("password");

    private static final By loginBy = By.cssSelector("input.btn-submit");
    
    private static final String URL = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl");

    private final String url;

    public PomsLogin(String url, WebDriver driver ) {
        super(driver);
        this.url = url == null ? URL : url;

    }
    public void gotoPage() {
//        log.info("poms {}", URL);
        driver.navigate().to(url);
    }

    public void gotoLogin(@Nonnull String user, @Nonnull String passwd) {
        gotoPage();
        login(user, passwd);
    }

     public void login(@Nonnull String user, @Nonnull String passwd) {
        gotoPage();
//    	log.info("Log in user {}", user);
        Assume.assumeNotNull(user, passwd);
        wait.until(ExpectedConditions.elementToBeClickable(usernameBy));
        WebElement usernameElement = driver.findElement(usernameBy);
        usernameElement.sendKeys(user);
        WebElement passwdElement = driver.findElement(passwdBy);
        passwdElement.sendKeys(passwd);
        WebElement login = driver.findElement(loginBy);
        login.click();
    }



    public void VPROand3voor12() {
        String user =  CONFIG.getProperty("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperty("SpeciaalVfGebruiker.PASSWORD");
        Assume.assumeNotNull(user, password);
        gotoLogin(user, password);
    }

    public void speciaalNPOGebruiker() {
        String user = CONFIG.getProperty("MISGebruiker.LOGIN");
		String password = CONFIG.getProperty("MISGebruiker.PASSWORD");
		gotoLogin(user, password);
	}
	public void speciaalAdminGebruiker() {
		String user = CONFIG.getProperty("AdminGebruiker.LOGIN");
		String password = CONFIG.getProperty("AdminGebruiker.PASSWORD");
		gotoLogin(user, password);
	}
	public void speciaalVf() {
        String user =  CONFIG.getProperty("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperty("SpeciaalVfGebruiker.PASSWORD");
        gotoLogin(user, password);
	}

    public void gtaaBrowserTest() {
        String user =  CONFIG.getProperty("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperty("SpeciaalVfGebruiker.PASSWORD");
        login(user, password);

    }
}
