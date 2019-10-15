package nl.pvanassen.sensorhub.app.model

import com.fasterxml.jackson.annotation.JsonProperty

data class NamedSensorUpdate(@JsonProperty("name") val name:String,
                             @JsonProperty("domoticsId") val domoticsId: Int)