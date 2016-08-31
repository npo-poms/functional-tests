package nl.vpro.poms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.media.MediaForm;
import nl.vpro.domain.api.media.MediaSearchResult;
import nl.vpro.domain.api.media.ProgramSearchResult;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.MediaType;
import nl.vpro.jackson2.Jackson2Mapper;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(Parameterized.class)
public class ApiMediaSearchTest extends AbstractSearchTest<MediaForm, MediaSearchResult> {


    {
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

    public ApiMediaSearchTest(String name, MediaForm form, String profile) {
        super(name, form, profile);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getForms() throws IOException {
        return ApiSearchTestHelper.getForms("/examples/media/", MediaForm.class, null, "vpro");
    }

    @Test
    public void search() throws IOException {
        System.out.println("--------------------" + name);
        MediaSearchResult searchResultItems = clients.getMediaService().find(form, profile, "", 0L, 10);
        Consumer<MediaSearchResult> tester = TESTERS.get(name);
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


    @Test
    public void searchMembers() throws IOException {
        System.out.println("----------------MEMBERS----" + name);
        MediaSearchResult searchResultItems = clients.getMediaService().findMembers(form, "AVRO_1656037", profile, "", 0L, 10);
        Consumer<MediaSearchResult> tester = TESTERS.get(name);
        if (tester != null) {
            System.out.println("USING  TESTER " + tester + " for " + name);
            tester.accept(searchResultItems);
        } else {
            System.out.println("No predicate defined for " + name);
            //Jackson2Mapper.getPrettyInstance().writeValue(System.out, searchResultItems);
        }
        File tempFile = File.createTempFile(name.replaceAll("/", "_"), ".members.json");
        System.out.println(tempFile);
        Jackson2Mapper.getPrettyInstance().writeValue(new FileOutputStream(tempFile), searchResultItems);
    }


    @Test
    public void searchEpisodes() throws IOException {
        System.out.println("--------------------EPISODES---" + name);
        ProgramSearchResult searchResultItems = clients.getMediaService().findEpisodes(form, "AVRO_1656037", profile, "", 0L, 10);
        File tempFile = File.createTempFile(name.replaceAll("/", "_"), ".episodes.json");
        System.out.println(tempFile);
        Jackson2Mapper.getPrettyInstance().writeValue(new FileOutputStream(tempFile), searchResultItems);
    }
}
