import ConfigurationLoader.currentConfig
import data.local.events.EventsRepositoryImpl
import data.local.users.UsersRepositoryImpl
import data.local.usersEvents.UsersEventsRepositoryImpl
import data.remote.alerts.AlertsRemoteRepository
import data.remote.events.EventsRemoteRepository
import di.configurationModule
import di.mainModule
import eu.vendeli.tgbot.TelegramBot
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.*
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.ktor.plugin.Koin
import routing.misc.miscRouting
import routing.monitoring.monitoringRouting

private val scope = CoroutineScope(Dispatchers.IO)

fun main() {
    val serverConfig = currentConfig.serverConfig
    embeddedServer(
        Netty,
        configure = {
            connector {
                host = serverConfig.host
                port = serverConfig.port
            }
            requestReadTimeoutSeconds = 15
            responseWriteTimeoutSeconds = 15
        },
        module = {
            startDI()
            logger.info { "starting server" }
            applyPlugins()
            installMonitoring()
            routing()
            scope.launch {
                val eventsRemoteRepository by inject<EventsRemoteRepository>()
                val eventsRepository by inject<EventsRepositoryImpl>()
                val usersEventsRepository by inject<UsersEventsRepositoryImpl>()
                val usersRepository by inject<UsersRepositoryImpl>()
                val alertsRemoteRepository by inject<AlertsRemoteRepository>()
                eventsRemoteRepository.listenForEvents().collect {
                    val currentEvents = it.items
                    usersRepository.usersWithEnabledNotifications().forEach { userId ->
                        val pastUserEvents = usersEventsRepository.getEventsByUserId(userId)
                        val newEvents = currentEvents.filter { event -> event.id !in pastUserEvents }
                        if (newEvents.isEmpty())
                            return@collect
                        val message = buildString {
                            appendLine("Появились новые события")
                            newEvents.forEach { event ->
                                appendLine()
                                appendLine(event.shortTitle)
                                appendLine("https://my.centraluniversity.ru/events/${event.slug}")
                            }
                        }
                        alertsRemoteRepository.alert(
                            userId,
                            message
                        )
                        newEvents.forEach { event ->
                            val eventExists = eventsRepository.getEventByEventId(event.id) != null
                            if (!eventExists) {
                                eventsRepository.addEvent(event)
                            }
                            usersEventsRepository.addEventToUser(userId, event.id)
                        }
                    }
                }
            }
            scope.launch {
                get<TelegramBot>().handleUpdates()
            }
        }
    ).start(wait = true)
}


fun Application.applyPlugins() {
    install(Authentication) {
        basic("prometheus") {
            realm = "Access to the '/metrics' path"
            validate { credentials ->
                with(currentConfig.grafanaConfig) {
                    return@validate credentials.name == nameForScrape && credentials.password == passwordForScrape
                }
            }
        }
    }
}

fun Application.installMonitoring() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        meterBinders = listOf(
            UptimeMetrics(),
            ProcessorMetrics(),
            ClassLoaderMetrics(),
            JvmCompilationMetrics(),
            JvmGcMetrics(),
            JvmHeapPressureMetrics(),
            JvmInfoMetrics(),
            JvmMemoryMetrics(),
            JvmThreadDeadlockMetrics(),
            JvmThreadMetrics(),
        )
        registry = appMicrometerRegistry
    }
    GlobalContext.getKoinApplicationOrNull()!!.koin.declare(appMicrometerRegistry)
}

fun Application.startDI() {
    install(Koin) {
        modules(
            configurationModule,
            databaseModule,
            mainModule,
        )
    }
}

fun Application.routing() {
    routing {
        logger.debug { "initializing routing" }
        monitoringRouting()
        miscRouting()
    }
}
