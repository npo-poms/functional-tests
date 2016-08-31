package nl.vpro.poms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import nl.vpro.api.client.resteasy.NpoApiClients;
import nl.vpro.domain.api.page.PageForm;
import nl.vpro.domain.api.page.PageSearchResult;
import nl.vpro.jackson2.Jackson2Mapper;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(Parameterized.class)
public class ApiPageTest {

    private static Map<String, Consumer<PageSearchResult>> TESTERS = new HashMap<>();
    static {
        TESTERS.put("NPA-331.json/woord", sr -> {
            assertThat(sr.getItems()).isNotEmpty();
            }
        );
    }


    String name;
    PageForm form;
    String profile;

    static NpoApiClients clients;
    @BeforeClass
    public static void initialize() throws IOException {
        clients = NpoApiClients.configured(Config.FILE.getAbsolutePath()).build();

    }

    public ApiPageTest(String name, PageForm form, String profile) {
        this.name = name;
        this.form = form;
        this.profile = profile;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getForms() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/examples/pages/*.json");
        List<Map.Entry<String, PageForm>> forms = new ArrayList<>();
        for (Resource resource : resources) {
            try {
                PageForm form = Jackson2Mapper.getLenientInstance().readerFor(PageForm.class).readValue(resource.getInputStream());
                forms.add(new AbstractMap.SimpleEntry<>(resource.getFilename(), form));
            } catch (Exception e) {

            }
        }
        List<Object[]> result = new ArrayList<>();
        for (String profile : Arrays.asList(null, "vpro", "woord")) {
            for (Map.Entry<String, PageForm> e: forms) {
                result.add(new Object[]{e.getKey() + "/" + profile, e.getValue(), profile});
            }
        }
        return result;
    }

    @Test
    public void search() throws IOException {
        System.out.println("--------------------" + name);
        PageSearchResult searchResultItems = clients.getPageService().find(form, profile, "", 0L, 10);
        Consumer<PageSearchResult> tester = TESTERS.get(name);
        if (tester != null) {
            System.out.println("USING  PREDICATE " + tester + " for " + name);
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
