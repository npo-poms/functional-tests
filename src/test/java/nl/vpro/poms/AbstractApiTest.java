package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import nl.vpro.api.client.resteasy.NpoApiClients;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractApiTest {

    static NpoApiClients clients;

    static {
        clients = NpoApiClients
            .configured(Config.FILE.getAbsolutePath())
            .setApiBaseUrl(Config.configOption("apiBaseUrl").orElse("https://rs-test.poms.omroep.nl/v1/"))
            .build();
        clients.setTrustAll(true);
        log.info("Using {}", clients);
    }


}
