package nl.pvanassen.sensorhub.app.repository

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class SensorEntity(@Id val macAddress:String, val name: String, val domoticsId: Int)