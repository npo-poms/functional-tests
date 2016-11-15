package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import javax.ws.rs.core.MediaType;

import nl.vpro.api.client.resteasy.NpoApiClients;
import nl.vpro.api.client.utils.NpoApiMediaUtil;
import nl.vpro.rs.media.MediaRestClient;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractApiTest {


    protected static final Duration ACCEPTABLE_DURATION_FRONTEND = Duration.ofMinutes(10);

    protected static final NpoApiClients clients =
        NpoApiClients.configured(Config.env(), Config.getProperties(Config.Prefix.npoapi))
            .mediaType(MediaType.APPLICATION_XML_TYPE)
            .build();
    protected static final NpoApiMediaUtil mediaUtil;
    protected static final MediaRestClient backend = MediaRestClient.configured(Config.env(), Config.getProperties(Config.Prefix.backendapi)).build();


    static {
        clients.setTrustAll(true);
        mediaUtil = new NpoApiMediaUtil(clients);
        log.info("Using {}", clients);
    }


}
