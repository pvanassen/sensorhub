package nl.pvanassen.sensorhub.app.client

import nl.pvanassen.sensorhub.app.config.SensorHubConfig
import nl.pvanassen.sensorhub.app.model.SensorId
import nl.pvanassen.sensorhub.app.model.TemperatureSensor
import nl.pvanassen.sensorhub.app.model.TimedSensor
import reactor.core.publisher.Mono
import reactor.netty.tcp.TcpClient

class GraphiteTcpClient(private val sensorHubConfig: SensorHubConfig): GraphiteClient {
    override fun sendSensor(namedSensor: Mono<TimedSensor<SensorId>>): Mono<Boolean> {
        return TcpClient.create()
                .host(sensorHubConfig.graphiteAddress)
                .port(sensorHubConfig.graphitePort)
                .handle { _, u -> u.sendString(createGraphiteString(namedSensor)) }
                .connect()
                .map { true }
    }

    private fun createGraphiteString(namedSensorMono: Mono<TimedSensor<SensorId>>): Mono<String> {
        return namedSensorMono.map { namedSensor ->
            when(val sensor = namedSensor.sensor) {
                is TemperatureSensor -> createTemperatureSensorGraphiteString(namedSensor.name, sensor, namedSensor.timestamp)
                else -> throw IllegalArgumentException()
            }
        }
    }

    private fun createTemperatureSensorGraphiteString(location: String, sensor: TemperatureSensor, date: Long): String =
            "${sensorHubConfig.graphitePrefix}.$location.temperature ${sensor.temperature} $date\n" +
                    "${sensorHubConfig.graphitePrefix}.$location.humidity ${sensor.humidity} $date\n"
}