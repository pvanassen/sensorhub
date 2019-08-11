package nl.pvanassen.sensorhub.app.handler

import nl.pvanassen.sensorhub.app.model.NamedSensorUpdate
import nl.pvanassen.sensorhub.app.service.NameResolverService
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body

class SensorHubHandler(private val nameResolverService: NameResolverService) {

    @Suppress("UNUSED_PARAMETER")
    fun listSensors(request: ServerRequest) =
            ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(nameResolverService.get())

    fun updateSensor(request: ServerRequest) =
            request.bodyToMono(NamedSensorUpdate::class.java)
                    .map { nameResolverService.update(request.pathVariable("id"), it) }
                    .flatMap { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(it) }
}