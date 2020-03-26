package nl.vpro.poms.pagespublisher;

import lombok.extern.log4j.Log4j2;

import java.net.URLEncoder;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import nl.vpro.api.client.pages.PageUpdateApiClient;
import nl.vpro.api.client.utils.Result;
import nl.vpro.api.client.utils.*;
import nl.vpro.domain.api.*;
import nl.vpro.domain.api.page.*;
import nl.vpro.domain.media.MediaObject;
import nl.vpro.domain.media.Schedule;
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
import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.vpro.api.client.utils.Config.Prefix.npo_pageupdate_api;
import static nl.vpro.domain.api.Order.DESC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michiel Meeuwissen
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
class PagesPublisherTest extends AbstractApiMediaBackendTest {

    private static final Duration ACCEPTABLE_DURATION = Duration.ofMinutes(3);
    private static final Duration ACCEPTABLE_PAGE_PUBLISHED_DURATION = Duration.ofMinutes(15);
    private static final Duration ACCEPTABLE_MEDIA_PUBLISHED_DURATION = Duration.ofMinutes(15);

    private static final PageUpdateApiUtil pageUpdateApiUtil = new PageUpdateApiUtil(
        PageUpdateApiClient.configured(
            CONFIG.env(npo_pageupdate_api),
            CONFIG.getProperties(npo_pageupdate_api)
        )
            .connectTimeout(Duration.ofSeconds(10))
            .warnThreshold(Duration.ofMillis(500))
            .socketTimeout(Duration.ofSeconds(60))
            .build(),
        PageUpdateRateLimiter.builder().build()
    );

    static {
        log.info("Using {}", pageUpdateApiUtil);
    }
    private static final String URL_PREFIX = "http://test.poms.nl/";
    private static final String TOP_STORY_URL = URL_PREFIX + "test001CreateOrUpdatePageTopStory";
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
        log.info("Testing with version {}", pageUpdateApiUtil.getPageUpdateApiClient().getVersionNumber());
        log.info("Backend available: {}", backend.isAvailable());
    }


    @Test
    @Order(1)
    @Tag("embeddedmedia")
    public void createOrUpdatePage(TestInfo testInfo) {

        String methodName = testInfo.getTestMethod().get().getName();
        LocalDate today = LocalDate.now();

        urlToday = URL_PREFIX  + URLEncoder.encode(methodName + today, UTF_8);
        urlYesterday = URL_PREFIX + URLEncoder.encode(methodName + today.minusDays(1), UTF_8);
        urlTomorrow = URL_PREFIX + URLEncoder.encode(methodName + today.plusDays(1), UTF_8);


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
                    LinkUpdate.topStory(TOP_STORY_URL, "mooie story over sterrenhopen"),
                    LinkUpdate.of(urlYesterday, "yesterday"),
                    LinkUpdate.of(urlTomorrow, "tomorrow")
                )
            .build();

        PageUpdate yesterday = pageUpdateApiUtil.get(urlYesterday);
        if (yesterday == null) {
            log.info("Article for yesterday {} not found (perhaps test didn't run yesterday). Making it for now, to test referrals too" , urlYesterday);
            Result<Void> r = pageUpdateApiUtil.saveAndWait(PageUpdateBuilder.article(urlYesterday)
                .broadcasters("VPRO")
                .title(title)
                .portal(portal)
                .build());
            assertThat(r.getStatus()).withFailMessage(r.toString()).isEqualTo(Result.Status.SUCCESS);
        }

        PageUpdate topStory = pageUpdateApiUtil.get(TOP_STORY_URL);
        if (topStory == null) {
            log.info("Topstory {} not found. Making it now", TOP_STORY_URL);
            Result<Void> r = pageUpdateApiUtil.saveAndWait(PageUpdateBuilder.article(TOP_STORY_URL)
                .broadcasters("VPRO")
                .title("Sterrenhopen en zo, heel interessant")
                .portal(portal)
                .build());
            assertThat(r.getStatus()).isEqualTo(Result.Status.SUCCESS);
        }

        Result<Void> result = pageUpdateApiUtil.saveAndWait(article);
        log.info("{}", result);
        assertThat(result.getStatus()).withFailMessage("" + result).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);
    }

    @Test
    @Order(100)
    public void checkPageArrived() {
        assumeThat(article).isNotNull();

        PageUpdate update = Utils.waitUntil(ACCEPTABLE_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () ->
            pageUpdateApiUtil.get(article.getUrl()),
            p -> Objects.equals(p.getTitle(), article.getTitle())
        );
        assertThat(update.getTitle()).isEqualTo(article.getTitle());
    }


    /**
     * Check if the page arrived correctly in ES. (Using the specific end point for that)
     *
     * This is more direct, and to test this the API itself doesn't even need to be running itself.
     */
    @Test
    @Order(101)
    public void checkPagePublished() {
        assumeThat(article).isNotNull();

        Page update = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () ->
            pageUpdateApiUtil.getPublishedPage(article.getUrl()).orElse(null),
            p -> Objects.equals(p.getTitle(), article.getTitle())
        );
        assertThat(update.getTitle()).isEqualTo(article.getTitle());
    }




    /**
     * Also, check if it arrived in the api itself. That should not make a difference, it looks at the same Elasticsearch.
     *
     * But _if_ it does, this probably points to some misconfiguration, (or some odd caching issue?)
     */
    @Test
    @Order(102)
    @AbortOnException.Except
    @Tag("frontendapi")
    public void checkArrivedInAPI() {
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

        assertThat(yesterday).isNotNull();
        assertThat(yesterday.getReferrals()).isNotNull();
        assertThat(yesterday.getReferrals().size()).isGreaterThanOrEqualTo(1);

        Page tomorrow = pageUtil.load(urlTomorrow)[0];

        assertThat(tomorrow).isNull();
        Page topStory = pageUtil.load(TOP_STORY_URL)[0];

        Optional<Referral> referral = topStory.getReferrals()
            .stream()
            .filter(r -> r.getPageRef().equals(urlToday))
            .findFirst();

        assertThat(referral).withFailMessage(TOP_STORY_URL + " has no referral " + urlToday).isPresent();
        assertThat(referral.get().getType()).isEqualTo(LinkType.TOP_STORY);

    }


    /**
     * Set a new title
     */
    @Test
    @Order(200)
    public void updateExistingArticle() {
        assumeThat(article).isNotNull();

        log.info("Updating {} tot title {}", article.getUrl(), title);
        article.setTitle(title);
        Result<Void> result = pageUpdateApiUtil.saveAndWait(article);


        log.info("{}",  result);
        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();

    }

    /**
     * Make sure this new titles arrived in ES
     */
    @Test
    @Order(201)
    public void checkUpdateExistingArticlePublished() {
        assumeThat(article).isNotNull();

        Page page = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            article.getUrl() + " has title " + article.getTitle(),
            () ->
                pageUpdateApiUtil.getPublishedPage(article.getUrl()).orElse(null), p -> Objects.equals(p.getTitle(), article.getTitle())
        );

        MediaObject embedded = pageUpdateApiUtil.getMedia(MID).orElseThrow(() -> new NotFoundException(MID));

        assertThat(page.getEmbeds()).hasSize(2);

        assumeThat(page.getEmbeds().get(0).getMedia()).isNotNull();

        assertThat(page.getEmbeds().get(0).getMedia().getMid()).isEqualTo(MID);
        assertThat(page.getEmbeds().get(0).getMedia().getMainTitle()).isEqualTo(embedded.getMainTitle());
    }

    /**
     * And also in the API of course (this should be the same)
     */
    @Test
    @Order(202)
    @AbortOnException.Except
    @Tag("frontendapi")
    public void checkUpdateExistingArticleArrivedInApi() {
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


    /**
     * Now we update a media that is embedded. The pages publisher should synchronize this change to all pages embedding this media.
     */
    @Test
    @Order(203)
    @Tag("embeddedmedia")
    public void updateExistingEmbeddedMedia() {
        assumeTrue(backend.isAvailable());

        MediaUpdate<?> embedded = backend.get(MID);
        embeddedDescription = "Updated by " + title;
        embedded.setMainDescription(embeddedDescription);
        backend.set(embedded);
    }

    /**
     * Let's first wait until this change arrives in the API, i.e. in ES. earlier, there is no chance that
     * the page publisher will be aware of this change.
     */

    @Test
    @Order(204)
    @Tag("embeddedmedia")
    public void checkUpdateExistingEmbedMediaArrivedInMedia() {
        assumeThat(article).isNotNull();
        assumeNotNull(embeddedDescription);

        MediaObject fromApi = Utils.waitUntil(ACCEPTABLE_MEDIA_PUBLISHED_DURATION,
            MID + " has description " + embeddedDescription,
            () -> pageUpdateApiUtil.getMedia(MID).orElse(null),
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
    @Order(205)
    @Tag("embeddedmedia")
    public void checkUpdateExistingEmbedMediaArrivedInPublishedPage() {
        Page page = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            //article.getUrl() + " has embedded " + MID + " with description " + embeddedDescription,
            () ->
                pageUpdateApiUtil.getPublishedPage(article.getUrl()),
            Check.<Optional<Page>>builder()
                .predicate(Optional::isPresent)
                .description("{} exists", article.getUrl())
            ,
            Check.<Optional<Page>>builder()
                .predicate(p -> Objects.equals(p.get().getEmbeds().get(0).getMedia().getMid(), MID))
                .description("{} embeds {}", article.getUrl(), MID)
            ,
            Check.<Optional<Page>>builder()
                .predicate(p -> Objects.equals(p.get().getEmbeds().get(0).getMedia().getMainDescription(), embeddedDescription))
                .description("{}/{} has description {}", article.getUrl(), MID,  embeddedDescription)
        ).get();

        assertThat(page.getEmbeds().get(0).getMedia().getMainDescription()).isEqualTo(embeddedDescription);
    }



    @Test
    @Order(206)
    @Tag("frontendapi")
    @Tag("embeddedmedia")
    public void checkUpdateExistingEmbedMediaArrivedInPagesApi() {
        assumeTrue(pageUtil.getClients().isAvailable());

        Page page = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            article.getUrl() + " has embedded " + MID + " with description " + embeddedDescription,
            () ->
                pageUtil.load(article.getUrl())[0],
            p -> p != null && Objects.equals(p.getEmbeds().get(0).getMedia().getMainDescription(), embeddedDescription)
        );

        assertThat(page.getEmbeds().get(0).getMedia().getMainDescription()).isEqualTo(embeddedDescription);
    }


    /**
     * How entirely remove an embed. Also that must be mirrored in all pages.
     */
    @Test
    @Order(210)
    @Tag("embeddedmedia")
    public void removeAnEmbed() {
        assumeThat(article).isNotNull();
        article.setEmbeds(new ArrayList<>(article.getEmbeds())); // make the damn list modifiable.
        article.getEmbeds().remove(0);
        Result<Void> result = pageUpdateApiUtil.saveAndWait(article);
        log.info("{}", result);
        assertThat(result.getStatus()).withFailMessage("" + result).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);

    }

    /**
     * Check that the remoed embed disappeared. i.e. on the given page there should only be one left.
     */
    @Test
    @Order(211)
    @Tag("embeddedmedia")
    public void checkRemoveAnEmbed() {
        assumeThat(article).isNotNull();
        Page page = Utils.waitUntil(ACCEPTABLE_PAGE_PUBLISHED_DURATION,
            article.getUrl() + " has only one embed",
            () ->
                pageUpdateApiUtil.getPublishedPage(article.getUrl()).orElse(null),
            p -> p != null && p.getEmbeds().size() == 1
        );

        assertThat(page.getEmbeds()).hasSize(1);
        assertThat(page.getEmbeds().get(0).getMedia().getMid()).isEqualTo(ANOTHER_MID);

    }

    @Test
    public void cleanUpOldPages() {
        LocalDate today = LocalDate.now(Schedule.ZONE_ID);

        LocalDate lastMonth = today.minusMonths(1);


    }


    private static final String TAG = "test_created_with_crid";
    private static final List<String> createdUrls = new ArrayList<>();
    private static final List<String> modifiedUrls = new ArrayList<>();


    /**
     * Now create an entirely new page, but use some of the same number of crids.
     *
     * The point is that the urls are generated on date, but the crids aren't.
     *
     * So in the database the crids are normally already present, but may be with different URL's.
     *
     * Als this should give no problems, and everything should be updated according.
     */
    @Test
    @Order(300)
    @Tag("crids")
    public void createPageSomeWithCrid(TestInfo testInfo) {
        String url = "http://test.poms.nl/\u00E9\u00E9n/" + URLEncoder.encode(testInfo.getTestMethod().get().getName() + SIMPLE_NOWSTRING, UTF_8);

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
            Result<Void> result = pageUpdateApiUtil.saveAndWait(article);
            assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
            log.info("Created {}", article);
        }
    }

    @Test
    @Order(301)
    @Tag("crids")
    public void checkPagesWithCrids() {
        for (int i = 0; i < NUMBER_OF_PAGES_TO_CREATED; i++) {
            String createdUrl = createdUrls.get(i);
            Utils.waitUntil(Duration.ofMinutes(1),
                () -> pageUpdateApiUtil.get(createdUrl),
                Check.<PageUpdate>builder()
                    .description("Has update  {}", createdUrl)
                    .predicate(Objects::nonNull)
            );
        }
        for (int i = 0; i < NUMBER_OF_PAGES_TO_CREATED; i++) {
            String createdUrl = createdUrls.get(i);
            Utils.waitUntil(Duration.ofMinutes(1),
                () -> pageUpdateApiUtil.getPublishedPage(createdUrl),
                Check.<Optional<Page>>builder()
                    .description("Has {}", createdUrl)
                    .predicate(Optional::isPresent)
            );
        }
    }

    /**
     * In the frontend api we're going to test a bit more sophisticated.
     *
     * We search everthing with the tag {@link #TAG}
     *
     * This should only result the createdUrls, nothing more, nothing less.
     */
    @Test
    @Order(302)
    @Tag("frontendapi")
    @Tag("crids")
    public void checkCreatedPageWithCridArrivedInES() throws JsonProcessingException {

        PageForm form = PageForm.builder()
            .tags(TAG)
            .addSortField(PageSortField.lastPublished, DESC)
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




    /**
     * Now iterate the set of pages again, give then the same crids, but make up new urls.
     *
     * This whould work, no duplicate crids should come to exist
     */
    @Test
    @Order(400)
    public void updateUrls(TestInfo testInfo) {
        //createdCrids.add(new Crid("crid://crids.functional.tests/3"));
        assumeThat(pageUpdateApiUtil.getPageUpdateApiClient().getVersionNumber()).isGreaterThanOrEqualTo(Version.of(5, 5));
        assumeTrue(createdUrls.size() > 0);

        String url = "http://test.poms.nl/\u00E9\u00E9n/" + URLEncoder.encode(testInfo.getTestMethod().get().getName() + SIMPLE_NOWSTRING, UTF_8);

        int i = 0;
        for (String crid: CREATED_CRIDS) {
            String modifiedUrl = url + "/modified/" + i++;
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
            Result<Void> result = pageUpdateApiUtil.saveAndWait(article);
            assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
            log.info("Update {}", article);
        }

    }

    @Test
    @Order(401)
    @Tag("frontendapi")
    public void checkModificationsArrivedInAPI() {
        assumeThat(pageUpdateApiUtil.getPageUpdateApiClient().getVersionNumber()).isGreaterThanOrEqualTo(Version.of(5, 5));

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


    /**
     * Now delete one of those create pages by crid.
     */
    @Test
    @Order(500)
    public void deleteByOneCrid() {
        Result<DeleteResult> result = pageUpdateApiUtil.delete(CREATED_CRIDS[0]);


        assertThat(result.getStatus())
            .withFailMessage(result.getErrors() == null ? "Status is not success but " + result.getStatus() : result.getErrors())
            .isEqualTo(Result.Status.SUCCESS);
    }


    /**
     * That should work.
     */
    @Test
    @Order(501)
    public void checkDeletedByCridDissappearedFromAPI() {
        assumeThat(pageUpdateApiUtil.getPageUpdateApiClient().getVersionNumber()).isGreaterThanOrEqualTo(Version.of(5, 5));
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


    /**
     * Delete the rest too.
     */
    @Test
    @Order(600)
    public void deleteByCrids() {
        Result<DeleteResult> result = pageUpdateApiUtil.deleteWhereStartsWith(CRID_PREFIX);

        //assertThat(result.getEntity().getCount()).isGreaterThan(0);;

        assertThat(result.getStatus())
            .withFailMessage(result.getErrors() == null ? "Status is not success but " + result.getStatus() : result.getErrors())
            .isEqualTo(Result.Status.SUCCESS);
    }



    /**
     * That too should work.
     */
    @Test
    @Order(601)
    public void checkDissappearedFromAPI() {
        assumeThat(pageUpdateApiUtil.getPageUpdateApiClient().getVersionNumber()).isGreaterThanOrEqualTo(Version.of(5, 5));
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


    /**
     * Some more testing about the state of the publisher
     */
    @Test
    @Order(700)
    public void consistency() {
        Set<String> checked = new LinkedHashSet<>();
        testConsistency(TOP_STORY_URL, checked, false);
        log.info("{}", checked);
    }



    /**
     * What happens if we post completely invalid XML?
     */
    @Test
    @Order(800)
    public void postCompletelyWrongXML() {
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
            .  statusCode(400); // bad request

    }


    /**
     * Now we post an xml that is more or less ok, but use an entity that does not exist (ldquo), this should be rejected.
     */
    @Test
    @Order(801)
    public void postInvalidXMLInvalidEntity() {
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



    /**
     * Now we post an xml that is more or less ok, no title, this is another example of invalid content
     */
    @Test
    @Order(802)
    public void postInvalidXML() {
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
                "</page>")
            .  post(url + "/api/pages/updates")
            .then()
            .  log().all()
            .  statusCode(400);
        // TODO, I dont see the actual message anywhere.

    }


    @Test
    @Disabled
    public void testPage() {
        Result<?> r = pageUpdateApiUtil.saveAndWait(PageUpdateBuilder.article("htpt://www.vpro.nl/1234")
            .crids("crid://bla/1234")
            .broadcasters("VPRO")
            .title(title)
            .build());
            assertThat(r.getStatus()).withFailMessage(r.toString()).isEqualTo(Result.Status.SUCCESS);

    }

    @Test
    @Disabled
    public void updateUrl() {
        Result<?> r = pageUpdateApiUtil.saveAndWait(PageUpdateBuilder.article("htpt://www.vpro.nl/1234/updated/again")
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


    /**
     * the giving URL must have referrals that exist too.
     */
    private void testConsistency(String url, Set<String> checked, boolean cleanup) {
        if (checked.contains(url)) {
            return;
        }
        checked.add(url);

        MultipleEntry<Page> multipleEntry = clients.getPageService().loadMultiple(url, null, null).getItems().get(0);
        if (multipleEntry.getResult() == null) {
            log.warn("Could not find {}", url);
            if (cleanup) {
                pageUpdateApiUtil.delete(url);
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
    @Order(1000)
    public void cleanUps() {
        MultipleEntry<Page> multipleEntry = clients.getPageService().loadMultiple(TOP_STORY_URL, null, null).getItems().get(0);

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
                log.info("result {} ", pageUpdateApiUtil.delete(r.getId()));
                removed.add(r.getId());
            }
        }
        if (! removed.isEmpty()) {
            log.warn("Removed {}!", removed);
        }

    }

    @Test
    @Order(1500)
    @Disabled
    public void test999CleanUp() {
        Result<DeleteResult> deleteResult = pageUpdateApiUtil.deleteWhereStartsWith(URL_PREFIX);
        log.info("{}", deleteResult);
    }

    protected void assumeNotNull(Object object) {
        assumeThat(object).isNotNull();

    }
}
