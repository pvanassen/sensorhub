package nl.pvanassen.sensorhub.app

import nl.pvanassen.sensorhub.app.client.DomoticsClient
import nl.pvanassen.sensorhub.app.client.StatsdClient
import nl.pvanassen.sensorhub.app.config.SensorHubConfig
import nl.pvanassen.sensorhub.app.handler.SensorHubHandler
import nl.pvanassen.sensorhub.app.repository.SensorHubRepository
import nl.pvanassen.sensorhub.app.server.UdpReceiver
import nl.pvanassen.sensorhub.app.service.NameResolverService
import org.springframework.boot.WebApplicationType
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.core.io.ClassPathResource
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.mongo.reactiveMongodb
import org.springframework.fu.kofu.webflux.webFlux
import org.springframework.util.SocketUtils

val app = application(WebApplicationType.REACTIVE) {
	val mongoPort = SocketUtils.findAvailableTcpPort()
	configurationProperties<SensorHubConfig>("app")

	beans {
		bean<SensorHubRepository>()
		bean<SensorHubHandler>()
		bean<NameResolverService>()
		bean<DomoticsClient>()
		bean<StatsdClient>()
		bean<UdpReceiver>()
	}
	listener<ApplicationReadyEvent> {
		ref<UdpReceiver>().init()
	}
	reactiveMongodb {
		val embededMongo = env.getProperty("app.mongo.embeded", Boolean::class.java) ?: true
		if (embededMongo) {
			uri = "mongodb://localhost:$mongoPort/test"
			embedded()
		}
		else {
			uri = env.getProperty("app.mongo.uri")
		}
	}
	webFlux {
		port = 8080
		router {
			val handler = ref<SensorHubHandler>()
			GET("/api/sensor", handler::listSensors)
			GET("/api/sensor/{id}", handler::getSensor)
			POST("/api/sensor/{id}", handler::updateSensor)
			resources("/**", ClassPathResource("frontend/dist/"))
		}
		codecs {
			jackson()
			resource()
		}
	}
}

fun main() {
	app.run()
}