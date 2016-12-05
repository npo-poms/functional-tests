package nl.vpro.poms;

import lombok.extern.slf4j.Slf4j;

import nl.vpro.rs.media.MediaRestClient;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractApiMediaBackendTest extends AbstractApiTest {

    protected static final MediaRestClient backend =
        MediaRestClient.configured(Config.env(), Config.getProperties(Config.Prefix.backendapi))
            .trustAll(true)
            .build();
    protected static final String backendVersion = backend.getVersion();
    protected static Float backendVersionNumber;


    static {
        try {
            backendVersionNumber = backend.getVersionNumber();
        } catch (Exception e) {
            backendVersionNumber = 0f;

        }
        log.info("Using {} ({})", backend, backendVersion);
    }


}
