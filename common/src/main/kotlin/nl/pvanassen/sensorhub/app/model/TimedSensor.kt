package nl.pvanassen.sensorhub.app.model

data class TimedSensor<out T: SensorId>(val sensor:T, val name: String, val domoticsId: Int, val timestamp: Long) {
    constructor(namedSensor: NamedSensor<T>, timestamp: Long) : this(namedSensor.sensor, namedSensor.name, namedSensor.domoticsId, timestamp)
}