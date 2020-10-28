package nl.vpro.poms.selenium.tests.poms;

import javax.annotation.Nonnull;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.util.AbstractTest;
import nl.vpro.poms.selenium.util.pages.AbstractLogin;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;

/**
 *
 */
@RunWith(Parameterized.class)
public abstract class AbstractPomsTest extends AbstractTest {

    private static final String URL = CONFIG.getProperties(Config.Prefix.poms).get("baseUrl");



    protected AbstractPomsTest(@Nonnull Browser browser) {
        super(browser);
    }

    public AbstractLogin login() {
        return keycloakLogin(URL);
    }
}
