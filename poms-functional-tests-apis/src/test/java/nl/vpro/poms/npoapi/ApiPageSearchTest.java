package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.stream.Stream;

import org.assertj.core.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.vpro.api.client.frontend.NpoApiClients;
import nl.vpro.domain.api.page.PageForm;
import nl.vpro.domain.api.page.PageSearchResult;
import nl.vpro.poms.ApiSearchTestHelper;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ApiPageSearchTest extends AbstractSearchTest<PageForm, PageSearchResult> {

    {
        addTester("NPA-331.json/woord/.*", sr -> {
            assertThat(sr.getItems()).isNotEmpty();
            }
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
}
