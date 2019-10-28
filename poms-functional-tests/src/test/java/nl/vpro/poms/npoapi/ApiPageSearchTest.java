package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.page.PageForm;
import nl.vpro.domain.api.page.PageSearchResult;
import nl.vpro.poms.ApiSearchTestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@RunWith(Parameterized.class)
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

    ApiPageSearchTest(String name, PageForm form, String profile, MediaType mediaType) {
        super(name, form, profile, mediaType);
    }


    @Parameterized.Parameters
    static Collection<Object[]> getForms() throws IOException {
        return ApiSearchTestHelper.getForms("/examples/pages/", PageForm.class, null, "vpro", "woord");
    }

    @Test
    void search() throws Exception {
        log.info(DASHES.substring(0, 30 - "search".length()) + name);
        PageSearchResult searchResultItems = clients.getPageService().find(form, profile, "", 0L, 10);
        assumeTrue(tester.apply(searchResultItems));
        test(name, searchResultItems);
    }
}
