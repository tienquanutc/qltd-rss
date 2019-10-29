package qldt

import groovy.util.logging.Slf4j
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.text.SimpleDateFormat
import java.util.regex.Pattern

@Slf4j
class QLDTParser {

    private static final SimpleDateFormat IMPORTANT_NEWS_DATE_SIMPLE_FORMAT = new SimpleDateFormat('dd/MM/yyyy')
    private static final Pattern IMPORTANT_NEWS_DATE_REGEX_PATTERN = Pattern.compile("\\(\\d+\\/\\d+\\/\\d+\\)\$")

    @SuppressWarnings("GrMethodMayBeStatic")
    List<Map> parseImportantNewList(String html) {
        Document document = Jsoup.parse(html)

        document.select(".important_news a").collect { aTagElement ->
            return [
                    url  : 'https://qldt.utc.edu.vn/CMCSoft.IU.Web.info/' + aTagElement.attr("href"),
                    title: aTagElement.text(),
                    date : safeExtractImportantNewsDate(aTagElement.text()),
            ]
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    Map parseImportantNewDetails(String html) {
        Document document = Jsoup.parse(html)

        String content = [document.getElementById("lblChitiet").ownText()].with {
            addAll(document.getElementById("lblChitiet").children().collect { it.text() })
            delegate
        }.join("<br/>")
        return [
                title      : document.getElementById("lblTieude").text(),
                short_title: document.getElementById("lblTomtat").text(),
                content    : content,
                files      : document.select("#pnlFile a").collect {
                    [
                            title: it.text(),
                            url  : encodeURLComponent('https://qldt.utc.edu.vn/CMCSoft.IU.Web.info/' + it.attr("href"))
                    ]
                }
        ]
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    Map parseViewStateForm(String html) {
        Document document = Jsoup.parse(html)

        return [
                __EVENTTARGET               : '',
                __EVENTARGUMENT             : '',
                __LASTFOCUS                 : '',
                __VIEWSTATE                 : document.getElementById("__VIEWSTATE").attr('value'),
                __VIEWSTATEGENERATOR        : document.getElementById("__VIEWSTATEGENERATOR").attr('value'),
                __EVENTVALIDATION           : document.getElementById("__EVENTVALIDATION").attr('value'),
                'PageHeader1$drpNgonNgu'    : 'AE56196269AF4476B422067C9B424504',
                'PageHeader1$hidisNotify'   : '0',
                'PageHeader1$hidValueNotify': '.',
                btnSubmit                   : 'Đăng nhập',
                hidUserId                   : '',
                hidUserFullName             : '',
                hidTrainingSystemId         : '',

        ]
    }

    static Date safeExtractImportantNewsDate(String text) {
        def matcher = IMPORTANT_NEWS_DATE_REGEX_PATTERN.matcher(text.trim())
        if (matcher.find()) {
            String dateString = matcher.group()
                    .replace(")", "")
                    .replace("(", "")
            try {
                return IMPORTANT_NEWS_DATE_SIMPLE_FORMAT.parse(dateString)
            } catch (any) {
                log.warn("Failure parse important news date, text = $text", any)
                return null
            }
        }
        log.warn("Can't find important news date,  $text")
        return null
    }

    public static String encodeURLComponent(final String decodedURL) {
        URL url = new URL(decodedURL);
        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef())
        return uri.toASCIIString()
    }
}
