package nl.pvanassen.sensorhub.app.repository

import nl.pvanassen.sensorhub.app.model.Sensor
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById

class SensorHubRepository(private val template: ReactiveMongoTemplate) : SensorRepository {

    override fun findById(id: String) = template.findById<SensorEntity>(id).map { Sensor(it.id, it.name, it.domoticsId, it.lastContact) }

    override fun findAll() = template.findAll<SensorEntity>().map { Sensor(it.id, it.name, it.domoticsId, it.lastContact) }

    override fun save(sensorEntity: Sensor) = template.save(SensorEntity(sensorEntity.id, sensorEntity.name, sensorEntity.domoticsId, sensorEntity.lastContact))
                .map { Sensor(it.id, it.name, it.domoticsId, it.lastContact) }
}