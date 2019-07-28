package nl.pvanassen.sensorhub.app.handler

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

class SensorHubHandler {
    fun json(request: ServerRequest) =
        request.bodyToMono(TemperatureSensor::class.java)
                .map { ServerResponse.ok() }

}