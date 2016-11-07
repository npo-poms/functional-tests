package nl.vpro.poms;

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

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(Parameterized.class)
public class ApiMediaParameterizedSearchTest extends AbstractSearchTest<MediaForm, MediaSearchResult> {



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
        TESTERS.put("facet-ageRating.json/null", sr -> {
            assertThat(sr.getFacets().getAgeRatings()).isNotNull();
            assertThat(sr.getFacets().getAgeRatings()).hasSize(5);
            assertThat(sr.getFacets().getAgeRatings().get(0).getId()).isEqualTo("6");
            assertThat(sr.getFacets().getAgeRatings().get(1).getId()).isEqualTo("9");
            assertThat(sr.getFacets().getAgeRatings().get(2).getId()).isEqualTo("12");
            assertThat(sr.getFacets().getAgeRatings().get(3).getId()).isEqualTo("16");
            assertThat(sr.getFacets().getAgeRatings().get(4).getId()).isEqualTo("ALL");
        });
    }

    public ApiMediaParameterizedSearchTest(String name, MediaForm form, String profile) {
        super(name, form, profile);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getForms() throws IOException {
        return ApiSearchTestHelper.getForms("/examples/media/", MediaForm.class, null, "vpro");
    }

    @Test
    public void search() throws Exception {
        System.out.println("--------------------" + name);
        MediaSearchResult searchResultItems = clients.getMediaService().find(form, profile, null, 0L, 10);
        Consumer<MediaSearchResult> tester = TESTERS.get(name);
        if (tester != null) {
            System.out.println("USING  TESTER " + tester + " for " + name);
            tester.accept(searchResultItems);
        } else {
            System.out.println("No predicate defined for " + name);
            //Jackson2Mapper.getPrettyInstance().writeValue(System.out, searchResultItems);
        }
        test(name, searchResultItems);
    }



    @Test
    public void searchMembers() throws Exception {
        System.out.println("----------------MEMBERS----" + name);
        MediaSearchResult searchResultItems = clients.getMediaService().findMembers(form, "POMS_S_VPRO_417550", profile, null, 0L, 10);
        Consumer<MediaSearchResult> tester = TESTERS.get(name);
        if (tester != null) {
            System.out.println("USING  TESTER " + tester + " for " + name);
            tester.accept(searchResultItems);
        } else {
            System.out.println("No predicate defined for " + name);
            //Jackson2Mapper.getPrettyInstance().writeValue(System.out, searchResultItems);
        }

        test(name + ".members.json", searchResultItems);
    }


    @Test
    public void searchEpisodes() throws Exception {
        System.out.println("--------------------EPISODES---" + name);
        ProgramSearchResult searchResultItems = clients.getMediaService().findEpisodes(form, "AVRO_1656037", profile, null, 0L, 10);
        test(name + ".episodes.json", searchResultItems);
    }

}
