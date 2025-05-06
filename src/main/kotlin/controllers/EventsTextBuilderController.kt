package controllers

import data.remote.events.Event

class EventsTextBuilderController {
    fun constructEventInfo(event: Event): String {
        return buildString {
            appendLine(event.shortTitle)
            appendLine("https://my.centraluniversity.ru/events/${event.slug}")
            appendLine(
                when (event.summary) {
                    null -> {
                        "описание отсутствует"
                    }
                    else -> {
                        event.summary
                    }
                }
            )
            appendLine(
                when (event.offlineTicketLimit) {
                    null -> {
                        "количество билетов не ограниченно"
                    }

                    else -> {
                        "всего билетов - ${event.offlineTicketLimit}"
                    }
                }
            )

            appendLine(
                when (event.eventTicketRegistration.isPossibleToRegister) {
                    true -> {
                        "вы еще можете зарегистрироваться"
                    }

                    false -> {
                        "к сожалению регистрация не доступна"
                    }
                }
            )
        }
    }
}
