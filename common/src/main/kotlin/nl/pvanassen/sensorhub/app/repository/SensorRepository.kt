package nl.pvanassen.sensorhub.app.repository

import nl.pvanassen.sensorhub.app.model.Sensor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface SensorRepository {
    fun findById(id: String): Mono<Sensor>
    fun findAll(): Flux<Sensor>
    fun save(sensorEntity: Sensor): Mono<Sensor>
}