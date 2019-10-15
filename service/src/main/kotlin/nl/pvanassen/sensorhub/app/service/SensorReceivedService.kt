package nl.pvanassen.sensorhub.app.service

import nl.pvanassen.sensorhub.app.client.DomoticsClient
import nl.pvanassen.sensorhub.app.client.StatsdClient
import nl.pvanassen.sensorhub.app.model.NamedSensor
import nl.pvanassen.sensorhub.app.model.SensorId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class SensorReceivedService(private val domoticsClient: DomoticsClient,
                            private val statsdClient: StatsdClient,
                            private val graphiteService: GraphiteService) {

    fun sensorReceived(namedSensor: Mono<NamedSensor<SensorId>>): Mono<Boolean> {
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