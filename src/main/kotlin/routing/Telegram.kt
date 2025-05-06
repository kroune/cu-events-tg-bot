package routing

import ADMIN_USER_ID
import controllers.EventsTextBuilderController
import controllers.UserController
import data.remote.alerts.AlertsRemoteRepository
import data.remote.events.EventsRemoteRepository
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.User
import inject
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

@CommandHandler(["/start"])
suspend fun start(user: User, bot: TelegramBot) {
    sendMessage {
        buildString {
            appendLine("Это бот, который будет уведомлять обо всех новых событиях цу")
            appendLine("Чтобы включить уведомления введите /enable")
        }
    }.send(user, bot)
    val userController by inject<UserController>()
    userController.registerUser(user.id)
}

@CommandHandler(["/enable"])
suspend fun enable(user: User, bot: TelegramBot) {
    sendMessage {
        buildString {
            appendLine("Теперь вам будут приходить уведомления о новых событиях")
        }
    }.send(user, bot)
    val userController by inject<UserController>()
    userController.enableNotifications(user.id)
}

@CommandHandler(["/disable"])
suspend fun disable(user: User, bot: TelegramBot) {
    sendMessage {
        buildString {
            appendLine("Вам больше не будут приходить уведомления о новых событиях")
        }
    }.send(user, bot)
    val userController by inject<UserController>()
    userController.disableNotifications(user.id)
}

@CommandHandler(["/events"])
suspend fun events(user: User, bot: TelegramBot) {
    val eventsRemoteRepository by inject<EventsRemoteRepository>()
    val alertsRemoteRepository by inject<AlertsRemoteRepository>()
    val events = withTimeoutOrNull(10.seconds) {
        eventsRemoteRepository.getEvents()
    }?.getOrElse {
        alertsRemoteRepository.alert(
            ADMIN_USER_ID,
            "произошла ошибка при запросе данных"
        )
        alertsRemoteRepository.alert(
            ADMIN_USER_ID,
            it.stackTraceToString()
        )
        null
    }?.items
    if (events == null) {
        alertsRemoteRepository.alert(
            ADMIN_USER_ID,
            text = "достингут timeout при отправке уведомления",
        )
    }
    val eventsTextBuilderController by inject<EventsTextBuilderController>()
    sendMessage {
        buildString {
            when (events) {
                null -> {
                    "произошла ошибка при получение информации (разраб уже уведомлен)"
                }
                else -> {
                    events.forEach { event ->
                        appendLine()
                        appendLine(
                            eventsTextBuilderController.constructEventInfo(event)
                        )
                    }
                }
            }
        }
    }.send(user, bot)
}
