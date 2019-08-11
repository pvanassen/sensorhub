package nl.pvanassen.sensorhub.app.repository

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class SensorEntity(@Id val id:String, val name: String, val domoticsId: Int, val lastContact: LocalDateTime)