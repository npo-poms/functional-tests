package nl.vpro.poms.selenium.pages;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.util.WebDriverUtil;

import static nl.vpro.poms.selenium.poms.AbstractTest.CONFIG;


@Slf4j
public class PomsLogin extends AbstractPage {

    private static final By usernameBy = By.id("username");

    private static final By passwdBy = By.id("password");

    private static final By loginBy = By.cssSelector("input.btn-primary");

    private static final String URL = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl");

    private final String url;

    public PomsLogin(String url, WebDriverUtil util) {
        super(util);
        this.url = url == null ? URL : url;

    }
    public void gotoPage() {
//        log.info("poms {}", URL);
//
//
        webDriverUtil.getDriver().navigate().to(url);
    }

    public void gotoLogin(@NonNull String user, @NonNull  String passwd) {
        gotoPage();
        login(user, passwd);
        // TODO: known issue: login will not sent back to correct url
        gotoPage();
    }

     public void login(@NonNull String user, @NonNull String passwd) {
//    	log.info("Log in user {}", user);
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
