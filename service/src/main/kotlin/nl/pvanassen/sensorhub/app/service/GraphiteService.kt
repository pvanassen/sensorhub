package nl.pvanassen.sensorhub.app.service

import nl.pvanassen.sensorhub.app.client.GraphiteClient
import nl.pvanassen.sensorhub.app.model.NamedSensor
import nl.pvanassen.sensorhub.app.model.SensorId
import nl.pvanassen.sensorhub.app.model.TimedSensor
import org.springframework.util.LinkedMultiValueMap
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.roundToInt

class GraphiteService(graphiteClient: GraphiteClient) {
    private val storeLock = ReentrantLock()
    private val store = LinkedMultiValueMap<Long, TimedSensor<SensorId>>()

    init {
        Flux.interval(Duration.ofSeconds(5))
                .map { getTimestamp() }
                .map { getAndClear(it) }
                .flatMap { Flux.fromIterable(it) }
                .flatMap { graphiteClient.sendSensor(Mono.just(it)) }
                .subscribeOn(Schedulers.elastic())
                .subscribe()
    }

    private fun getAndClear(timestamp: Long): List<TimedSensor<SensorId>> {
        return storeLock.withLock {
            val cache = store.values
                    .flatten()
                    .groupingBy { "${it.timestamp}.${it.name}" }
                    .reduce { _, sensor1, sensor2 ->
                        maxOf(sensor1, sensor2, Comparator<TimedSensor<SensorId>> { s1, s2 -> (s2.timestamp - s1.timestamp).toInt() }) }
                    .values
            store.keys.filter { it <= timestamp - 60000 }
                    .forEach { store.remove(it) }
            return cache.toList()
        }
    }

    fun storeStats(namedSensor: Mono<NamedSensor<SensorId>>): Mono<Boolean> {
        return namedSensor
                .map { TimedSensor<SensorId>(it, getTimestamp()) }
                .map { store(it) }
                .map { true}
    }

    private fun getTimestamp(): Long {
        val now = LocalDateTime.now().withNano(0)
        return now.withSecond((now.second / 5.0).roundToInt() * 5)
                .toEpochSecond(ZoneOffset.UTC)
    }


    private fun store(timedSensor: TimedSensor<SensorId>) {
        storeLock.withLock {
            store[timedSensor.timestamp] = timedSensor
        }
    }
}