package nl.vpro.poms.selenium.pages;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.util.WebDriverUtil;

import java.util.Optional;

import static nl.vpro.poms.selenium.poms.AbstractTest.CONFIG;


@Slf4j
public class PomsLogin extends AbstractPage {

    private static final By usernameBy = By.id("username");

    private static final By passwdBy = By.id("password");

    private static final By loginBy = By.cssSelector("input.btn-primary");

    private static final String POMS_URL = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl");
    private static final String GTAA_URL = CONFIG.getProperties(Config.Prefix.npo_api).get("baseUrl")+ "/thesaurus/example/secure";

    private final String url;

    public PomsLogin(String url, WebDriverUtil util) {
        super(util);
        this.url = url == null ? POMS_URL : url;

    }

    public void gotoPage() {
        webDriverUtil.getDriver().navigate().to(url);
    }

    public void gotoLogin(@NonNull String user, @NonNull  String passwd) {
        //To download the popup.js we need to be loggedin in the Frontend API
        webDriverUtil.getDriver().navigate().to(GTAA_URL);
        login(user, passwd);
        webDriverUtil.getDriver().navigate().to(POMS_URL);
        loginKeyCloak(user, passwd);
        gotoPage();
    }

     public void login(@NonNull String user, @NonNull String passwd) {
         webDriverUtil.waitAndSendkeys(usernameBy, user);
         webDriverUtil.waitAndSendkeys(passwdBy, passwd);
         webDriverUtil.waitAndClick(By.cssSelector("input.btn-submit"));
    }

    public void loginKeyCloak(@NonNull String user, @NonNull String passwd) {
        webDriverUtil.waitAndSendkeys(usernameBy, user);
        webDriverUtil.waitAndSendkeys(passwdBy, passwd);
        webDriverUtil.waitAndClick(loginBy);
    }

    public void VPROand3voor12() {
        String user =  CONFIG.getProperty("SpeciaalVfGebruiker.LOGIN");
        String password =  CONFIG.getProperty("SpeciaalVfGebruiker.PASSWORD");
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
        gotoLogin(user, password);

    }
}
