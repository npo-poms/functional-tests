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
import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.media.MediaForm;
import nl.vpro.domain.api.media.MediaSearchResult;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.MediaType;
import nl.vpro.jackson2.Jackson2Mapper;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(Parameterized.class)
public class ApiMediaTest {

    private static Map<String, Consumer<MediaSearchResult>> TESTERS = new HashMap<>();
    static {
        TESTERS.put("clips.json/null", sr -> {
            for (SearchResultItem<? extends MediaObject> m : sr.getItems()) {
                assertThat(m.getResult().getMediaType()).isEqualTo(MediaType.CLIP);
            }
        });
        TESTERS.put("facet-relations-and-filter.json/null", sr -> {
            assertThat(sr.getFacets().getRelations()).isNotNull();
            assertThat(sr.getFacets().getRelations().get(0).getName()).isEqualTo("labels");


        });
    }


    String name;
    MediaForm form;
    String profile;

    static NpoApiClients clients;
    @BeforeClass
    public static void initialize() throws IOException {
        clients = NpoApiClients.configured(Config.FILE.getAbsolutePath()).build();

    }

    public ApiMediaTest(String name, MediaForm form, String profile) {
        this.name = name;
        this.form = form;
        this.profile = profile;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getForms() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/examples/media/*.json");
        List<Map.Entry<String, MediaForm>> forms = new ArrayList<>();
        for (Resource resource : resources) {
            try {
                MediaForm form = Jackson2Mapper.getLenientInstance().readerFor(MediaForm.class).readValue(resource.getInputStream());
                forms.add(new AbstractMap.SimpleEntry<>(resource.getFilename(), form));
            } catch (Exception e) {

            }
        }
        List<Object[]> result = new ArrayList<>();
        for (String profile : Arrays.asList(null, "vpro")) {
            for (Map.Entry<String, MediaForm> e: forms) {
                result.add(new Object[]{e.getKey() + "/" + profile, e.getValue(), profile});
            }
        }
        return result;
    }

    @Test
    public void search() throws IOException {
        System.out.println("--------------------" + name);
        MediaSearchResult searchResultItems = clients.getMediaService().find(form, profile, "", 0L, 10);
        Consumer<MediaSearchResult> tester = TESTERS.get(name);
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
