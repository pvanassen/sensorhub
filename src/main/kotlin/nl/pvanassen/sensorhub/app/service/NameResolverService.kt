package nl.pvanassen.sensorhub.app.service

import nl.pvanassen.sensorhub.app.model.EmptySensor
import nl.pvanassen.sensorhub.app.model.NamedSensorUpdate
import nl.pvanassen.sensorhub.app.model.Sensor
import nl.pvanassen.sensorhub.app.repository.SensorEntity
import nl.pvanassen.sensorhub.app.repository.SensorHubRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class NameResolverService(private val sensorHubRepository: SensorHubRepository) {
    fun getOrCreate(sensor: Sensor): Mono<NamedSensor<Sensor>> =
            sensorHubRepository.findById(sensor.macAddress)
                    .switchIfEmpty(saveButReturnEmpty(sensor))
                    .map { NamedSensor(sensor, it.name, it.domoticsId) }

    fun get(): Flux<NamedSensor<EmptySensor>> =
        sensorHubRepository.findAll()
                .map { NamedSensor(EmptySensor(it.macAddress), it.name, it.domoticsId) }

    private fun saveButReturnEmpty(sensor: Sensor): Mono<SensorEntity> {
        return Mono.defer { sensorHubRepository.save(SensorEntity(sensor.macAddress, sensor.macAddress, 0)) }
                .flatMap { Mono.empty<SensorEntity>() }
    }

    fun update(macAddress: String, sensorUpdate: NamedSensorUpdate): Mono<NamedSensor<EmptySensor>> =
        sensorHubRepository.save(SensorEntity(macAddress, sensorUpdate.name, sensorUpdate.domoticsId))
                .map { NamedSensor(EmptySensor(it.macAddress), it.name, it.domoticsId)}

}