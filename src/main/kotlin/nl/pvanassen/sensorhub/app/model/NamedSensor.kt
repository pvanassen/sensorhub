package nl.pvanassen.sensorhub.app.model

import com.fasterxml.jackson.annotation.JsonUnwrapped
import java.time.LocalDateTime

data class NamedSensor<out T: Sensor>(@JsonUnwrapped val sensor:T, val name: String, val domoticsId: Int, val lastContact: LocalDateTime)