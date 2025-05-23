package data.remote.events

import ADMIN_USER_ID
import ConfigurationLoader
import data.remote.alerts.AlertsRemoteRepository
import globalLogger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class EventsRemoteRepository : KoinComponent {
    private val url = URLBuilder(
        protocol = URLProtocol.HTTPS,
        host = "my.centraluniversity.ru",
        pathSegments = listOf("api", "event-builder", "public", "events", "list")
    ).build()

    @OptIn(ExperimentalTime::class)
    suspend fun getEvents(): Result<Response> {
        val timeAsString = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        return runCatching {
            val client by inject<HttpClient>()
            val config by inject<ConfigurationLoader.ConfigMember>()
            val request = client.post(url) {
                contentType(ContentType.Application.Json)
                cookie(
                    "bff.cookie",
                    config.authCookie
                )
                setBody(
                    """{"paging":{"limit":100,"offset":0,"sorting":[{"by":"startDate","isAsc":true}]},"filter":{"showOnlyMine":false,"endDateGreaterThanOrEqualTo":"$timeAsString"}}"""
                )
            }
            val content = request.bodyAsText()
            globalLogger.debug {
                content
            }
            val result = request.body<Response>()
            // they return 200 and empty list if cookie is invalid, only way to know that cookie is still valid
            assert(result.items.isNotEmpty())
            result
        }
    }

    fun listenForEvents(
        delay: Duration = 2.seconds
    ): Flow<Response> {
        return flow {
            val alertsRemoteRepository by inject<AlertsRemoteRepository>()
            while (true) {
                getEvents().fold(
                    onSuccess = {
                        emit(it)
                    },
                    onFailure = {
                        alertsRemoteRepository.alert(
                            ADMIN_USER_ID,
                            "произошла ошибка при запросе данных"
                        )
                        alertsRemoteRepository.alert(
                            ADMIN_USER_ID,
                            it.stackTraceToString()
                        )
                    }
                )
                delay(delay)
            }
        }
    }
}

@Serializable
data class Response(
    val paging: Paging,
    val items: List<Event>,
)

@Serializable
data class Paging(
    val limit: Int,
    val offset: Int,
    val totalCount: Int
)

@Serializable
data class Event(
    val id: Long,
    val slug: String,
    val summary: String?,
    val startDate: String,
    val endDate: String,
    val createdAt: String,
    val shortTitle: String,
    val eventTicketRegistration: EventTicketRegistration,
    val offlineTicketLimit: Long?
)

@Serializable
data class EventTicketRegistration(
    val isPossibleToRegister: Boolean,
    val status: String
)