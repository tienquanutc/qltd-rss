package handler

import groovy.util.logging.Slf4j
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.common.template.TemplateEngine
import qldt.QLDTWebClient

import java.util.concurrent.CompletableFuture

@Slf4j
class NewFeedHandler extends ViewsHandler {

    private static final Integer CACHE_INVALID_TIME_MILLIS = 5 * 60 * 1000 //5minutes

    private QLDTWebClient qldtWebClient
    private List<Map> cachedNews = Collections.emptyList()
    private long lastCrawledTimeMillis = System.currentTimeMillis()

    NewFeedHandler(TemplateEngine templateEngine, QLDTWebClient qldtWebClient) {
        super(templateEngine, "/news_feed.peb")
        this.qldtWebClient = qldtWebClient
    }

    @Override
    void handle(RoutingContext ctx) {
        println ctx.request().headers()
        long currentTimeMillis = System.currentTimeMillis()
        String selfUrl = ctx.request().absoluteURI().replace('http:', 'https:')
        boolean refresh = ctx.request().getParam("refresh") == 'true'
        boolean cacheIsValid = cachedNews && ((this.lastCrawledTimeMillis + CACHE_INVALID_TIME_MILLIS) > currentTimeMillis) && !refresh

        CompletableFuture<List<Map>> newsFuture = cacheIsValid ? CompletableFuture.completedFuture(cachedNews) :
                this.qldtWebClient.importantNews().handle { news, error ->
                    if (error) log.warn("error", error)
                    if (news) {
                        this.cachedNews = news
                        this.lastCrawledTimeMillis = currentTimeMillis
                    }
                    return this.cachedNews
                }

        newsFuture.thenApply { news ->
            ctx.put("news", cachedNews)
                    .put("last_crawled_at", lastCrawledTimeMillis)
                    .put("self_url", selfUrl)
                    .put("Cache-Control", "max-age=${CACHE_INVALID_TIME_MILLIS}")
                    .put("base_url", ctx.request().scheme() + "://" + ctx.request().host())
                    .response().putHeader("Content-Type", "text/xml; charset=UTF-8")

            this.renderView(ctx)
        }.exceptionally(ctx.&fail)
    }
}
