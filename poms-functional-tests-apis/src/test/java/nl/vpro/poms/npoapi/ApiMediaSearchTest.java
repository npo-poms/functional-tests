package nl.vpro.poms.npoapi;

import java.util.*;

import org.junit.jupiter.api.Test;

import nl.vpro.domain.api.Match;
import nl.vpro.domain.api.TermFacetResultItem;
import nl.vpro.domain.api.media.MediaForm;
import nl.vpro.domain.media.MediaType;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;

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

        Map<String, Long> withProfile = toMap(clients.getMediaService().find(form, "vpro-predictions", "none", 0L, 0).getFacets().getBroadcasters());
        Map<String, Long> withoutProfile = toMap(clients.getMediaService().find(form, null, "none", 0L, 0).getFacets().getBroadcasters());
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

        Map<String, Long> withProfile = toMap(clients.getMediaService().find(form, "vpro-predictions", "none", 0L, 0).getFacets().getBroadcasters());
        Map<String, Long> withoutProfile = toMap(clients.getMediaService().find(form, null, "none", 0L, 0).getFacets().getBroadcasters());
        for (String k : withoutProfile.keySet()) {
            assertThat(withoutProfile.get(k)).isGreaterThanOrEqualTo(withProfile.getOrDefault(k, 0L));
        }
        log.info("{} -> {}", withoutProfile, withProfile);
    }

    private Map<String, Long> toMap(List<TermFacetResultItem> results) {
        Map<String, Long> values = new LinkedHashMap<>();
        for (TermFacetResultItem b : results) {
            values.put(b.getId(), b.getCount());
        }
        return values;
    }



}
