package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.subtitles.SubtitlesForm;
import nl.vpro.domain.api.subtitles.SubtitlesSearchResult;
import nl.vpro.domain.subtitles.StandaloneCue;
import nl.vpro.poms.ApiSearchTestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Slf4j
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

    static Collection<Object[]> getForms() throws IOException {
        return ApiSearchTestHelper.getForms("/examples/subtitles/", SubtitlesForm.class);
    }

    @ParameterizedTest
    @MethodSource("getForms")
    void search(String name, SubtitlesForm form, MediaType mediaType) throws Exception {

        log.info(DASHES.substring(0, 30 - "search".length()) + name);
        SubtitlesSearchResult searchResultItems = clients.getSubtitlesRestService().search(form, 0L, 10);
        assumeTrue(tester.apply(searchResultItems));
        test(name, searchResultItems);
    }



}
