package nl.vpro.poms.pagespublisher;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.vpro.api.client.resteasy.PageUpdateApiClient;
import nl.vpro.api.client.utils.Config;
import nl.vpro.api.client.utils.PageUpdateApiUtil;
import nl.vpro.api.client.utils.PageUpdateRateLimiter;
import nl.vpro.api.client.utils.Result;
import nl.vpro.domain.api.page.PageForm;
import nl.vpro.domain.page.LinkType;
import nl.vpro.domain.page.Page;
import nl.vpro.domain.page.Referral;
import nl.vpro.domain.page.Section;
import nl.vpro.domain.page.update.*;
import nl.vpro.poms.AbstractApiMediaBackendTest;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.rules.DoAfterException;
import nl.vpro.poms.Utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PagesPublisherTest extends AbstractApiTest {


    static PageUpdateApiUtil util = new PageUpdateApiUtil(
        PageUpdateApiClient.configured(
            CONFIG.env(),
            CONFIG.getProperties(Config.Prefix.pageupdate_api)
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
    }


    @Test
    public void test001CreateOrUpdatePage() throws UnsupportedEncodingException {

        urlToday = "http://test.poms.nl/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now(), "UTF-8");
        urlYesterday = "http://test.poms.nl/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now().minusDays(1), "UTF-8");
        urlTomorrow = "http://test.poms.nl/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now().plusDays(1), "UTF-8");


        PortalUpdate portal = new PortalUpdate("WETENSCHAP24", "http://test.poms.nl");
        portal.setSection(new Section("/" + testMethod.getMethodName(), "Display name " + testMethod.getMethodName()));

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
            assertThat(r.getStatus()).isEqualTo(Result.Status.SUCCESS);
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
        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);
    }

    @Test
    public void test100Arrived() throws Exception {
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
    public void test101ArrivedInAPI() throws Exception {
        assumeNotNull(article);
        Page page = Utils.waitUntil(Duration.ofMinutes(1),
            article.getUrl() + " has title " + article.getTitle(),
            () ->
            pageUtil.load(article.getUrl())[0], p -> Objects.equals(p.getTitle(), article.getTitle())
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
    public void test201ArrivedInApi() throws Exception {
        assumeNotNull(article);
        Page page = Utils.waitUntil(Duration.ofMinutes(1),
            article.getUrl() + " has title " + article.getTitle(),
            () ->
                pageUtil.load(article.getUrl())[0], p -> Objects.equals(p.getTitle(), article.getTitle())
        );
    }

    private static final String TAG = "test_created_with_crid";
    private static final String CRID_PREFIX = "crid://crids.functional.tests/";


    @Test
    public void test300CreateSomeWithCrid() throws UnsupportedEncodingException {
        String url = "http://test.poms.nl/" + URLEncoder.encode(testMethod.getMethodName() + LocalDate.now(), "UTF-8");

        for (int i = 0; i < 10; i++) {
            PageUpdate article =
                PageUpdateBuilder.article(url + "/" + i)
                    .broadcasters("VPRO")
                    .crids(CRID_PREFIX + i)
                    .title(title)
                    .tags(TAG)
                    .build();
            Result result = util.save(article);
            assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
            log.info("Created {}", article);

        }
    }

    @Test
    public void test301ArrivedInAPIThenDeleteByCrid() throws Exception {
        PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        Utils.waitUntil(Duration.ofMinutes(2),
        "Has pages with tag " + TAG,
            () -> pageUtil.find(form, null, 0L, 11).getSize() >= 10);


        // Then delete by crid

        Result result = util.deleteWhereStartsWith(CRID_PREFIX);
        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);


    }

    @Test
    public void test302DissappearedFromAPI() throws Exception {
        PageForm form = PageForm.builder()
            .tags(TAG)
            .build();

        Utils.waitUntil(Duration.ofMinutes(2),
            "Has no pages with tag",
            () -> pageUtil.find(form, null, 0L, 11).getSize() == 0);


    }
}
