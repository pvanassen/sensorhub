package nl.pvanassen.sensorhub.app

import nl.pvanassen.sensorhub.app.client.DomoticsRestClient
import nl.pvanassen.sensorhub.app.client.GraphiteTcpClient
import nl.pvanassen.sensorhub.app.client.StatsdUdpClient
import nl.pvanassen.sensorhub.app.config.SensorHubConfig
import nl.pvanassen.sensorhub.app.handler.SensorHubHandler
import nl.pvanassen.sensorhub.app.repository.SensorHubRepository
import nl.pvanassen.sensorhub.app.server.UdpReceiver
import nl.pvanassen.sensorhub.app.service.GraphiteService
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
		bean<DomoticsRestClient>()
		bean<StatsdUdpClient>()
		bean<UdpReceiver>()
		bean<GraphiteService>()
		bean<GraphiteTcpClient>()
		bean<RedirectFilter>()
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
			resources("/", ClassPathResource("frontend/index.html"))
			resources("/**", ClassPathResource("frontend/"))
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