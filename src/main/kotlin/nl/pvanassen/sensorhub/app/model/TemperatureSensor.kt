package nl.pvanassen.sensorhub.app.model


data class TemperatureSensor(override val id:String,
                             val humidity:Float,
                             val temperature:Float): Sensor()