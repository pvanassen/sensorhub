package nl.pvanassen.sensorhub.app

import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class RedirectFilter: WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return if (exchange.request.uri.path == "/") {
            chain.filter(exchange.mutate().request(exchange.request.mutate().path("/index.html").build()).build())
        } else chain.filter(exchange)
    }
}