package nl.vpro.poms.pagespublisher;

import lombok.extern.log4j.Log4j2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;

import org.junit.jupiter.api.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import nl.vpro.api.client.pages.PageUpdateApiClient;
import nl.vpro.api.client.utils.Result;
import nl.vpro.api.client.utils.*;
import nl.vpro.domain.api.Order;
import nl.vpro.domain.api.*;
import nl.vpro.domain.api.page.*;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.update.MediaUpdate;
import nl.vpro.domain.page.*;
import nl.vpro.domain.page.update.*;
import nl.vpro.jackson2.Jackson2Mapper;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.test.jupiter.AbortOnException;
import nl.vpro.testutils.Utils;
import nl.vpro.testutils.Utils.Check;
import nl.vpro.util.Version;
import nl.vpro.validation.URI;

import static io.restassured.RestAssured.given;
import static nl.vpro.api.client.utils.Config.Prefix.npo_pageupdate_api;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michiel Meeuwissen
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Log4j2
class PagesPublisherTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);
    private static final Duration ACCEPTABLE_PAGE_PUBLISHED_DURATION = Duration.ofMinutes(15);
    private static final Duration ACCEPTABLE_MEDIA_PUBLISHED_DURATION = Duration.ofMinutes(15);

    private static final PageUpdateApiUtil util = new PageUpdateApiUtil(
        PageUpdateApiClient.configured(
            CONFIG.env(),
            CONFIG.getProperties(npo_pageupdate_api)
        )
            .connectTimeout(Duration.ofSeconds(10))
            .warnThreshold(Duration.ofMillis(500))
            .socketTimeout(Duration.ofSeconds(60))
            .build(),
        PageUpdateRateLimiter.builder().build()
    );

    static {
        log.info("Using {}", util);
    }

    private static final String topStoryUrl = "http://test.poms.nl/test001CreateOrUpdatePageTopStory";
    private static PageUpdate article;

    private static String urlToday;
    private static String urlYesterday;
    private static String urlTomorrow;

    private static final int NUMBER_OF_PAGES_TO_CREATED = 10;
    private static final String CRID_PREFIX = "crid://crids.functional.tests/";
    private static final String[] CREATED_CRIDS = new String[NUMBER_OF_PAGES_TO_CREATED];

    static {
        for (int i = 0; i < NUMBER_OF_PAGES_TO_CREATED; i++) {
            CREATED_CRIDS[i] = CRID_PREFIX + i;
        }
    }



    @BeforeAll
    public static void setup() {
        log.info("Testing with version {}", util.getPageUpdateApiClient().getVersionNumber());
        log.info("Backend available: {}", backend.isAvailable());

    }


    @Test
    public void test001CreateOrUpdatePage(TestInfo testInfo) throws UnsupportedEncodingException {

        String methodName = testInfo.getTestMethod().get().getName();
        LocalDate today = LocalDate.now();

        urlToday = "http://test.poms.nl/" + URLEncoder.encode(methodName + today, "UTF-8");
        urlYesterday = "http://test.poms.nl/" + URLEncoder.encode(methodName + today.minusDays(1), "UTF-8");
        urlTomorrow = "http://test.poms.nl/" + URLEncoder.encode(methodName + today.plusDays(1), "UTF-8");


        PortalUpdate portal = new PortalUpdate("WETENSCHAP24", "http://test.poms.nl");
        portal.setSection(
            Section.builder()
                .path("/" + methodName)
                .displayName("Display name " + methodName)
                .build()
        );

        article =
            PageUpdateBuilder.article(urlToday)
                .broadcasters("VPRO")
                .title(title)
                .embeds(
                    EmbedUpdate.builder()
                        .midRef(MID)
                        .title("leuke embed")
                        .description("embed in " + title)
                        .build(),
                    EmbedUpdate.builder()
                        .midRef(ANOTHER_MID)
                        .title("nog een leuke embed")
                        .description("another embed in " + title)
                        .build()
                    )
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
            Result<Void> r = util.saveAndWait(PageUpdateBuilder.article(urlYesterday)
                .broadcasters("VPRO")
                .title(title)
                .portal(portal)
                .build());
            assertThat(r.getStatus()).withFailMessage(r.toString()).isEqualTo(Result.Status.SUCCESS);
        }

        PageUpdate topStory = util.get(topStoryUrl);
        if (topStory == null) {
            log.info("Topstory {} not found. Making it now", topStoryUrl);
            Result<Void> r = util.saveAndWait(PageUpdateBuilder.article(topStoryUrl)
                .broadcasters("VPRO")
                .title("Sterrenhopen en zo, heel interessant")
                .portal(portal)
                .build());
            assertThat(r.getStatus()).isEqualTo(Result.Status.SUCCESS);
        }

        Result<Void> result = util.saveAndWait(article);
        log.info("{}", result);
        assertThat(result.getStatus()).withFailMessage("" + result).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);
    }

    @Test
    public void test100Arrived() {
        assumeThat(article).isNotNull();

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
        assumeThat(article).isNotNull();

        Page update = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () ->
            util.getPublishedPage(article.getUrl()).orElse(null),
            p -> Objects.equals(p.getTitle(), article.getTitle())
        );
        assertThat(update.getTitle()).isEqualTo(article.getTitle());
    }



    @Test
    public void test102ArrivedInAPI() {
        assumeThat(article).isNotNull();
        assumeTrue(pageUtil.getClients().isAvailable());
        log.info("Loading {} from API", article.getUrl());
        Page page = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
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
        assertThat(page.getEmbeds()).hasSize(2);
        assertThat(page.getEmbeds().get(0).getMedia()).isNotNull();
        assertThat(page.getEmbeds().get(0).getMedia().getMainTitle()).isEqualTo("testclip michiel");
        assertThat(page.getEmbeds().get(0).getTitle()).isEqualTo("leuke embed");
        assertThat(page.getEmbeds().get(0).getDescription()).isEqualTo("embed in " + article.getTitle());
        assertThat(page.getEmbeds().get(1).getMedia()).isNotNull();
        assertThat(page.getEmbeds().get(1).getMedia().getMid()).isEqualTo(ANOTHER_MID);
        assertThat(page.getEmbeds().get(1).getTitle()).isEqualTo("nog een leuke embed");
        assertThat(page.getEmbeds().get(1).getDescription()).isEqualTo("another embed in " + article.getTitle());
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


    /**
     * Set a new title
     */
    @Test
    public void test200UpdateExistingArticle() {
        assumeThat(article).isNotNull();

        log.info("Updating {} tot title {}", article.getUrl(), title);
        article.setTitle(title);
        Result<Void> result = util.saveAndWait(article);


        log.info("{}",  result);
        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();

    }

    @Test
    public void test201Published() {
        assumeThat(article).isNotNull();

        Page page = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () ->
                util.getPublishedPage(article.getUrl()).orElse(null), p -> Objects.equals(p.getTitle(), article.getTitle())
        );

        MediaObject embedded = util.getMedia(MID).orElseThrow(() -> new NotFoundException(MID));

        assertThat(page.getEmbeds()).hasSize(2);

        assumeThat(page.getEmbeds().get(0).getMedia()).isNotNull();

        assertThat(page.getEmbeds().get(0).getMedia().getMid()).isEqualTo(MID);
        assertThat(page.getEmbeds().get(0).getMedia().getMainTitle()).isEqualTo(embedded.getMainTitle());
    }

    @Test
    public void test202ArrivedInApi() {
        assumeThat(article).isNotNull();
        assumeTrue(pageUtil.getClients().isAvailable());

        String url = article.getUrl();
        Page page = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () -> pageUtil.get(url),
            p -> {
                log.info("{} -> {}", url, p);
                return p != null && Objects.equals(p.getTitle(), article.getTitle());
            }
        );

        MediaObject embedded = mediaUtil.findByMid(MID);
        assertThat(embedded).isNotNull();
        assertThat(page.getEmbeds().get(0).getMedia().getMid()).isEqualTo(MID);
        assertThat(page.getEmbeds().get(0).getMedia().getMainTitle()).isNotNull();
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
    public void test204ArrivedInMediaApi() {
        assumeThat(article).isNotNull();
        assumeNotNull(embeddedDescription);
        assumeTrue(pageUtil.getClients().isAvailable());

        MediaObject fromApi = Utils.waitUntil(ACCEPTABLE_MEDIA_PUBLISHED_DURATION,
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
        log.info("Received from api {}", fromApi);
    }

    @Test
    public void test205ArrivedInPagesApi() {
        assumeTrue(pageUtil.getClients().isAvailable());

        Page page = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            article.getUrl() + " has embedded " + MID + " with description " + embeddedDescription,
            () ->
                pageUtil.load(article.getUrl())[0],
            p -> p != null && Objects.equals(p.getEmbeds().get(0).getMedia().getMainDescription(), embeddedDescription)
        );

        assertThat(page.getEmbeds().get(0).getMedia().getMainDescription()).isEqualTo(embeddedDescription);
    }


    @Test
    public void test210RemoveAnEmbed() {
        assumeThat(article).isNotNull();
        article.setEmbeds(new ArrayList<>(article.getEmbeds())); // make the damn list modifiable.
        article.getEmbeds().remove(0);
        Result<Void> result = util.saveAndWait(article);
        log.info("{}", result);
        assertThat(result.getStatus()).withFailMessage("" + result).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);

    }

    @Test
    public void test211CheckRemoveAnEmbed() {
        assumeThat(article).isNotNull();
        Page page = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            article.getUrl() + " has only one embed",
            () ->
                pageUtil.load(article.getUrl())[0],
            p -> p != null && p.getEmbeds().size() == 1
        );

        assertThat(page.getEmbeds()).hasSize(1);
        assertThat(page.getEmbeds().get(0).getMedia().getMid()).isEqualTo(ANOTHER_MID);

    }


    private static final String TAG = "test_created_with_crid";
    private static final List<String> createdUrls = new ArrayList<>();
    private static final List<String> modifiedUrls = new ArrayList<>();

    @Test
    public void test300CreateSomeWithCrid(TestInfo testInfo) throws UnsupportedEncodingException {
        String url = "http://test.poms.nl/\u00E9\u00E9n/" + URLEncoder.encode(testInfo.getTestMethod().get().getName() + LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), "UTF-8");

        for (int i = 0; i < NUMBER_OF_PAGES_TO_CREATED; i++) {
            String createdUrl = url + "/" + i;
            createdUrls.add(createdUrl);
            PageUpdate article =
                PageUpdateBuilder.article(createdUrl)
                    .broadcasters("VPRO")
                    .crids(CREATED_CRIDS[i])
                    .title(title)
                    .tags(TAG)
                    .creationDate(Instant.now())
                    .lastModified(Instant.now())
                    .build();
            Result<Void> result = util.saveAndWait(article);
            assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
            log.info("Created {}", article);
        }
    }

    @Test
    public void test301ArrivedInAPIAnd() throws JsonProcessingException {
        assumeThat(util.getPageUpdateApiClient().getVersionNumber()).isGreaterThanOrEqualTo(Version.of(5, 5));
        //assumeTrue(pageUtil.getClients().isAvailable());

        PageForm form = PageForm.builder()
            .tags(TAG)
            .addSortField(PageSortField.lastPublished, Order.DESC)
            .build();

        log.info("{}", Jackson2Mapper.getPrettyInstance().writeValueAsString(form));

        PageSearchResult searchResultItems = Utils.waitUntil(
            ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            "Has pages with tag " + TAG,
            () -> pageUtil.find(form, null, 0L, 240),
            (sr) -> {
                List<@NotNull @URI String> collect = sr.getItems()
                    .stream()
                    .map(SearchResultItem::getResult)
                    .map(Page::getUrl)
                    .collect(Collectors.toList());
                log.info("Found {}", collect);
                return collect.containsAll(createdUrls);
            }
        );
        List<String> foundCrids = new ArrayList<>();
        List<String> foundUrls= new ArrayList<>();


        assertThat(searchResultItems.getSize()).isGreaterThanOrEqualTo(10); // at least our 10.
        for (SearchResultItem<? extends Page> item : searchResultItems) {
            log.info("Found {} with crids: {}", item, item.getResult().getCrids());
            foundCrids.addAll(item.getResult().getCrids());
            foundUrls.add(item.getResult().getUrl());
        }
        assertThat(foundCrids).containsOnlyOnce(CREATED_CRIDS);
        assertThat(foundUrls).containsOnlyOnce(createdUrls.toArray(new String[0]));
    }


    @Test
    public void test302UpdateUrls(TestInfo testInfo) throws UnsupportedEncodingException {
        //createdCrids.add(new Crid("crid://crids.functional.tests/3"));
        assumeThat(util.getPageUpdateApiClient().getVersionNumber()).isGreaterThanOrEqualTo(Version.of(5, 5));
        assumeTrue(createdUrls.size() > 0);

        String url = "http://test.poms.nl/\u00E9\u00E9n/" + URLEncoder.encode(testInfo.getTestMethod().get().getName() + LocalDate.now(), "UTF-8");

        int i = 0;
        for (String crid: CREATED_CRIDS) {
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
            Result<Void> result = util.saveAndWait(article);
            assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
            log.info("Created {}", article);
        }

    }

    @Test
    public void test303ModificationsArrivedInAPI() {
        assumeThat(util.getPageUpdateApiClient().getVersionNumber()).isGreaterThanOrEqualTo(Version.of(5, 5));

        assumeTrue(createdUrls.size() > 0);
        assumeTrue(modifiedUrls.size() > 0);
        assumeTrue(pageUtil.getClients().isAvailable());

        PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        PageSearchResult searchResultItems = Utils.waitUntil(
            ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            "Has pages " + modifiedUrls,
            () -> pageUtil.find(form, null, 0L, 240),
            (sr) -> sr.asResult().getItems().stream().map(Page::getUrl).collect(Collectors.toList()).containsAll(modifiedUrls)
        );
        List<String> foundCrids = new ArrayList<>();
        List<String> foundUrls= new ArrayList<>();


        for (SearchResultItem<? extends Page> item : searchResultItems) {
            log.info("Found {} with crids: {}", item, item.getResult().getCrids());
            foundCrids.addAll(item.getResult().getCrids());
            foundUrls.add(item.getResult().getUrl());
        }
        assertThat(foundCrids).containsOnlyOnce(CREATED_CRIDS);
        assertThat(foundUrls).doesNotContain(createdUrls.toArray(new String[0]));
        assertThat(foundUrls).containsOnlyOnce(modifiedUrls.toArray(new String[0]));
    }


    @Test
    public void test304DeleteByOneCrid() {
        Result<DeleteResult> result = util.delete(CREATED_CRIDS[0]);


        assertThat(result.getStatus())
            .withFailMessage(result.getErrors() == null ? "Status is not success but " + result.getStatus() : result.getErrors())
            .isEqualTo(Result.Status.SUCCESS);
    }


    @Test
    public void test305DissappearedFromAPI() {
        assumeThat(util.getPageUpdateApiClient().getVersionNumber()).isGreaterThanOrEqualTo(Version.of(5, 5));
        String cridToDelete = CREATED_CRIDS[0];
        assumeTrue(pageUtil.getClients().isAvailable());

        Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            () -> "Has no page with crid " + cridToDelete,
            () -> {
                Page found = pageUtil.load(cridToDelete)[0];
                log.info("Found {}", found);
                return found == null;
            }
        );
    }


    @Test
    public void test306DeleteByCrids() {
        Result<DeleteResult> result = util.deleteWhereStartsWith(CRID_PREFIX);

        //assertThat(result.getEntity().getCount()).isGreaterThan(0);;

        assertThat(result.getStatus())
            .withFailMessage(result.getErrors() == null ? "Status is not success but " + result.getStatus() : result.getErrors())
            .isEqualTo(Result.Status.SUCCESS);
    }



    @Test
    public void test307DissappearedFromAPI() {
        assumeThat(util.getPageUpdateApiClient().getVersionNumber()).isGreaterThanOrEqualTo(Version.of(5, 5));
        assumeTrue(pageUtil.getClients().isAvailable());

        PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
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
    @Disabled
    public void testPage() {
        Result<?> r = util.saveAndWait(PageUpdateBuilder.article("htpt://www.vpro.nl/1234")
            .crids("crid://bla/1234")
            .broadcasters("VPRO")
            .title(title)
            .build());
            assertThat(r.getStatus()).withFailMessage(r.toString()).isEqualTo(Result.Status.SUCCESS);

    }

    @Test
    @Disabled
    public void updateUrl() {
        Result<?> r = util.saveAndWait(PageUpdateBuilder.article("htpt://www.vpro.nl/1234/updated/again")
            .crids("crid://bla/1234")
            .broadcasters("VPRO")
            .title(title)
            .build());
            assertThat(r.getStatus()).withFailMessage(r.toString()).isEqualTo(Result.Status.SUCCESS);

    }

    @Test
    @Disabled
    public void getContent() throws JsonProcessingException {
         PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        PageSearchResult searchResultItems = pageUtil.find(form, null, 0L, 240);
        log.info("{}\n{}", Jackson2Mapper.getPrettyInstance().writeValueAsString(form), searchResultItems);
        List<String> foundCrids = new ArrayList<>();
        List<String> foundUrls= new ArrayList<>();


        assertThat(searchResultItems.getSize()).isEqualTo(10);
        for (SearchResultItem<? extends Page> item : searchResultItems) {
            log.info("Found {} with crids: {}", item, item.getResult().getCrids());
            foundCrids.addAll(item.getResult().getCrids());
            foundUrls.add(item.getResult().getUrl());
        }
        log.info("Found crids: {}, found urls: {}" , foundCrids, foundUrls);

    }

    private void testConsistency(String url, Set<String> checked, boolean cleanup) {
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
    @AbortOnException.NoAbort
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
    @Disabled
    public void test999CleanUp() {
        util.deleteWhereStartsWith("http://test.poms.nl/");
    }

    protected void assumeNotNull(Object object) {
        assumeThat(object).isNotNull();

    }
}
