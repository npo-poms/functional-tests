package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.TermFacetResultItem;
import nl.vpro.domain.api.media.MediaForm;
import nl.vpro.domain.api.media.MediaSearchResult;
import nl.vpro.domain.api.media.ProgramSearchResult;
import nl.vpro.domain.media.DescendantRef;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.MediaType;
import nl.vpro.poms.ApiSearchTestHelper;
import nl.vpro.util.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
@Slf4j
public class ApiMediaParameterizedSearchTest extends AbstractSearchTest<MediaForm, MediaSearchResult> {


    {
        addTester("clips.json/null/(xml|json)", sr -> {
            for (SearchResultItem<? extends MediaObject> m : sr.getItems()) {
                assertThat(m.getResult().getMediaType()).isEqualTo(MediaType.CLIP);
            }
        });
        addTester("facet-relations-and-filter.json/null/(xml|json)", sr -> {
            assertThat(sr.getFacets().getRelations()).isNotNull();
            assertThat(sr.getFacets().getRelations().get(0).getName()).isEqualTo("labels");
        });
        addTester("facet-ageRating.json/null/(xml|json)", sr -> {
            assertThat(sr.getFacets().getAgeRatings()).isNotNull();
            assertThat(sr.getFacets().getAgeRatings()).hasSize(5);
            assertThat(sr.getFacets().getAgeRatings().get(0).getId()).isEqualTo("6");
            assertThat(sr.getFacets().getAgeRatings().get(1).getId()).isEqualTo("9");
            assertThat(sr.getFacets().getAgeRatings().get(2).getId()).isEqualTo("12");
            assertThat(sr.getFacets().getAgeRatings().get(3).getId()).isEqualTo("16");
            assertThat(sr.getFacets().getAgeRatings().get(4).getId()).isEqualTo("ALL");
        });
        addTester("facet-relations-and-subsearch.json/null/(xml|json)", sr -> {
            assertThat(sr.getFacets().getRelations()).isNotNull();
            assertThat(sr.getFacets().getRelations()).hasSize(2);
            assertThat(sr.getFacets().getRelations().get(0).getName()).isEqualTo("labels");
            for (TermFacetResultItem s : sr.getFacets().getRelations().get(0).getFacets()) {
                System.out.println("" + s);
            }


        });
        addTester("search-schedule-events.json/null/(xml|json)", sr -> {
            String testName = ApiMediaParameterizedSearchTest.this.testMethod.getMethodName();
            if (testName.startsWith("search[")
            //Config.env() != Env.DEV // SADLY on DEV 2doc events are not coming in.
                ) {
                assertThat(sr.getItems().size()).isGreaterThan(0);
            }
            for (SearchResultItem<?> item : sr.getItems()) {
                MediaObject object = (MediaObject) item.getResult();
                assertThat(object.getDescendantOf().stream().map(DescendantRef::getMidRef).collect(Collectors.toSet())).contains("POMS_S_VPRO_472240");
                assertThat(object.getScheduleEvents().stream()
                    .anyMatch(e -> e.getStartInstant().isAfter(Instant.ofEpochMilli(1369391170000L)) && e.getStartInstant().isBefore(Instant.ofEpochMilli(1503397570000L))))
                    .isTrue();

            }
        });

        addTester(Version.of(5, 5),"facet-title-az.json/null/(xml|json)", sr -> {
            String testName = ApiMediaParameterizedSearchTest.this.testMethod.getMethodName();
            if (testName.startsWith("searchMembers")) {
                // POMS_S_VPRO_417550 has no members a*
            } else {
                assertThat(sr.getFacets().getTitles()).hasSize(26);
                assertThat(sr.getFacets().getTitles().get(0).getId()).isEqualTo("a");
                assertThat(sr.getFacets().getTitles().get(1).getId()).isEqualTo("b");

                assertThat(sr.getFacets().getTitles().get(0).getCount()).isGreaterThan(0);
                assertThat(sr.getFacets().getTitles().get(1).getCount()).isGreaterThan(0);

                // this json search on a* so the facet 'a' should be selected
                // TODO: I think this fails.
                assertThat(sr.getSelectedFacets()).isNotNull();
                assertThat(sr.getSelectedFacets().getTitles()).isNotNull();
                assertThat(sr.getSelectedFacets().getTitles()).hasSize(1);
                assertThat(sr.getSelectedFacets().getTitles().get(0).getId()).isEqualTo("a");
                assertThat(sr.getFacets().getTitles().get(0).isSelected()).isTrue();
            }
        });

          addTester(Version.of(5, 4, 2),"visualsegments.json/null/(xml|json)", sr -> {
              String testName = ApiMediaParameterizedSearchTest.this.testMethod.getMethodName();
              if (testName.startsWith("search[")) {
                  assertThat(sr.getSize()).isGreaterThan(0);
              }

        });

        addAssumer("channels.json/.*/(xml|json)", minVersion(5, 3));
        addAssumer("lastModifiedDesc.json/.*/(xml|json)", minVersion(5, 3));
        addAssumer("facet-title-az.json/.*/(xml|json)", minVersion(5, 5));
        addAssumer("NPA-403-array.json/.*/json", minVersion(5, 5));



    }

    public ApiMediaParameterizedSearchTest(String name, MediaForm form, String profile, javax.ws.rs.core.MediaType mediaType) {
        super(name, form, profile, mediaType);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getForms() throws IOException {
        return ApiSearchTestHelper.getForms("/examples/media/", MediaForm.class, null, "vpro");
    }

    @Test
    public void search() throws Exception {
        log.info(DASHES.substring(0, 30 - "search".length()) + name);
        MediaSearchResult searchResultItems = clients.getMediaService().find(form, profile, null, 0L, 10);
        assumeTrue(tester.apply(searchResultItems));
        test(name, searchResultItems);
    }


    @Test
    public void searchMembers() throws Exception {
        log.info(DASHES.substring(0, 30 - "searchMembers".length()) + name);
        MediaSearchResult searchResultItems = clients.getMediaService().findMembers(form, "POMS_S_VPRO_417550", profile, null, 0L, 10);
        assumeTrue(tester.apply(searchResultItems));
        test(name + ".members.json", searchResultItems);
    }


    @Test
    public void searchEpisodes() throws Exception {
        log.info(DASHES.substring(0, 30 - "searchEpisodes".length()) + name);
        ProgramSearchResult searchResultItems = clients.getMediaService().findEpisodes(form, "AVRO_1656037", profile, null, 0L, 10);
        test(name + ".episodes.json", searchResultItems);
    }

}
