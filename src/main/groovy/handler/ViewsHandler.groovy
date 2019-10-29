package handler


import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.common.template.TemplateEngine

abstract class ViewsHandler implements Handler<RoutingContext> {

    private static String VIEW_DIRECTORY = "views"
    private TemplateEngine templateEngine
    private String viewFileName

    ViewsHandler(TemplateEngine templateEngine, String viewFileName) {
        this.templateEngine = templateEngine
        this.viewFileName = viewFileName
    }

    void renderView(RoutingContext ctx) {
        this.renderView(ctx, this.viewFileName)
    }

    void renderView(RoutingContext ctx, String viewFileName) {
        try {
            ctx.response().putHeader("X-Frame-Options", "SAMEORIGIN")
            ctx.response().putHeader("X-XSS-Protection", "1; mode=block")
            this.templateEngine.render(ctx.data(), VIEW_DIRECTORY + viewFileName, { AsyncResult<Buffer> render ->
                if (render.failed()) throw render.cause()
                if (!ctx.response().headers().contains("Content-Type")) ctx.response().putHeader("Content-Type", "text/html; charset=UTF-8")
                ctx.response().end(render.result())
            })
        } catch (any) {
            any.printStackTrace()
            ctx.fail(any)
        }
    }
}
