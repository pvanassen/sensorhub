package nl.pvanassen.sensorhub.app.handler

import mu.KotlinLogging
import nl.pvanassen.sensorhub.app.client.DomoticsClient
import nl.pvanassen.sensorhub.app.client.StatsdClient
import nl.pvanassen.sensorhub.app.model.NamedSensorUpdate
import nl.pvanassen.sensorhub.app.model.Sensor
import nl.pvanassen.sensorhub.app.model.TemperatureSensor
import nl.pvanassen.sensorhub.app.service.NameResolverService
import nl.pvanassen.sensorhub.app.service.NamedSensor
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class SensorHubHandler(private val nameResolverService: NameResolverService,
                       private val domoticsClient: DomoticsClient,
                       private val statsdClient: StatsdClient) {

    val logger = KotlinLogging.logger {}

    fun temperature(request: ServerRequest) =
            request.bodyToMono(TemperatureSensor::class.java)
                    .map { nameResolverService.getOrCreate(it) }
                    .flatMap { actions(it) }
                    .flatMap { ServerResponse.ok().build() }
                    .doOnError { logger.info("Error on temperature", it) }

    fun listSensors(request: ServerRequest) =
            ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(nameResolverService.get())

    fun updateSensor(request: ServerRequest) =
            request.bodyToMono(NamedSensorUpdate::class.java)
                    .map { nameResolverService.update(request.pathVariable("mac"), it) }
                    .flatMap { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).syncBody(it)}

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