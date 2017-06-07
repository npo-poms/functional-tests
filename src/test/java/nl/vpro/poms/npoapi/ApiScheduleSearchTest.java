package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;

import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.media.ScheduleForm;
import nl.vpro.domain.api.media.ScheduleSearchResult;
import nl.vpro.poms.ApiSearchTestHelper;

import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
@Slf4j
public class ApiScheduleSearchTest extends AbstractSearchTest<ScheduleForm, ScheduleSearchResult> {



    public ApiScheduleSearchTest(String name, ScheduleForm form, String profile, MediaType mediaType) {
        super(name, form, profile, mediaType);
    }


    @Parameterized.Parameters
    public static Collection<Object[]> getForms() throws IOException {
        return ApiSearchTestHelper.getForms("/examples/schedule/", ScheduleForm.class, null, "vpro", "woord");
    }

    @Test
    public void search() throws Exception {
        log.info(DASHES.substring(0, 30 - "search".length()) + name);
        ScheduleSearchResult searchResultItems;
        try {
            searchResultItems = clients.getScheduleService().find(form, profile, "", 0L, 10);
            assumeTrue(tester.apply(searchResultItems));
        } catch (ServerErrorException rs) {
            if (rs.getResponse().getStatus() == 501) {
                throw new AssumptionViolatedException(name + " seems to be not implemented yet");
            } else {
                throw rs;
            }
        }
        test(name, searchResultItems);

    }
}
