package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.assertj.core.api.Assumptions;
import org.assertj.core.api.Fail;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import nl.vpro.domain.api.media.RedirectEntry;
import nl.vpro.domain.api.media.RedirectList;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.util.IntegerVersion;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
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
            if (! Objects.equals(entry.getTo(), entry.getUltimate())) {
                Assumptions.assumeThat(apiVersionNumber)
                    .withFailMessage("Known to fail before 5.12. See NPA-533/ %s", entry)
                    .isGreaterThanOrEqualTo(IntegerVersion.of(5, 12));
            } else {
                // TODO: Still suffering https://jira.vpro.nl/browse/NPA-535
            }
            Fail.fail("Could not resolve %s: %s", entry, nfe.getMessage(),  nfe);

        }
    }
}
