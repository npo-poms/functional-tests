package nl.vpro.poms.pagespublisher;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.api.client.resteasy.PageUpdateApiClient;
import nl.vpro.api.client.utils.Config;
import nl.vpro.api.client.utils.PageUpdateApiUtil;
import nl.vpro.api.client.utils.PageUpdateRateLimiter;
import nl.vpro.api.client.utils.Result;
import nl.vpro.domain.api.IdList;
import nl.vpro.domain.api.MultipleEntry;
import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.page.PageForm;
import nl.vpro.domain.api.page.PageSearchResult;
import nl.vpro.domain.page.*;
import nl.vpro.domain.page.update.*;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.poms.Utils;
import nl.vpro.rules.DoAfterException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.*;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PagesPublisherTest extends AbstractApiTest {


    static PageUpdateApiUtil util = new PageUpdateApiUtil(
        PageUpdateApiClient.configured(
            CONFIG.env(),
            CONFIG.getProperties(Config.Prefix.npo_pageupdate_api)
        ).build(),
        PageUpdateRateLimiter.builder().build()
    );

    static {
        log.info("Using {}", util);
    }

    static final String topStoryUrl = "http://test.poms.nl/test001CreateOrUpdatePageTopStory";
    static PageUpdate article;

    static String urlToday;
    static String urlYesterday;
    static String urlTomorrow;


    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> PagesPublisherTest.exception = t);

    private static Throwable exception = null;

    @Before
    public void setup() {
        assumeNoException(exception);

        log.info("Testing with version {}", util.getPageUpdateApiClient().getVersionNumber());
    }


    @Test
    public void test001CreateOrUpdatePage() throws UnsupportedEncodingException {

        urlToday = "http://test.poms.nl/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now(), "UTF-8");
        urlYesterday = "http://test.poms.nl/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now().minusDays(1), "UTF-8");
        urlTomorrow = "http://test.poms.nl/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now().plusDays(1), "UTF-8");


        PortalUpdate portal = new PortalUpdate("WETENSCHAP24", "http://test.poms.nl");
        portal.setSection(
            Section.builder()
                .path("/" + testMethod.getMethodName())
                .displayName("Display name " + testMethod.getMethodName())
                .build()
        );

        article =
            PageUpdateBuilder.article(urlToday)
                .broadcasters("VPRO")
                .title(title)
                .embeds(EmbedUpdate.builder()
                    .midRef(AbstractApiMediaBackendTest.MID)
                    .title("leuke embed")
                    .description("embed in " + title)
                    .build())
                .portal(portal)
                .links(
                    LinkUpdate.topStory(topStoryUrl, "mooie story over sterrenhopen"),
                    LinkUpdate.of(urlYesterday, "yesterday"),
                    LinkUpdate.of(urlTomorrow, "tomorrow")
                )
            .build();

        Page yesterday = pageUtil.load(urlYesterday)[0];
        if (yesterday == null) {
            log.info("Article for yesterday {} not found (perhaps test didn't run yesterday). Making it for now, to test referrals too" , urlYesterday);
            Result r = util.save(PageUpdateBuilder.article(urlYesterday)
                .broadcasters("VPRO")
                .title(title)
                .portal(portal)
                .build());
            assertThat(r.getStatus()).withFailMessage(r.toString()).isEqualTo(Result.Status.SUCCESS);
        }

        Page topStory = pageUtil.load(topStoryUrl)[0];
        if (topStory == null) {
            log.info("Topstory {} not found. Making it now", topStoryUrl);
            Result r = util.save(PageUpdateBuilder.article(topStoryUrl)
                .broadcasters("VPRO")
                .title("Sterrenhopen en zo, heel interessant")
                .portal(portal)
                .build());
            assertThat(r.getStatus()).isEqualTo(Result.Status.SUCCESS);
        }

        Result result = util.save(article);
        log.info("{}", result);
        assertThat(result.getStatus()).withFailMessage("" + result).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);
    }

    @Test
    public void test100Arrived() {
        assumeNotNull(article);

        PageUpdate update = Utils.waitUntil(Duration.ofMinutes(1),
            article.getUrl() + " has title " + article.getTitle(),
            () ->
            util.get(article.getUrl()),
            p -> Objects.equals(p.getTitle(), article.getTitle())
        );
        assertThat(update.getTitle()).isEqualTo(article.getTitle());
    }

    @Test
    public void test101ArrivedInAPI() {
        assumeNotNull(article);
        Page page = Utils.waitUntil(Duration.ofMinutes(1),
            article.getUrl() + " has title " + article.getTitle() + " in " + pageUtil,
            () ->
            pageUtil.load(article.getUrl())[0], p -> p != null && Objects.equals(p.getTitle(), article.getTitle())
        );

        assertThat(page.getTitle()).isEqualTo(article.getTitle());
        assertThat(page.getEmbeds()).hasSize(1);
        assertThat(page.getEmbeds().get(0).getMedia()).isNotNull();
        assertThat(page.getEmbeds().get(0).getMedia().getMainTitle()).isEqualTo("testclip michiel");
        assertThat(page.getEmbeds().get(0).getTitle()).isEqualTo("leuke embed");
        assertThat(page.getEmbeds().get(0).getDescription()).isEqualTo("embed in " + article.getTitle());
        assertThat(page.getLinks()).hasSize(3);
        assertThat(page.getLinks().get(0).getType()).isEqualTo(LinkType.TOP_STORY);

        Page yesterday = pageUtil.load(urlYesterday)[0];

        assertThat(yesterday.getReferrals().size()).isGreaterThanOrEqualTo(1);

        Page tomorrow = pageUtil.load(urlTomorrow)[0];

        assertThat(tomorrow).isNull();

        Page topStory = pageUtil.load(topStoryUrl)[0];

        Optional<Referral> referral = topStory.getReferrals()
            .stream()
            .filter(r -> r.getPageRef().equals(urlToday))
            .findFirst();

        assertThat(referral).withFailMessage(topStoryUrl + " has no referral " + urlToday).isPresent();
        assertThat(referral.get().getType()).isEqualTo(LinkType.TOP_STORY);

    }

    @Test
    public void test200UpdateExisting() {
        assumeNotNull(article);
        log.info("Updating {} tot title {}", article.getUrl(), title);
        article.setTitle(title);
        Result result = util.save(article);


        log.info("{}",  result);
        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();

    }

    @Test
    public void test201ArrivedInApi() {
        assumeNotNull(article);
        Page page = Utils.waitUntil(Duration.ofMinutes(1),
            article.getUrl() + " has title " + article.getTitle(),
            () ->
                pageUtil.load(article.getUrl())[0], p -> Objects.equals(p.getTitle(), article.getTitle())
        );
    }

    private static final String TAG = "test_created_with_crid";
    private static final String CRID_PREFIX = "crid://crids.functional.tests/";
    private static final List<Crid> createdCrids = new ArrayList<>();

    @Test
    public void test300CreateSomeWithCrid() throws UnsupportedEncodingException {
        String url = "http://test.poms.nl/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now(), "UTF-8");

        for (int i = 0; i < 10; i++) {
            createdCrids.add(new Crid(CRID_PREFIX + i));
            PageUpdate article =
                PageUpdateBuilder.article(url + "/" + i)
                    .broadcasters("VPRO")
                    .crids(CRID_PREFIX + i)
                    .title(title)
                    .tags(TAG)
                    .creationDate(Instant.now())
                    .lastModified(Instant.now())
                    .build();
            Result result = util.save(article);
            assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
            log.info("Created {}", article);

        }
    }

    @Test
    public void test301ArrivedInAPIThenDeleteByCrid() {
        assumeTrue(util.getPageUpdateApiClient().getVersionNumber() >= 5.5);
        assumeTrue(createdCrids.size() > 0);

        PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        PageSearchResult searchResultItems = Utils.waitUntil(
            Duration.ofMinutes(2),
            "Has pages with tag " + TAG,
            () -> pageUtil.find(form, null, 0L, 240),
            (sr) -> sr.getSize() >= 10
        );

        for (SearchResultItem<? extends Page> item : searchResultItems) {
            log.info("Found {} with crids: ", item, item.getResult().getCrids());
            createdCrids.removeAll(item.getResult().getCrids());
        }

        // Then delete by crid
        Result result = util.deleteWhereStartsWith(CRID_PREFIX);

        assertThat(createdCrids).isEmpty();

        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
    }

    @Test
    public void test302DissappearedFromAPI() {
        assumeTrue(util.getPageUpdateApiClient().getVersionNumber() >= 5.5);
        PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        Utils.waitUntil(Duration.ofMinutes(2),
            "Has no pages with tag",
            () -> {
                PageSearchResult searchResultItems = pageUtil.find(form, null, 0L, 11);
                log.info("Found {}", searchResultItems);
                return searchResultItems.getSize() == 0;
            }
        );


    }


    @Test
    public void test400Consistency() {
        Set<String> checked = new LinkedHashSet<>();
        testConsistency(topStoryUrl, checked, false);
        log.info("{}", checked);
    }
    protected void testConsistency(String url, Set<String> checked, boolean cleanup) {
        if (checked.contains(url)) {
            return;
        }
        checked.add(url);

        MultipleEntry<Page> multipleEntry = clients.getPageService().loadMultiple(url, null, null).getItems().get(0);
        if (multipleEntry.getResult() == null) {
            log.warn("Could not find {}", url);
            if (cleanup) {
                util.delete(url);
            }
            return;
        }
        IdList list = new IdList();
        if (multipleEntry.getResult().getReferrals() != null) {
            for (Referral r : multipleEntry.getResult().getReferrals()) {
                list.add(r.getPageRef());
            }

            List<? extends MultipleEntry<Page>> referralsAsPage = clients.getPageService().loadMultiple(list, null, null).getItems();

            List<String> notFound = new ArrayList<>();
            for (MultipleEntry<Page> r : referralsAsPage) {
                log.info("{} -> {}", r.getId(), r.getResult());
                if (r.getResult() == null) {
                    notFound.add(r.getId());
                }
                testConsistency(r.getId(), checked, cleanup);
            }
            if (!notFound.isEmpty()) {
                log.warn("Not Found{}!", notFound);
            }
        }
    }


    @Test
    public void test999CleanUps() {

        MultipleEntry<Page> multipleEntry = clients.getPageService().loadMultiple(topStoryUrl, null, null).getItems().get(0);

        assertThat(multipleEntry.getResult()).isNotNull();

        IdList list = new IdList();
        for (Referral r : multipleEntry.getResult().getReferrals()) {
            list.add(r.getPageRef());
        }
        List<? extends MultipleEntry<Page>> referralsAsPage = clients.getPageService().loadMultiple(list, null, null).getItems();

        List<String> removed = new ArrayList<>();
        for (MultipleEntry<Page> r : referralsAsPage) {
            log.info("{} -> {}", r.getId(), r.getResult());
            if (r.getResult() == null) {
                log.info("result {} ", util.delete(r.getId()));
                removed.add(r.getId());
            }
        }
        if (! removed.isEmpty()) {
            log.warn("Removed {}!", removed);
        }

    }

    @Test
    @Ignore
    public void test999CleanUp() {
        util.delete("http://test.poms.nl/test001CreateOrUpdatePage2018-01-12");
    }
}
