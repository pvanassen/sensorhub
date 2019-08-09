package nl.pvanassen.sensorhub.app.server

import mu.KotlinLogging
import nl.pvanassen.sensorhub.app.client.DomoticsClient
import nl.pvanassen.sensorhub.app.client.StatsdClient
import nl.pvanassen.sensorhub.app.model.Sensor
import nl.pvanassen.sensorhub.app.model.TemperatureSensor
import nl.pvanassen.sensorhub.app.service.NameResolverService
import nl.pvanassen.sensorhub.app.service.NamedSensor
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.netty.udp.UdpInbound
import reactor.netty.udp.UdpOutbound
import reactor.netty.udp.UdpServer
import javax.annotation.PostConstruct

class UdpReceiver(private val nameResolverService: NameResolverService,
                  private val domoticsClient: DomoticsClient,
                  private val statsdClient: StatsdClient) {

    private val logger = KotlinLogging.logger {}

    @PostConstruct
    fun init() {
        UdpServer.create()
                .port(1234)
                .handle {i: UdpInbound?, _: UdpOutbound? -> handleUdp(i!!) }
                .bind()
    }

    private fun handleUdp(input: UdpInbound): Publisher<Void> {
        return input.receive()
                .asString()
                .flatMap { createTemperatureSensorList(it) }
                .map { nameResolverService.getOrCreate(it) }
                .flatMap { actions(it) }
                .flatMap { Mono.empty<Void>() }
                .doOnError { logger.info("Error on temperature", it) }
    }

    private fun createTemperatureSensorList(line: String) =
        Flux.just(line)
                .flatMap { it.splitToSequence("\n").toFlux() }
                .map { createTemperatureSensor(it) }


    private fun createTemperatureSensor(line: String): TemperatureSensor {
        val split = line.splitToSequence("|").toList()
        val mac = split.get(0)
        val temperature = split.get(1).toFloat()
        val humidity = split.get(2).toFloat()
        return TemperatureSensor(mac, temperature, humidity)
    }

    private fun actions(namedSensor: Mono<NamedSensor<Sensor>>) =
            Flux.merge(domoticsClient.sendTemperature(namedSensor),
                    statsdClient.sendTemperature(namedSensor))
                    .collectList()
                    .map { isSuccess(it) }

    private fun isSuccess(results: List<Boolean>) =
            results.stream()
                    .distinct()
                    .filter { result -> !result }
                    .findFirst()
                    .orElse(false)

}

// String(locatie) + ".temperature:" + String(t) + "|g\n" + String(locatie) + ".humidity:" + String(h) + "|g"
// aa:bb:cc:dd:ee:ff|12.2|30\n