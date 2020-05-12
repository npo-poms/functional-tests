package nl.specialisterren.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.slim.JsonHttpTest;
import nl.vpro.api.client.frontend.NpoApiAuthentication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class NpoJsonHttpTest extends JsonHttpTest {
    protected NpoApiAuthentication auth;

    public NpoJsonHttpTest(String apiKey, String secret, String origin) {
        auth = new NpoApiAuthentication(apiKey, secret, origin);
    }

    @Override
    protected boolean getImpl(String serviceUrl, boolean followRedirect) {
        System.out.println("Authenticating GET request with URL \"" + serviceUrl + "\"");
        Map<String, Object> authHeaders;
        try {
            authHeaders = auth.authenticate(new URI(createUrlWithParams(serviceUrl)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        for (Map.Entry<String,Object> h : authHeaders.entrySet()) {
            System.out.println("Setting value \"" + h.getValue() + "\" for header \"" + h.getKey() + "\"");
            setValueForHeader(h.getValue(), h.getKey());
        }

        return super.getImpl(serviceUrl, followRedirect);
    }
}
