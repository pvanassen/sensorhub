package nl.pvanassen.sensorhub.app.service

import mu.KotlinLogging
import nl.pvanassen.sensorhub.app.model.*
import nl.pvanassen.sensorhub.app.repository.SensorRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId

class NameResolverService(private val sensorHubRepository: SensorRepository) {
    private val logger = KotlinLogging.logger {}

    fun getOrCreate(sensor: SensorId): Mono<NamedSensor<SensorId>> = sensorHubRepository.findById(sensor.id)
            .flatMap { updateLastContact(it) }
            .switchIfEmpty(saveButReturnEmpty(sensor))
            .map { map(it, sensor) }

    fun get(id: String): Mono<NamedSensor<EmptySensor>> =
            sensorHubRepository.findById(id)
                    .map { mapEmpty(it) }

    fun get(): Flux<NamedSensor<EmptySensor>> =
            sensorHubRepository.findAll()
                    .map { mapEmpty(it) }

    private fun updateLastContact(sensorEntity: Sensor): Mono<Sensor> {
        logger.info("Updating last contact for {}", sensorEntity.id)
        return sensorHubRepository.save(sensorEntity.copy(lastContact = LocalDateTime.now()))
    }

    private fun saveButReturnEmpty(sensor: SensorId): Mono<Sensor> =
            Mono.defer { sensorHubRepository.save(Sensor(sensor.id, sensor.id, 0, LocalDateTime.now())) }
                    .flatMap { Mono.empty<Sensor>() }


    fun update(id: String, sensorUpdate: NamedSensorUpdate): Mono<NamedSensor<EmptySensor>> =
            sensorHubRepository.findById(id)
                    .map { it.copy(name = sensorUpdate.name, domoticsId = sensorUpdate.domoticsId) }
                    .flatMap { sensorHubRepository.save(it) }
                    .map { mapEmpty(it) }

    private fun mapEmpty(sensorEntity: Sensor): NamedSensor<EmptySensor> =
            map(sensorEntity, EmptySensor(sensorEntity.id))

    private fun <T : SensorId> map(sensorEntity: Sensor, sensor: T): NamedSensor<T> =
            NamedSensor(sensor = sensor, name = sensorEntity.name, domoticsId = sensorEntity.domoticsId, lastContact = sensorEntity.lastContact.atZone(ZoneId.systemDefault()))

}