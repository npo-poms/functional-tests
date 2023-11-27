package nl.specialisterren.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.slim.XmlHttpTest;

/**
 * Extension of XmlHttpTest that adds functionality to allow non-xml responses
 *
 * XmlHttpTest returns false on getFrom, postTo and so on when the response
 * has a body and that body is not xml. This is very inconvenient when testing
 * APIs that return responses of various types. Therefore, this class allows
 * turning that behavior on and off.
 *
 * Note that xPath and all related functions still throw an exception if the
 * response body is not xml. Therefore, as long as you run at least 1 XPath
 * after a request that should return an xml response, your test will still
 * fail on receiving a non-xml response, even if you called
 * acceptNonXmlResponsesInSubsequentRequests.
 */
public class HybridXmlHttpTest extends XmlHttpTest {
    protected boolean acceptNonXmlResponses = false;

    public void acceptNonXmlResponsesInSubsequentRequests() {
        acceptNonXmlResponses = true;
    }

    public void rejectNonXmlResponsesInSubsequentRequests() {
        acceptNonXmlResponses = false;
    }

    @Override
    public boolean responseIsValid() {
        if (acceptNonXmlResponses) {
            // Return true or false as HttpTest would if throwExceptionOnHttpRequestFailure were false
            // We rarely enable those exceptions anyway, and this way we avoid relying on implementation details,
            // greatly reducing the probability that a HSAC update will break this override
            int statusCode = responseStatus();
            return !(statusCode < 100 || (statusCode >= 400 && statusCode <= 599));
        } else {
            return super.responseIsValid();
        }
    }
	
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
