package nl.pvanassen.sensorhub.app.config

data class SensorHubConfig(val graphiteAddress: String,
                           val graphitePort: Int,
                           val graphitePrefix: String)