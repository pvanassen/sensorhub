package nl.pvanassen.sensorhub.app.client

import nl.pvanassen.sensorhub.app.model.Sensor
import nl.pvanassen.sensorhub.app.model.TemperatureSensor
import nl.pvanassen.sensorhub.app.service.NamedSensor
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class DomoticsClient {

    private val client = WebClient.create("http://192.168.0.2:9080")

    fun sendTemperature(namedSensor: Mono<NamedSensor<Sensor>>): Mono<Boolean> {
        return namedSensor.map { path(it) }
                .flatMap { path -> client.get().uri { uriBuilder -> uriBuilder.path(path).build() }.exchange()}
                .map { it.statusCode().is2xxSuccessful }

    }

    private fun path(namedSensor: NamedSensor<Sensor>): String {
        return when(val sensor = namedSensor.sensor) {
            is TemperatureSensor -> path(sensor, namedSensor.domoticsId)
            else -> throw IllegalStateException()
        }
    }

    private fun path(sensor: TemperatureSensor, id: Int):String {
        val humidityStat = when {
            sensor.humidity > 70 -> 3
            sensor.humidity < 30 -> 2
            sensor.humidity in 30.0..45.0 -> 0
            else -> 1
        }
        return "/temperature.html?type=command&param=udevice&idx=$id&nvalue=0&svalue=${sensor.temperature};${sensor.humidity};$humidityStat;"
    }

}
