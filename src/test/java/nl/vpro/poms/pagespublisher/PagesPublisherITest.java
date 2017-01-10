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
import nl.vpro.api.client.utils.PageUpdateApiUtil;
import nl.vpro.api.client.utils.PageUpdateRateLimiter;
import nl.vpro.api.client.utils.Result;
import nl.vpro.domain.page.*;
import nl.vpro.domain.page.update.*;
import nl.vpro.poms.AbstractApiTest;
import nl.vpro.poms.Config;
import nl.vpro.poms.DoAfterException;
import nl.vpro.poms.Utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;

/**
 * @author Michiel Meeuwissen
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PagesPublisherITest extends AbstractApiTest {


    static PageUpdateApiUtil util = new PageUpdateApiUtil(
        PageUpdateApiClient.configured(
            Config.env(), Config.getProperties(Config.Prefix.pageupdateapi)
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
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        PagesPublisherITest.exception = t;
    });

    private static Throwable exception = null;

    @Before
    public void setup() {
        assumeNoException(exception);
    }


    @Test
    public void test001CreateOrUpdatePage() throws UnsupportedEncodingException {

        urlToday = "http://test.poms.nl/" + URLEncoder.encode(name.getMethodName() + LocalDate.now(), "UTF-8");
        urlYesterday = "http://test.poms.nl/" + URLEncoder.encode(name.getMethodName() + LocalDate.now().minusDays(1), "UTF-8");
        urlTomorrow = "http://test.poms.nl/" + URLEncoder.encode(name.getMethodName() + LocalDate.now().plusDays(1), "UTF-8");


        PortalUpdate portal = new PortalUpdate("WETENSCHAP24", "http://test.poms.nl");
        portal.setSection(new Section("/" + name.getMethodName(), "Display name " + name.getMethodName()));

        article =
            PageUpdateBuilder.article(urlToday)
                .broadcasters("VPRO")
                .title(title)
                .embeds(EmbedUpdate.builder().midRef("WO_VPRO_025057").title("leuke embed").description("embed in " + title).build())
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


        System.out.println(result);
        assertThat(result.getStatus()).isEqualTo(Result.Status.SUCCESS);
        assertThat(result.getErrors()).isNull();
        log.info("{} -> {}", article, result);
    }

    @Test
    public void test100Arrived() throws Exception {
        assumeNotNull(article);

        PageUpdate update = Utils.waitUntil(Duration.ofMinutes(1), () ->
            util.get(article.getUrl()),
            p -> Objects.equals(p.getTitle(), article.getTitle())
        );
        assertThat(update.getTitle()).isEqualTo(article.getTitle());
    }

    @Test
    public void test101ArrivedInAPI() throws Exception {
        assumeNotNull(article);
        Page page = Utils.waitUntil(Duration.ofMinutes(1), () ->
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

        assertThat(yesterday.getReferrals()).hasSize(1);

        Page tomorrow = pageUtil.load(urlTomorrow)[0];

        assertThat(tomorrow).isNull();

        Page topStory = pageUtil.load(topStoryUrl)[0];

        Optional<Referral> referral = topStory.getReferrals().stream().filter(r -> r.getPageRef().equals(urlToday)).findFirst();

        assertThat(referral).isPresent();
        assertThat(referral.get().getType()).isEqualTo(LinkType.TOP_STORY);

    }
}
