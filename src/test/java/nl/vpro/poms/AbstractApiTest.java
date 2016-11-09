package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import nl.vpro.api.client.resteasy.NpoApiClients;
import nl.vpro.api.client.utils.NpoApiMediaUtil;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractApiTest {


    static final Duration ACCEPTABLE_DURATION_FRONTEND = Duration.ofMinutes(10);

    static final NpoApiClients clients;
    static final NpoApiMediaUtil mediaUtil;

    static {
        clients = NpoApiClients
            .configured(Config.env(), Config.getProperties(Config.Prefix.npoapi))

            .build();
        clients.setTrustAll(true);
        mediaUtil = new NpoApiMediaUtil(clients);
        log.info("Using {}", clients);
    }


}
