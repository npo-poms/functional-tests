package nl.specialisterren.fitnesse.fixture.slim;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import nl.hsac.fitnesse.fixture.slim.JsonHttpTest;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import nl.vpro.api.client.frontend.NpoApiAuthentication;

/**
 * This seems just to add authentication. I wonder wether it _really_ was necessary to override every single method for this.
 */
public class NpoJsonHttpTest extends JsonHttpTest {
    protected final NpoApiAuthentication auth;

    public NpoJsonHttpTest(String apiKey, String secret, String origin) {
        auth = new NpoApiAuthentication(apiKey, secret, origin);
    }

    @Override
    protected boolean getImpl(String serviceUrl, boolean followRedirect) {
        setAuthenticationHeaders(serviceUrl);

        return super.getImpl(serviceUrl, followRedirect);
    }

    @Override
    protected boolean sendToImpl(String body, String serviceUrl, String aContentType, String method) {
        setAuthenticationHeaders(serviceUrl);

        return super.sendToImpl(body, serviceUrl, aContentType, method);
    }

    // newer api will require this

/*
    @Override
    protected boolean sendFileImpl(String partName, String fileName, String serviceUrl, String method) {
        setAuthenticationHeaders(serviceUrl);

        return super.sendFileImpl(partName, fileName, serviceUrl, method);
    }
    */
     @Override
    protected boolean sendFileImpl(String fileName, String serviceUrl, String method) {
        setAuthenticationHeaders(serviceUrl);

         return super.sendFileImpl(fileName, serviceUrl, method);
    }

    @Override
    public boolean headFrom(String serviceUrl) {
        setAuthenticationHeaders(serviceUrl);

        return super.headFrom(serviceUrl);
    }

    protected void setAuthenticationHeaders(String url) {
        Map<String, Object> authHeaders;
        String paramsUrl = createUrlWithParams(url);

        try {
            authHeaders = auth.authenticate(new URI(paramsUrl));
        } catch (URISyntaxException e) {
            throw new SlimFixtureException("Unable to parse URL for API authentication!", e);
        }

        for (Map.Entry<String,Object> h : authHeaders.entrySet()) {
            setValueForHeader(h.getValue(), h.getKey());
        }
    }

    public boolean isSingleValue(Object object) {
		return (object != null && !(object instanceof net.minidev.json.JSONArray));
	}

    public Object elementOfJsonPathDefault(int index, String path, Object defaultValue) {
        try {
            return elementOfJsonPath(index, path);
        } catch (IndexOutOfBoundsException e) {
            return defaultValue;
        }
    }

	public boolean repeatUntilJsonPathOfFirstItemsIsNot(final String jsonPath, int numberOfItems, final Object expectedValue) {
		RepeatCompletion completion;
        if (expectedValue == null) {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
					Object actual;
					boolean result = true;
					for (int i=0; i < numberOfItems; i++) {
						actual = jsonPath(String.format(jsonPath, i));
						result = result && (actual != null);
					}
                    return result;
                }
            };
        } else {
            completion = new RepeatLastCall() {
                @Override
                public boolean isFinished() {
					Object actual;
					boolean result = true;
					for (int i=0; i < numberOfItems; i++) {
						actual = jsonPath(String.format(jsonPath, i));
						result = result && !compareActualToExpected(expectedValue, actual);
					}
                    return result;
                }
            };
        }
        return repeatUntil(completion);
	}

	public String storeIfDevElseStoreCurrentEnvIs(String valueDev, String valueTest, String env) {
		if (env.equals("Dev"))
			return valueDev;

		return valueTest;
	}
}
