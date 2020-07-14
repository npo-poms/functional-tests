package nl.vpro.poms.npoapi;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import nl.vpro.domain.api.IdList;
import nl.vpro.domain.api.MultipleEntry;
import nl.vpro.domain.page.Page;
import nl.vpro.domain.page.Referral;
import nl.vpro.poms.AbstractApiTest;

import static org.assertj.core.api.Assertions.assertThat;


@Log4j2
public class ApiPageTest extends AbstractApiTest {

    private static final String topStoryUrl = "http://test.poms.nl/test001CreateOrUpdatePageTopStory";


    /**
     * Tests if the state (as maintained by {@link nl.vpro.poms.pagespublisher.PagesPublisherTest} is indeed consistent.
     *
     * I.e. all mentioned referrals of the top story do indeed also exist.
     */
    @Test
    void topStoryOnlyReferredByExistingPages() {

        MultipleEntry<Page> multipleEntry = clients.getPageService().loadMultiple(topStoryUrl, null, null).getItems().get(0);

        Assumptions.assumeTrue(multipleEntry.getResult() != null, "This test cannot be performed because " + topStoryUrl + " has not yet been created at all");

        IdList list = new IdList();
        for (Referral r : multipleEntry.getResult().getReferrals()) {
            list.add(r.getPageRef());
        }
        log.info("{} has the following referrals {}. Now loading them as a page", topStoryUrl, list);
        List<? extends MultipleEntry<Page>> referralsAsPage =
            clients.getPageService().loadMultiple(list, null, null).getItems();

        List<MultipleEntry<Page>> notFound = new ArrayList<>();
        for (MultipleEntry<Page> r : referralsAsPage) {
            log.info("{} -> {}", r.getId(), r.getResult());
            if (r.getResult() == null) {
                notFound.add(r);
            }
        }
        for (MultipleEntry<Page> nf : notFound) {
            MultipleEntry<Page> retried = clients.getPageService().loadMultiple(nf.getId(), null, null).getItems().get(0);
            if (retried.getResult() != null) {
                log.error("tried again: {}", retried);
            }
        }
        assertThat(notFound).isEmpty();
        log.info("That's ok, every referral page is effectively found");


    }



}
