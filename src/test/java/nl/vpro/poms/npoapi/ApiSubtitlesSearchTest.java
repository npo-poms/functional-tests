package nl.vpro.poms.npoapi;

import org.junit.Test;

import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.SimpleMatchType;
import nl.vpro.domain.api.SimpleTextMatcher;
import nl.vpro.domain.api.subtitles.SubtitlesForm;
import nl.vpro.domain.api.subtitles.SubtitlesSearch;
import nl.vpro.domain.api.subtitles.SubtitlesSearchResult;
import nl.vpro.domain.subtitles.StandaloneCue;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiSubtitlesSearchTest extends AbstractApiTest {


    @Test
    public void search()  {
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
