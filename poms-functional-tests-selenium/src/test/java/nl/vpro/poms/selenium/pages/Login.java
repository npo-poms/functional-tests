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
public class Login extends AbstractPage {

    private static final By usernameBy = By.id("username");

    private static final By passwdBy = By.id("password");

    private static final By loginBy = By.cssSelector("input.btn-submit");
    
    private static final String URL = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl");

    public Login(WebDriver driver ) {
        super(driver);
    }
    public void gotoPage() {
//        log.info("poms {}", URL);
        driver.navigate().to(URL);
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
        String user =  CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");
        Assume.assumeNotNull(user, password);
        gotoLogin(user, password);
    }

    public void speciaalNPOGebruiker() {
        String user = CONFIG.getProperties().get("MISGebruiker.LOGIN");
		String password = CONFIG.getProperties().get("MISGebruiker.PASSWORD");
		gotoLogin(user, password);
	}
	public void speciaalAdminGebruiker() {
		String user = CONFIG.getProperties().get("AdminGebruiker.LOGIN");
		String password = CONFIG.getProperties().get("AdminGebruiker.PASSWORD");
		gotoLogin(user, password);
	}
	public void speciaalVf() {
        String user =  CONFIG.getProperties().get("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperties().get("SpeciaalVfGebruiker.PASSWORD");
        gotoLogin(user, password);
	}

}
