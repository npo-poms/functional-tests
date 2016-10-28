package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import nl.vpro.api.client.resteasy.NpoApiClients;
import nl.vpro.api.client.utils.NpoApiMediaUtil;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractApiTest {

    static final NpoApiClients clients;
    static final NpoApiMediaUtil mediaUtil;

    static {
        clients = NpoApiClients
            .configured(Config.FILE.getAbsolutePath())
            .apiBaseUrl(Config.configOption("apiBaseUrl").orElse("https://rs-test.poms.omroep.nl/v1/"))
            .build();
        clients.setTrustAll(true);
        mediaUtil = new NpoApiMediaUtil(clients);
        log.info("Using {}", clients);
    }


}
