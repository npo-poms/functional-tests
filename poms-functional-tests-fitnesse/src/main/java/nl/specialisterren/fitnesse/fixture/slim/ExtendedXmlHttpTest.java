package nl.specialisterren.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.slim.XmlHttpTest;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import nl.vpro.api.client.frontend.NpoApiAuthentication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ExtendedXmlHttpTest extends XmlHttpTest {
	public boolean repeatUntilXPathIsOr(final String xPath, final String expectedValue1, final String expectedValue2) {
        RepeatCompletion completion;
        if (expectedValue1 == null || expectedValue2 == null) {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    return xPath(xPath) == null;
                }
            };
        } else {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
                    Object actual = xPath(xPath);
                    return compareActualToExpected(expectedValue1, actual) || compareActualToExpected(expectedValue2, actual);
                }
            };
        }
        return repeatUntil(completion);
    }
}
