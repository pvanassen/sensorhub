package nl.pvanassen.sensorhub.app.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TemperatureSensor(@JsonProperty("m") override val macAddress:String,
                             @JsonProperty("h") val humidity:Float,
                             @JsonProperty("t") val temperature:Float): Sensor(macAddress)