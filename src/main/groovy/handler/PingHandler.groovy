package handler

import groovy.transform.CompileStatic
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.InetAddressValidator

import java.lang.management.ManagementFactory
import java.lang.management.MemoryPoolMXBean
import java.lang.management.ThreadMXBean


class PingHandler implements Handler<RoutingContext> {

    @Override
    void handle(RoutingContext ctx) {
        if ('true' == ctx.request().getParam("gc")) {
            System.gc()
        }

        Runtime runtime = Runtime.getRuntime();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean()
        def resultMap = [:]

        resultMap.jvm = [
                heap   : [
                        used_heap: ((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)).round(1) + " MB",
                        heap_size: (runtime.totalMemory() / (1024 * 1024)).round(1) + " MB",
                        max_heap : (runtime.maxMemory() / (1024 * 1024)).round(1) + " MB"
                ],
                threads: [
                        live_threads  : threadMXBean.threadCount,
                        daemon_threads: threadMXBean.daemonThreadCount
                ]
        ]

        for (MemoryPoolMXBean memoryMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if ("Metaspace" == memoryMXBean.getName()) {
                resultMap.metaspace = [
                        used_metaspace: (memoryMXBean.getUsage().getUsed() / (1024 * 1024)).round(1) + " MB"
                ]
            }
        }

        resultMap.request = [
                headers: ctx.request().headers().names().collectEntries {
                    [(it): ctx.request().headers().get(it)]
                },
                network: [
                        ip: getIp(ctx.request())
                ]
        ]

        ctx.response()
                .putHeader("Cache-Control", "no-store")
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject(resultMap as Map<String, Object>).encode())
    }

    static String getIp(HttpServerRequest request) {
        return getIp(request, "X-Forwarded-For")
    }

    @CompileStatic
    static String getIp(HttpServerRequest request, String header) {
        String ip = getIpFromHeader(request, header, false)
        if (StringUtils.isNotBlank(ip)) {
            return ip
        }
        return request.remoteAddress().host()
    }

    @CompileStatic
    static String getIpFromHeader(HttpServerRequest request, String header, boolean allowPrivate) {
        String value = request.getHeader(header)
        if (StringUtils.isBlank(value)) {
            return null
        }
        StringTokenizer tokenizer = new StringTokenizer(value, ",")
        while (tokenizer.hasMoreTokens()) {
            String ip = tokenizer.nextToken().trim()
            if (!InetAddressValidator.getInstance().isValid(ip)) {
                continue
            }
            if (!allowPrivate && isIPv4Private(ip)) {
                continue
            }
            return ip
        }

        return null
    }

    /**
     * Check private IP is v4
     * 10.0.0.0 ~ 10.255.255.255
     * 172.16.0.0 ~ 172.31.255.255
     * 192.168.0.0 ~ 192.168.255.255
     * @param ip
     * @return
     */
    @CompileStatic
    static boolean isIPv4Private(String ip) {
        if (!InetAddressValidator.instance.isValidInet4Address(ip)) {
            return false
        }
        String[] octets = ip.split("\\.")
        long longIp = (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16) + (Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3])
        return (167772160L <= longIp && longIp <= 184549375L) || (2886729728L <= longIp && longIp <= 2887778303L) || (3232235520L <= longIp && longIp <= 3232301055L)
    }

}
