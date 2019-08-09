package nl.pvanassen.sensorhub.app

import nl.pvanassen.sensorhub.app.client.DomoticsClient
import nl.pvanassen.sensorhub.app.client.StatsdClient
import nl.pvanassen.sensorhub.app.config.SensorHubConfig
import nl.pvanassen.sensorhub.app.handler.SensorHubHandler
import nl.pvanassen.sensorhub.app.repository.SensorHubRepository
import nl.pvanassen.sensorhub.app.service.NameResolverService
import org.springframework.boot.WebApplicationType
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
	}
	reactiveMongodb {
		uri = "mongodb://localhost:$mongoPort/test"
		embedded()
	}
	webFlux {
		port = 8080
		router {
			val handler = ref<SensorHubHandler>()
			POST("/", handler::temperature)
			GET("/", handler::listSensors)
			POST("/update/{mac}", handler::updateSensor)
		}
		codecs {
			jackson()
		}
	}
}

fun main() {
	app.run()
}