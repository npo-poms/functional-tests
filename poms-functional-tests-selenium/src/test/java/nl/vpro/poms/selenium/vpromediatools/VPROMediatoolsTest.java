package nl.vpro.poms.selenium.vpromediatools;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;

import nl.vpro.poms.selenium.AbstractTest5;
import nl.vpro.poms.selenium.pages.AbstractLogin;
import nl.vpro.poms.selenium.util.WebDriverFactory;

public class VPROMediatoolsTest extends AbstractTest5 {

    public VPROMediatoolsTest() {
    }

    @Override
    protected AbstractLogin login(WebDriverFactory.Browser browser) {
        // TODO
        return null;

    }

    @ParameterizedTest
    @AbstractTest5.Browsers
    @Disabled("Not yet implemented")
    public void SPOMSVPROTOOLS1(){

    }


}
