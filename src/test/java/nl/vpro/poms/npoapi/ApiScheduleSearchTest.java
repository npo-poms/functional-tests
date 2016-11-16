package nl.vpro.poms.npoapi;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

import javax.ws.rs.ServerErrorException;

import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.media.ScheduleForm;
import nl.vpro.domain.api.media.ScheduleSearchResult;
import nl.vpro.poms.ApiSearchTestHelper;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(Parameterized.class)
public class ApiScheduleSearchTest extends AbstractSearchTest<ScheduleForm, ScheduleSearchResult> {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    {
        TESTERS.put("facet-broadcaster-ned3.xml/null", sr -> {
            Assume.assumeTrue("Known to fail. Facetting in schedule search not supported. See NPA-337", false);
            assertThat(sr.getFacets().getBroadcasters()).isNotEmpty(); // FAILS
        });
    }
    public ApiScheduleSearchTest(String name, ScheduleForm form, String profile) {
        super(name, form, profile);
    }


    @Parameterized.Parameters
    public static Collection<Object[]> getForms() throws IOException {
        return ApiSearchTestHelper.getForms("/examples/schedule/", ScheduleForm.class, null, "vpro", "woord");
    }

    @Test
    public void search() throws Exception {
        System.out.println("--------------------" + name);
        ScheduleSearchResult searchResultItems;
        try {
            searchResultItems = clients.getScheduleService().find(form, profile, "", 0L, 10);
            Consumer<ScheduleSearchResult> tester = TESTERS.get(name);
            if (tester != null) {
                System.out.println("USING  TESTER " + tester + " for " + name);
                tester.accept(searchResultItems);
            } else {
                System.out.println("No predicate defined for " + name);
            }
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
