package nl.pvanassen.sensorhub.app.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface SensorHubRepository: ReactiveMongoRepository<Sensor, String>