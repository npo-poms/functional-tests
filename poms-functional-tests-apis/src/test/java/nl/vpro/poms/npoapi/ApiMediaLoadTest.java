package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.vpro.api.client.frontend.NpoApiClients;
import nl.vpro.domain.api.Error;
import nl.vpro.domain.api.*;
import nl.vpro.domain.api.media.MediaForm;
import nl.vpro.domain.api.media.RedirectList;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Log4j2
public class ApiMediaLoadTest extends AbstractApiTest {
    static List<Arguments> arguments;

    ApiMediaLoadTest() {
    }

    public static Stream<Arguments>  getParameters() {

        if (arguments == null) {
            // collect some existing mids
            arguments = new ArrayList<>();

            for (String profile : Arrays.asList(null, "vpro", "eo")) {
                List<String> mids = new ArrayList<>();
                if (! "eo".equals(profile)) {
                    mids.add("VPWON_1181223"); // NPA-341 ?
                }
                try {
                    mids.addAll(clients.getMediaService().find(new MediaForm(), profile, "", 0L, 10).asResult().stream().map(MediaObject::getMid).collect(Collectors.toList()));
                    if (mids.size() == 0) {
                        throw new IllegalStateException("No media found for profile " + profile);
                    }

                } catch (javax.ws.rs.ServiceUnavailableException ue) {
                    log.warn(ue.getMessage());
                }
                log.info("For profile {}: Found mids {}", profile, mids);
                for (MediaType mediaType : Arrays.asList(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE)) {
                    for (String properties : Arrays.asList(null, "none", "all", "title")) {

                        arguments.add(
                            Arguments.arguments(
                                clients.toBuilder()
                                    .profile(profile)
                                    .accept(mediaType)
                                    .properties(properties)
                                    .toString((c) -> "client:" + c.getProfile() + "/" + c.getAccept() + "/" + c.getProperties())
                                    .build(),
                                mids));
                    }

                }

            }
        }
        return arguments.stream();
    }

    @MethodSource("getParameters")
    @Retention(RetentionPolicy.RUNTIME)
    @interface Params {
    }

    @ParameterizedTest
    @Params
    public void load(NpoApiClients clients, List<String> mids) {
        assumeThat(mids.size()).isGreaterThan(0);
        for (String mid : mids) {
            log.info("Loading {}", mid);
            MediaObject o = clients.getMediaService().load(mid, null, null);
            assertThat(o.getMid()).isEqualTo(mid);
            assertThat(o.getMainTitle()).isNotEmpty(); // NPA-362
            if (clients.hasAllProperties()) {
                if (clients.getProfile() != null) {
                    assertThat(clients.getAssociatedProfile().get().getMediaProfile().test(o)).isTrue();
                }
            }
        }
    }

    @ParameterizedTest
    @Params
    void loadOutsideProfile(NpoApiClients clients, List<String> mids) {
        assumeThat(clients.getProfile()).isNotNull();
        assumeThat(clients.getProfile()).isNotEqualTo("eo");

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

    @ParameterizedTest
    @Params
    void loadMultiple(NpoApiClients clients, List<String> mids) {
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
            if (clients.getProfile() != null && clients.hasAllProperties()) {
                assertThat(clients.getAssociatedProfile().get().getMediaProfile().test(o.getItems().get(i).getResult())).isTrue();
            }
        }

    }




}
