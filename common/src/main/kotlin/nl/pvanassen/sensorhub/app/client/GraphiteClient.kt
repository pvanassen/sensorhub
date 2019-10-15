package nl.pvanassen.sensorhub.app.client

import nl.pvanassen.sensorhub.app.model.SensorId
import nl.pvanassen.sensorhub.app.model.TimedSensor
import reactor.core.publisher.Mono

interface GraphiteClient {
    fun sendSensor(namedSensor: Mono<TimedSensor<SensorId>>): Mono<Boolean>
}