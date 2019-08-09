package nl.pvanassen.sensorhub.app.service

import nl.pvanassen.sensorhub.app.model.Sensor

data class NamedSensor<out T: Sensor>(val sensor:T, val name: String, val domoticsId: Int)