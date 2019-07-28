package nl.pvanassen.sensorhub.app.service

import nl.pvanassen.sensorhub.app.handler.TemperatureSensor
import nl.pvanassen.sensorhub.app.repository.Sensor
import nl.pvanassen.sensorhub.app.repository.SensorHubRepository
import reactor.core.publisher.Mono

class TemperatureSensorHandler(val sensorHubRepository: SensorHubRepository) {
    fun handle(sensor: TemperatureSensor): Mono<String> =
        sensorHubRepository.findById(sensor.macAddress)
                .switchIfEmpty(sensorHubRepository.save(Sensor(sensor.macAddress, sensor.macAddress)))
                .map { it.name }
}