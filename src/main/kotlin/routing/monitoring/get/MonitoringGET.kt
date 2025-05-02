package routing.monitoring.get

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.koin.ktor.ext.inject

fun Route.monitoringRoutingGET() {
    get("/metrics") {
        val registry by inject<PrometheusMeterRegistry>()
        call.respondText { registry.scrape() }
    }
}
