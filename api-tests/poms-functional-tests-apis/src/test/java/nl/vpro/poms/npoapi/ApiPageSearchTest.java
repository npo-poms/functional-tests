package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.vpro.api.client.frontend.NpoApiClients;
import nl.vpro.domain.api.TermFacetResultItem;
import nl.vpro.domain.api.page.PageForm;
import nl.vpro.domain.api.page.PageSearchResult;
import nl.vpro.poms.ApiSearchTestHelper;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class ApiPageSearchTest extends AbstractSearchTest<PageForm, PageSearchResult> {

    {
        addTester("NPA-331.json/woord/.*", sr ->
            assertThat(sr.getItems()).isNotEmpty()
        );
        addAssumer("regexp.json/.*", minVersion(4, 8, 6));
        addAssumer("tags.json/.*", minVersion(4, 8, 6));

    }

    ApiPageSearchTest() {

    }


    static Stream<Arguments> getForms() throws IOException {
        return ApiSearchTestHelper.getForms(clients, "/examples/pages/", PageForm.class, null, "vpro", "woord");
    }

    @ParameterizedTest
    @MethodSource("getForms")
    void search(String name, PageForm form, NpoApiClients clients) throws Exception {
        log.info(DASHES.substring(0, 30 - "search".length()) + name);
        PageSearchResult searchResultItems = clients.getPageService().find(form, null, "", 0L, 10);
        Assumptions.assumeThat(tester.apply(searchResultItems)).isTrue();
        test(name, searchResultItems);
    }

    @Test
    void facetPortals() {
        PageForm form = PageForm.builder()
            .portalFacet()
            .build();

        PageSearchResult searchResultItems = clients.getPageService().find(form, null, "", 0L, 10);

        List<TermFacetResultItem> portals = searchResultItems.getFacets().getPortals();
        boolean allIdsEqualValue = true;
        for (TermFacetResultItem p : portals) {
            log.info("{}: {}", p.getValue(), p);
            if (!p.getId().equals(p.getValue())) {
                allIdsEqualValue = false;
            }
        }
        assertThat(allIdsEqualValue).isFalse(); // for some this may be a fall back, but the 'value' should be the display value as provided by the portal service.


    }
}
