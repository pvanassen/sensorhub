package nl.pvanassen.sensorhub.app.client

import nl.pvanassen.sensorhub.app.model.NamedSensor
import nl.pvanassen.sensorhub.app.model.SensorId
import reactor.core.publisher.Mono

interface StatsdClient {
    fun sendSensor(namedSensor: Mono<NamedSensor<SensorId>>): Mono<Boolean>
}