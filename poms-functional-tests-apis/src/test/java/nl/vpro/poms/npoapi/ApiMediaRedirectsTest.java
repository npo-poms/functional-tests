package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.Lists;

import nl.vpro.domain.api.media.RedirectEntry;
import nl.vpro.domain.api.media.RedirectList;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Log4j2
class ApiMediaRedirectsTest extends AbstractApiTest {

    static Collection<Object[]> getRedirects() {
        RedirectList list = getRedirectsList();
        return list.getList()
            .stream()
            .map(e -> new Object[]{e})
            .collect(Collectors.toList());
    }

    static RedirectList getRedirectsList() {
        try (Response response = clients.getMediaService().redirects(null)) {
            assertThat(response.getStatus()).isEqualTo(200);
            RedirectList list = response.readEntity(RedirectList.class);
            assertThat(list).isNotEmpty();
            return list;
        }
    }

    @Test
    @Disabled("Used to find a the mids to republish to fix NPA-535 (for tests)")
    public void showRedirects() {
        Set<String> mids = new HashSet<>();
        getRedirectsList().forEach(re -> {
            mids.add(re.getFrom());
            mids.add(re.getTo());
        });
        List<String> list = new ArrayList<>(mids);
        List<List<String>> partition = Lists.partition(list, 200);
        log.info("relevant mids: {}", list.size());
        for (List<String> p : partition) {
            log.info("{}", String.join(",", p));
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
            try {
                clients.getMediaService().load(entry.getTo(), null, null);

                Fail.fail("Destination should give 404 too %s", entry);
            } catch (javax.ws.rs.NotFoundException unfe) {
                //  https://jira.vpro.nl/browse/NPA-535

                log.info("OK destination {} is gives 404 too", entry.getTo());
            }
        }
    }
}
