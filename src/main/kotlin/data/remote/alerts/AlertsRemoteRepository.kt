package data.remote.alerts

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.message.sendMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AlertsRemoteRepository : KoinComponent {
    suspend fun alert(
        userId: Long,
        text: String
    ) {
        sendMessage { text }.send(userId, get<TelegramBot>())
    }
}