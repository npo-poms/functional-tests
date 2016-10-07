package nl.vpro.poms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import nl.vpro.domain.api.IdList;
import nl.vpro.domain.api.MultipleMediaResult;
import nl.vpro.domain.api.media.MediaForm;
import nl.vpro.domain.api.profile.Profile;
import nl.vpro.domain.media.MediaObject;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(Parameterized.class)
public class ApiMediaLoadTest extends AbstractApiTest {

    private final String profileName;
    private Profile profile;
    private final List<String> mids;

    public ApiMediaLoadTest(String profileName, List<String> mids) {
        this.profileName = profileName;
        this.mids = mids;

    }

    @Before
    public void setup() {
        if (profileName != null) {
            profile = clients.getProfileService().load(profileName, null);
        }
    }


    @Parameterized.Parameters
    public static Collection<Object[]> getMediaObjects() throws IOException {
        List<Object[]> result = new ArrayList<>();
        for (String profile : Arrays.asList(null, "vpro")) {
            List<String> mids = clients.getMediaService().find(new MediaForm(), profile, "none", 0L, 10).asResult().stream().map(MediaObject::getMid).collect(Collectors.toList());
            result.add(new Object[]{profile, mids});
        }
        return result;
    }

    @Test
    public void load() throws Exception {
        MediaObject o = clients.getMediaService().load(mids.get(0), null, null);
        assertThat(o.getMid()).isEqualTo(mids.get(0));
        if (profileName != null) {
            assertThat(profile.getMediaProfile().test(o)).isTrue();
        }

    }

    @Test
    public void loadInProfile() throws Exception {
        Assume.assumeNotNull(profileName);
        MediaObject o = clients.getMediaService().load(mids.get(0), null, profileName);
        assertThat(o.getMid()).isEqualTo(mids.get(0));
    }

    @Test
    public void loadMultiple() throws Exception {
        MultipleMediaResult o = clients.getMediaService().loadMultiple(IdList.of(mids), null, null);

        for (int i = 0; i < mids.size(); i++) {
            assertThat(o.getItems().get(i).getResult().getMid()).isEqualTo(mids.get(i));
            if (profileName != null) {
                assertThat(profile.getMediaProfile().test(o.getItems().get(i).getResult())).isTrue();
            }
        }

    }



}
