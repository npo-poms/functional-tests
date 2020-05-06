package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.vpro.api.client.frontend.NpoApiClients;
import nl.vpro.domain.api.*;
import nl.vpro.domain.api.media.*;
import nl.vpro.domain.media.*;
import nl.vpro.poms.ApiSearchTestHelper;
import nl.vpro.test.util.jackson2.Jackson2TestUtil;
import nl.vpro.util.IntegerVersion;
import nl.vpro.util.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


@Log4j2
public class ApiMediaParameterizedSearchTest extends AbstractSearchTest<MediaForm, MediaSearchResult> {

    @Retention(RetentionPolicy.RUNTIME)
    @ParameterizedTest(name = "Elaborate name listing all {arguments}")
    @MethodSource("getForms")
    private @interface Params {

    }
    @BeforeEach
    @Override
    public void setUp(TestInfo testInfo) {
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
            if (apiVersionNumber.isNotBefore(IntegerVersion.of(5, 12))) {
                assertThat(sr.getFacets().getAgeRatings()).hasSize(7);
                assertThat(sr.getFacets().getAgeRatings().get(0).getId()).isEqualTo("6");
                assertThat(sr.getFacets().getAgeRatings().get(1).getId()).isEqualTo("9");
                assertThat(sr.getFacets().getAgeRatings().get(2).getId()).isEqualTo("12");
                assertThat(sr.getFacets().getAgeRatings().get(3).getId()).isEqualTo("14");
                assertThat(sr.getFacets().getAgeRatings().get(4).getId()).isEqualTo("16");
                assertThat(sr.getFacets().getAgeRatings().get(5).getId()).isEqualTo("18");
                assertThat(sr.getFacets().getAgeRatings().get(6).getId()).isEqualTo("ALL");
            } else {
                assertThat(sr.getFacets().getAgeRatings()).hasSize(5);
                assertThat(sr.getFacets().getAgeRatings().get(0).getId()).isEqualTo("6");
                assertThat(sr.getFacets().getAgeRatings().get(1).getId()).isEqualTo("9");
                assertThat(sr.getFacets().getAgeRatings().get(2).getId()).isEqualTo("12");
                assertThat(sr.getFacets().getAgeRatings().get(3).getId()).isEqualTo("16");
                assertThat(sr.getFacets().getAgeRatings().get(4).getId()).isEqualTo("ALL");
            }
        });
        addTester("facet-relations-and-subsearch.json/null/(xml|json)", sr -> {
            assertThat(sr.getFacets().getRelations()).isNotNull();
            assertThat(sr.getFacets().getRelations()).hasSize(2);
            assertThat(sr.getFacets().getRelations().get(0).getName()).isEqualTo("labels");
            for (TermFacetResultItem s : sr.getFacets().getRelations().get(0).getFacets()) {
                log.info("" + s);
            }


        });
        addTester("search-schedule-events.json/null/(xml|json)", sr -> {
            String testName = testInfo.getTestMethod().get().getName();
            if (testName.startsWith("search[")
            //Config.env() != Env.DEV // SADLY on DEV 2doc events are not coming in.
                ) {
                assertThat(sr.getItems().size()).isGreaterThan(0);
            }
            for (SearchResultItem<?> item : sr.getItems()) {
                MediaObject object = (MediaObject) item.getResult();
                assertThat(object.getDescendantOf().stream().map(DescendantRef::getMidRef).collect(Collectors.toSet())).contains("POMS_S_VPRO_472240");
                assertThat(MediaObjects.getScheduleEvents(object).stream()
                    .anyMatch(e -> e.getStartInstant().isAfter(Instant.ofEpochMilli(1369391170000L)) && e.getStartInstant().isBefore(Instant.ofEpochMilli(1503397570000L))))
                    .isTrue();

            }
        });

        addTester(Version.of(5, 5),"facet-title-az.json/null/(xml|json)", sr -> {
            String testName = testInfo.getTestMethod().get().getName();
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
              String testName = testInfo.getTestMethod().get().getName();
              if (testName.startsWith("search[")) {
                  assertThat(sr.getSize()).isGreaterThan(0);
              }

        });



        addTester(Version.of(5, 12, 0),"NPA-490.json/null/(xml|json)", sr -> {
            assertThat(sr.getSize()).isEqualTo(0);
            for (SearchResultItem<? extends MediaObject> r : sr) {
                log.info("{}", r);
            }
        });

        addAssumer("channels.json/.*/(xml|json)", minVersion(5, 3));
        addAssumer("lastModifiedDesc.json/.*/(xml|json)", minVersion(5, 3));
        addAssumer("facet-title-az.json/.*/(xml|json)", minVersion(5, 5));
        addAssumer("NPA-403-array.json/.*/json", minVersion(5, 5));

        super.setUp(testInfo);

    }

    ApiMediaParameterizedSearchTest() {
    }

    public  static Stream<Arguments> getForms() throws IOException {
        return ApiSearchTestHelper.getForms(clients, "/examples/media/", MediaForm.class, null, "vpro");
    }


    @ParameterizedTest
    @Params
    void search(String name, MediaForm form, NpoApiClients clients) throws Exception {
        MediaSearchResult searchResultItems = clients.getMediaService().find(form, null, null, 0L, 10);
        assumeTrue(tester.apply(searchResultItems));
        test(form, name, searchResultItems);
    }


    @ParameterizedTest
    @Params
    void searchMembers(String name, MediaForm form, NpoApiClients clients) throws Exception {
        log.info(DASHES.substring(0, 30 - "searchMembers".length()) + name);
        MediaSearchResult searchResultItems = clients.getMediaService().findMembers(form, "POMS_S_VPRO_417550", null, null, 0L, 10);
        assumeTrue(tester.apply(searchResultItems));
        test(form, name + ".members.json", searchResultItems);
    }


    @ParameterizedTest
    @Params
    void searchEpisodes(String name, MediaForm form, NpoApiClients clients) throws Exception {
        log.info(DASHES.substring(0, 30 - "searchEpisodes".length()) + name);
        ProgramSearchResult searchResultItems = clients.getMediaService().findEpisodes(form, "AVRO_1656037",  null, null, 0L, 10);
        test(form, name + ".episodes.json", searchResultItems);
    }


    <T extends MediaObject> GenericMediaSearchResult<T> test(MediaForm form, String name, GenericMediaSearchResult<T> object) throws Exception {
        if (form  != null) {
            for (SearchResultItem<? extends T> re : object) {
                MediaObject mo = re.getResult();
                MediaSearch.TestResult testResult = form.getTestResult(mo);
                assertThat(testResult.test().getAsBoolean()).withFailMessage(testResult.getDescription()).isEqualTo(true);

            }
        }
        return Jackson2TestUtil.roundTrip(object);

    }

}
