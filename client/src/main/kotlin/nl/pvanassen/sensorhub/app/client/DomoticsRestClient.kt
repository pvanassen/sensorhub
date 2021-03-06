package nl.pvanassen.sensorhub.app.client

import nl.pvanassen.sensorhub.app.model.NamedSensor
import nl.pvanassen.sensorhub.app.model.SensorId
import nl.pvanassen.sensorhub.app.model.TemperatureSensor
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class DomoticsRestClient: DomoticsClient {

    private val client = WebClient.create("http://192.168.178.3:9080")

    override fun sendTemperature(namedSensor: Mono<NamedSensor<SensorId>>): Mono<Boolean> {
        return namedSensor.map { path(it) }
                .flatMap { path -> client.get().uri { uriBuilder -> uriBuilder.path(path).build() }.exchange()}
                .map { it.statusCode().is2xxSuccessful }

    }

    private fun path(namedSensor: NamedSensor<SensorId>): String {
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
