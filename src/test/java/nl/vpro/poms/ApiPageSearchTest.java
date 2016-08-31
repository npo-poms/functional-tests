package nl.vpro.poms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.page.PageForm;
import nl.vpro.domain.api.page.PageSearchResult;
import nl.vpro.jackson2.Jackson2Mapper;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(Parameterized.class)
public class ApiPageSearchTest extends AbstractSearchTest<PageForm, PageSearchResult> {

    {
        TESTERS.put("NPA-331.json/woord", sr -> {
            assertThat(sr.getItems()).isNotEmpty();
            }
        );
    }

    public ApiPageSearchTest(String name, PageForm form, String profile) {
        super(name, form, profile);
    }


    @Parameterized.Parameters
    public static Collection<Object[]> getForms() throws IOException {
        return ApiSearchTestHelper.getForms("/examples/pages/", PageForm.class, null, "vpro", "woord");
    }

    @Test
    public void search() throws IOException {
        System.out.println("--------------------" + name);
        PageSearchResult searchResultItems = clients.getPageService().find(form, profile, "", 0L, 10);
        Consumer<PageSearchResult> tester = TESTERS.get(name);
        if (tester != null) {
            System.out.println("USING  TESTER " + tester + " for " + name);
            tester.accept(searchResultItems);
        } else {
            System.out.println("No predicate defined for " + name);
            //Jackson2Mapper.getPrettyInstance().writeValue(System.out, searchResultItems);
        }
        File tempFile = File.createTempFile(name.replaceAll("/", "_"), ".json");
        System.out.println(tempFile);
        Jackson2Mapper.getPrettyInstance().writeValue(new FileOutputStream(tempFile), searchResultItems);
    }
}
