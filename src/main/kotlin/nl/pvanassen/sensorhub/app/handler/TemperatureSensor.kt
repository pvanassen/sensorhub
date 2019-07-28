package nl.pvanassen.sensorhub.app.handler

import com.fasterxml.jackson.annotation.JsonProperty

data class TemperatureSensor(@JsonProperty("m") val macAddress:String,
                        @JsonProperty("h") val humidity:Float,
                        @JsonProperty("t") val temperature:Float)