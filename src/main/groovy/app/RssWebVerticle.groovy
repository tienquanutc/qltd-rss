package app

import com.mitchellbosecke.pebble.PebbleEngine
import handler.NewFeedHandler
import handler.PingHandler
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.ext.web.common.template.TemplateEngine
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.templ.pebble.impl.PebbleTemplateEngineImpl
import io.vertx.ext.web.templ.pebble.impl.PebbleVertxLoader
import qldt.QLDTWebClient

class RssWebVerticle extends AbstractVerticle {

    private static String WEB_ROOT_DIRECTORY = "webroot"

    Router router
    TemplateEngine templateEngine
    QLDTWebClient qldtWebClient

    HttpServerOptions httpServerOptions

    RssWebVerticle(HttpServerOptions httpServerOptions) {
        this.httpServerOptions = httpServerOptions
    }

    @Override
    void start(Future<Void> startFuture) throws Exception {
        this.router = Router.router(this.vertx)

        this.templateEngine = new PebbleTemplateEngineImpl(
                new PebbleEngine.Builder()
                        .loader(new PebbleVertxLoader(this.vertx))
                        .cacheActive(false)
                        .build()
        )

        this.qldtWebClient = new QLDTWebClient(
                WebClient.create(this.vertx, new WebClientOptions(
                        keepAlive: true,
                        tryUseCompression: true,
                        userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36'
                )),
                //auth here
                [studentId: "160702211", password: "16021995"]
        )

        this.setupRouter()
        def httpServer = this.vertx.createHttpServer(this.httpServerOptions)
        httpServer.requestHandler(this.router).listen(this.httpServerOptions.port) { asyncResult ->
            if (asyncResult.succeeded()) {
                println "Server started at http://127.0.0.1:" + httpServer.actualPort()
            } else {
                httpServer.close()
                throw new RuntimeException("Unable to start Server at http://127.0.0.1:" + httpServer.actualPort(), asyncResult.cause())
            }
        }
    }

    void setupRouter() {
        router.getWithRegex('/static/.*').handler(StaticHandler.create(WEB_ROOT_DIRECTORY))
        router.get("/").handler(new PingHandler())

        NewFeedHandler newFeedHandler = new NewFeedHandler(templateEngine, qldtWebClient)
        router.get("/important-news").handler(newFeedHandler)
        router.get("/important-news/").handler(newFeedHandler)

        router.route().last().handler { ctx -> ctx.response().setStatusCode(400).end(Json.encode([status_code: 404, message: "notfound"])) }
        router.route().failureHandler { ctx -> ctx.response().setStatusCode(500).end(Json.encode([status_code: 500, message: "failure"])) }
    }
}
