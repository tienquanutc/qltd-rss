package qldt

import com.spotify.futures.CompletableFutures
import groovy.util.logging.Slf4j
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@Slf4j
class QLDTWebClient {

    private static final long INVALID_AUTH_MS = 5 * 60 * 1000 //5 minutes

    private static final String URL_QLDT_LOGIN = 'https://qldt.utc.edu.vn/CMCSoft.IU.Web.info/login.aspx'
    private static final String URL_QLDT_HOME = 'https://qldt.utc.edu.vn/CMCSoft.IU.Web.info/Home.aspx'

    private final WebClient webClient
    private final QLDTParser qldtParser
    private final String studentId
    private final String password

    private String cookie = ""
    private long lastLoginEpoxMs = System.currentTimeMillis()

    QLDTWebClient(WebClient webClient, Map loginParams) {
        this.webClient = webClient
        this.qldtParser = new QLDTParser()
        this.studentId = loginParams.studentId
        this.password = loginParams.password

        assert studentId: "Student Id must be not null"
        assert password: "Password Id must be not null"
    }

    CompletableFuture<List<Map>> importantNews() {
        return this.loginIfNeed().thenCompose {
            wrap {
                this.webClient.getAbs(URL_QLDT_HOME)
                        .putHeader('Cookie', cookie)
                        .send(it)
            }.thenCompose { HttpResponse<Buffer> homeResponse ->
                assert homeResponse.statusCode() == 200: "Bad status code ${homeResponse.statusCode()}"

                List<Map> importantNewsList = this.qldtParser.parseImportantNewList(homeResponse.bodyAsString())

                List<CompletableFuture<Map>> importantNewsFutures = importantNewsList.collect { importantNews ->
                    String url = importantNews.url as String
                    wrap {
                        this.webClient.getAbs(url)
                                .putHeader('Cookie', cookie)
                                .send(it)
                    }.thenApply { HttpResponse<Buffer> importantNewsDetailsResponse ->
                        assert importantNewsDetailsResponse.statusCode() == 200: "Bad status code ${importantNewsDetailsResponse.statusCode()} - $url"
                        Map importantNewsDetails = this.qldtParser.parseImportantNewDetails(importantNewsDetailsResponse.bodyAsString())
                        importantNews.putAll(importantNewsDetails)
                        return importantNews
                    }
                }

                return CompletableFutures.allAsList(importantNewsFutures)
            }
        }
    }

    private CompletableFuture<Void> loginIfNeed() {
        return !this.cookie || this.lastLoginEpoxMs + INVALID_AUTH_MS > System.currentTimeMillis() ?
                this.login() : CompletableFuture.completedFuture(null as Void)
    }

    private CompletableFuture<Void> login() {
        return wrap {
            this.webClient.getAbs(URL_QLDT_LOGIN)
                    .send(it)
        }.thenCompose { HttpResponse<Buffer> getLoginResponse ->
            assert getLoginResponse.followedRedirects(): "Prefetch login not redirect"
            assert getLoginResponse.statusCode() == 200: "Bad status code ${getLoginResponse.statusCode()}"

            String loginPageUrl = "https://qldt.utc.edu.vn" + getLoginResponse.followedRedirects().first()
            Map viewStateMap = this.qldtParser.parseViewStateForm(getLoginResponse.bodyAsString())

            return wrap {
                this.webClient.postAbs(loginPageUrl)
                        .sendForm(
                                MultiMap.caseInsensitiveMultiMap()
                                        .addAll(viewStateMap)
                                        .add('txtUserName', studentId)
                                        .add('txtPassword', password.md5())
                                , it)
            }.thenApply { HttpResponse<Buffer> postLoginResponse ->
                this.cookie = postLoginResponse.getHeader('Set-Cookie')
                return (null as Void)
            }
        }
    }


    private static <T> CompletableFuture<T> wrap(Consumer<Handler<AsyncResult<T>>> completionConsumer) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>()
        try {
            completionConsumer.accept({ AsyncResult<T> asyncResult ->
                if (asyncResult.succeeded()) completableFuture.complete(asyncResult.result())
                else completableFuture.completeExceptionally(asyncResult.cause())
            } as Handler<AsyncResult<T>>)
        } catch (Exception e) {
            completableFuture.completeExceptionally(e)
        }
        return completableFuture
    }

}
