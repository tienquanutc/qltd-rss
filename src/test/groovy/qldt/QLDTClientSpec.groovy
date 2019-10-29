package qldt

import groovy.json.JsonOutput
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.client.WebClient
import spock.lang.Shared
import spock.lang.Specification

class QLDTClientSpec extends Specification {

    @Shared
    QLDTWebClient qldtClient = new QLDTWebClient(WebClient.create(Vertx.vertx()), [studentId: System.properties.getProperty('studentId'), password: System.properties.getProperty('password')])

    def "test fetchImportantNews"() {
        when:
        def news = qldtClient.importantNews().get()

        then:
        println JsonOutput.prettyPrint(Json.encode(news))
        assert 1 == 1
    }
}
