package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.stream.Stream;

import javax.ws.rs.ServerErrorException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;

import nl.vpro.api.client.frontend.NpoApiClients;
import nl.vpro.domain.api.ApiScheduleEvent;
import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.media.ScheduleForm;
import nl.vpro.domain.api.media.ScheduleSearchResult;
import nl.vpro.poms.ApiSearchTestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Slf4j
class ApiScheduleSearchTest extends AbstractSearchTest<ScheduleForm, ScheduleSearchResult> {

    {
        addTester("rerun.json/null/(xml|json)", sr ->
        {
            assertThat(sr.getItems().size()).isGreaterThan(0);
            for (SearchResultItem<?> item : sr.getItems()) {
                ApiScheduleEvent event = (ApiScheduleEvent) item.getResult();
                assertThat(event.getRepeat().isRerun()).isTrue();
            }
        });
    }

    ApiScheduleSearchTest() {
    }


    static Stream<Arguments> getForms() throws IOException {
        return ApiSearchTestHelper.getForms(clients, "/examples/schedule/", ScheduleForm.class, null, "vpro", "woord");
    }

    @ParameterizedTest
    @MethodSource("getForms")
    public void search(String name, ScheduleForm form, NpoApiClients clients) throws Exception {
        log.info(DASHES.substring(0, 30 - "search".length()) + name);
        ScheduleSearchResult searchResultItems;
        try {
            searchResultItems = clients.getScheduleService().find(form, null, "", 0L, 10);
            assumeTrue(tester.apply(searchResultItems));
        } catch (ServerErrorException rs) {
            if (rs.getResponse().getStatus() == 501) {
                throw new TestAbortedException(name + " seems to be not implemented yet");
            } else {
                throw rs;
            }
        }
        test(name, searchResultItems);

    }
}
