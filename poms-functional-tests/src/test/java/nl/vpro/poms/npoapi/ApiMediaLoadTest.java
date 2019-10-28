package nl.vpro.poms.npoapi;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.Error;
import nl.vpro.domain.api.*;
import nl.vpro.domain.api.media.MediaForm;
import nl.vpro.domain.api.media.RedirectList;
import nl.vpro.domain.api.profile.Profile;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assume.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Slf4j
class ApiMediaLoadTest extends AbstractApiTest {

    private final String profileName;
    private Profile profile;
    private final List<String> mids;

    ApiMediaLoadTest(String profileName, List<String> mids, MediaType mediaType, String properties) {
        this.profileName = profileName;
        this.mids = mids;
        clients.setAccept(mediaType);
        clients.setProperties(properties);

    }

    @BeforeEach
    void setup() {
        clients.setProfile(profileName);
        if (profileName != null) {
            profile = clients.getProfileService().load(profileName, null);
        }
    }


    @Parameterized.Parameters
    static Collection<Object[]> getParameters() {
        List<Object[]> result = new ArrayList<>();
        for (MediaType mediaType : Arrays.asList(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE)) {
            for (String profile : Arrays.asList(null, "vpro")) {
                for (String properties : Arrays.asList(null, "none", "all", "title")) {
                    List<String> mids = new ArrayList<>();
                    mids.add("VPWON_1181223"); // NPA-341 ?
                    try {
                        mids.addAll(clients.getMediaService().find(new MediaForm(), profile, "", 0L, 10).asResult().stream().map(MediaObject::getMid).collect(Collectors.toList()));
                        if (mids.size() == 0) {
                            throw new IllegalStateException("No media found for profile " + profile);
                        }

                    } catch (javax.ws.rs.ServiceUnavailableException ue) {
                        log.warn(ue.getMessage());
                    }
                    result.add(new Object[]{profile, mids, mediaType, properties});
                }

            }
        }
        return result;
    }

    @Test
    void load() {
        assumeThat(mids.size()).isGreaterThan(0));
        MediaObject o = clients.getMediaService().load(mids.get(0), null, null);
        assertThat(o.getMid()).isEqualTo(mids.get(0));
        assertThat(o.getMainTitle()).isNotEmpty(); // NPA-362
        if (clients.hasAllProperties()) {
            if (profileName != null) {
                assertThat(profile.getMediaProfile().test(o)).isTrue();
            }
        }
    }

    @Test
    void loadOutsideProfile() {
        assumeThat(profileName).isNotNull();
        assumeFalse(profileName.equals("eo"));

        assumeTrue(mids.size() > 0);
        try {
            clients.setAcceptableLanguages(Collections.singletonList(Locale.US));
            clients.getMediaService().load(mids.get(0), null, "eo");
        } catch (NotFoundException nfe) {
            assertThat(nfe.getResponse().getEntity()).isInstanceOf(Error.class);
            Error error = (Error) nfe.getResponse().getEntity();
            assertThat(error.getMessage()).contains("is niet van de omroep EO");
            //assertThat(error.getTestResult().getDescription().getValue()).contains("is niet van de omroep EO");

            return;
        }
        throw new AssertionError("Should have given NotFoundException");
    }

    @Test
    void loadMultiple() {
        clients.setProfile(null);
        RedirectList redirects = mediaUtil.redirects();
        MultipleMediaResult o = clients.getMediaService().loadMultiple(
            IdList.of(mids), null, null);

        for (int i = 0; i < mids.size(); i++) {

            assertThat(o.getItems().get(i).getResult()).withFailMessage("Not found " + mids.get(i)).isNotNull();
            assertThat(o.getItems().get(i).getResult().getMainTitle()).isNotEmpty();// NPA-362
            assertThat(o.getItems().get(i).getError()).isNull();
            MediaObject mo = o.getItems().get(i).getResult();
            String mid = mo.getMid();
            if (!Objects.equals(mid, mids.get(i))) {
                String redirected = redirects.getMap().get(mids.get(i));
                if (redirected != null) {
                    log.info("{} is redirected to {}", mids.get(i), redirected);
                    mids.set(i, redirected);
                }

            }
            assertThat(mid).isEqualTo(mids.get(i));
            if (profileName != null && clients.hasAllProperties()) {
                assertThat(profile.getMediaProfile().test(o.getItems().get(i).getResult())).isTrue();
            }
        }

    }




}
