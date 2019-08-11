package nl.pvanassen.sensorhub.app.service

import nl.pvanassen.sensorhub.app.model.EmptySensor
import nl.pvanassen.sensorhub.app.model.NamedSensorUpdate
import nl.pvanassen.sensorhub.app.model.Sensor
import nl.pvanassen.sensorhub.app.repository.SensorEntity
import nl.pvanassen.sensorhub.app.repository.SensorHubRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class NameResolverService(private val sensorHubRepository: SensorHubRepository) {
    fun getOrCreate(sensor: Sensor): Mono<NamedSensor<Sensor>> =
            sensorHubRepository.findById(sensor.id)
                    .flatMap { updateLastContact(it) }
                    .switchIfEmpty(saveButReturnEmpty(sensor))
                    .map { map(it, sensor) }

    fun get(): Flux<NamedSensor<EmptySensor>> =
        sensorHubRepository.findAll()
                .map { mapEmpty(it) }

    private fun updateLastContact(sensorEntity: SensorEntity): Mono<SensorEntity> =
        sensorHubRepository.save(sensorEntity.copy(lastContact = LocalDateTime.now()))


    private fun saveButReturnEmpty(sensor: Sensor): Mono<SensorEntity> {
        return Mono.defer { sensorHubRepository.save(SensorEntity(sensor.id, sensor.id, 0, LocalDateTime.now())) }
                .flatMap { Mono.empty<SensorEntity>() }
    }

    fun update(id: String, sensorUpdate: NamedSensorUpdate): Mono<NamedSensor<EmptySensor>> =
            sensorHubRepository.findById(id)
                    .map { it.copy(name = sensorUpdate.name, domoticsId = sensorUpdate.domoticsId) }
                    .flatMap { sensorHubRepository.save(it) }
                    .map { mapEmpty(it) }

    private fun mapEmpty(sensorEntity: SensorEntity): NamedSensor<EmptySensor> =
            map(sensorEntity, EmptySensor(sensorEntity.id))

    private fun <T: Sensor> map(sensorEntity: SensorEntity, sensor: T): NamedSensor<T> =
        NamedSensor(sensor = sensor, name = sensorEntity.name, domoticsId = sensorEntity.domoticsId, lastContact = sensorEntity.lastContact)

}