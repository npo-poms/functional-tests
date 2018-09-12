package nl.vpro.poms.pagespublisher;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import org.junit.*;
import org.junit.runners.MethodSorters;

import nl.vpro.api.client.resteasy.PageUpdateApiClient;
import nl.vpro.api.client.utils.PageUpdateApiUtil;
import nl.vpro.api.client.utils.PageUpdateRateLimiter;
import nl.vpro.api.client.utils.Result;
import nl.vpro.domain.api.IdList;
import nl.vpro.domain.api.MultipleEntry;
import nl.vpro.domain.api.SearchResultItem;
import nl.vpro.domain.api.page.PageForm;
import nl.vpro.domain.api.page.PageSearchResult;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.update.MediaUpdate;
import nl.vpro.domain.page.*;
import nl.vpro.domain.page.update.*;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.rules.DoAfterException;
import nl.vpro.testutils.Utils;
import nl.vpro.testutils.Utils.Check;


import static io.restassured.RestAssured.given;
import static nl.vpro.api.client.utils.Config.Prefix.npo_pageupdate_api;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.*;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PagesPublisherTest extends AbstractApiMediaBackendTest {


    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);

    static PageUpdateApiUtil util = new PageUpdateApiUtil(
        PageUpdateApiClient.configured(
            CONFIG.env(),
            CONFIG.getProperties(npo_pageupdate_api)
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
    public DoAfterException doAfterException = new DoAfterException((t) ->
        PagesPublisherTest.exception = t
    );

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
                    .midRef(MID)
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

        PageUpdate yesterday = util.get(urlYesterday);
        if (yesterday == null) {
            log.info("Article for yesterday {} not found (perhaps test didn't run yesterday). Making it for now, to test referrals too" , urlYesterday);
            Result r = util.save(PageUpdateBuilder.article(urlYesterday)
                .broadcasters("VPRO")
                .title(title)
                .portal(portal)
                .build());
            assertThat(r.getStatus()).withFailMessage(r.toString()).isEqualTo(Result.Status.SUCCESS);
        }

        PageUpdate topStory = util.get(topStoryUrl);
        if (topStory == null) {
            log.info("Topstory {} not found. Making it now", topStoryUrl);
            Result r = util.save(PageUpdateBuilder.article(topStoryUrl)
                .broadcasters("VPRO")
                .title("Sterrenhopen en zo, heel interessant")
                .portal(portal)
                .build());
            assertThat(r.getStatus()).isEqualTo(Result.Status.SUCCESS);
        }

        Result<Void> result = util.save(article);
        log.info("{}", result);
        assertThat(result.getStatus()).withFailMessage("" + result).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);
    }

    @Test
    public void test100Arrived() {
        assumeNotNull(article);

        PageUpdate update = Utils.waitUntil(ACCEPTABLE_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () ->
            util.get(article.getUrl()),
            p -> Objects.equals(p.getTitle(), article.getTitle())
        );
        assertThat(update.getTitle()).isEqualTo(article.getTitle());
    }


    @Test
    public void test101Published() {
        assumeNotNull(article);

        Page update = Utils.waitUntil(ACCEPTABLE_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () ->
            util.getPage(article.getUrl()).orElse(null),
            p -> Objects.equals(p.getTitle(), article.getTitle())
        );
        assertThat(update.getTitle()).isEqualTo(article.getTitle());
    }



    @Test
    public void test102ArrivedInAPI() {
        assumeNotNull(article);
        assumeTrue(pageUtil.getClients().isAvailable());

        Page page = Utils.waitUntil(ACCEPTABLE_DURATION,
            () ->
                pageUtil.load(article.getUrl())[0],
            Check.<Page>builder()
                .description(article.getUrl() + " has title " + article.getTitle())
                .predicate(p -> Objects.equals(p.getTitle(), article.getTitle()))
                .build(),
            Check.<Page>builder()
                .description(article.getUrl() + " has embeds")
                .predicate(p -> p.getEmbeds() != null && p.getEmbeds().size() > 0)
                .build()
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
    public void test201Published() {
        assumeNotNull(article);

        Page page = Utils.waitUntil(ACCEPTABLE_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () ->
                util.getPage(article.getUrl()).orElse(null), p -> Objects.equals(p.getTitle(), article.getTitle())
        );

        MediaObject embedded = util.getMedia(MID).orElseThrow(() -> new NotFoundException(MID));
        assertThat(page.getEmbeds().get(0).getMedia().getMid()).isEqualTo(MID);
        assertThat(page.getEmbeds().get(0).getMedia().getMainTitle()).isEqualTo(embedded.getMainTitle());
    }

    @Test
    public void test202ArrivedInApi() {
        assumeNotNull(article);
        assumeTrue(pageUtil.getClients().isAvailable());

        Page page = Utils.waitUntil(ACCEPTABLE_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () ->
                pageUtil.load(article.getUrl())[0], p -> Objects.equals(p.getTitle(), article.getTitle())
        );

        MediaObject embedded = mediaUtil.findByMid(MID);
        assertThat(page.getEmbeds().get(0).getMedia().getMid()).isEqualTo(MID);
        assertThat(page.getEmbeds().get(0).getMedia().getMainTitle()).isEqualTo(embedded.getMainTitle());
    }

    private static String embeddedDescription;

    @Test
    public void test203UpdateExistingEmbeddedMedia() {
        assumeTrue(backend.isAvailable());

        MediaUpdate<?> embedded = backend.get(MID);
        embeddedDescription = "Updated by " + title;
        embedded.setMainDescription(embeddedDescription);
        backend.set(embedded);

    }


    @Test
    public void test204ArrivedInApi() {
        assumeNotNull(article);
        assumeNotNull(embeddedDescription);
        assumeTrue(pageUtil.getClients().isAvailable());

        MediaObject fromApi = Utils.waitUntil(ACCEPTABLE_DURATION,
            MID + " has description " + embeddedDescription,
            () -> mediaUtil.findByMid(MID),
            mo -> {
                if (mo != null) {
                    log.info("{} : {}", mo, mo.getMainDescription());
                    return Objects.equals(mo.getMainDescription(), embeddedDescription);
                } else {
                    return false;
                }
            }
        );

        Page page = Utils.waitUntil(ACCEPTABLE_DURATION,
            article.getUrl() + " has embedded " + MID + " with description " + embeddedDescription,
            () ->
                pageUtil.load(article.getUrl())[0],
            p -> p != null && Objects.equals(p.getEmbeds().get(0).getMedia().getMainDescription(), embeddedDescription)
        );

        assertThat(page.getEmbeds().get(0).getMedia().getMainDescription()).isEqualTo(embeddedDescription);
    }

    private static final String TAG = "test_created_with_crid";
    private static final String CRID_PREFIX = "crid://crids.functional.tests/";
    private static final List<Crid> createdCrids = new ArrayList<>();
    private static final List<String> createdUrls = new ArrayList<>();
    private static final List<String> modifiedUrls = new ArrayList<>();

    @Test
    public void test300CreateSomeWithCrid() throws UnsupportedEncodingException {
        String url = "http://test.poms.nl/\u00E9\u00E9n/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now(), "UTF-8");

        for (int i = 0; i < 10; i++) {
            createdCrids.add(new Crid(CRID_PREFIX + i));
            String createdUrl = url + "/" + i;
            createdUrls.add(createdUrl);
            PageUpdate article =
                PageUpdateBuilder.article(createdUrl)
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
    public void test301ArrivedInAPI() {
        assumeTrue(util.getPageUpdateApiClient().getVersionNumber() >= 5.5);
        assumeTrue(createdCrids.size() > 0);
        assumeTrue(pageUtil.getClients().isAvailable());

        PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        PageSearchResult searchResultItems = Utils.waitUntil(
            ACCEPTABLE_DURATION,
            "Has pages with tag " + TAG,
            () -> pageUtil.find(form, null, 0L, 240),
            (sr) -> sr.getItems().stream().map(SearchResultItem::getResult).map(Page::getUrl).collect(Collectors.toList()).containsAll(createdUrls)
        );
        List<Crid> foundCrids = new ArrayList<>();
        List<String> foundUrls= new ArrayList<>();


        for (SearchResultItem<? extends Page> item : searchResultItems) {
            log.info("Found {} with crids: {}", item, item.getResult().getCrids());
            foundCrids.addAll(item.getResult().getCrids());
            foundUrls.add(item.getResult().getUrl());
        }
        assertThat(foundCrids).containsOnlyOnce(createdCrids.toArray(new Crid[0]));
        assertThat(foundUrls).containsOnlyOnce(createdUrls.toArray(new String[0]));
    }


    @Test
    public void test302UpdateUrls() throws UnsupportedEncodingException {
        //createdCrids.add(new Crid("crid://crids.functional.tests/3"));
        assumeTrue(util.getPageUpdateApiClient().getVersionNumber() >= 5.5);
        assumeTrue(createdCrids.size() > 0);
        assumeTrue(createdUrls.size() > 0);

        String url = "http://test.poms.nl/\u00E9\u00E9n/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now(), "UTF-8");

        int i = 0;
        for (Crid crid: createdCrids) {
            String modifiedUrl = url + "/" + i++;
            modifiedUrls.add(modifiedUrl);
            PageUpdate article =
                PageUpdateBuilder.article(modifiedUrl)
                    .broadcasters("VPRO")
                    .crids(crid)
                    .title(title + " (modified)")
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
    public void test303ModificationsArrivedInAPI() {
        assumeTrue(util.getPageUpdateApiClient().getVersionNumber() >= 5.5);
        assumeTrue(createdCrids.size() > 0);
        assumeTrue(createdUrls.size() > 0);
        assumeTrue(modifiedUrls.size() > 0);
        assumeTrue(pageUtil.getClients().isAvailable());

        PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        PageSearchResult searchResultItems = Utils.waitUntil(
            ACCEPTABLE_DURATION,
            "Has pages " + modifiedUrls,
            () -> pageUtil.find(form, null, 0L, 240),
            (sr) -> sr.asResult().getItems().stream().map(Page::getUrl).collect(Collectors.toList()).containsAll(modifiedUrls)
        );
        List<Crid> foundCrids = new ArrayList<>();
        List<String> foundUrls= new ArrayList<>();


        for (SearchResultItem<? extends Page> item : searchResultItems) {
            log.info("Found {} with crids: ", item, item.getResult().getCrids());
            foundCrids.addAll(item.getResult().getCrids());
            foundUrls.add(item.getResult().getUrl());
        }
        assertThat(foundCrids).containsOnlyOnce(createdCrids.toArray(new Crid[0]));
        assertThat(foundUrls).doesNotContain(createdUrls.toArray(new String[0]));
        assertThat(foundUrls).containsOnlyOnce(modifiedUrls.toArray(new String[0]));
    }


    @Test
    public void test304DeleteByCrid() {
        Result<DeleteResult> result = util.deleteWhereStartsWith(CRID_PREFIX);

        //assertThat(result.getEntity().getCount()).isGreaterThan(0);;

        assertThat(result.getStatus())
            .withFailMessage(result.getErrors() == null ? "Status is not success but " + result.getStatus() : result.getErrors())
            .isEqualTo(Result.Status.SUCCESS);
    }



    @Test
    public void test305DissappearedFromAPI() {
        assumeTrue(util.getPageUpdateApiClient().getVersionNumber() >= 5.5);
        assumeTrue(pageUtil.getClients().isAvailable());

        PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        Utils.waitUntil(ACCEPTABLE_DURATION,
            () -> "Has no pages with tag",
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

    @Test
    public void test500PostInvalid() {
        String user = CONFIG.requiredOption(npo_pageupdate_api, "user");
        String password= CONFIG.requiredOption(npo_pageupdate_api, "password");
        String url = CONFIG.requiredOption(npo_pageupdate_api, "baseUrl");

        given()
            .auth().basic(user, password)
            .log().all()
            .when()
            .  contentType("application/xml")
            .  body("<a />")
            .  post(url + "/api/pages/updates")
            .then()
            .  log().all()
            .  statusCode(400);

    }

    @Test
    public void test501PostInvalid() {
        String user = CONFIG.requiredOption(npo_pageupdate_api, "user");
        String password= CONFIG.requiredOption(npo_pageupdate_api, "password");
        String url = CONFIG.requiredOption(npo_pageupdate_api, "baseUrl");

        given()
            .auth().basic(user, password)
            .log().all()
            .when()
            .  contentType("application/xml")
            .  body("<page type=\"AUDIO\" url=\"http://test.kassa.nl/article/1234\"  xmlns=\"urn:vpro:pages:update:2013\">\n" +
                "  <crid>crid://bla/vara/1234</crid>\n" +
                "  <broadcaster>VARA</broadcaster>\n" +
                "  <title>Hoi5 &ldquo;</title>\n" +
                "</page>")
            .  post(url + "/api/pages/updates")
            .then()
            .  log().all()
            .  statusCode(400);

    }


    @Test
    @Ignore
    public void testPage() {
        Result<?> r = util.save(PageUpdateBuilder.article("htpt://www.vpro.nl/1234")
            .crids("crid://bla/1234")
            .broadcasters("VPRO")
            .title(title)
            .build());
            assertThat(r.getStatus()).withFailMessage(r.toString()).isEqualTo(Result.Status.SUCCESS);

    }

    @Test
    @Ignore
    public void updateUrl() {
        Result<?> r = util.save(PageUpdateBuilder.article("htpt://www.vpro.nl/1234/updated/again")
            .crids("crid://bla/1234")
            .broadcasters("VPRO")
            .title(title)
            .build());
            assertThat(r.getStatus()).withFailMessage(r.toString()).isEqualTo(Result.Status.SUCCESS);

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
        util.deleteWhereStartsWith("http://test.poms.nl/");
    }
}
