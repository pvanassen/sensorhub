package nl.pvanassen.sensorhub.app

import nl.pvanassen.sensorhub.app.config.SensorHubConfig
import nl.pvanassen.sensorhub.app.handler.SensorHubHandler
import nl.pvanassen.sensorhub.app.repository.SensorHubRepository
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.webflux.webFlux

val app = application(WebApplicationType.REACTIVE) {
	configurationProperties<SensorHubConfig>("app")
	beans {
		bean<SensorHubRepository>()
		bean<SensorHubHandler>()
	}
	webFlux {
		port = 8080
		router {
			val handler = ref<SensorHubHandler>()
			GET("/", handler::json)
		}
		codecs {
			jackson()
		}
	}
}

fun main() {
	app.run()
}