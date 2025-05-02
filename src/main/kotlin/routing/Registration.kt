package routing

import controllers.UserController
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.User
import inject

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