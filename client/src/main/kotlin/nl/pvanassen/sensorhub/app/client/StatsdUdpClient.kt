package nl.pvanassen.sensorhub.app.client

import nl.pvanassen.sensorhub.app.model.NamedSensor
import nl.pvanassen.sensorhub.app.model.SensorId
import nl.pvanassen.sensorhub.app.model.TemperatureSensor
import reactor.core.publisher.Mono
import reactor.netty.udp.UdpClient
import reactor.netty.udp.UdpInbound
import reactor.netty.udp.UdpOutbound

class StatsdUdpClient: StatsdClient {
    override fun sendSensor(namedSensor: Mono<NamedSensor<SensorId>>): Mono<Boolean> {
        return UdpClient.create()
                .host("192.168.178.3")
                .port(8125)
                .handle {_: UdpInbound?, u: UdpOutbound? -> u?.sendString(createStatdString(namedSensor)) }
                .connect()
                .map { true }
    }

    private fun createStatdString(namedSensorMono: Mono<NamedSensor<SensorId>>): Mono<String> {
        return namedSensorMono.map { namedSensor ->
            when(val sensor = namedSensor.sensor) {
                is TemperatureSensor -> createTemperatureSensorStatdString(namedSensor.name, sensor)
                else -> throw IllegalArgumentException()
        } }
    }

    private fun createTemperatureSensorStatdString(location: String, sensor: TemperatureSensor): String =
        "$location.temperature:${sensor.temperature}|g\n$location.humidity:${sensor.humidity}|g"

}