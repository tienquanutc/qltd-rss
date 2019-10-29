package app


import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions

class Main {

    static {
        //System.setProperty('studentId', 'Your Student Here')
        //System.setProperty('password', 'Your Password Here')
    }

    static void main(String[] args) {
        Vertx vertx = Vertx.vertx()
        vertx.deployVerticle(new RssWebVerticle(new HttpServerOptions(port: 5000)))
    }
}
