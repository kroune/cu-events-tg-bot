import ConfigurationLoader.currentConfig
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
import kotlinx.coroutines.*
import org.koin.core.context.GlobalContext
import org.koin.ktor.plugin.Koin
import routing.misc.miscRouting
import routing.monitoring.monitoringRouting
import services.EventNotificationsService

private val criticalErrorLogger = CoroutineExceptionHandler { context, exception ->
    normalScope.launch {
        globalLogger.error(exception) { "произошла критическая ошибка" }
        // we are dead
    }
}

private val scope = CoroutineScope(Dispatchers.IO + criticalErrorLogger)
val normalScope = CoroutineScope(Dispatchers.IO)

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
            get<EventNotificationsService>().start(scope)
            scope.launch {
                while (isActive) {
                    runCatching {
                        get<TelegramBot>().handleUpdates()
                    }.onFailure {
                        get<TelegramBot>().update.stopListener()
                        logger.error(it) { "error during listening for updates" }
                    }
                }
                logger.error { "scope was cancelled" }
            }
        }
    ).start(wait = true)
}


fun Application.applyPlugins() {
    install(Authentication) {
        basic("prometheus") {
            realm = "Access to the '/metrics' path"
            validate { credentials ->
                with(currentConfig.prometheusScraperConfig) {
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
