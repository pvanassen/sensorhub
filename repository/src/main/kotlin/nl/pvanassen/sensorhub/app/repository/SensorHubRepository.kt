package nl.pvanassen.sensorhub.app.repository

import mu.KotlinLogging
import nl.pvanassen.sensorhub.app.model.Sensor
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import reactor.core.publisher.Mono

class SensorHubRepository(private val template: ReactiveMongoTemplate) : SensorRepository {

    private val logger = KotlinLogging.logger {}

    override fun findById(id: String) = template.findById<SensorEntity>(id).map { Sensor(it.id, it.name, it.domoticsId, it.lastContact) }

    override fun findAll() = template.findAll<SensorEntity>().map { Sensor(it.id, it.name, it.domoticsId, it.lastContact) }

    override fun save(sensorEntity: Sensor): Mono<Sensor> {
        logger.info("Saving sensor {}, {}", sensorEntity.id, sensorEntity.lastContact)
        return template.save(SensorEntity(sensorEntity.id, sensorEntity.name, sensorEntity.domoticsId, sensorEntity.lastContact))
                .map { Sensor(it.id, it.name, it.domoticsId, it.lastContact) }
    }
}