package data.remote.alerts

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.message.sendMessage
import globalLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AlertsRemoteRepository : KoinComponent {
    suspend fun alert(
        userId: Long,
        text: String
    ) {
        globalLogger.debug {
            "sending alert: $userId, $text"
        }
        sendMessage { text }.send(userId, get<TelegramBot>())
    }
}