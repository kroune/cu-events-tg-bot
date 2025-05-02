package routing.monitoring

import routing.monitoring.get.monitoringRoutingGET
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.monitoringRouting() {
    authenticate("prometheus") {
        monitoringRoutingGET()
    }
}