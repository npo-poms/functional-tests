package nl.vpro.poms.npoapi;

import org.junit.jupiter.api.Test;

import nl.vpro.domain.api.*;
import nl.vpro.domain.api.subtitles.*;
import nl.vpro.domain.subtitles.StandaloneCue;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;

class ApiSubtitlesSearchTest extends AbstractApiTest {


    @Test
    void search()  {
        SubtitlesForm form = SubtitlesForm.builder()
            .searches(SubtitlesSearch.builder()
                .text(SimpleTextMatcher.must("balkenende", SimpleMatchType.TEXT))
                .build())
            .build();

        SubtitlesSearchResult result = clients.getSubtitlesRestService().search(form, 0L, 10);

        assertThat(result.getSize()).isGreaterThan(0);

        for (SearchResultItem<? extends StandaloneCue> item : result.getItems()) {
            log.info("{} ", item);
            assertThat(item.getResult().getContent()).containsIgnoringCase("balkenende");

        }


    }


}
