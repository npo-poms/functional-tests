package nl.vpro.poms;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.media.ScheduleForm;
import nl.vpro.domain.api.media.ScheduleSearchResult;

@RunWith(Parameterized.class)
public class ApiScheduleSearchTest extends AbstractSearchTest<ScheduleForm, ScheduleSearchResult> {

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
        ScheduleSearchResult searchResultItems = clients.getScheduleService().find(form, profile, "", 0L, 10);
        Consumer<ScheduleSearchResult> tester = TESTERS.get(name);
        if (tester != null) {
            System.out.println("USING  TESTER " + tester + " for " + name);
            tester.accept(searchResultItems);
        } else {
            System.out.println("No predicate defined for " + name);
        }
        test(name, searchResultItems);

    }
}
