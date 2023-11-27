package nl.specialisterren.fitnesse.fixture.slim;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import nl.hsac.fitnesse.fixture.slim.XmlHttpTest;
import nl.vpro.api.client.frontend.NpoApiAuthentication;

/**
 *  This seems just to add authentication. I wonder wether it _really_ was necessary to override every single method for this.
 */
public class NpoXmlHttpTest extends XmlHttpTest {
    protected final NpoApiAuthentication auth;

    public NpoXmlHttpTest(String apiKey, String secret, String origin) {
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
    /*@Override
    protected boolean sendFileImpl(String partName, String fileName, String serviceUrl, String method) {
        setAuthenticationHeaders(serviceUrl);

        return super.sendFileImpl(partName, fileName, serviceUrl, method);
    }*/

    //@Override
    protected boolean sendFileImpl(String fileName, String serviceUrl, String method) {
        setAuthenticationHeaders(serviceUrl);

        return super.sendFileImpl(fileName, serviceUrl, method, "POST");
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
}
