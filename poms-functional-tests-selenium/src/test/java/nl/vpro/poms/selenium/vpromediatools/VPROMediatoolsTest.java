package nl.vpro.poms.selenium.vpromediatools;

import nl.vpro.poms.selenium.AbstractTest;
import nl.vpro.poms.selenium.pages.AbstractLogin;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;

public class VPROMediatoolsTest extends AbstractTest {

    public VPROMediatoolsTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Override
    protected AbstractLogin login() {
        // TODO
        return null;

    }

    @Test
    @Ignore("Not yet implemented")
    public void SPOMSVPROTOOLS1(){

    }


}
