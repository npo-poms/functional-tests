package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.subtitles.SubtitlesForm;
import nl.vpro.domain.api.subtitles.SubtitlesSearchResult;
import nl.vpro.poms.ApiSearchTestHelper;

import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
@Slf4j
public class ApiSubtitlesParameterizedSearchTest extends AbstractSearchTest<SubtitlesForm, SubtitlesSearchResult> {


    public ApiSubtitlesParameterizedSearchTest(String name, SubtitlesForm form, javax.ws.rs.core.MediaType mediaType) {
        super(name, form, null, mediaType);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getForms() throws IOException {
        return ApiSearchTestHelper.getForms("/examples/subtitles/", SubtitlesForm.class);
    }

    @Test
    public void search() throws Exception {
        log.info(DASHES.substring(0, 30 - "search".length()) + name);
        SubtitlesSearchResult searchResultItems = clients.getSubtitlesRestService().search(form, 0L, 10);
        assumeTrue(tester.apply(searchResultItems));
        test(name, searchResultItems);
    }



}
