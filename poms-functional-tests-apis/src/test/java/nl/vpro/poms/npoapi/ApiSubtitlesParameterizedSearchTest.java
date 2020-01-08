package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.vpro.api.client.frontend.NpoApiClients;
import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.subtitles.SubtitlesForm;
import nl.vpro.domain.api.subtitles.SubtitlesSearchResult;
import nl.vpro.domain.subtitles.StandaloneCue;
import nl.vpro.poms.ApiSearchTestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Log4j2
class ApiSubtitlesParameterizedSearchTest extends AbstractSearchTest<SubtitlesForm, SubtitlesSearchResult> {

    {
        addTester("bla.json/(xml|json)", sr -> {
            for (SearchResultItem<? extends StandaloneCue> m : sr.getItems()) {
                assertThat(m.getResult().getContent()).containsIgnoringCase("bla");
            }
        });
        addTester("empty.json/(xml|json)", sr -> {
            assertThat(sr.getItems()).hasSize(10);
        });
    }

    static Stream<Arguments> getForms() throws IOException {
        return ApiSearchTestHelper.getForms(clients, "/examples/subtitles/", SubtitlesForm.class);
    }

    @ParameterizedTest
    @MethodSource("getForms")
    void search(String name, SubtitlesForm form, NpoApiClients clients) throws Exception {

        log.info(DASHES.substring(0, 30 - "search".length()) + name);
        SubtitlesSearchResult searchResultItems = clients.getSubtitlesRestService().search(form, 0L, 10);
        assumeTrue(tester.apply(searchResultItems));
        test(name, searchResultItems);
    }



}
