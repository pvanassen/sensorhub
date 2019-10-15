package nl.pvanassen.sensorhub.app.model

import java.time.LocalDateTime

data class Sensor(val id:String, val name: String, val domoticsId: Int, val lastContact: LocalDateTime)