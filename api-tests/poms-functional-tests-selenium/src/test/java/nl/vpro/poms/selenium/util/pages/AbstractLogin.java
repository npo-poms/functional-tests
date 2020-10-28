package nl.vpro.poms.selenium.util.pages;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import nl.vpro.poms.selenium.util.WebDriverUtil;

import static nl.vpro.poms.selenium.tests.poms.AbstractPomsTest.CONFIG;

/**
 * We use single signon for most deployments in the POMS Eco system.
 *
 * Currently either CAS or KEYCLOAK (sharing the same log ins). We're moving to Keycloak.
 */
@Log4j2
public abstract class AbstractLogin extends AbstractPage {


    private final String url;

    protected AbstractLogin(@NonNull String url, @NonNull WebDriverUtil util) {
        super(util);
        this.url = url;
    }

    public void gotoLogin(@NonNull String user, @NonNull  String passwd) {
        webDriverUtil.getDriver().navigate().to(url);
        login(user, passwd);
        gotoPage();
    }


    public void gotoPage() {
        webDriverUtil.getDriver().navigate().to(url);
    }

    abstract void login(@NonNull String user, @NonNull String passwd);


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
