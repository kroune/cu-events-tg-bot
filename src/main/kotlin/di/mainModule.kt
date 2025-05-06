package di

import ConfigurationLoader
import ch.qos.logback.classic.Logger
import controllers.EventsTextBuilderController
import controllers.UserController
import data.local.events.EventsRepositoryImpl
import data.local.users.UsersRepositoryImpl
import data.local.usersEvents.UsersEventsRepositoryImpl
import data.remote.alerts.AlertsRemoteRepository
import data.remote.events.EventsRemoteRepository
import eu.vendeli.tgbot.TelegramBot
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val mainModule = module {
    single { KotlinLogging.logger(Logger.ROOT_LOGGER_NAME) }
    single {
        TelegramBot(get<ConfigurationLoader.ConfigMember>().botToken)
    }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
    single { EventsRepositoryImpl(get<Database>()) }
    single { UsersEventsRepositoryImpl(get<Database>()) }
    single { UsersRepositoryImpl(get<Database>()) }
    single { EventsRemoteRepository() }
    single { AlertsRemoteRepository() }
    single { UserController() }
    single { EventsTextBuilderController() }
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    get<Json>()
                )
            }
        }
    }
}