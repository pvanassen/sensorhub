package nl.pvanassen.sensorhub.app.repository

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById

class SensorHubRepository(private val template: ReactiveMongoTemplate) {

    fun findById(macAddress: String) = template.findById<SensorEntity>(macAddress)

    fun findAll() = template.findAll<SensorEntity>()

    fun save(sensorEntity: SensorEntity) = template.save(sensorEntity)
}