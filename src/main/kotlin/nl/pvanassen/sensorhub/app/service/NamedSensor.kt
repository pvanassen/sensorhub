package nl.pvanassen.sensorhub.app.service

import nl.pvanassen.sensorhub.app.model.Sensor
import java.time.LocalDateTime

data class NamedSensor<out T: Sensor>(val sensor:T, val name: String, val domoticsId: Int, val lastContact: LocalDateTime)