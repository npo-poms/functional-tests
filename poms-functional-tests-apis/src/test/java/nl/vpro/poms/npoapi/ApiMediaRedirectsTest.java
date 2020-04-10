package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import nl.vpro.domain.api.media.RedirectEntry;
import nl.vpro.domain.api.media.RedirectList;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Log4j2
class ApiMediaRedirectsTest extends AbstractApiTest {

    static Collection<Object[]> getRedirects() {
        try (Response response = clients.getMediaService().redirects(null)) {
            assertThat(response.getStatus()).isEqualTo(200);
            RedirectList list = response.readEntity(RedirectList.class);
            assertThat(list).isNotEmpty();
            return list.getList()
                .stream()
                .map(e -> new Object[]{e})
                .collect(Collectors.toList());
        }
    }

    @ParameterizedTest
    @MethodSource("getRedirects")
    void testRedirect(RedirectEntry entry) {
        try {
            assertThat(clients.getMediaService()
                .load(entry.getFrom(), null, null)
                .getMid()
            ).isEqualTo(entry.getUltimate());
        } catch (javax.ws.rs.NotFoundException nfe) {
            if (apiVersionNumber.isBefore(5, 12)) {
                log.info("NPA-533");
                throw new NotFoundException(nfe.getMessage() + " (known to fail in this version  NPA-533", nfe);
            }
            throw nfe;
        }
    }
}
