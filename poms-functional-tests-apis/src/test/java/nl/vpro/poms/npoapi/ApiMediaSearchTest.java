package nl.vpro.poms.npoapi;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import nl.vpro.domain.api.*;
import nl.vpro.domain.api.media.*;
import nl.vpro.domain.api.profile.Profile;
import nl.vpro.domain.constraint.PredicateTestResult;
import nl.vpro.domain.media.*;
import nl.vpro.domain.user.Broadcaster;
import nl.vpro.jackson2.Jackson2Mapper;
import nl.vpro.poms.AbstractApiTest;

import static nl.vpro.domain.api.FacetResults.toSimpleMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class ApiMediaSearchTest extends AbstractApiTest {


    @Test
    void searchWithProfileNone()  {
        assertThat(clients.getMediaService().find(new MediaForm(), null, "none", 0L, 10).asResult()).hasSize(10);

    }

    @Test
    void facetBroadcastersWithProfile() {
        MediaForm form = MediaForm.builder()
            .broadcasterFacet()
            .build();

        Map<String, Long> withProfile = toSimpleMap(clients.getMediaService().find(form, "vpro-predictions", "none", 0L, 0).getFacets().getBroadcasters());
        Map<String, Long> withoutProfile = toSimpleMap(clients.getMediaService().find(form, null, "none", 0L, 0).getFacets().getBroadcasters());
        for (String k : withoutProfile.keySet()) {
            assertThat(withoutProfile.get(k)).isGreaterThanOrEqualTo(withProfile.getOrDefault(k, 0L));
        }
        log.info("{} -> {}", withoutProfile, withProfile);

    }
    @Test
    void facetBroadcastersAndSearchWithProfile() {
        MediaForm form = MediaForm.builder()
            .broadcasterFacet()
            .types(Match.MUST, MediaType.CLIP)
            .build();

        Map<String, Long> withProfile = toSimpleMap(clients.getMediaService().find(form, "vpro-predictions", "none", 0L, 0).getFacets().getBroadcasters());
        Map<String, Long> withoutProfile = toSimpleMap(clients.getMediaService().find(form, null, "none", 0L, 0).getFacets().getBroadcasters());
        for (String k : withoutProfile.keySet()) {
            assertThat(withoutProfile.get(k)).isGreaterThanOrEqualTo(withProfile.getOrDefault(k, 0L));
        }
        log.info("{} -> {}", withoutProfile, withProfile);
    }

    @Test
    void moreFacets() throws JsonProcessingException {
        LocalDateTime until = LocalDateTime.of(2019, 1, 1, 12, 0);
        Pattern broadcastersMidPattern = Pattern.compile("WO_NTR_1.*");

        MediaForm form = MediaFormBuilder.form()
            .broadcasterFacet(MediaFacet.builder()
                .filter(MediaSearch.builder()
                    .mediaIds(
                        TextMatcherList.must(
                            TextMatcher.must(broadcastersMidPattern.pattern(), StandardMatchType.REGEX))
                    )
                    .build())
                .sort(FacetOrder.COUNT_DESC)
                .build())
            .ageRatingFacet(MediaFacet.builder()
                // And this case is limited even further
                .filter(MediaSearch.builder()
                    .mediaIds(
                        TextMatcherList.must(
                            TextMatcher.must("WO_NTR_10.*", StandardMatchType.REGEX))
                    )
                    .build()
                )
                .sort(FacetOrder.COUNT_DESC)
                .build()
            )
            .facetFilter(MediaSearch.builder()
                .sortDates(DateRangeMatcherList
                    .builder()
                    .value(
                        DateRangeMatcher.builder()
                            .localEnd(until)
                        .build()
                    )
                    .build()
                )
                .build()
            )
            .types(MediaType.CLIP)
            .mediaIds(Match.MUST, "WO_NTR_.*")
            .build();

        log.info("{}", Jackson2Mapper.getPrettyInstance().writeValueAsString(form));
        MediaSearchResult result = clients.getMediaService().find(form, "cinema", "all", 0L, 240);

        Map<String, Long> broadcasters = toSimpleMap(result.getFacets().getBroadcasters());
        log.info("{}", broadcasters);

        Profile profile = clients.getProfileService().load("cinema", null);
        // now explore result and compare
        Map<String, AtomicLong> fromResult = new HashMap<>();
        for (SearchResultItem<? extends MediaObject> m : result.getItems()) {
            MediaObject mediaObject = m.getResult();
            PredicateTestResult predicateTestResult = profile.getMediaProfile().getPredicate().testWithReason(mediaObject);
            if (! predicateTestResult.applies()) {
                fail("Found result %s not in profile %s because: %s", mediaObject, profile.getMediaProfile(), predicateTestResult.getDescription().getValue());
            }
            if (mediaObject.getSortInstant() == null || mediaObject.getSortInstant().isAfter(until.atZone(Schedule.ZONE_ID).toInstant())) {
                log.info("Filtered because sort date");
                continue;
            }
            if (! broadcastersMidPattern.matcher(mediaObject.getMid()).matches()) {
                log.info("Filtered because mid");
                continue;
            }
            for (Broadcaster b : mediaObject.getBroadcasters()) {
                fromResult.computeIfAbsent(b.getId(), (br) -> new AtomicLong(0)).incrementAndGet();
            }
        }
        Map<String, Long> sorted = fromResult
            .entrySet()
            .stream()
            .map((e) -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().get()))
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                LinkedHashMap::new));
        log.info("{}", sorted);



    }




}
