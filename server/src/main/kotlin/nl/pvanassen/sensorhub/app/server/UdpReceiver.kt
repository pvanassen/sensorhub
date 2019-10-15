package nl.pvanassen.sensorhub.app.server

import mu.KotlinLogging
import nl.pvanassen.sensorhub.app.client.DomoticsClient
import nl.pvanassen.sensorhub.app.client.StatsdClient
import nl.pvanassen.sensorhub.app.model.NamedSensor
import nl.pvanassen.sensorhub.app.model.SensorId
import nl.pvanassen.sensorhub.app.model.TemperatureSensor
import nl.pvanassen.sensorhub.app.service.GraphiteService
import nl.pvanassen.sensorhub.app.service.NameResolverService
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.scheduler.Schedulers
import reactor.netty.udp.UdpInbound
import reactor.netty.udp.UdpOutbound
import reactor.netty.udp.UdpServer
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.streams.toList

class UdpReceiver(private val nameResolverService: NameResolverService,
                  private val domoticsClient: DomoticsClient,
                  private val statsdClient: StatsdClient,
                  private val graphiteService: GraphiteService) {

    private val logger = KotlinLogging.logger {}

    fun init() {
        for (address in getExternalIp()) {
            UdpServer.create()
                    .host(address)
                    .port(1234)
                    .handle { i: UdpInbound?, _: UdpOutbound? -> handleUdp(i!!) }
                    .bind()
                    .subscribeOn(Schedulers.elastic())
                    .subscribe()
        }
    }

    private fun getExternalIp(): List<String> {
        return NetworkInterface.networkInterfaces()
                .filter { it.isUp }
                .flatMap { it.interfaceAddresses.stream() }
                .map { it.address }
                .filter { !it.isLoopbackAddress }
                .filter { it is Inet4Address }
                .map {it.hostAddress }
                .toList()
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
        logger.info { "Received $line" }
        val split = line.splitToSequence("|").toList()
        val prefix = split[0]
        if (prefix != "t") {
            throw IllegalArgumentException("Can't yet handle $prefix")
        }
        val id = split[1]
        val temperature = split[2].toFloat()
        val humidity = split[3].toFloat()
        return TemperatureSensor(id = id, temperature = temperature, humidity = humidity)
    }

    private fun actions(namedSensor: Mono<NamedSensor<SensorId>>): Mono<Boolean> {
        return namedSensor.flatMap {
            if (it.name == it.sensor.id) {
                Mono.empty()
            } else {
                Mono.just(it)
            }
        }
                .flux()
                .flatMap {
                    Flux.merge(domoticsClient.sendTemperature(namedSensor),
                            statsdClient.sendSensor(namedSensor),
                            graphiteService.storeStats(namedSensor))
                }
                .collectList()
                .map { isSuccess(it) }
                .switchIfEmpty(Mono.just(true))
    }

    private fun isSuccess(results: List<Boolean>) =
            results.stream()
                    .distinct()
                    .filter { result -> !result }
                    .findFirst()
                    .orElse(false)

}