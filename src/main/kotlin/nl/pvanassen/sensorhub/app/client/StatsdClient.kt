package nl.pvanassen.sensorhub.app.client

import nl.pvanassen.sensorhub.app.model.Sensor
import nl.pvanassen.sensorhub.app.model.TemperatureSensor
import nl.pvanassen.sensorhub.app.service.NamedSensor
import reactor.core.publisher.Mono
import reactor.netty.udp.UdpClient
import reactor.netty.udp.UdpInbound
import reactor.netty.udp.UdpOutbound

class StatsdClient {
    fun sendTemperature(namedSensor: Mono<NamedSensor<Sensor>>): Mono<Boolean> {
        return UdpClient.create()
                .host("192.168.0.2")
                .port(8125)
                .handle {_: UdpInbound?, u: UdpOutbound? -> u?.sendString(createStatdString(namedSensor)) }
                .connect()
                .map { true }
    }

    private fun createStatdString(namedSensorMono: Mono<NamedSensor<Sensor>>): Mono<String> {
        return namedSensorMono.map { namedSensor ->
            when(val sensor = namedSensor.sensor) {
                is TemperatureSensor -> createTemperatureSensorStatdString(namedSensor.name, sensor)
                else -> throw IllegalArgumentException()
        } }
    }

    private fun createTemperatureSensorStatdString(location: String, sensor: TemperatureSensor): String =
        "$location.temperature:${sensor.temperature}|g\n$location.humidity:${sensor.humidity}|g"

}